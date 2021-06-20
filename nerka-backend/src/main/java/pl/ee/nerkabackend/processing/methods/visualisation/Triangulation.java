package pl.ee.nerkabackend.processing.methods.visualisation;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.ee.nerkabackend.exception.TriangulationException;
import pl.ee.nerkabackend.processing.methods.correspondingpoints.selector.CorrespondingPointsSelector;
import pl.ee.nerkabackend.processing.model.CorrespondingPoints;
import pl.ee.nerkabackend.processing.model.KidneyVisualisationObject;
import pl.ee.nerkabackend.processing.model.Layer;
import pl.ee.nerkabackend.processing.model.LayerPoint;
import pl.ee.nerkabackend.processing.model.triangulation.Triangle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.DoubleStream;

@Slf4j
@Component
public class Triangulation implements VisualisationMethod {

    @Autowired
    @Qualifier("quarterCorrespondingPointsSelector")
    private CorrespondingPointsSelector correspondingPointsSelector;

    @Value("${triangulation.max.angle.indexes.ratio.diff.coefficient}")
    private Double maxAngleIndexesRatioDiffCoefficient;

    @Value("${triangulation.length.sum.indexes.ratio.diff.coefficient}")
    private Double lengthSumIndexesRatioDiffCoefficient;

    @Value("${triangulation.longest.edge.indexes.ratio.diff.coefficient}")
    private Double longestEdgeIndexesRatioDiffCoefficient;

    @Value("${triangulation.points.distance.indexes.ratio.diff.coefficient}")
    private Double pointsDistanceIndexesRatioCoefficient;

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

        do {
            double ratioDiffValue = calculateAdditionalValueFromLayersLengthRatio(topLayerIndex, bottomLayerIndex, layersPointsCount, layersLengthRatio);
            LayerPoint nextPoint = getNextTrianglePointByPointsDistance(currentTopPoint, currentBottomPoint,
                    nextTopPoint, nextBottomPoint, ratioDiffValue);
            Triangle nextTriangle = new Triangle(currentTopPoint, currentBottomPoint, nextPoint);
            triangles.add(nextTriangle);

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

        log.info("getTrianglesBetweenLayers() end");
        return triangles;
    }

    private LayerPoint getNextTrianglePointByAngle(LayerPoint topPoint, LayerPoint bottomPoint,
                                                  LayerPoint nextTopPoint, LayerPoint nextBottomPoint,
                                                   double indexesRatioDiff) {
        if(nextTopPoint == null && nextBottomPoint == null) {
            throw new TriangulationException("No next point specified for topPoint: "+topPoint.toString()+", bottomPoint: "+bottomPoint.toString());
        }
        if(nextTopPoint == null) {
            log.debug("getNextTrianglePointByAngle() end - no top point found, next point is bottom point");
            return nextBottomPoint;
        }
        if(nextBottomPoint == null) {
            log.debug("getNextTrianglePointByAngle() end - no bottom point found, next point is top point");
            return nextTopPoint;
        }

        log.info("getNextTrianglePointByAngle() start - topPoint: {}, bottomPoint: {}, nextTopPoint: {}, nextBottomPoint: {}, indexesRatioDiff: {}", topPoint, bottomPoint, nextTopPoint, nextBottomPoint, indexesRatioDiff);
        Triangle triangleOptionTop = new Triangle(topPoint, bottomPoint, nextTopPoint);
        Triangle triangleOptionBottom = new Triangle(topPoint, bottomPoint, nextBottomPoint);
        double maxAngleOfTriangleOptionTop = getMaxAngleOfTriangle(triangleOptionTop);
        double maxAngleOfTriangleOptionBottom = getMaxAngleOfTriangle(triangleOptionBottom)+indexesRatioDiff*maxAngleIndexesRatioDiffCoefficient;
        log.debug("top max: {}, bottom max: {}, bottom max with ratio correction: {}", maxAngleOfTriangleOptionTop, maxAngleOfTriangleOptionBottom-indexesRatioDiff*maxAngleIndexesRatioDiffCoefficient, maxAngleOfTriangleOptionBottom);

        if(maxAngleOfTriangleOptionBottom < maxAngleOfTriangleOptionTop) {
            log.info("getNextTrianglePointByAngle() end - next point is bottom point");
            return nextBottomPoint;
        } else {
            log.info("getNextTrianglePointByAngle() end - next point is top point");
            return nextTopPoint;
        }
    }

