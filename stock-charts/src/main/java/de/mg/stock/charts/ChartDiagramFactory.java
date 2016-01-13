package de.mg.stock.charts;


import javafx.geometry.Side;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.VBox;

public class ChartDiagramFactory {

    static LineChart<String, Number> initializeChart(String title, boolean showPercentages, VBox container) {

        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        if (showPercentages) {
            yAxis.setLabel("%");
        } else {
            yAxis.setLabel("€");
        }
        yAxis.setForceZeroInRange(false);
        yAxis.setSide(Side.RIGHT);

        final LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setPrefHeight(700);
        lineChart.setTitle(title);
        lineChart.setTitleSide(Side.LEFT);
        lineChart.setCreateSymbols(false);
        lineChart.setVerticalGridLinesVisible(false);
        lineChart.setHorizontalGridLinesVisible(true);

        XYChart.Series seriesMax = new XYChart.Series();
        XYChart.Series seriesMin = new XYChart.Series();
        XYChart.Series seriesAverage = new XYChart.Series();
        lineChart.getData().addAll(seriesMax, seriesMin, seriesAverage);

        lineChart.setLegendVisible(false);

        container.getChildren().add(lineChart);

        lineChart.getStylesheets().add("charts.css");

        return lineChart;
    }

    static void removeCharts(VBox container) {
        while (container.getChildren().size() > 0)
            container.getChildren().remove(0);
    }
}
