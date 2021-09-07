package pl.ee.nerkabackend.processing.methods.intermediateLayers;

import org.apache.commons.math3.util.Pair;
import pl.ee.nerkabackend.processing.model.LayerPoint;

import java.util.ArrayList;
import java.util.Arrays;

public class LagrangeInterpolation {

    public static ArrayList<LayerPoint> interpolate(double[] x, double[] y, double[] z, int layerMultiplier, int order) {
        ArrayList<LayerPoint> interpolatedPointVector = new ArrayList<>();
        double yStep = (y[y.length - 1] - y[0]) / ((y.length - 1) * layerMultiplier);
        for (int i = 0; i < (y.length - 1) * layerMultiplier + 1; i++) {
            double currentY = y[0] + yStep * i;
            Pair<Integer,Integer> intervals = getIntervalIndices(y, currentY, order);
            System.out.println(intervals.getFirst() + " " + intervals.getSecond());
            double[] subY = Arrays.copyOfRange(y, intervals.getFirst(), intervals.getSecond());
            double[] subX = Arrays.copyOfRange(x, intervals.getFirst(), intervals.getSecond());
            double[] subZ = Arrays.copyOfRange(z, intervals.getFirst(), intervals.getSecond());
            LayerPoint newPoint = new LayerPoint(
                (int) getInterpolationValue(subY, subX, currentY),
                (int) getInterpolationValue(subY, subZ, currentY),
                (int) currentY
            );
            interpolatedPointVector.add(newPoint);
        }

        return interpolatedPointVector;
    }

    private static double getInterpolationValue(double[] x, double[] y, double xi) {
        double result = 0;
        for (int i = 0; i < x.length; i++) {
            double term = y[i];
            for (int j = 0; j < x.length; j++) {
                if (j != i) {
                    term = term*(xi - x[j]) / (x[i] - x[j]);
                }
            }
            result += term;
        }
        return result;
    }

    private static Pair<Integer, Integer> getIntervalIndices(double[] y, double currentY, int order) {
        if (order >= y.length) {
            System.out.println("Order bigger than vector size");
            return null;
        }
        for (int indiceFrom = 0; indiceFrom <= y.length; indiceFrom+= order) {
            if (indiceFrom + order >= y.length) {
                return new Pair<>(y.length - order - 1, y.length);
            } else if (currentY <= y[indiceFrom+order] && currentY >= y[indiceFrom]) {
                return new Pair<>(indiceFrom, indiceFrom + order + 1);
            }
            //System.out.println(currentY + " " + y[y.length - 1] + " " + y[y.length - order - 1] + " " + indiceFrom + " " + y.length);
        }
        System.out.println("This should have been never reached. Interval not found");
        return null;
    }
}
