package pl.ee.nerkabackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.ee.nerkabackend.dto.LayerDTO;
import pl.ee.nerkabackend.dto.RawLayerDTO;
import pl.ee.nerkabackend.dto.LayerPointDTO;
import pl.ee.nerkabackend.exception.NoDataException;
import pl.ee.nerkabackend.service.CtlParserService;
import pl.ee.nerkabackend.service.KidneyProcessingService;
import pl.ee.nerkabackend.service.LayerProcessingService;

import java.io.IOException;
import java.util.List;

@RestController
public class TestController {

    @Autowired
    private CtlParserService ctlParserService;

    @Autowired
    private LayerProcessingService layerProcessingService;

    @Autowired
    private KidneyProcessingService kidneyProcessingService;

    @GetMapping("/test")
    private ResponseEntity<RawLayerDTO> test() throws IOException, NoDataException {
        RawLayerDTO result = ctlParserService.loadKidneyDataFromFile("/ct23a/ct23a_kidney_8-0.ctl");
        return ResponseEntity.ok(result);
    }

    @GetMapping("/border")
    private ResponseEntity<List<LayerPointDTO>> getBorder() throws IOException, NoDataException {
        List<LayerPointDTO> reducedPoints = layerProcessingService.getReducedPointsFromFile("/ct23a/ct23a_kidney_2-0.ctl", 1.5);
        return ResponseEntity.ok(reducedPoints);
    }

    @GetMapping("/layers")
    private ResponseEntity<List<LayerDTO>> getLayers() {
        List<String> layersToLoad = List.of(
                "/ct23a/ct23a_kidney_1-0.ctl",
                "/ct23a/ct23a_kidney_2-0.ctl",
                "/ct23a/ct23a_kidney_3-0.ctl",
                "/ct23a/ct23a_kidney_4-0.ctl",
                "/ct23a/ct23a_kidney_5-0.ctl",
                "/ct23a/ct23a_kidney_6-0.ctl",
                "/ct23a/ct23a_kidney_7-0.ctl");
        List<LayerDTO> layers = kidneyProcessingService.loadKidneyLayers(layersToLoad, 100);
        return ResponseEntity.ok(layers);
    }
}
