package pl.ee.nerkabackend.processing.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.ee.nerkabackend.processing.methods.MethodProvider;
import pl.ee.nerkabackend.processing.methods.MethodTypes;
import pl.ee.nerkabackend.processing.methods.borderpoint.BorderPointSelector;
import pl.ee.nerkabackend.processing.model.*;
import pl.ee.nerkabackend.exception.LayerProcessingException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class LayerProcessingService {

    @Value("${kidney.distance.between.layers}")
    private Integer distanceBetweenLayers;

    @Qualifier("bottomBorderPointSelector")
    @Autowired
    private BorderPointSelector borderPointSelector;

    public Layer processLayer(RawLayer rawLayer, MethodTypes.PointsDeterminationMethodType type, Object... params) {
        try {
            List<LayerPoint> orderedPoints = getOrderedPoints(rawLayer);
            List<LayerPoint> reducedPoints = MethodProvider
                    .getPointsDeterminationMethod(type)
                    .determinePointsOnLayer(orderedPoints, params);
            if(rawLayer.getTranslation() != null) {
                List<LayerPoint> translatedPoints = translatePoints(reducedPoints, rawLayer.getTranslation());
                return new Layer(translatedPoints, rawLayer.getName());
            }
            return new Layer(reducedPoints, rawLayer.getName());

        } catch (LayerProcessingException e) {
            log.error("[ERROR] An error occurred during layer processing - exception message: '{}'", e.getMessage());
            return null;
        }
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

        Stack<LayerPoint> ambiguousPoints = new Stack<>();
        LayerPoint firstPoint = borderPointSelector.getBorderPoint(kidneyLayer, null);
        LayerPoint currentPoint = firstPoint;
        LayerPoint nextPoint = getNextPoint(currentPoint.getX(), currentPoint.getY(), kidneyLayer);

        while(nextPoint != null) {
            log.debug("getOrderedPoints - next point: x={}, y={} ", nextPoint.getX(), nextPoint.getY());

            // pierwszy punkt zawsze ma więcej niż jednego sąsiada
            if(currentPoint != firstPoint && countNeighbours(currentPoint.getX(), currentPoint.getY(), kidneyLayer.getData()) > 1) {
                ambiguousPoints.push(currentPoint);
                log.info("getOrderedPoints() ambiguous point found: {}", currentPoint);
            }

            layerData[currentPoint.getX()][currentPoint.getY()] = 0;
            orderedPoints.add(new LayerPoint(currentPoint));
            currentPoint = nextPoint;
            nextPoint = getNextPoint(currentPoint.getX(), currentPoint.getY(), kidneyLayer);

            if(nextPoint == null && !areNeighbours(currentPoint, firstPoint)) {
                if(ambiguousPoints.size() > 0) {
                    nextPoint = ambiguousPoints.pop();
                    log.info("getOrderedPoints() returning to point: {}", nextPoint);
                } else {
                    log.warn("getOrderedPoints() no ambiguous point in stack to return to - current dead-end point: {}", currentPoint);
                }
            }
        }

        orderedPoints.add(new LayerPoint(currentPoint));

        if(orderedPoints.size()+ambiguousPoints.size() != pointsCount) {
            log.warn("getOrderedPoints() ambiguous points left in stack: {}", ambiguousPoints.size());
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

    private List<LayerPoint> translatePoints(List<LayerPoint> points, LayerTranslation layerTranslation) {
        log.info("translatePoints() start for translation data: {}", layerTranslation);
        List<LayerPoint> translatedPoints = points.stream()
                .map(point -> translatePoint(point, layerTranslation))
                .collect(Collectors.toList());
        log.info("translatePoints() end");
        return translatedPoints;
    }

    private LayerPoint translatePoint(LayerPoint layerPoint, LayerTranslation layerTranslation) {
        int translatedX = (int) (layerPoint.getX()+layerTranslation.getX());
        int translatedY = (int) (layerPoint.getY()+layerTranslation.getY());
        int translatedZ = (int) (layerPoint.getHeight()+layerTranslation.getZ());
        return new LayerPoint(translatedX, translatedY, translatedZ);
    }

    private int countNeighbours(int x, int y, int[][] layerData) {
        return Stream.of(layerData[x][y-1], layerData[x-1][y], layerData[x+1][y], layerData[x][y+1])
                .reduce(Integer::sum)
                .get();
    }

    public boolean areNeighbours(LayerPoint point1, LayerPoint point2) {
        return point1.getHeight() == point2.getHeight() &&
                ((point1.getX() == point2.getX()-1 && point1.getY() == point2.getY()) ||
                (point1.getX() == point2.getX()+1 && point1.getY() == point2.getY()) ||
                (point1.getX() == point2.getX() && point1.getY() == point2.getY()-1) ||
                (point1.getX() == point2.getX() && point1.getY() == point2.getY()+1));
    }
}
