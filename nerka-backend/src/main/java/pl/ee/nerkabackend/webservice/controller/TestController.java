package pl.ee.nerkabackend.webservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.ee.nerkabackend.processing.methods.MethodTypes;
import pl.ee.nerkabackend.processing.methods.visualisation.Triangulation;
import pl.ee.nerkabackend.processing.model.Layer;
import pl.ee.nerkabackend.processing.model.triangulation.Triangle;
import pl.ee.nerkabackend.processing.service.KidneyProcessingService;

import java.util.ArrayList;
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
                "/ct23a/ct23a_kidney_7-0.ctl",
                "/ct23a/ct23a_kidney_8-0.ctl",
                "/ct23a/ct23a_kidney_9-0.ctl",
                "/ct23a/ct23a_kidney_10-0.ctl",
                "/ct23a/ct23a_kidney_11-0.ctl",
                "/ct23a/ct23a_kidney_12-0.ctl");
        List<Layer> layers = kidneyProcessingService.getKidneyLayers(layersToLoad,
                MethodTypes.KidneyVisualisationMethodType.TRIANGULATION, 5.0);
        return ResponseEntity.ok(layers);
    }

    @GetMapping("/triangles")
    private ResponseEntity<List<Triangle>> triangleTest() {
        Triangulation triangulation = new Triangulation();

        List<String> layersToLoad = List.of(
                "/ct23a/ct23a_kidney_1-0.ctl",
                "/ct23a/ct23a_kidney_2-0.ctl",
                "/ct23a/ct23a_kidney_3-0.ctl",
                "/ct23a/ct23a_kidney_4-0.ctl",
                "/ct23a/ct23a_kidney_5-0.ctl",
                "/ct23a/ct23a_kidney_6-0.ctl",
                "/ct23a/ct23a_kidney_7-0.ctl",
                "/ct23a/ct23a_kidney_8-0.ctl",
                "/ct23a/ct23a_kidney_9-0.ctl",
                "/ct23a/ct23a_kidney_10-0.ctl",
                "/ct23a/ct23a_kidney_11-0.ctl",
                "/ct23a/ct23a_kidney_12-0.ctl");

        List<Layer> layers = kidneyProcessingService.getKidneyLayers(layersToLoad,
                MethodTypes.KidneyVisualisationMethodType.TRIANGULATION, 5.0);
        List<Triangle> triangles = new ArrayList<>();

        for(int i=layers.size()-1; i>0; i--) {
            triangles.addAll(triangulation.getTrianglesBetweenLayers(layers.get(i), layers.get(i-1)));
        }

        return ResponseEntity.ok(triangles);
    }

    /***
     * TRIANGULACJA Z WRZUCONYCH PLIKÓW
     * NIE BĘDZIE DZIAŁAĆ PRAWIDŁOWO W PRZYPADKU WRZUCENIA WIELU PLIKÓW DLA JEDNEJ WARSTWY NP. kidney_10-0.ctl i kidney_10-1.ctl
     * DZIAŁA TYLKO DLA PLIKÓW Z NERKAMI, BEZ RAKA
     * JEŻELI KSZTAŁT NERKI JEST BARDZO NIEREGULARNY, MOGĄ POJAWIAĆ SIĘ BŁĘDY PRZY PRZETWARZANIU
     ***/

    @PostMapping(value = "/trianglesFromFiles", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    private ResponseEntity<List<Triangle>> trianglesFromFiles(@RequestParam("files") MultipartFile[] files) {
        List<Layer> layers = kidneyProcessingService.getKidneyLayers(files,
                MethodTypes.KidneyVisualisationMethodType.TRIANGULATION, 1.2);
        List<Triangle> triangles = new ArrayList<>();
        Triangulation triangulation = new Triangulation();

        for(int i=layers.size()-1; i>0; i--) {
            triangles.addAll(triangulation.getTrianglesBetweenLayers(layers.get(i), layers.get(i-1)));
        }

        return ResponseEntity.ok(triangles);
    }
}
