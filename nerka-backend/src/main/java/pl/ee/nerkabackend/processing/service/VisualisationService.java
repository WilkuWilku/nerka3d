package pl.ee.nerkabackend.processing.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.ee.nerkabackend.processing.methods.MethodTypes;
import pl.ee.nerkabackend.processing.methods.intermediateLayers.IntermediateLayersService;
import pl.ee.nerkabackend.processing.methods.visualisation.triangulation.TriangulationService;
import pl.ee.nerkabackend.processing.methods.visualisation.triangulation.builder.TriangulationServiceBuilder;
import pl.ee.nerkabackend.processing.model.Layer;
import pl.ee.nerkabackend.processing.model.LayerPoint;
import pl.ee.nerkabackend.processing.model.triangulation.Triangle;
import pl.ee.nerkabackend.webservice.dto.ParametersDTO;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class VisualisationService {

    @Autowired
    private KidneyProcessingService kidneyProcessingService;

    @Autowired
    private IntermediateLayersService intermediateLayersService;

    @Autowired
    private TriangulationServiceBuilder triangulationServiceBuilder;

    public List<Triangle> getKidneyVisualisationFromFiles(ParametersDTO parameters, MultipartFile[] files) {
        log.info("getKidneyVisualisationFromFiles start with parameters: {}", parameters);
        List<Layer> layers;
        boolean skipIndexRatioDiffCoefficient;

        switch (parameters.getPointsDeterminationMethod()) {
            case EVENLY_DISTRIBUTED_EQUINUMEROUS:
                layers = kidneyProcessingService.getKidneyLayers(files,
                        MethodTypes.KidneyVisualisationMethodType.TRIANGULATION_EQUINUMEROUS, parameters.getNumberOfPointsOnLayer());
                layers = intermediateLayersService
                        .getLayersWithIntermediateLayers(layers, parameters.getNumberOfIntermediateLayers(), parameters.getInterpolationMethod());
                skipIndexRatioDiffCoefficient = true;
                break;
            case EVENLY_DISTRIBUTED:
                layers = kidneyProcessingService.getKidneyLayers(files,
                        MethodTypes.KidneyVisualisationMethodType.TRIANGULATION, parameters.getDisplayedPointsPercent());
                skipIndexRatioDiffCoefficient = false;
                break;
            default: throw new RuntimeException("Unsupported points determination method: "+parameters.getPointsDeterminationMethod());
        }

        List<Triangle> triangles = new ArrayList<>();

        log.info("getKidneyVisualisationFromFiles() creating dedicated service");
        TriangulationService triangulationService = triangulationServiceBuilder
                .withTriangulationMethod(parameters.getTriangulationMethod())
                .withIndexRatioDiffCoefficient(skipIndexRatioDiffCoefficient ? 0 : parameters.getIndexesRatioDiffCoefficient())
                .build();

        for(int i=layers.size()-1; i>0; i--) {
            triangles.addAll(triangulationService
                    .getTrianglesBetweenLayers(layers.get(i), layers.get(i-1)));
        }
        log.info("getKidneyVisualisationFromFiles() end - triangles created: {}", triangles.size());
        return triangles;
    }

}
