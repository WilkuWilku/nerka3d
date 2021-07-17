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
import org.springframework.stereotype.Service;
import pl.ee.nerkabackend.report.model.Measurement;
import pl.ee.nerkabackend.report.model.Report;

import java.io.*;
import java.time.LocalDateTime;
import java.util.List;

import static pl.ee.nerkabackend.constants.Constants.RESOURCES_DIR_PATH;

@Service
@Slf4j
public class ReportingService {

    private final static int CURRENT_RATIO_ROW_NUMBER = 3;
    private final static int TARGET_RATIO_ROW_NUMBER = 4;
    private final static int STEP_ROW_NUMBER = 5;

    public void exportReportToXlsx(Report report, String filename) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        FileOutputStream fileOut = null;
        try {
            XSSFSheet sheet = workbook.createSheet();
            Row row = sheet.createRow(0);
            Cell cell = row.createCell(0);
            cell.setCellValue("REPORT");

            cell = row.createCell(1);
            cell.setCellValue(report.getIdentifier());

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

            Row currentRatioRow = sheet.createRow(CURRENT_RATIO_ROW_NUMBER);
            Row targetRatioRow = sheet.createRow(TARGET_RATIO_ROW_NUMBER);
            Row stepRow = sheet.createRow(STEP_ROW_NUMBER);

            appendReportData(report, currentRatioRow, targetRatioRow, stepRow);
            drawChart(report, sheet);

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

    private void appendReportData(Report report, Row currentRatioRow, Row targetRatioRow, Row stepRow) {
        List<Measurement> measurements = report.getMeasurements();

        Cell cell = null;
        for(int i=0; i<measurements.size(); i++) {
            cell = currentRatioRow.createCell(i);
            cell.setCellValue(measurements.get(i).getCurrentRatio());

            cell = targetRatioRow.createCell(i);
            cell.setCellValue(report.getTargetRatio());

            cell = stepRow.createCell(i);
            cell.setCellValue(i);
        }
    }

    private void drawChart(Report report, XSSFSheet sheet) {
        XSSFDrawing drawing = sheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 0, 6, 20, 30);
        XSSFChart chart = drawing.createChart(anchor);
        chart.setTitleText("Triangulation efficiency");
        chart.setTitleOverlay(false);

        XDDFNumericalDataSource<Double> currentRatioSource = XDDFDataSourcesFactory.fromNumericCellRange(sheet,
                new CellRangeAddress(CURRENT_RATIO_ROW_NUMBER, CURRENT_RATIO_ROW_NUMBER, 0, report.getMeasurements().size()));

        XDDFNumericalDataSource<Double> targetRatioSource = XDDFDataSourcesFactory.fromNumericCellRange(sheet,
                new CellRangeAddress(TARGET_RATIO_ROW_NUMBER, TARGET_RATIO_ROW_NUMBER, 0, report.getMeasurements().size()));

        XDDFNumericalDataSource<Double> stepSource = XDDFDataSourcesFactory.fromNumericCellRange(sheet,
                new CellRangeAddress(STEP_ROW_NUMBER, STEP_ROW_NUMBER, 0, report.getMeasurements().size()));

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
}
