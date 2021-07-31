package pl.ee.nerkabackend.processing.methods.visualisation;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.ee.nerkabackend.exception.TriangulationException;
import pl.ee.nerkabackend.processing.model.LayerPoint;

@Service
@Slf4j
public class TriangulationByPointsDistance implements TriangulationMethod {

    @Value("${triangulation.points.distance.indexes.ratio.diff.coefficient}")
    private Double pointsDistanceIndexesRatioCoefficient;

    @Override
    public LayerPoint getNextPoint(LayerPoint topPoint, LayerPoint bottomPoint,
                                                            LayerPoint nextTopPoint, LayerPoint nextBottomPoint,
                                                            double indexesRatioDiff) {
        log.info("getNextPoint() [TriangulationByPointsDistance] start - topPoint: {}, bottomPoint: {}, nextTopPoint: {}, nextBottomPoint: {}, indexesRatioDiff: {}",
                topPoint, bottomPoint, nextTopPoint, nextBottomPoint, indexesRatioDiff);

        if(nextTopPoint == null && nextBottomPoint == null) {
            throw new TriangulationException("No next point specified for topPoint: "+topPoint.toString()+", bottomPoint: "+bottomPoint.toString());
        }
        if(nextTopPoint == null) {
            log.debug("getNextPoint() [TriangulationByPointsDistance] end - no top point found, next point is bottom point");
            return nextBottomPoint;
        }
        if(nextBottomPoint == null) {
            log.debug("getNextPoint() [TriangulationByPointsDistance] end - no bottom point found, next point is top point");
            return nextTopPoint;
        }

        double distanceToNextTopPoint = getDistanceBetweenPoints(nextTopPoint, bottomPoint);
        double distanceToNextBottomPoint = getDistanceBetweenPoints(nextBottomPoint, topPoint)-indexesRatioDiff* pointsDistanceIndexesRatioCoefficient;
        log.debug("top length: {}, bottom length: {}, bottom length with ratio correction: {}", distanceToNextTopPoint, distanceToNextBottomPoint+indexesRatioDiff*pointsDistanceIndexesRatioCoefficient, distanceToNextBottomPoint);

        if (distanceToNextBottomPoint < distanceToNextTopPoint) {
            log.debug("getNextPoint() [TriangulationByPointsDistance] end - next point is bottom point");
            return nextBottomPoint;
        } else {
            log.debug("getNextPoint() [TriangulationByPointsDistance] end - next point is top point");
            return nextTopPoint;
        }
    }

    private double getDistanceBetweenPoints(LayerPoint point1, LayerPoint point2) {
        Vector3D v1 = new Vector3D(point1.getX(), point1.getY(), point1.getHeight());
        Vector3D v2 = new Vector3D(point2.getX(), point2.getY(), point2.getHeight());
        return v1.distance(v2);
    }
}
