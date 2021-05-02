package pl.ee.nerkabackend.dto;

import lombok.Data;

@Data
public class RawLayerDTO {
    private int[][] data;
    private int index;
    private int layerNumber;
    private String name;
}
