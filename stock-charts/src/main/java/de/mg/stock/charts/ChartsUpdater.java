package de.mg.stock.charts;

import de.mg.stock.dto.ChartDataDTO;
import de.mg.stock.dto.StocksEnum;
import javafx.geometry.Side;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import static de.mg.stock.dto.StocksEnum.EMERGING;
import static de.mg.stock.dto.StocksEnum.SMALL200;
import static de.mg.stock.dto.StocksEnum.WORLD;

class ChartsUpdater {

    public static final ChartsUpdater INSTANCE = new ChartsUpdater();

    private VBox chartsContainer;
    private Label statusLeft, statusRight;

    private ShowTypeEnum showType = null;
    private boolean showPercentages = false;
    private LocalDate since = null;
    // same default as in fxml file:
    private int points = 100;

    private LocalDateTime lastChartData = null;

    private ChartsUpdater() {
    }

    void initialize(VBox chartsContainer, Label statusLeft, Label statusRight) {
        this.chartsContainer = chartsContainer;
        this.statusLeft = statusLeft;
        this.statusRight = statusRight;
    }

    void reinitializeCharts() {
        if (showType != null) {
            switch (showType) {
                case AGGREGATED:
                    drawCharts("aggregated");
                    break;
                case ALL:
                    drawCharts(WORLD.getName(), EMERGING.getName(), SMALL200.getName());
                    break;
                case WORLD:
                    drawCharts(WORLD.getName());
                    break;
                case EMERGING:
                    drawCharts(EMERGING.getName());
                    break;
                case SMALL200:
                    drawCharts(SMALL200.getName());
                    break;
                default:
                    throw new RuntimeException("wrong type");
            }
            redrawChart();
        }
    }

    private void drawCharts(String... titles) {
        ChartDiagramFactory.removeCharts(chartsContainer);
        for (String title : titles)
            ChartDiagramFactory.initializeChart(title, showPercentages, chartsContainer);
    }

    void redrawChart() {
        if (showType != null) {
            switch (showType) {
                case AGGREGATED: {
                    if (since != null) {
                        ChartDataDTO data = ChartsRestClient.INSTANCE.getAggregatedChartData(since, points);
                        updateChart(data, 0);
                    }
                    break;
                }
                case ALL:
                    updateChart(load(WORLD), 0);
                    updateChart(load(EMERGING), 1);
                    updateChart(load(SMALL200), 2);
                    break;
                case WORLD:
                    updateChart(load(WORLD), 0);
                    break;
                case EMERGING:
                    updateChart(load(EMERGING), 0);
                    break;
                case SMALL200:
                    updateChart(load(SMALL200), 0);
                    break;
                default:
                    throw new RuntimeException("wrong type");
            }
        }
    }

    void updateStatus(LocalDateTime lastChartUpdate) {
        long secAgo = lastChartUpdate.until(LocalDateTime.now(), ChronoUnit.SECONDS);
        String lastUpdateStr = (lastChartData != null) ? lastChartData.format(DateTimeFormatter.ofPattern("dd.MM. hh:mm")) : "n/a";
        statusLeft.setText(String.format("loaded %ds ago, last data from %s", secAgo, lastUpdateStr));
        //statusRight.setText("");
    }

    private ChartDataDTO load(StocksEnum stock) {
        ChartDataDTO result;
        if (since == null)
            result = ChartsRestClient.INSTANCE.getChartData(stock, points);
        else
            result = ChartsRestClient.INSTANCE.getChartDataSince(stock, since, showPercentages, points);
        return result;
    }


    private void updateChart(ChartDataDTO serverData, int diagramIndex) {

        LineChart<String, Number> chart = (LineChart<String, Number>) chartsContainer.getChildren().get(diagramIndex);

        XYChart.Series seriesMax = chart.getData().get(0);
        XYChart.Series seriesMin = chart.getData().get(1);
        XYChart.Series seriesAverage = chart.getData().get(2);
        seriesMax.getData().clear();
        seriesMin.getData().clear();
        seriesAverage.getData().clear();

        serverData.getItems().stream().forEach(data -> {

            updateLastChartData(data.getDateTime());

            String pattern = (data.isInstantPrice()) ? "dd. HH:mm" : "dd.MM.yy";
            String label = data.getDateTime().format(DateTimeFormatter.ofPattern(pattern));

            if (data.getMax() != null)
                seriesMax.getData().add(new XYChart.Data(label, data.getMax()));
            if (data.getMin() != null)
                seriesMin.getData().add(new XYChart.Data(label, data.getMin()));
            if (data.getAverage() != null)
                seriesAverage.getData().add(new XYChart.Data(label, data.getAverage()));
        });
    }

    private void updateLastChartData(LocalDateTime dataTime) {
        if (lastChartData == null || dataTime.isAfter(lastChartData))
            lastChartData = dataTime;
    }


    void setShowType(ShowTypeEnum showType) {
        this.showType = showType;
    }

    public ShowTypeEnum getShowType() {
        return showType;
    }

    void setShowPercentages(boolean showPercentages) {
        this.showPercentages = showPercentages;
    }

    void setSince(LocalDate value) {
        since = value;
    }

    public void setPoints(String pointsStr) {
        try {
            this.points = Integer.valueOf(pointsStr);
        } catch (NumberFormatException e) {
            // ignore
        }
    }
}
