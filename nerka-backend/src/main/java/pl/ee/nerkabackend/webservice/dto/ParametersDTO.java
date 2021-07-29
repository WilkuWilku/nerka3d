package pl.ee.nerkabackend.webservice.dto;

import lombok.Data;
import pl.ee.nerkabackend.processing.methods.intermediateLayers.IntermediateLayersService;

@Data
public class ParametersDTO {
    private int numberOfPointsOnLayer;
    private int numberOfIntermediateLayers;
    private IntermediateLayersService.InterpolationType interpolationMethod;
}
