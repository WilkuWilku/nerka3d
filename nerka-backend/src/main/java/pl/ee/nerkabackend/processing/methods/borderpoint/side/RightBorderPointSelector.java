package pl.ee.nerkabackend.processing.methods.borderpoint.side;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.ee.nerkabackend.exception.LayerProcessingException;
import pl.ee.nerkabackend.processing.methods.borderpoint.BorderPointSelector;
import pl.ee.nerkabackend.processing.model.LayerPoint;
import pl.ee.nerkabackend.processing.model.RawLayer;

import java.util.Map;

@Slf4j
@Service
public class RightBorderPointSelector implements BorderPointSelector {

    @Value("${kidney.distance.between.layers}")
    private Integer distanceBetweenLayers;

    @Override
    public LayerPoint getBorderPoint(RawLayer rawLayer, Map<String, Object> params) {
        log.info("RightBorderPointSelector::getBorderPoint start - layerName: {}, number: {}, index: {}",
                rawLayer.getName(), rawLayer.getLayerNumber(), rawLayer.getIndex());
        int[][] layerData = rawLayer.getData();
        for(int y=0; y<layerData[0].length; y++) {
            for(int x=0; x<layerData.length; x++) {
                if(layerData[x][y] == 1) {
                    log.info("RightBorderPointSelector::getBorderPoint end - first point: x={}, y={}", x, y);
                    return new LayerPoint(x, y, rawLayer.getLayerNumber()*distanceBetweenLayers);
                }
            }
        }
        throw new LayerProcessingException("No points found in layer");
    }
}
