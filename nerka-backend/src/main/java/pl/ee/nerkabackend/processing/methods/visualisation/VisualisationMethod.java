package pl.ee.nerkabackend.processing.methods.visualisation;

import pl.ee.nerkabackend.processing.model.KidneyVisualisationObject;
import pl.ee.nerkabackend.processing.model.Layer;

import java.util.List;

public interface VisualisationMethod {

    KidneyVisualisationObject getKidneyVisualisationObject(List<Layer> layers, Object... params);
}
