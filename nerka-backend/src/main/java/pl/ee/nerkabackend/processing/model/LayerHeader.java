package pl.ee.nerkabackend.processing.model;

import lombok.Data;

@Data
public class LayerHeader {
    private String name;
    private int index;
    private int number;
    private String objectType;
}
