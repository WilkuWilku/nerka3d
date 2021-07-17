package pl.ee.nerkabackend.report.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Measurement {
    private int currentStep;
    private int topIndex;
    private int bottomIndex;
    private double currentRatio;
}
