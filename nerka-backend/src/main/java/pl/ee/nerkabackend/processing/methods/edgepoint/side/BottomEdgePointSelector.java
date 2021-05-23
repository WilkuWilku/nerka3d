package pl.ee.nerkabackend.processing.methods.edgepoint.side;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.ee.nerkabackend.processing.methods.edgepoint.EdgePointSelector;
import pl.ee.nerkabackend.processing.model.Layer;
import pl.ee.nerkabackend.processing.model.LayerPoint;

import java.util.Comparator;

@Component
@Slf4j
public class BottomEdgePointSelector implements EdgePointSelector {

    @Override
    public LayerPoint getEdgePoint(Layer layer) {
        log.info("getEdgePoint() start for layer: {}", layer.getName());
        LayerPoint bottomEdgePoint = layer.getPoints().stream()
                .max(Comparator.comparing(LayerPoint::getX))
                .orElse(null);
        log.info("getEdgePoint() bottom edge point found: {}", bottomEdgePoint);
        return bottomEdgePoint;
    }
}
