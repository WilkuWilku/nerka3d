package pl.ee.nerkabackend.report;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.PresetLineDash;
import org.apache.poi.xddf.usermodel.XDDFLineProperties;
import org.apache.poi.xddf.usermodel.XDDFPresetLineDash;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.ee.nerkabackend.report.model.ComparisonValueMeasurement;
import pl.ee.nerkabackend.report.model.RatioMeasurement;
import pl.ee.nerkabackend.report.model.Report;

import java.io.*;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static pl.ee.nerkabackend.constants.Constants.RESOURCES_DIR_PATH;

@Service
@Slf4j
public class ReportingService {

    @Value("${triangulation.reporting.first.records.skipped}")
    private Integer firstRecordsSkipped;

    private final static int CURRENT_RATIO_ROW_NUMBER = 3;
    private final static int TARGET_RATIO_ROW_NUMBER = 4;
    private final static int CORRECTION_VALUE_ROW_NUMBER = 31;
    private final static int SELECTED_VALUE_ROW_NUMBER = 32;
    private final static int STEP_ROW_NUMBER = 5;

    public void exportReportToXlsx(Report report, String filename) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        FileOutputStream fileOut = null;
        try {
            XSSFSheet sheet = workbook.createSheet();

            Row currentRatioRow = sheet.createRow(CURRENT_RATIO_ROW_NUMBER);
            Row targetRatioRow = sheet.createRow(TARGET_RATIO_ROW_NUMBER);
            Row stepRow = sheet.createRow(STEP_ROW_NUMBER);
            Row correctionValueRow = sheet.createRow(CORRECTION_VALUE_ROW_NUMBER);
            Row selectedValueRow = sheet.createRow(SELECTED_VALUE_ROW_NUMBER);

            appendBasicInfo(report, sheet, workbook);
            appendReportData(report, currentRatioRow, targetRatioRow, stepRow, correctionValueRow, selectedValueRow);
            drawIndexesRatioChart(report, sheet);
            drawCorrectionChart(report, sheet);
            appendStatisticsSection(report, sheet);

            String filePath = RESOURCES_DIR_PATH+filename;
            fileOut = new FileOutputStream(filePath);
            workbook.write(fileOut);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            workbook.close();
            if (fileOut != null) {
                fileOut.close();
            }
        }
    }

    private void appendBasicInfo(Report report, XSSFSheet sheet, XSSFWorkbook workbook) {
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        cell.setCellValue("REPORT");

        cell = row.createCell(1);
        cell.setCellValue(report.getIdentifier());

        cell = row.createCell(9);
        cell.setCellValue("ALGRTHM");

        cell = row.createCell(10);
        cell.setCellValue(report.getTriangulationMethodType().toString());


        row = sheet.createRow(1);
        cell = row.createCell(0);
        cell.setCellValue("TIMESTAMP");

        cell = row.createCell(2);
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("dd/mm/yyyy hh:mm:ss"));
        cell.setCellValue(LocalDateTime.now());
        cell.setCellStyle(cellStyle);
        sheet.addMergedRegion(CellRangeAddress.valueOf("A2:B2"));
        sheet.addMergedRegion(CellRangeAddress.valueOf("C2:D2"));

        cell = row.createCell(9);
        cell.setCellValue("IRD Coef");

        cell = row.createCell(10);
        cell.setCellValue(report.getIndexesRatioDiffCoefficient());
    }

    private void appendReportData(Report report, Row currentRatioRow, Row targetRatioRow, Row stepRow, Row correctionValueRow,
                                  Row selectedValueRow) {
        List<RatioMeasurement> ratioMeasurements = report.getRatioMeasurements();
        List<ComparisonValueMeasurement> comparisonValueMeasurements = report.getComparisonValueMeasurements();

        Cell cell = null;
        for(int i = 0; i< ratioMeasurements.size(); i++) {
            cell = currentRatioRow.createCell(i);
            cell.setCellValue(ratioMeasurements.get(i).getCurrentRatio());

            cell = targetRatioRow.createCell(i);
            cell.setCellValue(report.getTargetRatio());

            cell = stepRow.createCell(i);
            cell.setCellValue(i);

            cell = correctionValueRow.createCell(i);
            cell.setCellValue(comparisonValueMeasurements.get(i).getCorrection());

            cell = selectedValueRow.createCell(i);
            cell.setCellValue(comparisonValueMeasurements.get(i).getSelectedValue());
        }
    }

    private void appendStatisticsSection(Report report, XSSFSheet sheet) {
        List<Double> corrections = report.getComparisonValueMeasurements().stream()
                .map(ComparisonValueMeasurement::getCorrection)
                .collect(Collectors.toList());
        List<Double> selectedValues = report.getComparisonValueMeasurements().stream()
                .map(ComparisonValueMeasurement::getSelectedValue)
                .collect(Collectors.toList());
        List<Double> currentRatios = report.getRatioMeasurements().stream()
                .skip(firstRecordsSkipped) // pierwsze wyliczane wartości są nieistotne i zbyt rozrzucone
                .map(RatioMeasurement::getCurrentRatio)
                .collect(Collectors.toList());

        Row row = sheet.createRow(10);
        Cell cell = row.createCell(21);
        cell.setCellValue("CORRECTION");

        row = sheet.createRow(11);
        cell = row.createCell(21);
        cell.setCellValue("MAX");
        cell = row.createCell(22);
        cell.setCellValue(corrections.stream().max(Comparator.comparing(Double::doubleValue)).get());

        row = sheet.createRow(12);
        cell = row.createCell(21);
        cell.setCellValue("MIN");
        cell = row.createCell(22);
        cell.setCellValue(corrections.stream().min(Comparator.comparing(Double::doubleValue)).get());

        row = sheet.createRow(13);
        cell = row.createCell(21);
        cell.setCellValue("AVG");
        cell = row.createCell(22);
        cell.setCellValue(corrections.stream().reduce(0.0, Double::sum)/corrections.size());

        row = sheet.createRow(15);
        cell = row.createCell(21);
        cell.setCellValue("SELECTED VALUE");

        row = sheet.createRow(16);
        cell = row.createCell(21);
        cell.setCellValue("MAX");
        cell = row.createCell(22);
        cell.setCellValue(selectedValues.stream().max(Comparator.comparing(Double::doubleValue)).get());

        row = sheet.createRow(17);
        cell = row.createCell(21);
        cell.setCellValue("MIN");
        cell = row.createCell(22);
        cell.setCellValue(selectedValues.stream().filter(v -> v > 0).min(Comparator.comparing(Double::doubleValue)).get());

        row = sheet.createRow(18);
        cell = row.createCell(21);
        cell.setCellValue("AVG");
        cell = row.createCell(22);
        cell.setCellValue(selectedValues.stream().filter(v -> v > 0).reduce(0.0, Double::sum)/selectedValues.stream().filter(v -> v > 0).count());

        row = sheet.createRow(20);
        cell = row.createCell(21);
        cell.setCellValue("CURRENT RATIO");

        row = sheet.createRow(21);
        cell = row.createCell(21);
        cell.setCellValue("MAX");
        cell = row.createCell(22);
        cell.setCellValue(currentRatios.stream().max(Comparator.comparing(Double::doubleValue)).get());

        row = sheet.createRow(22);
        cell = row.createCell(21);
        cell.setCellValue("MIN");
        cell = row.createCell(22);
        cell.setCellValue(currentRatios.stream().filter(v -> v > 0).min(Comparator.comparing(Double::doubleValue)).get());

        row = sheet.createRow(23);
        cell = row.createCell(21);
        cell.setCellValue("AVG");
        cell = row.createCell(22);
        cell.setCellValue(currentRatios.stream().filter(v -> v > 0).reduce(0.0, Double::sum)/selectedValues.stream().filter(v -> v > 0).count());

        row = sheet.createRow(24);
        cell = row.createCell(21);
        cell.setCellValue("TARGET");
        cell = row.createCell(22);
        cell.setCellValue(report.getTargetRatio());

        row = sheet.createRow(25);
        cell = row.createCell(21);
        cell.setCellValue("SKIP");
        cell = row.createCell(22);
        cell.setCellValue(firstRecordsSkipped);

        row = sheet.createRow(40);
        cell = row.createCell(21);
        cell.setCellValue("ZEROS");
        cell = row.createCell(22);
        cell.setCellValue(selectedValues.stream().filter(v -> v == 0.0).count());
    }

    private void drawIndexesRatioChart(Report report, XSSFSheet sheet) {
        XSSFDrawing drawing = sheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 0, 6, 20, 30);
        XSSFChart chart = drawing.createChart(anchor);
        chart.setTitleText("Indexes ratio vs target ratio");
        chart.setTitleOverlay(false);

        XDDFNumericalDataSource<Double> currentRatioSource = XDDFDataSourcesFactory.fromNumericCellRange(sheet,
                new CellRangeAddress(CURRENT_RATIO_ROW_NUMBER, CURRENT_RATIO_ROW_NUMBER, 0, report.getRatioMeasurements().size()));

        XDDFNumericalDataSource<Double> targetRatioSource = XDDFDataSourcesFactory.fromNumericCellRange(sheet,
                new CellRangeAddress(TARGET_RATIO_ROW_NUMBER, TARGET_RATIO_ROW_NUMBER, 0, report.getRatioMeasurements().size()));

        XDDFNumericalDataSource<Double> stepSource = XDDFDataSourcesFactory.fromNumericCellRange(sheet,
                new CellRangeAddress(STEP_ROW_NUMBER, STEP_ROW_NUMBER, 0, report.getRatioMeasurements().size()));

        XDDFChartLegend legend = chart.getOrAddLegend();
        legend.setPosition(LegendPosition.TOP);

        XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
        bottomAxis.setTitle("Triangulation step");

        XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
        leftAxis.setTitle("Ratio");

        XDDFLineChartData data = (XDDFLineChartData) chart.createData(ChartTypes.LINE, bottomAxis, leftAxis);
        XDDFLineChartData.Series series1 = (XDDFLineChartData.Series) data.addSeries(stepSource, currentRatioSource);
        series1.setTitle("Current ratio", null);
        series1.setSmooth(false);
        series1.setMarkerStyle(MarkerStyle.NONE);

        XDDFLineChartData.Series series2 = (XDDFLineChartData.Series) data.addSeries(stepSource, targetRatioSource);
        series2.setTitle("Target Ratio", null);
        series2.setSmooth(false);
        series2.setMarkerStyle(MarkerStyle.NONE);
        XDDFLineProperties line = new XDDFLineProperties();
        line.setPresetDash(new XDDFPresetLineDash(PresetLineDash.DOT));
        series2.setLineProperties(line);

        chart.plot(data);
    }

    private void drawCorrectionChart(Report report, XSSFSheet sheet) {
        XSSFDrawing drawing = sheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 0, 33, 20, 60);
        XSSFChart chart = drawing.createChart(anchor);
        chart.setTitleText("Selected values and corrections");
        chart.setTitleOverlay(false);

        XDDFNumericalDataSource<Double> correctionValueSource = XDDFDataSourcesFactory.fromNumericCellRange(sheet,
                new CellRangeAddress(CORRECTION_VALUE_ROW_NUMBER, CORRECTION_VALUE_ROW_NUMBER, 0, report.getComparisonValueMeasurements().size()));

        XDDFNumericalDataSource<Double> selectedValueSource = XDDFDataSourcesFactory.fromNumericCellRange(sheet,
                new CellRangeAddress(SELECTED_VALUE_ROW_NUMBER, SELECTED_VALUE_ROW_NUMBER, 0, report.getComparisonValueMeasurements().size()));

        XDDFNumericalDataSource<Double> stepSource = XDDFDataSourcesFactory.fromNumericCellRange(sheet,
                new CellRangeAddress(STEP_ROW_NUMBER, STEP_ROW_NUMBER, 0, report.getRatioMeasurements().size()));

        XDDFChartLegend legend = chart.getOrAddLegend();
        legend.setPosition(LegendPosition.TOP);

        XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
        bottomAxis.setTitle("Triangulation step");

        XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
        leftAxis.setTitle("Value");

        XDDFLineChartData data = (XDDFLineChartData) chart.createData(ChartTypes.LINE, bottomAxis, leftAxis);
        XDDFLineChartData.Series series1 = (XDDFLineChartData.Series) data.addSeries(stepSource, correctionValueSource);
        series1.setTitle("Correction value", null);
        series1.setSmooth(false);
        series1.setMarkerStyle(MarkerStyle.NONE);

        XDDFLineChartData.Series series2 = (XDDFLineChartData.Series) data.addSeries(stepSource, selectedValueSource);
        series2.setTitle("Selected Value", null);
        series2.setSmooth(false);
        series2.setMarkerStyle(MarkerStyle.NONE);

        chart.plot(data);
    }
}
