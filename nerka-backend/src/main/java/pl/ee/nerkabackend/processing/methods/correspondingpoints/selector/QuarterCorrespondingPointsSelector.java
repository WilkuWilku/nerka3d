package pl.ee.nerkabackend.processing.methods.correspondingpoints.selector;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.ee.nerkabackend.processing.collections.LoopArrayList;
import pl.ee.nerkabackend.processing.methods.correspondingpoints.divergence.SideCorrespondingPointsDivergence;
import pl.ee.nerkabackend.processing.methods.edgepoint.EdgePointSelector;
import pl.ee.nerkabackend.processing.methods.edgepoint.side.resolver.EdgePointSelectorResolver;
import pl.ee.nerkabackend.processing.model.*;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Slf4j
public class QuarterCorrespondingPointsSelector implements CorrespondingPointsSelector {

    @Autowired
    private EdgePointSelectorResolver edgePointSelectorResolver;

    @Autowired
    @Qualifier("distanceSideCorrespondingPointsDivergence")
    private SideCorrespondingPointsDivergence sideCorrespondingPointsDivergence;

    @Value("${quarter.corresponding.points.points.to.check.on.one.side}")
    private Integer defaultPointsToCheckOnOneSide;

    @Value("${quarter.corresponding.points.distance.between.checked.points}")
    private Integer defaultDistanceBetweenCheckedPoints;

    @Override
    public CorrespondingPoints getTwoCorrespondingPoints(Layer topLayer, Layer bottomLayer) {
        log.info("getTwoCorrespondingPoints() start - topLayer: {}, bottomLayer: {}", topLayer.getName(), bottomLayer.getName());

        CorrespondingPoints bestCorrespondingPoints = Arrays.stream(LayerSide.values())
                .map(layerSide -> getBestCorrespondingPointsOnSide(topLayer, bottomLayer, layerSide))
                .min(Comparator.comparing(correspondingPoints -> sideCorrespondingPointsDivergence.getPointsDivergence(correspondingPoints)))
                .orElse(null);

        log.info("getTwoCorrespondingPoints() end - best corresponding points between layers: {}", bestCorrespondingPoints);
        return bestCorrespondingPoints;
    }

    public SideCorrespondingPoints getBestCorrespondingPointsOnSide(Layer topLayer, Layer bottomLayer, LayerSide layerSide) {
        EdgePointSelector edgePointSelector = edgePointSelectorResolver.getEdgePointSelectorForSide(layerSide);
        LayerPoint topEdgePoint = edgePointSelector.getEdgePoint(topLayer);
        LayerPoint bottomEdgePoint = edgePointSelector.getEdgePoint(bottomLayer);

        int topEdgePointIndex = topLayer.getPoints().indexOf(topEdgePoint);
        int bottomEdgePointIndex = bottomLayer.getPoints().indexOf(bottomEdgePoint);

        List<LayerPoint> pointsAroundTopEdgePoint = getAllCheckedPointsAround(topLayer.getPoints(), topEdgePointIndex, defaultPointsToCheckOnOneSide, defaultDistanceBetweenCheckedPoints);
        List<LayerPoint> pointsAroundBottomEdgePoint = getAllCheckedPointsAround(bottomLayer.getPoints(), bottomEdgePointIndex, defaultPointsToCheckOnOneSide, defaultDistanceBetweenCheckedPoints);

        SideCorrespondingPoints bestCorrespondingPoints = pointsAroundTopEdgePoint.stream()
                .map(topPoint -> getBestCorrespondingPointsFromTopPoint(topPoint, pointsAroundBottomEdgePoint, layerSide))
                .min(Comparator.comparing(correspondingPoints -> sideCorrespondingPointsDivergence.getPointsDivergence(correspondingPoints)))
                .orElse(null);
        log.debug("getBestCorrespondingPointsOnSide() best corresponding point on side: {} => {}", layerSide.name(), bestCorrespondingPoints);
        return bestCorrespondingPoints;
    }

    public SideCorrespondingPoints getBestCorrespondingPointsFromTopPoint(LayerPoint topPoint, List<LayerPoint> bottomLayerPoints, LayerSide layerSide) {
        SideCorrespondingPoints bestCorrespondingPoints = bottomLayerPoints.stream()
                .map(bottomPoint -> new SideCorrespondingPoints(topPoint, bottomPoint, layerSide))
                .min(Comparator.comparing(correspondingPoints -> sideCorrespondingPointsDivergence.getPointsDivergence(correspondingPoints)))
                .orElse(null);
        log.debug("getBestCorrespondingPointsFromTopPoint() best corresponding points from topPoint: {} => {}", topPoint, bestCorrespondingPoints);
        return bestCorrespondingPoints;
    }


    public List<LayerPoint> getAllCheckedPointsAround(List<LayerPoint> layerPoints, int pointIndex, int pointsToCheckOnOneSide, int distanceBetweenCheckedPoints) {
        log.info("getAllCheckedPointsAround() start - pointIndex: {}, pointsToCheckOnOneSide: {}, distanceBetweenCheckedPoints: {}", pointIndex, pointsToCheckOnOneSide, distanceBetweenCheckedPoints);

        LoopArrayList<LayerPoint> points = new LoopArrayList<>(layerPoints);

        List<Integer> indexOffsets = getIndexOffsets(pointsToCheckOnOneSide, distanceBetweenCheckedPoints);

        List<LayerPoint> checkedPoints = indexOffsets.stream()
                .map(offset -> offset+pointIndex)
                .map(points::get)
                .collect(Collectors.toList());

        log.info("getAllCheckedPointsAround() end");
        return checkedPoints;
    }

    private List<Integer> getIndexOffsets(int pointsToCheckOnOneSide, int distanceBetweenCheckedPoints) {
        List<Integer> indexOffsets = Stream.iterate(
                -pointsToCheckOnOneSide * distanceBetweenCheckedPoints,
                i -> i <= pointsToCheckOnOneSide * distanceBetweenCheckedPoints,
                i -> i + distanceBetweenCheckedPoints)
                .collect(Collectors.toList());
        log.debug("getIndexOffsets() calculated index offsets: {}", indexOffsets);
        return indexOffsets;
    }
//
//    public double getAngleBetweenPoints(LayerPoint startPoint, LayerPoint middlePoint, LayerPoint endPoint) {
//        Vector3D vector1 = new Vector3D(startPoint.getX()-middlePoint.getX(),
//                startPoint.getY()-middlePoint.getY(),
//                startPoint.getHeight()-middlePoint.getHeight());
//        Vector3D vector2 = new Vector3D(endPoint.getX()-middlePoint.getX(),
//        endPoint.getY()-middlePoint.getY(),
//        endPoint.getHeight()-middlePoint.getHeight());
//        return Math.toDegrees(Vector3D.angle(vector1, vector2));
//    }


}
