package pl.ee.nerkabackend.processing.model.triangulation;

import lombok.AllArgsConstructor;
import lombok.Data;
import pl.ee.nerkabackend.processing.model.LayerPoint;

@Data
@AllArgsConstructor
public class Triangle {
    private LayerPoint vertex1;
    private LayerPoint vertex2;
    private LayerPoint vertex3;
}
