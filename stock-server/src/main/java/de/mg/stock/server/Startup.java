package de.mg.stock.server;

import de.mg.stock.server.update.StockUpdateTasks;

import javax.annotation.PostConstruct;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.inject.Inject;

@Singleton
@javax.ejb.Startup
@SuppressWarnings("unused")
public class Startup {

    @Inject
    private StockUpdateTasks stockUpdateTasks;

    @PostConstruct
    public void atStartup() {
        stockUpdateTasks.updateAsync();
    }

}
