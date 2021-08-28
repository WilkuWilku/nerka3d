package pl.ee.nerkabackend.report;

import lombok.Data;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import pl.ee.nerkabackend.report.model.ComparisonValueMeasurement;

import java.util.List;
import java.util.Map;

@Data
@Component
@RequestScope
public class ReportingValuesContainer {
    // Map<LayersIdentifier, List<Measurement>
    private Map<String, List<ComparisonValueMeasurement>> measurements;

    public void logValues(double selectedValue, double correction, String layersIdentifier) {
        ComparisonValueMeasurement comparisonValueMeasurement = ComparisonValueMeasurement.builder()
                .selectedValue(selectedValue)
                .correction(correction)
                .build();
        getMeasurements().get(layersIdentifier).add(comparisonValueMeasurement);
    }
}
