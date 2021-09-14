package pl.ee.nerkabackend.processing.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LayerPoint {
    private int x;
    private int y;
    private int height;

    public LayerPoint(LayerPoint point) {
        this.x = point.getX();
        this.y = point.getY();
        this.height = point.getHeight();
    }

    public LayerPoint() {

    }

    @Override
    public String toString() {
        return "LayerPoint(" + x +
                ", " + y +
                ", " + height +
                ')';
    }
}
