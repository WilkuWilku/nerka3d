package pl.ee.nerkabackend.processing.methods.correspondingpoints.divergence;

import pl.ee.nerkabackend.processing.model.SideCorrespondingPoints;

public interface SideCorrespondingPointsDivergence {
    double getPointsDivergence(SideCorrespondingPoints correspondingPoints);
}
