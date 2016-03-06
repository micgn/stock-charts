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

package de.mg.stock.server;

import de.mg.stock.server.dao.StockDAO;
import de.mg.stock.server.logic.StockFacade;
import de.mg.stock.server.update.StockUpdateTasks;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.inject.Inject;
import java.util.logging.Logger;

@Singleton
@javax.ejb.Startup
@SuppressWarnings("unused")
public class Startup {

    private static Logger logger = Logger.getLogger(Startup.class.getName());

    @Inject
    private StockUpdateTasks stockUpdateTasks;

    @Inject
    private StockDAO stockDAO;

    @PostConstruct
    public void atStartup() {
        logger.info("found: " + StockFacade.stockStats(stockDAO.findAllStocks()));
        stockUpdateTasks.updateAsync();
    }

}
