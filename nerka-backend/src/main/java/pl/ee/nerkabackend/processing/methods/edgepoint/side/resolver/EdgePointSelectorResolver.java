package pl.ee.nerkabackend.processing.methods.edgepoint.side.resolver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import pl.ee.nerkabackend.processing.methods.edgepoint.EdgePointSelector;
import pl.ee.nerkabackend.processing.methods.edgepoint.side.BottomEdgePointSelector;
import pl.ee.nerkabackend.processing.methods.edgepoint.side.LeftEdgePointSelector;
import pl.ee.nerkabackend.processing.methods.edgepoint.side.RightEdgePointSelector;
import pl.ee.nerkabackend.processing.methods.edgepoint.side.TopEdgePointSelector;
import pl.ee.nerkabackend.processing.model.LayerSide;

@Component
public class EdgePointSelectorResolver {

    @Autowired
    private ApplicationContext applicationContext;

    public EdgePointSelector getEdgePointSelectorForSide(LayerSide layerSide) {
        Class<? extends EdgePointSelector> selectorClass;
        switch (layerSide) {
            case TOP: selectorClass = TopEdgePointSelector.class; break;
            case BOTTOM: selectorClass = BottomEdgePointSelector.class; break;
            case LEFT: selectorClass = LeftEdgePointSelector.class; break;
            case RIGHT: selectorClass = RightEdgePointSelector.class; break;
            default: throw new IllegalArgumentException("Implementation of EdgePointSelector interface not found for layerSide: "+layerSide.name());
        }
        return applicationContext.getBean(selectorClass);
    }
}
