package pl.ee.nerkabackend.processing.methods.correspondingpoints.selector;

import pl.ee.nerkabackend.processing.model.*;

import java.util.List;

public interface CorrespondingPointsSelector {
    CorrespondingPoints getTwoCorrespondingPoints(Layer topLayer, Layer bottomLayer);
    SideCorrespondingPoints getBestCorrespondingPointsFromTopPoint(LayerPoint topPoint, List<LayerPoint> bottomLayerPoints, LayerSide layerSide);
}
