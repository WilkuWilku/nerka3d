package pl.ee.nerkabackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class LayerDTO {
    private List<LayerPointDTO> points;
    private String name;
}
