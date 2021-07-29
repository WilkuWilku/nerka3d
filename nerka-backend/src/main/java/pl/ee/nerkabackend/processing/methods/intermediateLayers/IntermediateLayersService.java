package pl.ee.nerkabackend.processing.methods.intermediateLayers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import pl.ee.nerkabackend.processing.methods.correspondingpoints.selector.CorrespondingPointsSelector;
import pl.ee.nerkabackend.processing.model.CorrespondingPoints;
import pl.ee.nerkabackend.processing.model.Layer;
import pl.ee.nerkabackend.processing.model.LayerPoint;
import pl.ee.nerkabackend.processing.model.SideCorrespondingPoints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class IntermediateLayersService {

    @Autowired
    @Qualifier("quarterCorrespondingPointsSelector")
    private CorrespondingPointsSelector correspondingPointsSelector;

    public List<Layer> getLayersWithIntermediateLayers(List<Layer> layers, int layerMultiplier, InterpolationType type) {
        setCorrespondingPointsAsFirstInLayers(layers.get(0), layers.get(1));
        for (int i = 1; i < layers.size() - 1; i++) {
            adjustFirstPointInLayerToFirstPointFromLayerAbove(layers.get(i).getPoints().get(0), layers.get(i+1));
        }

        ArrayList<ArrayList<LayerPoint>> verticalLines = new ArrayList<>();
        for (int j = 0; j < layers.get(0).getPoints().size(); j++) {
            verticalLines.add(new ArrayList<>());
        }

        for (int i = 0; i < layers.size(); i++) {
            for (int j = 0; j < layers.get(0).getPoints().size(); j++) {
                LayerPoint point = layers.get(i).getPoints().get(j);
                verticalLines.get(j).add(point);
            }
        }

        ArrayList<ArrayList<LayerPoint>> verticalLinesWithIntermediateLayers = new ArrayList<>();
        verticalLines.forEach(verticalLine -> {
            double[] x = verticalLine.stream().mapToDouble(LayerPoint::getX).toArray();
            double[] y = verticalLine.stream().mapToDouble(LayerPoint::getHeight).toArray();
            double[] z = verticalLine.stream().mapToDouble(LayerPoint::getY).toArray();
            verticalLinesWithIntermediateLayers.add(interpolateWithMethod(x, y, z, layerMultiplier, type));
        });

        ArrayList<Layer> layersWithIntermediateLayers = new ArrayList<>();
        for (int i = 0; i < verticalLinesWithIntermediateLayers.get(0).size(); i++) {
            ArrayList<LayerPoint> layerPoints = new ArrayList<>();
            for (int j = 0; j < verticalLinesWithIntermediateLayers.size(); j++) {
                layerPoints.add(verticalLinesWithIntermediateLayers.get(j).get(i));
            }
            layersWithIntermediateLayers.add(new Layer(layerPoints, String.format("layer%s", i)));
        }

        return layersWithIntermediateLayers;
    }

    private void setCorrespondingPointsAsFirstInLayers(Layer topLayer, Layer bottomLayer) {
        CorrespondingPoints bestCorrespondingPoints = correspondingPointsSelector.getTwoCorrespondingPoints(topLayer, bottomLayer);
        int topPointIndex = topLayer.getPoints().indexOf(bestCorrespondingPoints.getTopPoint());
        int bottomPointIndex = bottomLayer.getPoints().indexOf(bestCorrespondingPoints.getBottomPoint());
        Collections.rotate(topLayer.getPoints(), -topPointIndex);
        Collections.rotate(bottomLayer.getPoints(), -bottomPointIndex);
    }

    private void adjustFirstPointInLayerToFirstPointFromLayerAbove(LayerPoint topPoint, Layer bottomLayer) {
        SideCorrespondingPoints bestCorrespondingPoints = correspondingPointsSelector.getBestCorrespondingPointsFromTopPoint(topPoint, bottomLayer.getPoints(), null);
        int bottomPointIndex = bottomLayer.getPoints().indexOf(bestCorrespondingPoints.getBottomPoint());
        Collections.rotate(bottomLayer.getPoints(), -bottomPointIndex);
    }

    public ArrayList<LayerPoint> interpolateWithMethod(double[] x, double[] y, double[] z, int layerMultiplier, InterpolationType type) {
        switch (type) {
            case Linear:
                return LinearInterpolation.interpolate(x, y, z, layerMultiplier);
            case CubicSpline:
                return CubicSplineInterpolation.interpolate(x, y, z, layerMultiplier);
            default:
                return null;
        }
    }

    public enum InterpolationType {
        CubicSpline, Linear
    }
}
