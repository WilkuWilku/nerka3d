package pl.ee.nerkabackend.processing.methods;

import lombok.Getter;
import lombok.Setter;

public class MethodTypes {
    public enum PointsDeterminationMethodType {
        EVENLY_DISTRIBUTED,
        EVENLY_DISTRIBUTED_EQUINUMEROUS,
        PLACEHOLDER_METHOD
    }

    public enum VisualisationMethodType {
        TRIANGULATION,
        TRIANGULATION_EQUINUMEROUS,
        PLACEHOLDER_METHOD
    }

    public enum CorrespondingPointsSelectionMethodType {
        SIDE,
        QUARTER
    }

    public enum TriangulationMethodType {
        BY_ANGLE,
        BY_LENGTH_SUM,
        BY_POINTS_DISTANCE
    }

    public enum KidneyVisualisationMethodType {
        TRIANGULATION(VisualisationMethodType.TRIANGULATION, PointsDeterminationMethodType.EVENLY_DISTRIBUTED),
        TRIANGULATION_EQUINUMEROUS(VisualisationMethodType.TRIANGULATION, PointsDeterminationMethodType.EVENLY_DISTRIBUTED_EQUINUMEROUS);

        @Getter
        @Setter
        private VisualisationMethodType visualisationMethodType;
        @Getter
        @Setter
        private PointsDeterminationMethodType pointsDeterminationMethod;

        KidneyVisualisationMethodType(VisualisationMethodType visualisationMethodType,
                                      PointsDeterminationMethodType pointsDeterminationMethod) {
            this.visualisationMethodType = visualisationMethodType;
            this.pointsDeterminationMethod = pointsDeterminationMethod;
        }
    }
}
