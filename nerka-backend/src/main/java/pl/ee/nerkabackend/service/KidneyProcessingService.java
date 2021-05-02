package pl.ee.nerkabackend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.ee.nerkabackend.dto.LayerDTO;
import pl.ee.nerkabackend.exception.NoDataException;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class KidneyProcessingService {

    @Autowired
    private LayerProcessingService layerProcessingService;

    public List<LayerDTO> loadKidneyLayers(List<String> filenamesToLoad, double pointsShownPercent) {
        log.info("loadKidneyLayers() start - files to load: {}", filenamesToLoad.size());
        long startTime = System.currentTimeMillis();
        List<LayerDTO> layers = filenamesToLoad.parallelStream()
                .map(filename -> {
                    try {
                        return layerProcessingService.loadLayer(filename, pointsShownPercent);
                    } catch (IOException | NoDataException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        log.info("loadKidneyLayers() end - successfully loaded layers: {} in [{} ms]", layers.size(), System.currentTimeMillis()-startTime);
        return layers;
    }
}
