package pl.ee.nerkabackend.processing.methods.visualisation.triangulation;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import pl.ee.nerkabackend.exception.TriangulationException;
import pl.ee.nerkabackend.processing.model.LayerPoint;
import pl.ee.nerkabackend.processing.model.triangulation.Triangle;
import pl.ee.nerkabackend.report.ReportingValuesContainer;

import javax.annotation.PostConstruct;

@Component
@Slf4j
@RequestScope
public class TriangulationByLengthSum extends AbstractTriangulationMethod {

    @Value("${triangulation.length.sum.indexes.ratio.diff.coefficient}")
    private Double defaultIndexesRatioDiffCoefficient;

    @PostConstruct
    private void setDefaultIndexesRatioDiffCoefficient() {
        super.setDefaultIndexesRatioDiffCoefficient(defaultIndexesRatioDiffCoefficient);
    }

    @Autowired
    private ReportingValuesContainer reportingValuesContainer;

    @Override
    public LayerPoint getNextPoint(LayerPoint topPoint, LayerPoint bottomPoint, LayerPoint nextTopPoint, LayerPoint nextBottomPoint, double indexesRatioDiff, String layersIdentifier) {
        log.info("getNextPoint() [TriangulationByLengthSum] start - topPoint: {}, bottomPoint: {}, nextTopPoint: {}, nextBottomPoint: {}, coef: {}, indexesRatioDiff: {}", topPoint, bottomPoint, nextTopPoint, nextBottomPoint, indexesRatioDiffCoefficient, indexesRatioDiff);

        if(nextTopPoint == null && nextBottomPoint == null) {
            throw new TriangulationException("No next point specified for topPoint: "+topPoint.toString()+", bottomPoint: "+bottomPoint.toString());
        }
        if(nextTopPoint == null) {
            log.debug("getNextPoint() [TriangulationByLengthSum] end - no top point found, next point is bottom point");
            reportingValuesContainer.logValues(0, 0, layersIdentifier);
            return nextBottomPoint;
        }
        if(nextBottomPoint == null) {
            log.debug("getNextPoint() [TriangulationByLengthSum] end - no bottom point found, next point is top point");
            reportingValuesContainer.logValues(0, 0, layersIdentifier);
            return nextTopPoint;
        }

        Triangle triangleOptionTop = new Triangle(topPoint, bottomPoint, nextTopPoint);
        Triangle triangleOptionBottom = new Triangle(topPoint, bottomPoint, nextBottomPoint);
        double lengthSumOfTriangleOptionTop = getTriangleEdgesLengthSum(triangleOptionTop);
        double lengthSumOfTriangleOptionBottom = getTriangleEdgesLengthSum(triangleOptionBottom)-indexesRatioDiff*indexesRatioDiffCoefficient;
        log.debug("top sum: {}, bottom sum: {}, bottom sum with ratio correction: {}", lengthSumOfTriangleOptionTop, lengthSumOfTriangleOptionBottom+indexesRatioDiff*indexesRatioDiffCoefficient, lengthSumOfTriangleOptionBottom);

        if (lengthSumOfTriangleOptionBottom < lengthSumOfTriangleOptionTop) {
            log.debug("getNextPoint() [TriangulationByLengthSum] end - next point is bottom point");
            reportingValuesContainer.logValues(lengthSumOfTriangleOptionBottom+indexesRatioDiff*indexesRatioDiffCoefficient,
                    indexesRatioDiff*indexesRatioDiffCoefficient, layersIdentifier);
            return nextBottomPoint;
        } else {
            log.debug("getNextPoint() [TriangulationByLengthSum] end - next point is top point");
            reportingValuesContainer.logValues(lengthSumOfTriangleOptionTop,
                    indexesRatioDiff*indexesRatioDiffCoefficient, layersIdentifier);
            return nextTopPoint;
        }
    }

    private double getTriangleEdgesLengthSum(Triangle triangle) {
        LayerPoint vertex1 = triangle.getVertex1();
        LayerPoint vertex2 = triangle.getVertex2();
        LayerPoint vertex3 = triangle.getVertex3();

        Vector3D v1 = new Vector3D(vertex1.getX(), vertex1.getY(), vertex1.getHeight());
        Vector3D v2 = new Vector3D(vertex2.getX(), vertex2.getY(), vertex2.getHeight());
        Vector3D v3 = new Vector3D(vertex3.getX(), vertex3.getY(), vertex3.getHeight());

        double distance1to2 = v1.distance(v2);
        double distance2to3 = v2.distance(v3);
        double distance3to1 = v3.distance(v1);

        return distance1to2 + distance2to3 + distance3to1;
    }

}
