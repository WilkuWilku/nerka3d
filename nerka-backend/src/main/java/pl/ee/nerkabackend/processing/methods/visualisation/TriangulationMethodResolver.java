package pl.ee.nerkabackend.processing.methods.visualisation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import pl.ee.nerkabackend.processing.methods.MethodTypes;

@Component
public class TriangulationMethodResolver {

    @Autowired
    private ApplicationContext applicationContext;

    public TriangulationMethod getTriangulationMethod(MethodTypes.TriangulationMethodType triangulationMethodType) {
        Class<? extends TriangulationMethod> triangulationMethodClass;
        switch (triangulationMethodType) {
            case BY_ANGLE:
                triangulationMethodClass = TriangulationByAngle.class; break;
            case BY_LENGTH_SUM:
                triangulationMethodClass = TriangulationByLengthSum.class; break;
            case BY_POINTS_DISTANCE:
                triangulationMethodClass = TriangulationByPointsDistance.class; break;
            default:
                throw new IllegalArgumentException("Implementation of TriangulationMethod interface not found for triangulationMethodType: "+triangulationMethodType.name());
        }
        return applicationContext.getBean(triangulationMethodClass);
    }

}
