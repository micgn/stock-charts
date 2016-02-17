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


import javafx.application.Platform;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

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
