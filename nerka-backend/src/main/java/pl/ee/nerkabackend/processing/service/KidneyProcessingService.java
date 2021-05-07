package pl.ee.nerkabackend.processing.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.ee.nerkabackend.exception.NoDataException;
import pl.ee.nerkabackend.processing.DataLoader;
import pl.ee.nerkabackend.processing.methods.MethodTypes;
import pl.ee.nerkabackend.processing.model.KidneyVisualisationObject;
import pl.ee.nerkabackend.processing.model.Layer;
import pl.ee.nerkabackend.processing.model.RawLayer;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class KidneyProcessingService {

    @Autowired
    private LayerProcessingService layerProcessingService;
    @Autowired
    private VisualisationService visualisationService;
    @Autowired
    private DataLoader dataLoader;

    public List<Layer> getKidneyLayers(
            List<String> filenamesToLoad,
            MethodTypes.KidneyVisualisationMethodType type,
            Object... params) {
        List<Layer> layers = getProcessedLayers(filenamesToLoad, type, params);
        return layers;
    }

    /**
     * This method is a placeholder for returning visualisation object. TODO
     * @param filenamesToLoad
     * @param type
     * @param params
     * @return
     */
    public KidneyVisualisationObject getKidneyVisualisation(
        List<String> filenamesToLoad,
        MethodTypes.KidneyVisualisationMethodType type,
        Object... params
    ) {
        List<Layer> layers = getProcessedLayers(filenamesToLoad, type, params);
        KidneyVisualisationObject kidneyVisualisationObject = this.visualisationService
                .processLayersIntoKidneyVisualisation(layers, type.getVisualisationMethodType(), params);
        return kidneyVisualisationObject;
    }

    private List<Layer> getProcessedLayers(List<String> filenamesToLoad, MethodTypes.KidneyVisualisationMethodType type, Object[] params) {
        log.info("loadKidneyLayers() start - files to load: {}", filenamesToLoad.size());
        long startTime = System.currentTimeMillis();
        List<Layer> layers = filenamesToLoad.parallelStream()
                .map(filename -> {
                    try {
                        RawLayer rawLayer = dataLoader.loadKidneyDataFromFile(filename);
                        return layerProcessingService.processLayer(rawLayer,
                                type.getPointsDeterminationMethod(), params);
                    } catch (IOException | NoDataException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        log.info("loadKidneyLayers() end - successfully loaded layers: {} in [{} ms]", layers.size(), System.currentTimeMillis() - startTime);
        return layers;
    }
}
