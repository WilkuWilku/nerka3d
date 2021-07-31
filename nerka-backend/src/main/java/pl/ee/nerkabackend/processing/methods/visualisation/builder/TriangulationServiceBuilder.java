package pl.ee.nerkabackend.processing.methods.visualisation.builder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import pl.ee.nerkabackend.processing.methods.MethodTypes;
import pl.ee.nerkabackend.processing.methods.visualisation.TriangulationService;

import java.util.Optional;

@Component
@RequestScope
@Slf4j
public class TriangulationServiceBuilder {

    @Autowired
    private ApplicationContext applicationContext;

    private MethodTypes.TriangulationMethodType triangulationMethodType;


    public TriangulationServiceBuilder withTriangulationMethod(MethodTypes.TriangulationMethodType triangulationMethodType) {
        this.triangulationMethodType = triangulationMethodType;
        return this;
    }

    public TriangulationService build() {
        log.info("TriangulationServiceBuilder: objectRef: {}, methodType: {}", this, triangulationMethodType);
        TriangulationService service = applicationContext.getBean(TriangulationService.class);
        Optional.ofNullable(triangulationMethodType).ifPresent(service::setTriangulationMethodType);
        return service;
    }
}
