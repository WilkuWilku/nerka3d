package pl.ee.nerkabackend.processing.methods;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

public class MethodTypes {
    public enum PointsDeterminationMethodType {
        EVENLY_DISTRIBUTED,
        PLACEHOLDER_METHOD
    }

    public enum VisualisationMethodType {
        TRIANGULARIZATION,
        PLACEHOLDER_METHOD
    }

    public enum KidneyVisualisationMethodType {
        TRIANGULARIZATION(VisualisationMethodType.TRIANGULARIZATION, PointsDeterminationMethodType.EVENLY_DISTRIBUTED);

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
