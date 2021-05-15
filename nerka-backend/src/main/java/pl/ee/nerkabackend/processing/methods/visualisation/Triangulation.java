package pl.ee.nerkabackend.processing.methods.visualisation;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import pl.ee.nerkabackend.exception.TriangulationException;
import pl.ee.nerkabackend.processing.model.KidneyVisualisationObject;
import pl.ee.nerkabackend.processing.model.Layer;
import pl.ee.nerkabackend.processing.model.LayerPoint;
import pl.ee.nerkabackend.processing.model.triangulation.Triangle;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

@Slf4j
public class Triangulation implements VisualisationMethod {

    @Override
    public KidneyVisualisationObject getKidneyVisualisationObject(List<Layer> layers, Object... params) {
        return null;
    }


    public List<Triangle> getTrianglesBetweenLayers(Layer topLayer, Layer bottomLayer) {
        log.info("getTrianglesBetweenLayers() start - topLayer: {}, bottomLayer: {}", topLayer.getName(), bottomLayer.getName());
        List<LayerPoint> topPoints = topLayer.getPoints();
        List<LayerPoint> bottomPoints = bottomLayer.getPoints();
        List<Triangle> triangles = new ArrayList<>();
        int topLayerIndex = 0;
        int bottomLayerIndex = 0;

        LayerPoint firstTopPoint = topPoints.get(0);
        LayerPoint firstBottomPoint = bottomPoints.get(0);

        LayerPoint currentTopPoint = firstTopPoint;
        LayerPoint currentBottomPoint = firstBottomPoint;
        LayerPoint nextTopPoint = topPoints.get(topLayerIndex + 1);
        LayerPoint nextBottomPoint = bottomPoints.get(bottomLayerIndex + 1);

        do {
            LayerPoint nextPoint = getNextTrianglePointByLength(currentTopPoint, currentBottomPoint, nextTopPoint, nextBottomPoint);
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

    public LayerPoint getNextTrianglePointByAngle(LayerPoint topPoint, LayerPoint bottomPoint,
                                                  LayerPoint nextTopPoint, LayerPoint nextBottomPoint) {
        log.info("getNextTrianglePointByAngle() start - topPoint: {}, bottomPoint: {}, nextTopPoint: {}, nextBottomPoint: {}", topPoint, bottomPoint, nextTopPoint, nextBottomPoint);
        Triangle triangleOptionTop = new Triangle(topPoint, bottomPoint, nextTopPoint);
        Triangle triangleOptionBottom = new Triangle(topPoint, bottomPoint, nextBottomPoint);
        double maxAngleOfTriangleOptionTop = getMaxAngleOfTriangle(triangleOptionTop);
        double maxAngleOfTriangleOptionBottom = getMaxAngleOfTriangle(triangleOptionBottom);
        if(maxAngleOfTriangleOptionBottom < maxAngleOfTriangleOptionTop) {
            log.info("getNextTrianglePointByAngle end - next point is bottom point");
            return nextBottomPoint;
        } else {
            log.info("getNextTrianglePointByAngle end - next point is top point");
            return nextTopPoint;
        }
    }

    public LayerPoint getNextTrianglePointByLength(LayerPoint topPoint, LayerPoint bottomPoint,
                                                   LayerPoint nextTopPoint, LayerPoint nextBottomPoint) {
        log.info("getNextTrianglePointByLength() start - topPoint: {}, bottomPoint: {}, nextTopPoint: {}, nextBottomPoint: {}", topPoint, bottomPoint, nextTopPoint, nextBottomPoint);

        if(nextTopPoint == null && nextBottomPoint == null) {
            throw new TriangulationException("No next point specified for topPoint: "+topPoint.toString()+", bottomPoint: "+bottomPoint.toString());
        }
        if(nextTopPoint == null) {
            return nextBottomPoint;
        }
        if(nextBottomPoint == null) {
            return nextTopPoint;
        }

        Triangle triangleOptionTop = new Triangle(topPoint, bottomPoint, nextTopPoint);
        Triangle triangleOptionBottom = new Triangle(topPoint, bottomPoint, nextBottomPoint);
        double lengthSumOfTriangleOptionTop = getTriangleEdgesLengthSum(triangleOptionTop);
        double lengthSumOfTriangleOptionBottom = getTriangleEdgesLengthSum(triangleOptionBottom);
        if (lengthSumOfTriangleOptionBottom < lengthSumOfTriangleOptionTop) {
            log.debug("getNextTrianglePointByLength end - next point is bottom point");
            return nextBottomPoint;
        } else {
            log.debug("getNextTrianglePointByLength end - next point is top point");
            return nextTopPoint;
        }
    }

    public double getMaxAngleOfTriangle(Triangle triangle) {
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

    public double getTriangleEdgesLengthSum(Triangle triangle) {
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
