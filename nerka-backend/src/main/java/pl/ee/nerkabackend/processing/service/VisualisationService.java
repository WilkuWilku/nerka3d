package pl.ee.nerkabackend.processing.service;

import org.springframework.stereotype.Service;
import pl.ee.nerkabackend.processing.methods.MethodTypes;
import pl.ee.nerkabackend.processing.model.KidneyVisualisationObject;
import pl.ee.nerkabackend.processing.model.Layer;

import java.util.List;

@Service
public class VisualisationService {
    //TODO: Here Implement visualisation visualisation dependent on type
    public KidneyVisualisationObject processLayersIntoKidneyVisualisation(
        List<Layer> layers,
        MethodTypes.VisualisationMethodType type,
        Object... params
    ) {
        return null;
    }
}
