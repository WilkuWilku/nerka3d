package pl.ee.nerkabackend.processing.methods.correspondingpoints.divergence;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.springframework.stereotype.Component;
import pl.ee.nerkabackend.processing.model.LayerPoint;
import pl.ee.nerkabackend.processing.model.SideCorrespondingPoints;

@Component
public class DistanceSideCorrespondingPointsDivergence implements SideCorrespondingPointsDivergence{

    @Override
    public double getPointsDivergence(SideCorrespondingPoints correspondingPoints) {
        LayerPoint topPoint = correspondingPoints.getTopPoint();
        LayerPoint bottomPoint = correspondingPoints.getBottomPoint();
        Vector3D point1 = new Vector3D(topPoint.getX(), topPoint.getY(), topPoint.getHeight());
        Vector3D point2 = new Vector3D(bottomPoint.getX(), bottomPoint.getY(), bottomPoint.getHeight());
        return Vector3D.distance(point1, point2);
    }
}
