package pl.ee.nerkabackend.report.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ComparisonValueMeasurement {
    private double selectedValue;
    private double correction;
}
