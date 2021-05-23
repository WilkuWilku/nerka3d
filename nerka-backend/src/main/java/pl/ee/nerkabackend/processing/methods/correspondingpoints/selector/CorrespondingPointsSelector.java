package pl.ee.nerkabackend.processing.methods.correspondingpoints.selector;

import pl.ee.nerkabackend.processing.model.CorrespondingPoints;
import pl.ee.nerkabackend.processing.model.Layer;

public interface CorrespondingPointsSelector {
    CorrespondingPoints getTwoCorrespondingPoints(Layer topLayer, Layer bottomLayer);
}
