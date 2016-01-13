package de.mg.stock.charts;


import javafx.application.Platform;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

class WorkerThread extends Thread {

    public static final int UPDATE_STATUS_SECONDS = 10;
    public static final int UPDATE_CHARTS_SECONDS = 300;

    private LocalDateTime lastChartUpdate = LocalDateTime.now();

    WorkerThread() {
        setDaemon(true);
        setName("Update Charts");
    }

    @Override
    public void run() {

        while (!this.isInterrupted()) {

            if (lastChartUpdate.until(LocalDateTime.now(), ChronoUnit.SECONDS) >= UPDATE_CHARTS_SECONDS) {
                Platform.runLater(ChartsUpdater.INSTANCE::redrawChart);
                lastChartUpdate = LocalDateTime.now();
            }

            Platform.runLater(() -> ChartsUpdater.INSTANCE.updateStatus(lastChartUpdate));

            try {
                sleep(UPDATE_STATUS_SECONDS * 1000);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }

    }
}
