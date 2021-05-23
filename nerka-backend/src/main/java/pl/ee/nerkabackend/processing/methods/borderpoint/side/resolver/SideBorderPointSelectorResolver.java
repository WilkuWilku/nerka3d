package pl.ee.nerkabackend.processing.methods.borderpoint.side.resolver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import pl.ee.nerkabackend.processing.methods.borderpoint.BorderPointSelector;
import pl.ee.nerkabackend.processing.methods.borderpoint.side.BottomBorderPointSelector;
import pl.ee.nerkabackend.processing.methods.borderpoint.side.LeftBorderPointSelector;
import pl.ee.nerkabackend.processing.methods.borderpoint.side.RightBorderPointSelector;
import pl.ee.nerkabackend.processing.methods.borderpoint.side.TopBorderPointSelector;
import pl.ee.nerkabackend.processing.model.LayerSide;

@Component
public class SideBorderPointSelectorResolver {

    @Autowired
    private ApplicationContext applicationContext;

    public BorderPointSelector getBorderPointSelectorForSide(LayerSide layerSide) {
        Class<? extends BorderPointSelector> selectorClass;
        switch (layerSide) {
            case TOP: selectorClass = TopBorderPointSelector.class; break;
            case BOTTOM: selectorClass = BottomBorderPointSelector.class; break;
            case LEFT: selectorClass = LeftBorderPointSelector.class; break;
            case RIGHT: selectorClass = RightBorderPointSelector.class; break;
            default: throw new IllegalArgumentException("Implementation of BorderPointSelector interface not found for: "+layerSide.name());
        }
        return applicationContext.getBean(selectorClass);
    }
}
