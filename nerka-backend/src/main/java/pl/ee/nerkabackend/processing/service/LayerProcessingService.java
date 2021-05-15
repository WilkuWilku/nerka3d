package pl.ee.nerkabackend.processing.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.ee.nerkabackend.processing.methods.MethodProvider;
import pl.ee.nerkabackend.processing.methods.MethodTypes;
import pl.ee.nerkabackend.processing.methods.borderpoint.BorderPointSelector;
import pl.ee.nerkabackend.processing.model.Layer;
import pl.ee.nerkabackend.processing.model.RawLayer;
import pl.ee.nerkabackend.processing.model.LayerPoint;
import pl.ee.nerkabackend.exception.LayerProcessingException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class LayerProcessingService {

    @Value("${kidney.distance.between.layers}")
    private Integer distanceBetweenLayers;

    @Qualifier("bottomBorderPointSelector")
    @Autowired
    private BorderPointSelector borderPointSelector;

    public Layer processLayer(RawLayer rawLayer, MethodTypes.PointsDeterminationMethodType type, Object... params) {
        List<LayerPoint> orderedPoints = getOrderedPoints(rawLayer);
        List<LayerPoint> reducedPoints = MethodProvider
            .getPointsDeterminationMethod(type)
            .determinePointsOnLayer(orderedPoints, params);
        return new Layer(reducedPoints, rawLayer.getName());
    }

    public List<LayerPoint> getPoints(RawLayer kidneyLayer) {
        log.info("getPoints() start - layerName: {}", kidneyLayer.getName());
        int[][] layerData = kidneyLayer.getData();
        List<LayerPoint> points = new ArrayList<>();

        for(int i = 0; i < layerData.length-1; i++) {
            for (int j = 0; j < layerData[i].length; j++) {
                if(layerData[i][j] == 1) {
                    points.add(new LayerPoint(i, j, kidneyLayer.getLayerNumber()*distanceBetweenLayers));
                }
            }
        }
        log.info("getPoints() end - border points count: {}", points.size());
        return points;
    }

    public List<LayerPoint> getOrderedPoints(RawLayer kidneyLayer) {
        log.info("getOrderedPoints() start - layerName: {}, layerNumber: {}", kidneyLayer.getName(), kidneyLayer.getLayerNumber());
        int[][] layerData = kidneyLayer.getData();
        List<LayerPoint> orderedPoints = new ArrayList<>();

        long pointsCount = Arrays.stream(layerData)
                .flatMapToInt(Arrays::stream)
                .filter(value -> value == 1)
                .count();
        log.info("getOrderedPoints() points count before ordering: {}", pointsCount);

        LayerPoint currentPoint = borderPointSelector.getBorderPoint(kidneyLayer, null);
        LayerPoint nextPoint;

        while((nextPoint = getNextPoint(currentPoint.getX(), currentPoint.getY(), kidneyLayer)) != null) {
            log.debug("getOrderedPoints - next point: x={}, y={} ", nextPoint.getX(), nextPoint.getY());
            layerData[currentPoint.getX()][currentPoint.getY()] = 0;
            orderedPoints.add(new LayerPoint(currentPoint));
            currentPoint = nextPoint;
        }
        orderedPoints.add(new LayerPoint(currentPoint));

        if(orderedPoints.size() != pointsCount) {
            throw new LayerProcessingException("Missing points during ordering - before: "+pointsCount+", after: "+orderedPoints.size());
        }

        log.info("getOrderedPoints() end - border ordered points count: {}", orderedPoints.size());
        return orderedPoints;
    }

    private LayerPoint getNextPoint(int x, int y, RawLayer rawLayer) {
        if(rawLayer.getData()[x+1][y] == 1){
            return new LayerPoint(x+1, y, rawLayer.getLayerNumber()*distanceBetweenLayers);
        }
        if(rawLayer.getData()[x][y+1] == 1){
            return new LayerPoint(x, y+1, rawLayer.getLayerNumber()*distanceBetweenLayers);
        }
        if(rawLayer.getData()[x][y-1] == 1){
            return new LayerPoint(x, y-1, rawLayer.getLayerNumber()*distanceBetweenLayers);
        }
        if(rawLayer.getData()[x-1][y] == 1){
            return new LayerPoint(x-1, y, rawLayer.getLayerNumber()*distanceBetweenLayers);
        }
        else return null;
    }
}
