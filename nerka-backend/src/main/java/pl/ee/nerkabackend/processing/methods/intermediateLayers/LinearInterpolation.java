package pl.ee.nerkabackend.processing.methods.intermediateLayers;

import pl.ee.nerkabackend.processing.model.LayerPoint;

import java.util.ArrayList;

public class LinearInterpolation {
    public static ArrayList<LayerPoint> interpolate(double[] x, double[] y, double[] z, int layerMultiplier) {
        ArrayList<LayerPoint> interpolatedPointVector = new ArrayList<>();
        double yStep = (y[y.length - 1] - y[0]) / ((y.length - 1) * layerMultiplier + 1);
        int currentArraysIndex = 0;
        for (int i = 0; i < (y.length - 1) * layerMultiplier + 1; i++) {
            double currentY = y[0] + yStep * i;
            if (currentY > y[currentArraysIndex + 1]) {
                currentArraysIndex++;
            }
            LayerPoint newPoint = new LayerPoint(
                (int) interpolate(currentY, y[currentArraysIndex], y[currentArraysIndex + 1], x[currentArraysIndex], x[currentArraysIndex + 1]),
                (int) interpolate(currentY, y[currentArraysIndex], y[currentArraysIndex + 1], z[currentArraysIndex], z[currentArraysIndex + 1]),
                (int) currentY
            );
            interpolatedPointVector.add(newPoint);
        }
        return interpolatedPointVector;
    }

    private static double interpolate(double x, double x0, double x1, double y0, double y1) {
        return y0 + (y1 - y0)/(x1 - x0)*(x - x0);
    }
}
