package pl.ee.nerkabackend.processing.methods.visualisation.triangulation;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import pl.ee.nerkabackend.exception.TriangulationException;
import pl.ee.nerkabackend.processing.model.LayerPoint;
import pl.ee.nerkabackend.processing.model.triangulation.Triangle;

import javax.annotation.PostConstruct;
import java.util.stream.DoubleStream;

@Component
@Slf4j
@RequestScope
public class TriangulationByAngle extends AbstractTriangulationMethod {

    @Value("${triangulation.max.angle.indexes.ratio.diff.coefficient}")
    private Double defaultIndexesRatioDiffCoefficient;

    @PostConstruct
    private void setDefaultIndexesRatioDiffCoefficient() {
        super.setDefaultIndexesRatioDiffCoefficient(defaultIndexesRatioDiffCoefficient);
    }

    @Override
    public LayerPoint getNextPoint(LayerPoint topPoint, LayerPoint bottomPoint, LayerPoint nextTopPoint, LayerPoint nextBottomPoint, double indexesRatioDiff) {
        log.info("getNextPoint() [TriangulationByAngle] start - topPoint: {}, bottomPoint: {}, nextTopPoint: {}, nextBottomPoint: {}, coef: {}, indexesRatioDiff: {}", topPoint, bottomPoint, nextTopPoint, nextBottomPoint, indexesRatioDiffCoefficient, indexesRatioDiff);

        if(nextTopPoint == null && nextBottomPoint == null) {
            throw new TriangulationException("No next point specified for topPoint: "+topPoint.toString()+", bottomPoint: "+bottomPoint.toString());
        }
        if(nextTopPoint == null) {
            log.debug("getNextPoint() [TriangulationByAngle] end - no top point found, next point is bottom point");
            return nextBottomPoint;
        }
        if(nextBottomPoint == null) {
            log.debug("getNextPoint() [TriangulationByAngle] end - no bottom point found, next point is top point");
            return nextTopPoint;
        }

        Triangle triangleOptionTop = new Triangle(topPoint, bottomPoint, nextTopPoint);
        Triangle triangleOptionBottom = new Triangle(topPoint, bottomPoint, nextBottomPoint);
        double maxAngleOfTriangleOptionTop = getMaxAngleOfTriangle(triangleOptionTop);
        double maxAngleOfTriangleOptionBottom = getMaxAngleOfTriangle(triangleOptionBottom)-indexesRatioDiff*indexesRatioDiffCoefficient;
        log.debug("top max: {}, bottom max: {}, bottom max with ratio correction: {}", maxAngleOfTriangleOptionTop, maxAngleOfTriangleOptionBottom+indexesRatioDiff*indexesRatioDiffCoefficient, maxAngleOfTriangleOptionBottom);

        if(maxAngleOfTriangleOptionBottom < maxAngleOfTriangleOptionTop) {
            log.info("getNextPoint() [TriangulationByAngle] end - next point is bottom point");
            return nextBottomPoint;
        } else {
            log.info("getNextPoint() [TriangulationByAngle] end - next point is top point");
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
}
