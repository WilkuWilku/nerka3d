package pl.ee.nerkabackend.processing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.ee.nerkabackend.constants.Constants;
import pl.ee.nerkabackend.processing.model.LayerHeader;
import pl.ee.nerkabackend.processing.model.LayerTranslation;
import pl.ee.nerkabackend.processing.model.RawLayer;
import pl.ee.nerkabackend.exception.DataLoadingException;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DataLoader {

    public RawLayer loadKidneyDataFromLocalFile(String filename) throws IOException, DataLoadingException {
        InputStream inputStream = openLocalKidneyDataFile(filename);
        return parseKidneyDataFromFile(inputStream, filename);
    }

    public RawLayer loadKidneyDataFromUploadedFile(MultipartFile multipartFile) throws IOException, DataLoadingException {
        InputStream inputStream = multipartFile.getInputStream();
        return parseKidneyDataFromFile(inputStream, multipartFile.getOriginalFilename());
    }

    private InputStream openLocalKidneyDataFile(String filename) throws FileNotFoundException {
        return Optional.ofNullable(getClass().getResourceAsStream(filename))
                .orElseThrow(() -> new FileNotFoundException(filename));
    }

    private RawLayer parseKidneyDataFromFile(InputStream inputStream, String filename) throws IOException, DataLoadingException {
        log.info("parseKidneyDataFromFile() start - filename: {}", filename);

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            RawLayer kidneyLayer = new RawLayer();

            String line = Optional.ofNullable(reader.readLine())
                .orElseThrow(() -> new DataLoadingException("No data found in file: "+filename));
            if(line.startsWith(Constants.CTL_TRANSLATION_PREFIX)) {
                log.info("parseKidneyDataFromFile() processing translation: {}", line);
                Double[] translationCoords = Arrays.stream(line.split(" "))
                        .dropWhile(element -> element.equals(Constants.CTL_TRANSLATION_PREFIX))
                        .map(Double::valueOf)
                        .toArray(Double[]::new);
                validateLayerTranslationData(translationCoords, line);

                LayerTranslation translation = new LayerTranslation(translationCoords[0], translationCoords[1], translationCoords[2]);
                log.info("parseKidneyDataFromFile() loaded translation: {}", translation);
                kidneyLayer.setTranslation(translation);
                line = reader.readLine();
            }

            LayerHeader layerHeader = parseHeader(line);
            if(layerHeader == null) {
                throw new DataLoadingException("No header found in file: "+filename);
            }
            kidneyLayer.setName(layerHeader.getName()+"/"+layerHeader.getNumber()+"/"+layerHeader.getIndex());

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

            log.info("parseKidneyDataFromFile() end");
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
        if(headerInput == null) {
            return null;
        }

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

    private void validateLayerTranslationData(Double[] translationData, String sourceLine) throws DataLoadingException {
        if(translationData.length != 3) {
            throw new DataLoadingException("Invalid translation data - expected 3 elements, received: "+
                    translationData.length+ ". Source line: "+sourceLine);
        }
        if(translationData[0] == null) {
            throw new DataLoadingException("Layer translation X is null. Source line: "+sourceLine);
        }
        if(translationData[1] == null) {
            throw new DataLoadingException("Layer translation Y is null. Source line: "+sourceLine);
        }
        if(translationData[2] == null) {
            throw new DataLoadingException("Layer translation Z is null. Source line: "+sourceLine);
        }
    }
}
