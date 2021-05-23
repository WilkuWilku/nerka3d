package pl.ee.nerkabackend.processing.methods.correspondingpoints.divergence;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.ee.nerkabackend.processing.model.SideCorrespondingPoints;

@Component
@Slf4j
public class AxisSideCorrespondingPointsDivergence implements SideCorrespondingPointsDivergence {

    public double getPointsDivergence(SideCorrespondingPoints correspondingPoints) {
        log.info("getPointsDivergence() start for: {}", correspondingPoints);
        switch (correspondingPoints.getLayerSide()) {
            case TOP:
            case BOTTOM: {
                double divergence = Math.abs(correspondingPoints.getTopPoint().getY()-correspondingPoints.getBottomPoint().getY());
                log.info("getPointsDivergence() calculated value: {}", divergence);
                return divergence;
            }
            case RIGHT:
            case LEFT: {
                double divergence = Math.abs(correspondingPoints.getTopPoint().getX()-correspondingPoints.getBottomPoint().getX());
                log.info("getPointsDivergence() calculated value: {}", divergence);
                return divergence;
            }
            default: throw new IllegalArgumentException("Unexpected value: "+correspondingPoints.getLayerSide().name());
        }
    }
}
