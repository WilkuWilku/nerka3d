package pl.ee.nerkabackend.processing.methods.visualisation;

import pl.ee.nerkabackend.processing.model.LayerPoint;

public interface TriangulationMethod {
    LayerPoint getNextPoint(LayerPoint topPoint, LayerPoint bottomPoint,
                            LayerPoint nextTopPoint, LayerPoint nextBottomPoint,
                            double indexesRatioDiff);
}
