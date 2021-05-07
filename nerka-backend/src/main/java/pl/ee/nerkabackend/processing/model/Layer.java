package pl.ee.nerkabackend.processing.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class Layer {
    private List<LayerPoint> points;
    private String name;
}
