/*
 * Copyright 2016 Michael Gnatz.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package de.mg.stock.charts;

import de.mg.stock.dto.AllInOneChartDto;
import de.mg.stock.dto.ChartDataDTO;
import de.mg.stock.dto.StocksEnum;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import javax.ws.rs.ProcessingException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static de.mg.stock.dto.StocksEnum.EMERGING;
import static de.mg.stock.dto.StocksEnum.SMALL200;
import static de.mg.stock.dto.StocksEnum.WORLD;
import static java.time.format.DateTimeFormatter.ofPattern;

class ChartsUpdater {

    public static final ChartsUpdater INSTANCE = new ChartsUpdater();

    private VBox chartsContainer;
    private Label statusLeft, statusRight;

    private ShowTypeEnum showType = null;
    private boolean showPercentages = false;
    private LocalDate since = null;
    // same default as in fxml file:
    private int points = 100;

    private final LastChartData lastChartData = new LastChartData();

    private ChartsUpdater() {
    }

    void initialize(VBox chartsContainer, Label statusLeft, Label statusRight) {
        this.chartsContainer = chartsContainer;
        this.statusLeft = statusLeft;
        this.statusRight = statusRight;
    }

    void reinitializeCharts() {
        if (showType == null) {
            return;
        }
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
            case ALL_IN_ONE:
                drawCharts("all in one");
                break;
            default:
                throw new RuntimeException("wrong type");
        }
        redrawChart();
    }

    private void drawCharts(String... titles) {
        ChartDiagramFactory.removeCharts(chartsContainer);
        for (String title : titles)
            ChartDiagramFactory.initializeChart(title, showPercentages, chartsContainer);
    }

    void redrawChart() {
        if (showType == null) {
            return;
        }
        switch (showType) {
            case AGGREGATED: {
                if (since != null)
                    updateChart(load(null), 0);
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
            case ALL_IN_ONE:
                updateAllInOne();
                break;
            default:
                throw new RuntimeException("wrong type");
        }
    }

    void updateStatus(LocalDateTime lastChartUpdate) {
        long secAgo = lastChartUpdate.until(LocalDateTime.now(), ChronoUnit.SECONDS);
        statusLeft.setText(String.format("loaded %ds ago, last data from %s", secAgo, lastChartData.get()));
    }

    private ChartDataDTO load(StocksEnum stock) {
        try {
            ChartDataDTO result;
            if (stock != null) {
                if (since == null)
                    result = ChartsRestClient.INSTANCE.getChartData(stock, points);
                else
                    result = ChartsRestClient.INSTANCE.getChartDataSince(stock, since, showPercentages, points);
            } else {
                result = ChartsRestClient.INSTANCE.getAggregatedChartData(since, points);
            }
            statusRight.setText("successfully loaded...");
            return result;

        } catch (ProcessingException e) {
            statusRight.setText("server too slow...");
            return null;
        }
    }


    private void updateChart(ChartDataDTO serverData, int diagramIndex) {

        if (serverData == null) return;

        LineChart<String, Number> chart = (LineChart<String, Number>) chartsContainer.getChildren().get(diagramIndex);

        XYChart.Series seriesMax = chart.getData().get(0);
        XYChart.Series seriesMin = chart.getData().get(1);
        XYChart.Series seriesAverage = chart.getData().get(2);
        seriesMax.getData().clear();
        seriesMax.setName("max");
        seriesMin.getData().clear();
        seriesMin.setName("min");
        seriesAverage.getData().clear();
        seriesAverage.setName("average");

        serverData.getItems().stream().forEach(data -> {

            lastChartData.update(data.getDateTime());

            String pattern = (data.isInstantPrice()) ? "dd. HH:mm" : "dd.MM.yy";
            String label = data.getDateTime().format(ofPattern(pattern));

            if (data.getMax() != null)
                seriesMax.getData().add(new XYChart.Data(label, data.getMax()));
            if (data.getMin() != null)
                seriesMin.getData().add(new XYChart.Data(label, data.getMin()));
            if (data.getAverage() != null)
                seriesAverage.getData().add(new XYChart.Data(label, data.getAverage()));
        });
    }

    private void updateAllInOne() {

        LineChart<String, Number> chart = (LineChart<String, Number>) chartsContainer.getChildren().get(0);

        XYChart.Series seriesWorld = chart.getData().get(0);
        XYChart.Series seriesEmerging = chart.getData().get(1);
        XYChart.Series seriesSmall200 = chart.getData().get(2);
        seriesWorld.getData().clear();
        seriesWorld.setName(WORLD.getName());
        seriesEmerging.getData().clear();
        seriesEmerging.setName(EMERGING.getName());
        seriesSmall200.getData().clear();
        seriesSmall200.setName(SMALL200.getName());

        AllInOneChartDto dto = ChartsRestClient.INSTANCE.getAllInOneChartData(since, points);
        dto.getItems().stream().forEach(data -> {

            final String pattern = "dd.MM.yy";
            String label = data.getDateTime().format(ofPattern(pattern));

            if (data.getAverage(WORLD) != null)
                seriesWorld.getData().add(new XYChart.Data(label, data.getAverage(WORLD)));
            if (data.getAverage(EMERGING) != null)
                seriesEmerging.getData().add(new XYChart.Data(label, data.getAverage(EMERGING)));
            if (data.getAverage(SMALL200) != null)
                seriesSmall200.getData().add(new XYChart.Data(label, data.getAverage(SMALL200)));
        });
        lastChartData.update(dto.lastDate());
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

    private class LastChartData {
        private Optional<LocalDateTime> time = Optional.empty();

        void update(LocalDateTime dataTime) {
            if (!time.isPresent() || dataTime.isAfter(time.get()))
                time = Optional.of(dataTime);
        }

        String get() {
            return time.map(t -> t.format(ofPattern("dd.MM. hh:mm"))).orElse("n/a");
        }
    }
}