    private LayerPoint getNextTrianglePointByEdgesLengthSum(LayerPoint topPoint, LayerPoint bottomPoint,
                                                   LayerPoint nextTopPoint, LayerPoint nextBottomPoint,
                                                    double indexesRatioDiff) {
        log.info("getNextTrianglePointByEdgesLengthSum() start - topPoint: {}, bottomPoint: {}, nextTopPoint: {}, nextBottomPoint: {}, indexesRatioDiff: {}",
                topPoint, bottomPoint, nextTopPoint, nextBottomPoint, indexesRatioDiff);

        if(nextTopPoint == null && nextBottomPoint == null) {
            throw new TriangulationException("No next point specified for topPoint: "+topPoint.toString()+", bottomPoint: "+bottomPoint.toString());
        }
        if(nextTopPoint == null) {
            log.debug("getNextTrianglePointByEdgesLengthSum() end - no top point found, next point is bottom point");
            return nextBottomPoint;
        }
        if(nextBottomPoint == null) {
            log.debug("getNextTrianglePointByEdgesLengthSum() end - no bottom point found, next point is top point");
            return nextTopPoint;
        }

        Triangle triangleOptionTop = new Triangle(topPoint, bottomPoint, nextTopPoint);
        Triangle triangleOptionBottom = new Triangle(topPoint, bottomPoint, nextBottomPoint);
        double lengthSumOfTriangleOptionTop = getTriangleEdgesLengthSum(triangleOptionTop);
        double lengthSumOfTriangleOptionBottom = getTriangleEdgesLengthSum(triangleOptionBottom)+indexesRatioDiff*lengthSumIndexesRatioDiffCoefficient;
        log.debug("top sum: {}, bottom sum: {}, bottom sum with ratio correction: {}", lengthSumOfTriangleOptionTop, lengthSumOfTriangleOptionBottom-indexesRatioDiff*lengthSumIndexesRatioDiffCoefficient, lengthSumOfTriangleOptionBottom);

        if (lengthSumOfTriangleOptionBottom < lengthSumOfTriangleOptionTop) {
            log.debug("getNextTrianglePointByEdgesLengthSum() end - next point is bottom point");
            return nextBottomPoint;
        } else {
            log.debug("getNextTrianglePointByEdgesLengthSum() end - next point is top point");
            return nextTopPoint;
        }
    }

    private LayerPoint getNextTrianglePointByPointsDistance(LayerPoint topPoint, LayerPoint bottomPoint,
                                                            LayerPoint nextTopPoint, LayerPoint nextBottomPoint,
                                                            double indexesRatioDiff) {
        log.info("getNextTrianglePointByPointsDistance() start - topPoint: {}, bottomPoint: {}, nextTopPoint: {}, nextBottomPoint: {}, indexesRatioDiff: {}",
                topPoint, bottomPoint, nextTopPoint, nextBottomPoint, indexesRatioDiff);

        if(nextTopPoint == null && nextBottomPoint == null) {
            throw new TriangulationException("No next point specified for topPoint: "+topPoint.toString()+", bottomPoint: "+bottomPoint.toString());
        }
        if(nextTopPoint == null) {
            log.debug("getNextTrianglePointByPointsDistance() end - no top point found, next point is bottom point");
            return nextBottomPoint;
        }
        if(nextBottomPoint == null) {
            log.debug("getNextTrianglePointByPointsDistance() end - no bottom point found, next point is top point");
            return nextTopPoint;
        }

        double distanceToNextTopPoint = getDistanceBetweenPoints(nextTopPoint, bottomPoint);
        double distanceToNextBottomPoint = getDistanceBetweenPoints(nextBottomPoint, topPoint)-indexesRatioDiff* pointsDistanceIndexesRatioCoefficient;
        log.debug("top length: {}, bottom length: {}, bottom length with ratio correction: {}", distanceToNextTopPoint, distanceToNextBottomPoint+indexesRatioDiff*pointsDistanceIndexesRatioCoefficient, distanceToNextBottomPoint);

        if (distanceToNextBottomPoint < distanceToNextTopPoint) {
            log.debug("getNextTrianglePointByPointsDistance() end - next point is bottom point");
            return nextBottomPoint;
        } else {
            log.debug("getNextTrianglePointByPointsDistance() end - next point is top point");
            return nextTopPoint;
        }
    }

    private double getMaxAngleOfTriangle(Triangle triangle) {
        LayerPoint vertex1 = triangle.getVertex1();
        LayerPoint vertex2 = triangle.getVertex2();
        LayerPoint vertex3 = triangle.getVertex3();

        Vector3D vector1to2 = new Vector3D(vertex1.getX() - vertex2.getX(),
                vertex1.getY() - vertex2.getY(),
                vertex1.getHeight() - vertex2.getHeight());
        Vector3D vector2to3 = new Vector3D(vertex2.getX() - vertex3.getX(),
                vertex2.getY() - vertex3.getY(),
                vertex2.getHeight() - vertex3.getHeight());
        Vector3D vector3to1 = new Vector3D(vertex3.getX() - vertex1.getX(),
                vertex3.getY() - vertex1.getY(),
                vertex3.getHeight() - vertex1.getHeight());

        // todo: może zostawić radiany żeby nie robić niepotrzebnych obliczeń
        double angle123 = Math.toDegrees(Vector3D.angle(vector1to2.negate(), vector2to3));
        double angle231 = Math.toDegrees(Vector3D.angle(vector2to3.negate(), vector3to1));
        double angle312 = 180-angle123-angle231;
        return DoubleStream.of(angle123, angle231, angle312)
                .max()
                .getAsDouble();
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

    private double getDistanceBetweenPoints(LayerPoint point1, LayerPoint point2) {
        Vector3D v1 = new Vector3D(point1.getX(), point1.getY(), point1.getHeight());
        Vector3D v2 = new Vector3D(point2.getX(), point2.getY(), point2.getHeight());
        return v1.distance(v2);
    }
}
