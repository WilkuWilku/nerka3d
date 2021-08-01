package pl.ee.nerkabackend.processing.methods.visualisation.triangulation.builder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import pl.ee.nerkabackend.processing.methods.MethodTypes;
import pl.ee.nerkabackend.processing.methods.visualisation.triangulation.TriangulationService;

import java.util.Optional;

@Component
@RequestScope
@Slf4j
public class TriangulationServiceBuilder {

    @Autowired
    private ApplicationContext applicationContext;

    private MethodTypes.TriangulationMethodType triangulationMethodType;
    private Double indexRatioDiffCoefficient;


    public TriangulationServiceBuilder withTriangulationMethod(MethodTypes.TriangulationMethodType triangulationMethodType) {
        this.triangulationMethodType = triangulationMethodType;
        return this;
    }

    public TriangulationServiceBuilder withIndexRatioDiffCoefficient(Double indexRatioDiffCoefficient) {
        this.indexRatioDiffCoefficient = indexRatioDiffCoefficient;
        return this;
    }

    public TriangulationService build() {
        log.info("TriangulationServiceBuilder: objectRef: {}, methodType: {}, indexRatioDiffCoefficient: {}",
                this, triangulationMethodType, indexRatioDiffCoefficient);
        TriangulationService service = applicationContext.getBean(TriangulationService.class);
        Optional.ofNullable(triangulationMethodType).ifPresent(service::setTriangulationMethodType);
        Optional.ofNullable(indexRatioDiffCoefficient).ifPresent(service::setIndexRatioDiffCoefficient);
        return service;
    }
}
