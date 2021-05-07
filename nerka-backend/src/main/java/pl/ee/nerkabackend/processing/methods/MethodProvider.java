package pl.ee.nerkabackend.processing.methods;

import pl.ee.nerkabackend.processing.methods.pointsdetermination.EvenlyDistributed;
import pl.ee.nerkabackend.processing.methods.pointsdetermination.PointsDeterminationMethod;
import pl.ee.nerkabackend.processing.methods.visualisation.Triangularization;
import pl.ee.nerkabackend.processing.methods.visualisation.VisualisationMethod;

public class MethodProvider {

    public static PointsDeterminationMethod getPointsDeterminationMethod(
            MethodTypes.PointsDeterminationMethodType type) {
        if (type.equals(MethodTypes.PointsDeterminationMethodType.EVENLY_DISTRIBUTED)) {
            return new EvenlyDistributed();
        }
        return null;
    }

    public static VisualisationMethod getVisualisationMethod(MethodTypes.VisualisationMethodType type) {
        if (type.equals(MethodTypes.VisualisationMethodType.TRIANGULARIZATION)) {
            return new Triangularization();
        }
        return null;
    }
}
