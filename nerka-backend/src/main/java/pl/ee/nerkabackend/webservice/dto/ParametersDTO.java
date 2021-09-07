package pl.ee.nerkabackend.webservice.dto;

import lombok.Data;
import pl.ee.nerkabackend.processing.methods.MethodTypes;
import pl.ee.nerkabackend.processing.methods.intermediateLayers.IntermediateLayersService;

@Data
public class ParametersDTO {
    private double displayedPointsPercent;
    private int numberOfPointsOnLayer;
    private int numberOfIntermediateLayers;
    private int lagrangianOrder;
    private IntermediateLayersService.InterpolationType interpolationMethod;
    private MethodTypes.TriangulationMethodType triangulationMethod;
    private MethodTypes.PointsDeterminationMethodType pointsDeterminationMethod;
    private double indexesRatioDiffCoefficient;
}
