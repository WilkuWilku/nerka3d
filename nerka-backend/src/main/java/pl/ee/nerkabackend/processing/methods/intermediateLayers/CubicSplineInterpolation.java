package pl.ee.nerkabackend.processing.methods.intermediateLayers;

import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import pl.ee.nerkabackend.processing.model.LayerPoint;

import java.util.ArrayList;

public class CubicSplineInterpolation {

    public static ArrayList<LayerPoint> interpolate(double[] x, double[] y, double[] z, int layerMultiplier) {
        SplineInterpolator splineInterpolator = new SplineInterpolator();
        PolynomialSplineFunction xInterpolationFunction = splineInterpolator.interpolate(y, x);
        PolynomialSplineFunction zInterpolationFunction = splineInterpolator.interpolate(y, z);

        ArrayList<LayerPoint> interpolatedPointVector = new ArrayList<>();
        double yStep = (y[y.length - 1] - y[0]) / ((y.length - 1) * layerMultiplier + 1);
        for (int i = 0; i < (y.length - 1) * layerMultiplier + 1; i++) {
            double currentY = y[0] + yStep * i;
            LayerPoint newPoint = new LayerPoint(
                (int) xInterpolationFunction.value(currentY),
                (int) zInterpolationFunction.value(currentY),
                (int) currentY
            );
            interpolatedPointVector.add(newPoint);
        }

        return interpolatedPointVector;
    }
}
