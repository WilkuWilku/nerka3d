package pl.ee.nerkabackend.processing.methods.visualisation.triangulation;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import pl.ee.nerkabackend.processing.methods.MethodTypes;
import pl.ee.nerkabackend.processing.methods.correspondingpoints.selector.CorrespondingPointsSelector;
import pl.ee.nerkabackend.processing.methods.visualisation.VisualisationMethod;
import pl.ee.nerkabackend.processing.methods.visualisation.triangulation.builder.TriangulationMethodBuilder;
import pl.ee.nerkabackend.processing.model.CorrespondingPoints;
import pl.ee.nerkabackend.processing.model.KidneyVisualisationObject;
import pl.ee.nerkabackend.processing.model.Layer;
import pl.ee.nerkabackend.processing.model.LayerPoint;
import pl.ee.nerkabackend.processing.model.triangulation.Triangle;
import pl.ee.nerkabackend.report.ReportingService;
import pl.ee.nerkabackend.report.ReportingValuesContainer;
import pl.ee.nerkabackend.report.model.ComparisonValueMeasurement;
import pl.ee.nerkabackend.report.model.RatioMeasurement;
import pl.ee.nerkabackend.report.model.Report;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.DoubleStream;

@Slf4j
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class TriangulationService implements VisualisationMethod {

    @Autowired
    @Qualifier("quarterCorrespondingPointsSelector")
    private CorrespondingPointsSelector correspondingPointsSelector;

    @Autowired
    private ReportingService reportingService;

    @Autowired
    private TriangulationMethodBuilder triangulationMethodBuilder;

    @Autowired
    private ReportingValuesContainer reportingValuesContainer;

    @Value("${triangulation.method.type}")
    @Setter
    private MethodTypes.TriangulationMethodType triangulationMethodType;

    @Setter
    private Double indexRatioDiffCoefficient;

    @Value("${triangulation.reporting.enabled}")
    private Boolean isReportingEnabled;

    @Override
    public KidneyVisualisationObject getKidneyVisualisationObject(List<Layer> layers, Object... params) {
        return null;
    }

    public List<Triangle> getTrianglesBetweenLayers(Layer topLayer, Layer bottomLayer) {
        log.info("getTrianglesBetweenLayers() start - topLayer: {}, bottomLayer: {}", topLayer.getName(), bottomLayer.getName());

        log.info("getTrianglesBetweenLayers() setting corresponding points as first in layers");
        setCorrespondingPointsAsFirstInLayers(topLayer, bottomLayer);

        List<LayerPoint> topPoints = topLayer.getPoints();
        List<LayerPoint> bottomPoints = bottomLayer.getPoints();
        List<Triangle> triangles = new ArrayList<>();
        int topLayerIndex = 0;
        int bottomLayerIndex = 0;
        final double layersLengthRatio = (double) topPoints.size() / bottomPoints.size();
        final int layersPointsCount = topPoints.size()+bottomPoints.size();

        LayerPoint firstTopPoint = topPoints.get(0);
        LayerPoint firstBottomPoint = bottomPoints.get(0);

        LayerPoint currentTopPoint = firstTopPoint;
        LayerPoint currentBottomPoint = firstBottomPoint;
        LayerPoint nextTopPoint = topPoints.get(topLayerIndex + 1);
        LayerPoint nextBottomPoint = bottomPoints.get(bottomLayerIndex + 1);

        List<RatioMeasurement> ratioMeasurements = new ArrayList<>();
        reportingValuesContainer.setMeasurements(new HashMap<>());
        reportingValuesContainer.getMeasurements().put(getLayersIdentifier(topLayer, bottomLayer), new ArrayList<>());
        int step = 0;

        AbstractTriangulationMethod triangulationMethod = triangulationMethodBuilder
                .withTriangulationMethodType(triangulationMethodType)
                .withIndexesRatioDiffCoefficient(indexRatioDiffCoefficient)
                .build();

        do {
            step++;
            double ratioDiffValue = calculateAdditionalValueFromLayersLengthRatio(topLayerIndex, bottomLayerIndex, layersPointsCount, layersLengthRatio);
            LayerPoint nextPoint = triangulationMethod.getNextPoint(currentTopPoint, currentBottomPoint,
                    nextTopPoint, nextBottomPoint, ratioDiffValue, getLayersIdentifier(topLayer, bottomLayer));
            Triangle nextTriangle = new Triangle(currentTopPoint, currentBottomPoint, nextPoint);
            triangles.add(nextTriangle);

            if(isReportingEnabled) {
                RatioMeasurement ratioMeasurement = RatioMeasurement.builder()
                        .currentStep(step)
                        .topIndex(topLayerIndex)
                        .bottomIndex(bottomLayerIndex)
                        .currentRatio(topLayerIndex == 0 || bottomLayerIndex == 0 ? 0 : (double) topLayerIndex / bottomLayerIndex)
                        .build();
                ratioMeasurements.add(ratioMeasurement);
            }

            if (nextPoint.equals(nextTopPoint)) {
                currentTopPoint = nextPoint;
                topLayerIndex++;

                if(topLayerIndex+1 < topPoints.size()) {
                    nextTopPoint = topPoints.get(topLayerIndex + 1);
                } else {
                    nextTopPoint = firstTopPoint;
                }
            } else {
                currentBottomPoint = nextPoint;
                bottomLayerIndex++;

                if (bottomLayerIndex + 1 < bottomPoints.size()) {
                    nextBottomPoint = bottomPoints.get(bottomLayerIndex + 1);
                } else {
                    nextBottomPoint = firstBottomPoint;
                }
            }

            if(currentTopPoint == firstTopPoint && nextTopPoint == firstTopPoint) {
                nextTopPoint = null;
            }
            if(currentBottomPoint == firstBottomPoint && nextBottomPoint == firstBottomPoint) {
                nextBottomPoint = null;
            }

        } while(topLayerIndex < topPoints.size() || bottomLayerIndex < bottomPoints.size());

        if(isReportingEnabled) {
            Report report = Report.builder()
                    .identifier("test layers " + topLayer.getName() + " & " + bottomLayer.getName())
                    .targetRatio(layersLengthRatio)
                    .triangulationMethodType(triangulationMethodType)
                    .indexesRatioDiffCoefficient(indexRatioDiffCoefficient)
                    .ratioMeasurements(ratioMeasurements)
                    .comparisonValueMeasurements(reportingValuesContainer.getMeasurements().get(getLayersIdentifier(topLayer, bottomLayer)))
                    .build();
            try {
                String filename = "test-report-"+System.currentTimeMillis()+"-"+topLayer.getName().replace("/", "_") + "&" + bottomLayer.getName().replace("/", "_")+".xlsx";
                reportingService.exportReportToXlsx(report, filename);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        log.info("getTrianglesBetweenLayers() end");
        return triangles;
    }

    private double getMaxEdgeOfTriangle(Triangle triangle) {
        LayerPoint vertex1 = triangle.getVertex1();
        LayerPoint vertex2 = triangle.getVertex2();
        LayerPoint vertex3 = triangle.getVertex3();

        Vector3D v1 = new Vector3D(vertex1.getX(), vertex1.getY(), vertex1.getHeight());
        Vector3D v2 = new Vector3D(vertex2.getX(), vertex2.getY(), vertex2.getHeight());
        Vector3D v3 = new Vector3D(vertex3.getX(), vertex3.getY(), vertex3.getHeight());

        double distance1to2 = v1.distance(v2);
        double distance2to3 = v2.distance(v3);
        double distance3to1 = v3.distance(v1);

        return DoubleStream.of(distance1to2, distance2to3, distance3to1)
                .max()
                .getAsDouble();
    }

    private void setCorrespondingPointsAsFirstInLayers(Layer topLayer, Layer bottomLayer) {
        CorrespondingPoints bestCorrespondingPoints = correspondingPointsSelector.getTwoCorrespondingPoints(topLayer, bottomLayer);
        int topPointIndex = topLayer.getPoints().indexOf(bestCorrespondingPoints.getTopPoint());
        int bottomPointIndex = bottomLayer.getPoints().indexOf(bestCorrespondingPoints.getBottomPoint());
        Collections.rotate(topLayer.getPoints(), -topPointIndex);
        Collections.rotate(bottomLayer.getPoints(), -bottomPointIndex);
    }

    private double calculateAdditionalValueFromLayersLengthRatio(int topLayerIndex, int bottomLayerIndex,
                                                                 int topAndBottomLayersLengthSum, double layersLengthRatio) {
        log.debug("calculating ratio - (topIndex/bottomIndex): {}/{}, lengthSum={}, currentRatio: {}, targetRatio={}",
                topLayerIndex, bottomLayerIndex, topAndBottomLayersLengthSum, (topLayerIndex != 0 && bottomLayerIndex != 0 ? (double) topLayerIndex/bottomLayerIndex : 0), layersLengthRatio);

        if(topLayerIndex == 0 || bottomLayerIndex == 0) {
            return 0;
        }
        double currentIndexesRatio = (double) topLayerIndex/bottomLayerIndex;
        return (currentIndexesRatio - layersLengthRatio)*(topLayerIndex+bottomLayerIndex)/topAndBottomLayersLengthSum;
    }

    private String getLayersIdentifier(Layer topLayer, Layer bottomLayer) {
        return topLayer.getName()+"&"+bottomLayer.getName();
    }

}
