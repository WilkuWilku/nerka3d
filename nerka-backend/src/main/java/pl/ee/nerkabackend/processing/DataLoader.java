package pl.ee.nerkabackend.processing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.ee.nerkabackend.processing.model.LayerHeader;
import pl.ee.nerkabackend.processing.model.RawLayer;
import pl.ee.nerkabackend.exception.NoDataException;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DataLoader {

    public RawLayer loadKidneyDataFromFile(String filename) throws IOException, NoDataException {
        log.info("loadKidneyDataFromFile() start - filename: {}", filename);
        InputStream inputStream = Optional.ofNullable(getClass().getResourceAsStream(filename))
                .orElseThrow(() -> new FileNotFoundException(filename));
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            RawLayer kidneyLayer = new RawLayer();

            String header = Optional.ofNullable(reader.readLine())
                .orElseThrow(() -> new NoDataException("No data found in file: "+filename));
            LayerHeader layerHeader = parseHeader(header);

            kidneyLayer.setName(layerHeader.getName());

            String line;
            List<List<Integer>> layerData = new ArrayList<>();
            while((line = reader.readLine()) != null) {
                List<Integer> row = parseRow(line);
                layerData.add(row);
            }
            int[][] rawLayerData = new int[4000][6000];
            layerData.stream()
                .map(row -> row.stream()
                    .mapToInt(Integer::intValue)
                    .toArray())
                .collect(Collectors.toList())
                .toArray(rawLayerData);

            int[][] kidneyBorderLayerData = getBorder(rawLayerData);

            kidneyLayer.setData(kidneyBorderLayerData);
            kidneyLayer.setLayerNumber(layerHeader.getNumber());

            log.info("loadKidneyDataFromFile() end");
            return kidneyLayer;
        }
    }

    private List<Integer> parseRow(String inputRowData) {
        int[] compressedRowData = Arrays.stream(inputRowData.split(";"))
            .mapToInt(Integer::parseInt)
            .toArray();
        List<Integer> row = new ArrayList<>();
        for (int i = 0; i < compressedRowData.length; i++) {
            Integer[] rowData = new Integer[compressedRowData[i]];
            Arrays.fill(rowData, i % 2);
            row.addAll(Arrays.asList(rowData));
        }
        return row;
    }

    private LayerHeader parseHeader(String headerInput) {
        headerInput = headerInput.split(".ctl")[0];

        String[] headerParts = headerInput.split("_");
        String name = headerParts[0];
        String objectType = headerParts[1];
        String layerOrderData = headerParts[2];

        String[] layerNumberAndIndex = layerOrderData.split("-");
        String number = layerNumberAndIndex[0];
        String index = layerNumberAndIndex[1];

        LayerHeader header = new LayerHeader();
        header.setName(name);
        header.setObjectType(objectType);
        header.setIndex(Integer.parseInt(index));
        header.setNumber(Integer.parseInt(number));
        return header;
    }

    private int[][] getBorder(int[][] layerData) {
        int[][] borderData = new int[4000][6000];
        for (int i = 0; i < layerData.length-1; i++) {
            for (int j = 0; j < layerData[i].length; j++) {
                if (layerData[i][j] == 1 && isBorder(layerData, i, j)) {
                    borderData[i][j] = 1;
                } else {
                    borderData[i][j] = 0;
                }
            }
        }
        return borderData;
    }

    private boolean isBorder(int[][] layerData, int x, int y) {
        return layerData[x+1][y] == 0
            || layerData[x+1][y+1] == 0
            || layerData[x+1][y-1] == 0
            || layerData[x-1][y] == 0
            || layerData[x-1][y+1] == 0
            || layerData[x-1][y-1] == 0
            || layerData[x][y+1] == 0
            || layerData[x][y-1] == 0;
    }
}
