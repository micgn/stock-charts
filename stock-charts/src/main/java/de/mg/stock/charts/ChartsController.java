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

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static de.mg.stock.charts.ShowTypeEnum.AGGREGATED;
import static de.mg.stock.charts.ShowTypeEnum.ALL;
import static de.mg.stock.charts.ShowTypeEnum.EMERGING;
import static de.mg.stock.charts.ShowTypeEnum.SMALL200;
import static de.mg.stock.charts.ShowTypeEnum.WORLD;

public class ChartsController {

    public static ChartsController INSTANCE;

    private ChartsUpdater updater = ChartsUpdater.INSTANCE;

    @FXML
    private DatePicker sinceDate;

    @FXML
    private ToggleGroup percentageSelection;

    @FXML
    private TextField points;

    public ChartsController() {
        INSTANCE = this;
    }

    public void showWorld() {
        updater.setShowType(WORLD);
        trigger();
    }

    public void showEmerging() {
        updater.setShowType(EMERGING);
        trigger();
    }

    public void showSmall200() {
        updater.setShowType(SMALL200);
        trigger();
    }

    public void showAll() {
        updater.setShowType(ALL);
        trigger();
    }

    public void showAggregated() {
        if (sinceDate.getValue() == null) {
            sinceDate.setValue(LocalDate.of(2015, 1, 1));
        }
        percentageSelection.selectToggle(percentageSelection.getToggles().get(1));
        updater.setShowPercentages(true);

        updater.setShowType(AGGREGATED);
        trigger();
    }

    public void showAbsolute() {
        if (updater.getShowType() != AGGREGATED) {
            updater.setShowPercentages(false);
            trigger();
        } else {
            percentageSelection.selectToggle(percentageSelection.getToggles().get(1));
        }
    }

    public void showPercentage() {
        updater.setShowPercentages(true);
        trigger();
    }

    public void update() {
        ChartsUpdater.INSTANCE.updateStatus(LocalDateTime.now());
        trigger();
    }

    private void trigger() {
        updater.setSince(sinceDate.getValue());
        updater.setPoints(points.getText());
        updater.reinitializeCharts();
    }


    public void quit() {
        System.exit(0);
    }

    public void exportData() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save...");
        fileChooser.setInitialFileName("stock-data.xml");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("XML", "*.xml"));
        File selectedFile = fileChooser.showSaveDialog(getWindow());
        if (selectedFile != null) {
            String backup = ChartsRestClient.INSTANCE.getBackup();
            try (PrintWriter out = new PrintWriter(selectedFile)) {
                out.write(backup);
                message("Exporting...", "file has been written");
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void importData() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import...");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("XML", "*.xml"));
        File selectedFile = fileChooser.showOpenDialog(getWindow());
        if (selectedFile != null && selectedFile.canRead()) {
            try {
                List<String> lines = Files.readAllLines(selectedFile.toPath());
                String in = lines.stream().collect(Collectors.joining());
                boolean success = ChartsRestClient.INSTANCE.restoreBackup(in);
                if (success) {
                    message("Importing...", "finished successfully");
                } else {
                    message("Importing...", "failed!");
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Window getWindow() {
        return sinceDate.getScene().getWindow();
    }

    private void message(String title, String text) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(text);
        alert.showAndWait();
    }
}
