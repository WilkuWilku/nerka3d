package pl.ee.nerkabackend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.ee.nerkabackend.dto.LayerDTO;
import pl.ee.nerkabackend.dto.RawLayerDTO;
import pl.ee.nerkabackend.dto.LayerPointDTO;
import pl.ee.nerkabackend.exception.LayerProcessingException;
import pl.ee.nerkabackend.exception.NoDataException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Slf4j
public class LayerProcessingService {

    @Value("${kidney.distance.between.layers}")
    private Integer distanceBetweenLayers;

    @Autowired
    private CtlParserService ctlParserService;

    public LayerDTO loadLayer(String filename, double pointsShownPercent) throws IOException, NoDataException {
        log.info("loadLayer() start - filename: {}, pointsShownPercent: {}", filename, pointsShownPercent);
        RawLayerDTO layer = ctlParserService.loadKidneyDataFromFile(filename);
        List<LayerPointDTO> orderedPoints = getOrderedPoints(layer);
        List<LayerPointDTO> reducedPoints = reducePoints(orderedPoints, pointsShownPercent);
        log.info("loadLayer() end");
        return new LayerDTO(reducedPoints, layer.getName());
    }

    public List<LayerPointDTO> getReducedPointsFromFile(String filename, double pointsShownPercent) throws IOException, NoDataException {
        RawLayerDTO layer = ctlParserService.loadKidneyDataFromFile(filename);
        List<LayerPointDTO> points = getOrderedPoints(layer);
        List<LayerPointDTO> reducedPoints = reducePoints(points, pointsShownPercent);
        return reducedPoints;
    }

    public List<LayerPointDTO> getPoints(RawLayerDTO kidneyLayer) {
        log.info("getPoints() start - layerName: {}", kidneyLayer.getName());
        int[][] layerData = kidneyLayer.getData();
        List<LayerPointDTO> points = new ArrayList<>();

        for(int i = 0; i < layerData.length-1; i++) {
            for (int j = 0; j < layerData[i].length; j++) {
                if(layerData[i][j] == 1) {
                    points.add(new LayerPointDTO(i, j, kidneyLayer.getLayerNumber()*distanceBetweenLayers));
                }
            }
        }
        log.info("getPoints() end - border points count: {}", points.size());
        return points;
    }

    public List<LayerPointDTO> getOrderedPoints(RawLayerDTO kidneyLayer) {
        log.info("getOrderedPoints() start - layerName: {}, layerNumber: {}", kidneyLayer.getName(), kidneyLayer.getLayerNumber());
        int[][] layerData = kidneyLayer.getData();
        List<LayerPointDTO> orderedPoints = new ArrayList<>();

        long pointsCount = Arrays.stream(layerData)
                .flatMapToInt(Arrays::stream)
                .filter(value -> value == 1)
                .count();
        log.info("getOrderedPoints() points count before ordering: {}", pointsCount);

        LayerPointDTO currentPoint = selectFirstPoint(kidneyLayer);
        LayerPointDTO nextPoint;

        while((nextPoint = getNextPoint(currentPoint.getX(), currentPoint.getY(), kidneyLayer)) != null) {
            log.debug("getOrderedPoints - next point: x={}, y={} ", nextPoint.getX(), nextPoint.getY());
            layerData[currentPoint.getX()][currentPoint.getY()] = 0;
            orderedPoints.add(new LayerPointDTO(currentPoint));
            currentPoint = nextPoint;
        }
        orderedPoints.add(new LayerPointDTO(currentPoint));

        if(orderedPoints.size() != pointsCount) {
            throw new LayerProcessingException("Missing points during ordering - before: "+pointsCount+", after: "+orderedPoints.size());
        }

        log.info("getOrderedPoints() end - border ordered points count: {}", orderedPoints.size());
        return orderedPoints;
    }

    public List<LayerPointDTO> reducePoints(List<LayerPointDTO> points, double pointsShownPercent) {
        log.info("reducePoints() start - points count before reduction: {}, pointsShownPercent: {}", points.size(), pointsShownPercent);
        if(pointsShownPercent <= 0 || pointsShownPercent > 100) {
            log.warn("reducePoints() incorrect pointsShownPercent value: {} - returning original points list", pointsShownPercent);
            return points;
        }
        int pointsNumber = points.size();
        int indexesRange = Math.round((float) (pointsNumber*pointsShownPercent)/100);
        log.info("reducePoints() points number: {}, indexesRange: {}", pointsNumber, indexesRange);
        List<LayerPointDTO> reducedPoints = IntStream.range(0, indexesRange)
                .boxed()
                .peek(index -> log.debug("before: {}, after: {}", index, (int)(index*100/pointsShownPercent)))
                .map(index -> (int) (index*100/pointsShownPercent))
                .map(points::get)
                .collect(Collectors.toList());
        log.info("reducePoints() end - points count after reduction: {}", reducedPoints.size());
        return reducedPoints;
    }

    private LayerPointDTO selectFirstPoint(RawLayerDTO rawLayer) {
        Random random = new Random();
        int x, y;
        int attemptCounter = 0;
        do {
            x = random.nextInt(4000);
            y = random.nextInt(6000);
            attemptCounter++;
        } while (rawLayer.getData()[x][y] != 1);
        log.info("selectFirstPoint() - first point: x={}, y={} [attempt #{}]", x, y, attemptCounter);
        return new LayerPointDTO(x, y, rawLayer.getLayerNumber()*distanceBetweenLayers);
    }

    private LayerPointDTO getNextPoint(int x, int y, RawLayerDTO rawLayer) {
        if(rawLayer.getData()[x+1][y] == 1){
            return new LayerPointDTO(x+1, y, rawLayer.getLayerNumber()*distanceBetweenLayers);
        }
        if(rawLayer.getData()[x][y+1] == 1){
            return new LayerPointDTO(x, y+1, rawLayer.getLayerNumber()*distanceBetweenLayers);
        }
        if(rawLayer.getData()[x][y-1] == 1){
            return new LayerPointDTO(x, y-1, rawLayer.getLayerNumber()*distanceBetweenLayers);
        }
        if(rawLayer.getData()[x-1][y] == 1){
            return new LayerPointDTO(x-1, y, rawLayer.getLayerNumber()*distanceBetweenLayers);
        }
        else return null;
    }
}
