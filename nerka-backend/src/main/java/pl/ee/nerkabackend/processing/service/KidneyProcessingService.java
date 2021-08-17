package pl.ee.nerkabackend.processing.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.ee.nerkabackend.exception.DataLoadingException;
import pl.ee.nerkabackend.processing.DataLoader;
import pl.ee.nerkabackend.processing.methods.MethodTypes;
import pl.ee.nerkabackend.processing.model.Layer;
import pl.ee.nerkabackend.processing.model.LayerPoint;
import pl.ee.nerkabackend.processing.model.RawLayer;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class KidneyProcessingService {

    @Autowired
    private LayerProcessingService layerProcessingService;

    @Autowired
    private DataLoader dataLoader;

    public List<Layer> getKidneyLayers(
            List<String> filenamesToLoad,
            MethodTypes.KidneyVisualisationMethodType type,
            Object... params) {
        List<Layer> layers = getProcessedLayers(filenamesToLoad, type, params);
        return layers;
    }

    public List<Layer> getKidneyLayers(
            MultipartFile[] files,
            MethodTypes.KidneyVisualisationMethodType type,
            Object... params) {
        List<Layer> layers = getProcessedLayers(files, type, params);
        return layers;
    }

    private List<Layer> getProcessedLayers(List<String> filenamesToLoad, MethodTypes.KidneyVisualisationMethodType type, Object[] params) {
        log.info("getProcessedLayers() start - files to load: {}", filenamesToLoad.size());
        long startTime = System.currentTimeMillis();
        List<Layer> layers = filenamesToLoad.parallelStream()
            .map(filename -> {
                try {
                    RawLayer rawLayer = dataLoader.loadKidneyDataFromLocalFile(filename);
                    return layerProcessingService.processLayer(rawLayer,
                            type.getPointsDeterminationMethod(), params);
                } catch (IOException | DataLoadingException e) {
                    e.printStackTrace();
                }
                return null;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        log.info("getProcessedLayers() end - successfully loaded layers: {} in [{} ms]", layers.size(), System.currentTimeMillis() - startTime);
        return layers;
    }

    private List<Layer> getProcessedLayers(MultipartFile[] uploadedFiles, MethodTypes.KidneyVisualisationMethodType type, Object[] params) {
        log.info("getProcessedLayers() start - files to load: {}", uploadedFiles.length);
        long startTime = System.currentTimeMillis();
        List<Layer> layers = Arrays.stream(uploadedFiles).parallel()
                .map(file -> {
                    try {
                        RawLayer rawLayer = dataLoader.loadKidneyDataFromUploadedFile(file);
                        return layerProcessingService.processLayer(rawLayer,
                                type.getPointsDeterminationMethod(), params);
                    } catch (IOException | DataLoadingException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        log.info("getProcessedLayers() end - successfully loaded layers: {} in [{} ms]", layers.size(), System.currentTimeMillis() - startTime);
        return layers;
    }

    public List<Layer> getRawLayers(MultipartFile[] uploadedFiles) {
        List<Layer> layerPoints = Arrays.stream(uploadedFiles).parallel()
                .map(file -> {
                    try {
                        RawLayer rawLayer = dataLoader.loadKidneyDataFromUploadedFile(file);
                        return layerProcessingService.getLayer(rawLayer);
                    } catch (DataLoadingException | IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .collect(Collectors.toList());
        return layerPoints;
    }
}
