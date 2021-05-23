package pl.ee.nerkabackend.processing.methods.borderpoint.random;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.ee.nerkabackend.processing.methods.borderpoint.BorderPointSelector;
import pl.ee.nerkabackend.processing.model.LayerPoint;
import pl.ee.nerkabackend.processing.model.RawLayer;

import java.util.Map;
import java.util.Random;

@Slf4j
@Component
public class RandomBorderPointSelector implements BorderPointSelector {

    @Value("${kidney.distance.between.layers}")
    private Integer distanceBetweenLayers;

    @Override
    public LayerPoint getBorderPoint(RawLayer rawLayer, Map<String, Object> params) {
        log.info("RandomBorderPointSelector::getBorderPoint start - layerName: {}, number: {}, index: {}",
                rawLayer.getName(), rawLayer.getLayerNumber(), rawLayer.getIndex());
        Random random = new Random();
        int x, y;
        int attemptCounter = 0;
        do {
            x = random.nextInt(4000);
            y = random.nextInt(6000);
            attemptCounter++;
        } while (rawLayer.getData()[x][y] != 1);
        log.info("RandomBorderPointSelector::getBorderPoint end - first point: x={}, y={} [attempt #{}]", x, y, attemptCounter);
        return new LayerPoint(x, y, rawLayer.getLayerNumber()*distanceBetweenLayers);
    }
}

