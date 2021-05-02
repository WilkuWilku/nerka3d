package pl.ee.nerkabackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LayerPointDTO {
    private int x;
    private int y;
    private int height;

    public LayerPointDTO(LayerPointDTO point) {
        this.x = point.getX();
        this.y = point.getY();
        this.height = point.getHeight();
    }
}
