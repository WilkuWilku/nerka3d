package pl.ee.nerkabackend.report.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Report {
    private String identifier;
    private double targetRatio;
    private List<Measurement> measurements;
}
