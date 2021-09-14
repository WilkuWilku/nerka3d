package pl.ee.nerkabackend.report;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.ee.nerkabackend.processing.methods.MethodTypes;
import pl.ee.nerkabackend.processing.methods.intermediateLayers.IntermediateLayersService;
import pl.ee.nerkabackend.processing.model.Layer;
import pl.ee.nerkabackend.processing.model.LayerPoint;
import pl.ee.nerkabackend.processing.service.KidneyProcessingService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

@Service
public class IntermediateLayersTestService {

    private static final IntermediateLayersService.InterpolationType CUBIC_NAME
            = IntermediateLayersService.InterpolationType.CubicSpline;
    private static final IntermediateLayersService.InterpolationType LAGRANGE_NAME
            = IntermediateLayersService.InterpolationType.Lagrange;
    private static final IntermediateLayersService.InterpolationType LINEAR_NAME
            = IntermediateLayersService.InterpolationType.Linear;

    @Autowired
    private KidneyProcessingService kidneyProcessingService;

    @Autowired
    private IntermediateLayersService intermediateLayersService;

    private HashMap<String, List<Long>> results;
    public HashMap<String, List<Long>> timeTestInterpolationMethods() {
        initiateResults();
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
        testLayerDependency(layersToLoad, 250, 20);
        testPointNumberDependency(layersToLoad, 600, 20, 20);
        testIntermediateLayerDependency(layersToLoad, 600, 100, 3);
        return results;
    }

    private void initiateResults() {
        results = new HashMap<>();
    }

    private void testIntermediateLayerDependency(List<String> layersToload, Integer numberOfPointsOnLayer, int maxNumberOfIntermediate, int step) {
        results.put("numberOfIntermediate-header", new ArrayList<>());
        results.put("numberOfIntermediate-lagrange", new ArrayList<>());
        results.put("numberOfIntermediate-lagrangeInterval", new ArrayList<>());
        results.put("numberOfIntermediate-linear", new ArrayList<>());
        results.put("numberOfIntermediate-cubic", new ArrayList<>());

        List<Layer> layers = kidneyProcessingService.getKidneyLayers(layersToload,
                MethodTypes.KidneyVisualisationMethodType.TRIANGULATION_EQUINUMEROUS, numberOfPointsOnLayer);
        for (int i = 2; i < maxNumberOfIntermediate; i+=step) {
            results.get("numberOfIntermediate-header").add((long) (i*step));
            performIntermediateLayersInjection(
                    "numberOfIntermediate-lagrange",
                    LAGRANGE_NAME,
                    layers,
                    i,
                    numberOfPointsOnLayer,
                    layers.size() - 1);
            performIntermediateLayersInjection(
                    "numberOfIntermediate-lagrangeInterval",
                    LAGRANGE_NAME,
                    layers,
                    i,
                    numberOfPointsOnLayer,
                    2);
            performIntermediateLayersInjection(
                    "numberOfIntermediate-linear",
                    LINEAR_NAME,
                    layers,
                    i,
                    numberOfPointsOnLayer,
                    0);
            performIntermediateLayersInjection(
                    "numberOfIntermediate-cubic",
                    CUBIC_NAME,
                    layers,
                    i,
                    numberOfPointsOnLayer,
                    0);
        }
    }

    private void testLayerDependency(List<String> layersToload, Integer numberOfPointsOnLayer, Integer numberOfIntermediateLayers) {
        results.put("numberOfLayers-header", new ArrayList<>());
        results.put("numberOfLayers-lagrange", new ArrayList<>());
        results.put("numberOfLayers-lagrangeInterval", new ArrayList<>());
        results.put("numberOfLayers-linear", new ArrayList<>());
        results.put("numberOfLayers-cubic", new ArrayList<>());

        List<Layer> layers = kidneyProcessingService.getKidneyLayers(layersToload,
                MethodTypes.KidneyVisualisationMethodType.TRIANGULATION_EQUINUMEROUS, numberOfPointsOnLayer);
        for (int i = 2; i < layers.size(); i++) {
            ArrayList<Layer> layersSubset = new ArrayList<>(layers);
            //layersSubset = new ArrayList<>(layersSubset.subList(0, i+1));
            layersSubset = new ArrayList<>();
            appendAdditionalLayers(layersSubset, layers, i*5);
            results.get("numberOfLayers-header").add((long) (i*5 + 1));
            performIntermediateLayersInjection(
                    "numberOfLayers-lagrange",
                    LAGRANGE_NAME,
                    layersSubset,
                    numberOfIntermediateLayers,
                    numberOfPointsOnLayer,
                    layersSubset.size() - 1);
            performIntermediateLayersInjection(
                    "numberOfLayers-lagrangeInterval",
                    LAGRANGE_NAME,
                    layersSubset,
                    numberOfIntermediateLayers,
                    numberOfPointsOnLayer,
                    2);
            performIntermediateLayersInjection(
                    "numberOfLayers-linear",
                    LINEAR_NAME,
                    layersSubset,
                    numberOfIntermediateLayers,
                    numberOfPointsOnLayer,
                    0);
            performIntermediateLayersInjection(
                    "numberOfLayers-cubic",
                    CUBIC_NAME,
                    layersSubset,
                    numberOfIntermediateLayers,
                    numberOfPointsOnLayer,
                    0);
        }
    }

