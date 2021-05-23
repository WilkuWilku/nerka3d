package pl.ee.nerkabackend.processing.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CorrespondingPoints {
    private LayerPoint topPoint;
    private LayerPoint bottomPoint;
}
