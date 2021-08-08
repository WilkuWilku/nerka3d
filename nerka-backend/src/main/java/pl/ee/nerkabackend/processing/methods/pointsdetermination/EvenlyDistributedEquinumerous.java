package pl.ee.nerkabackend.processing.methods.pointsdetermination;

import lombok.extern.slf4j.Slf4j;
import pl.ee.nerkabackend.processing.model.LayerPoint;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class EvenlyDistributedEquinumerous implements PointsDeterminationMethod {

    @Override
    public List<LayerPoint> determinePointsOnLayer(List<LayerPoint> points, Object... params) {
        double pointsOnLayer = (int) params[0];
        double pointsShownPercent = pointsOnLayer / points.size() * 100;
        log.info("reducePoints() start - points count before reduction: {}, pointsShownPercent: {}", points.size(), pointsShownPercent);
        if(pointsShownPercent <= 0 || pointsShownPercent > 100) {
            log.warn("reducePoints() incorrect pointsShownPercent value: {} - returning original points list", pointsShownPercent);
            return points;
        }
        int pointsNumber = points.size();
        int indexesRange = Math.round((float) (pointsNumber*pointsShownPercent)/100);
        double delta = (double) pointsNumber/indexesRange;
        log.info("reducePoints() points number: {}, indexesRange: {}, delta: {}", pointsNumber, indexesRange, delta);
        List<LayerPoint> reducedPoints = IntStream.range(0, indexesRange)
                .boxed()
                .peek(index -> log.debug("before: {}, after: {}", index, (int)(index*delta)))
                .map(index -> (int) (index*delta))
                .map(points::get)
                .collect(Collectors.toList());
        log.info("reducePoints() end - points count after reduction: {}", reducedPoints.size());
        return reducedPoints;
    }
}
