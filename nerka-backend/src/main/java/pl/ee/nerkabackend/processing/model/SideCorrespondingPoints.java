package pl.ee.nerkabackend.processing.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SideCorrespondingPoints extends CorrespondingPoints {
    private LayerSide layerSide;

    public SideCorrespondingPoints(LayerPoint topPoint, LayerPoint bottomPoint, LayerSide layerSide) {
        super(topPoint, bottomPoint);
        this.layerSide = layerSide;
    }
}
