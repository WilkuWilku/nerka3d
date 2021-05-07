package pl.ee.nerkabackend.webservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.ee.nerkabackend.processing.methods.MethodTypes;
import pl.ee.nerkabackend.processing.model.Layer;
import pl.ee.nerkabackend.processing.model.RawLayer;
import pl.ee.nerkabackend.processing.model.LayerPoint;
import pl.ee.nerkabackend.exception.NoDataException;
import pl.ee.nerkabackend.processing.service.KidneyProcessingService;
import pl.ee.nerkabackend.processing.service.LayerProcessingService;

import java.io.IOException;
import java.util.List;

@RestController
public class TestController {

    @Autowired
    private KidneyProcessingService kidneyProcessingService;

    @GetMapping("/layers")
    private ResponseEntity<List<Layer>> getLayers() {
        List<String> layersToLoad = List.of(
                "/ct23a/ct23a_kidney_1-0.ctl",
                "/ct23a/ct23a_kidney_2-0.ctl",
                "/ct23a/ct23a_kidney_3-0.ctl",
                "/ct23a/ct23a_kidney_4-0.ctl",
                "/ct23a/ct23a_kidney_5-0.ctl",
                "/ct23a/ct23a_kidney_6-0.ctl",
                "/ct23a/ct23a_kidney_7-0.ctl");
        List<Layer> layers = kidneyProcessingService.getKidneyLayers(layersToLoad,
                MethodTypes.KidneyVisualisationMethodType.TRIANGULARIZATION, 3.0);
        return ResponseEntity.ok(layers);
    }
}
