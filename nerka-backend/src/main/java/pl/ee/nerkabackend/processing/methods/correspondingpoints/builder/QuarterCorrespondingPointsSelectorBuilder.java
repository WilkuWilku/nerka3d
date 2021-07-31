package pl.ee.nerkabackend.processing.methods.correspondingpoints.builder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import pl.ee.nerkabackend.processing.methods.correspondingpoints.selector.QuarterCorrespondingPointsSelector;

import java.util.Optional;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class QuarterCorrespondingPointsSelectorBuilder {

    @Autowired
    private ApplicationContext applicationContext;

    private Integer distanceBetweenCheckedPoints;
    private Double checkedPointsPercentOnLayer;


    public QuarterCorrespondingPointsSelectorBuilder withDistanceBetweenCheckedPoints(Integer distanceBetweenCheckedPoints) {
        this.distanceBetweenCheckedPoints = distanceBetweenCheckedPoints;
        return this;
    }

    public QuarterCorrespondingPointsSelectorBuilder withCheckedPointsPercentOnLayer(Double checkedPointsPercentOnLayer) {
        this.checkedPointsPercentOnLayer = checkedPointsPercentOnLayer;
        return this;
    }

    public QuarterCorrespondingPointsSelector build() {
        QuarterCorrespondingPointsSelector selector = applicationContext.getBean(QuarterCorrespondingPointsSelector.class);
        Optional.ofNullable(distanceBetweenCheckedPoints).ifPresent(selector::setDistanceBetweenCheckedPoints);
        Optional.ofNullable(checkedPointsPercentOnLayer).ifPresent(selector::setCheckedPointsPercentOnLayer);
        return selector;
    }
}
