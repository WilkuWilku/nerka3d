package pl.ee.nerkabackend.report.model;

import lombok.Builder;
import lombok.Data;
import pl.ee.nerkabackend.processing.methods.MethodTypes;

import java.util.List;

@Data
@Builder
public class Report {
    private String identifier;
    private double targetRatio;
    private MethodTypes.TriangulationMethodType triangulationMethodType;
    private Double indexesRatioDiffCoefficient;
    private List<RatioMeasurement> ratioMeasurements;
    private List<ComparisonValueMeasurement> comparisonValueMeasurements;
}
