package pl.ee.nerkabackend.processing.methods.visualisation.triangulation;


import lombok.Setter;
import pl.ee.nerkabackend.processing.model.LayerPoint;

public abstract class AbstractTriangulationMethod {
    @Setter
    protected Double indexesRatioDiffCoefficient;

    public abstract LayerPoint getNextPoint(LayerPoint topPoint, LayerPoint bottomPoint,
                            LayerPoint nextTopPoint, LayerPoint nextBottomPoint,
                            double indexesRatioDiff);

    protected void setDefaultIndexesRatioDiffCoefficient(Double defaultValue) {
        this.indexesRatioDiffCoefficient = defaultValue;
    }
}
