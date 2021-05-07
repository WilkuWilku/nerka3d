package pl.ee.nerkabackend.processing.methods.pointsdetermination;

import pl.ee.nerkabackend.processing.model.LayerPoint;

import java.util.List;

public interface PointsDeterminationMethod {

    List<LayerPoint> determinePointsOnLayer(List<LayerPoint> points, Object... params);
}