    private void appendAdditionalLayers(List<Layer> layersSubset, List<Layer> layers, int i) {
        Random r = new Random();
        int layersHeightDiff = layers.get(1).getPoints().get(0).getHeight() - layers.get(0).getPoints().get(0).getHeight();
        for (int j = 0; j < i; j++) {
            Layer layerOriginal = layers.get(r.nextInt(layers.size()-1));
            Layer layerToAdd = cloneLayer(layerOriginal);
            int lastLayerHeight = layersSubset.size() == 0 ? 0 : layersSubset.get(layersSubset.size() - 1).getPoints().get(0).getHeight();
            layerToAdd.getPoints().forEach(layerPoint -> layerPoint.setHeight(lastLayerHeight + layersHeightDiff));
            layersSubset.add(layerToAdd);
        }
    }

    private Layer cloneLayer(Layer input) {
        Layer result = new Layer(new ArrayList<>(), "n/a");
        input.getPoints().forEach(layerPoint -> {
            LayerPoint newPoint = new LayerPoint();
            newPoint.setHeight(layerPoint.getHeight());
            newPoint.setX(layerPoint.getX());
            newPoint.setY(layerPoint.getY());
            result.getPoints().add(newPoint);
        });
        return result;
    }

    private void testPointNumberDependency(List<String> layersToload, Integer maxNumberOfPoints, Integer step, Integer numberOfIntermediateLayers) {
        results.put("numberOfPoints-header", new ArrayList<>());
        results.put("numberOfPoints-lagrange", new ArrayList<>());
        results.put("numberOfPoints-lagrangeInterval", new ArrayList<>());
        results.put("numberOfPoints-cubic", new ArrayList<>());
        results.put("numberOfPoints-linear", new ArrayList<>());

        for (int i = 10; i < maxNumberOfPoints; i+=step) {
            List<Layer> layers = kidneyProcessingService.getKidneyLayers(layersToload,
                    MethodTypes.KidneyVisualisationMethodType.TRIANGULATION_EQUINUMEROUS, i);
            results.get("numberOfPoints-header").add((long) (i + 1));
            performIntermediateLayersInjection(
                    "numberOfPoints-lagrange",
                    LAGRANGE_NAME,
                    layers,
                    numberOfIntermediateLayers,
                    i,
                    layers.size() - 1);
            performIntermediateLayersInjection(
                    "numberOfPoints-lagrangeInterval",
                    LAGRANGE_NAME,
                    layers,
                    numberOfIntermediateLayers,
                    i,
                    2);
            performIntermediateLayersInjection(
                    "numberOfPoints-linear",
                    LINEAR_NAME,
                    layers,
                    numberOfIntermediateLayers,
                    i,
                    0);
            performIntermediateLayersInjection(
                    "numberOfPoints-cubic",
                    CUBIC_NAME,
                    layers,
                    numberOfIntermediateLayers,
                    i,
                    0);
        }
    }

    private void performIntermediateLayersInjection(
            String key,
            IntermediateLayersService.InterpolationType interpolationType,
            List<Layer> layers,
            Integer numberOfIntermediateLayers,
            Integer numberOfPointsOnLayer,
            Integer lagrangianOrder
    ) {
        Long startTime = System.currentTimeMillis();
        intermediateLayersService
                .getLayersWithIntermediateLayers(
                        layers,
                        numberOfIntermediateLayers,
                        lagrangianOrder,
                        interpolationType);
        Long endTime = System.currentTimeMillis();
        results.get(key).add(endTime - startTime);
    }

    private ArrayList<Layer> cloneList(ArrayList<Layer> input) {
        ArrayList<Layer> result = new ArrayList<>();
        input.forEach(inputLayer -> {
            Layer layer = new Layer(new ArrayList<>(), "a");
            layer.getPoints().addAll(inputLayer.getPoints());
            result.add(layer);
        });
        return result;
    }

}
