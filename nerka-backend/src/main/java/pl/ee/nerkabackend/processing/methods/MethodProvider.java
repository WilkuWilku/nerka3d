package pl.ee.nerkabackend.processing.methods;

import pl.ee.nerkabackend.processing.methods.pointsdetermination.EvenlyDistributed;
import pl.ee.nerkabackend.processing.methods.pointsdetermination.EvenlyDistributedEquinumerous;
import pl.ee.nerkabackend.processing.methods.pointsdetermination.PointsDeterminationMethod;
import pl.ee.nerkabackend.processing.methods.visualisation.triangulation.TriangulationService;
import pl.ee.nerkabackend.processing.methods.visualisation.VisualisationMethod;

public class MethodProvider {

    public static PointsDeterminationMethod getPointsDeterminationMethod(
            MethodTypes.PointsDeterminationMethodType type) {
        if (type.equals(MethodTypes.PointsDeterminationMethodType.EVENLY_DISTRIBUTED)) {
            return new EvenlyDistributed();
        } else if (type.equals(MethodTypes.PointsDeterminationMethodType.EVENLY_DISTRIBUTED_EQUINUMEROUS)) {
            return new EvenlyDistributedEquinumerous();
        }
        return null;
    }

    public static VisualisationMethod getVisualisationMethod(MethodTypes.VisualisationMethodType type) {
        if (type.equals(MethodTypes.VisualisationMethodType.TRIANGULATION)) {
            return new TriangulationService();
        }
        return null;
    }
}
