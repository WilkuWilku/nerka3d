package pl.ee.nerkabackend.processing.methods.visualisation.triangulation.builder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import pl.ee.nerkabackend.processing.methods.MethodTypes;
import pl.ee.nerkabackend.processing.methods.visualisation.triangulation.AbstractTriangulationMethod;
import pl.ee.nerkabackend.processing.methods.visualisation.triangulation.resolver.TriangulationMethodResolver;

import java.util.Optional;

@Component
@RequestScope
@Slf4j
public class TriangulationMethodBuilder {

    @Autowired
    private TriangulationMethodResolver triangulationMethodResolver;

    private MethodTypes.TriangulationMethodType triangulationMethodType;
    private Double indexesRatioDiffCoefficient;

    public TriangulationMethodBuilder withTriangulationMethodType(MethodTypes.TriangulationMethodType triangulationMethodType) {
        this.triangulationMethodType = triangulationMethodType;
        return this;
    }

    public TriangulationMethodBuilder withIndexesRatioDiffCoefficient(Double indexesRatioDiffCoefficient) {
        this.indexesRatioDiffCoefficient = indexesRatioDiffCoefficient;
        return this;
    }

    public AbstractTriangulationMethod build() {
        log.info("TriangulationMethodBuilder: objectRef: {}, methodType: {}, indexRatioDiffCoefficient: {}",
                this, triangulationMethodType, indexesRatioDiffCoefficient);
        AbstractTriangulationMethod triangulationMethod;
        if(triangulationMethodType != null) {
            triangulationMethod = triangulationMethodResolver.getTriangulationMethod(triangulationMethodType);
        } else {
            return null;
        }

        Optional.ofNullable(indexesRatioDiffCoefficient).ifPresent(triangulationMethod::setIndexesRatioDiffCoefficient);
        return triangulationMethod;
    }

}
