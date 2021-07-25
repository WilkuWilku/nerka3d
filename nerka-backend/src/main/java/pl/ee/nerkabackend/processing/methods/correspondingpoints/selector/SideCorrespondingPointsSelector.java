package pl.ee.nerkabackend.processing.methods.correspondingpoints.selector;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import pl.ee.nerkabackend.processing.methods.correspondingpoints.divergence.SideCorrespondingPointsDivergence;
import pl.ee.nerkabackend.processing.methods.edgepoint.EdgePointSelector;
import pl.ee.nerkabackend.processing.methods.edgepoint.side.resolver.EdgePointSelectorResolver;
import pl.ee.nerkabackend.processing.model.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Component
@Slf4j
public class SideCorrespondingPointsSelector implements CorrespondingPointsSelector {

    @Autowired
    private EdgePointSelectorResolver edgePointSelectorResolver;

    @Autowired
    @Qualifier("axisSideCorrespondingPointsDivergence")
    private SideCorrespondingPointsDivergence sideCorrespondingPointsDivergence;


    @Override
    public CorrespondingPoints getTwoCorrespondingPoints(Layer topLayer, Layer bottomLayer) {
        log.info("getTwoCorrespondingPoints() started for layers: {} and {}", topLayer.getName(), bottomLayer.getName());
        SideCorrespondingPoints topSideCorrespondingPoints = findTwoEdgePointsOnLayer(topLayer, bottomLayer, LayerSide.TOP);
        SideCorrespondingPoints bottomSideCorrespondingPoints = findTwoEdgePointsOnLayer(topLayer, bottomLayer, LayerSide.BOTTOM);
        SideCorrespondingPoints leftSideCorrespondingPoints = findTwoEdgePointsOnLayer(topLayer, bottomLayer, LayerSide.LEFT);
        SideCorrespondingPoints rightSideCorrespondingPoints = findTwoEdgePointsOnLayer(topLayer, bottomLayer, LayerSide.RIGHT);

        Stream<SideCorrespondingPoints> bestMatchingCorrespondingPointsStream = Stream.of(
                topSideCorrespondingPoints, bottomSideCorrespondingPoints, leftSideCorrespondingPoints, rightSideCorrespondingPoints);

        SideCorrespondingPoints bestMatchingCorrespondingPoints = bestMatchingCorrespondingPointsStream
                .min(Comparator.comparing(sideCorrespondingPointsDivergence::getPointsDivergence))
                .orElse(null);
        log.info("getTwoCorrespondingPoints() end - best corresponding points: {}", bestMatchingCorrespondingPoints);
        return bestMatchingCorrespondingPoints;
    }

    @Override
    public SideCorrespondingPoints getBestCorrespondingPointsFromTopPoint(LayerPoint topPoint, List<LayerPoint> bottomLayerPoints, LayerSide layerSide) {
        //this method is not needed here
        return null;
    }

    public SideCorrespondingPoints findTwoEdgePointsOnLayer(Layer topLayer, Layer bottomLayer, LayerSide layerSide) {
        EdgePointSelector edgePointSelector = edgePointSelectorResolver.getEdgePointSelectorForSide(layerSide);
        LayerPoint topEdgePoint = edgePointSelector.getEdgePoint(topLayer);
        LayerPoint bottomEdgePoint = edgePointSelector.getEdgePoint(bottomLayer);
        return new SideCorrespondingPoints(topEdgePoint, bottomEdgePoint, layerSide);
    }


}
