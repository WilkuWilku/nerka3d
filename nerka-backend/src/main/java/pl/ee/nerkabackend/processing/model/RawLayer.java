package pl.ee.nerkabackend.processing.model;

import lombok.Data;

@Data
public class RawLayer {
    private int[][] data;
    private int index;
    private int layerNumber;
    private String name;
    private LayerTranslation translation;
}
