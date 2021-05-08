package pl.ee.nerkabackend.processing.methods.borderpoint;

import pl.ee.nerkabackend.processing.model.LayerPoint;
import pl.ee.nerkabackend.processing.model.RawLayer;

import java.util.Map;

public interface BorderPointSelector {
    LayerPoint getBorderPoint(RawLayer rawLayer, Map<String, Object> params);
}
