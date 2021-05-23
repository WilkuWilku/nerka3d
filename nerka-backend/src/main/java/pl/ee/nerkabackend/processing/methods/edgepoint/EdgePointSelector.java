package pl.ee.nerkabackend.processing.methods.edgepoint;

import pl.ee.nerkabackend.processing.model.Layer;
import pl.ee.nerkabackend.processing.model.LayerPoint;

public interface EdgePointSelector {
    LayerPoint getEdgePoint(Layer layer);
}
