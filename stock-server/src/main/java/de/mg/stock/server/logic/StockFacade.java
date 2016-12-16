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

package de.mg.stock.server.logic;

import de.mg.stock.dto.AllInOneChartDto;
import de.mg.stock.dto.ChartDataDTO;
import de.mg.stock.dto.StockKeyDataDto;
import de.mg.stock.dto.StocksEnum;
import de.mg.stock.server.Config;
import de.mg.stock.server.dao.StockDAO;
import de.mg.stock.server.model.Stock;
import de.mg.stock.server.util.KeyDataCsvBuilder;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Logger;

import static java.time.format.DateTimeFormatter.ofPattern;
import static javax.ejb.TransactionAttributeType.REQUIRED;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Stateless
@TransactionAttribute(REQUIRED)
@Path("/")
public class StockFacade {

    private static Logger logger = Logger.getLogger(StockFacade.class.getName());

    @PersistenceContext
    private EntityManager em;

    @Inject
    private ChartBuilder chartBuilder;

    @Inject
    private StockDAO stockDAO;

    @Inject
    private KeyDataBuilder keyDataBuilder;

    @Inject
    private KeyDataCsvBuilder keyDataCsvBuilder;

    @Inject
    private Config config;

    @GET
    @Produces(APPLICATION_XML)
    @Path("chartData/{symbol}/{points}")
    public Response getChartData(@PathParam("symbol") String symbol,
                                 @PathParam("points") String pointsStr) {

        return getChartDataSince(symbol, "", "", pointsStr);
    }

    @GET
    @Produces(APPLICATION_XML)
    @Path("chartDataSince/{symbol}/{percentages}/{since}/{points}")
    public Response getChartDataSince(@PathParam("symbol") String symbol,
                                      @PathParam("percentages") String percentagesStr,
                                      @PathParam("since") String dateStr,
                                      @PathParam("points") String pointsStr) {

        Stock stock = stockDAO.findStock(symbol);
        if (stock == null) {
            return Response.status(Response.Status.OK).entity(new ChartDataDTO()).build();
        }
        em.detach(stock);

        ChartDataDTO data = chartBuilder.createOne(stock, Integer.valueOf(pointsStr), toDate(dateStr), Boolean.valueOf(percentagesStr));

        data.getItems().stream().filter(item -> !item.isValid()).forEach(item -> logger.warning("invalid: " + item));

        return Response.status(Response.Status.OK).entity(data).build();
    }


    @GET
    @Produces(APPLICATION_XML)
    @Path("aggregatedChartData/{date}/{points}")
    public Response getAggregatedChartData(@PathParam("date") String dateStr,
                                           @PathParam("points") String pointsStr) {

        List<Stock> stocks = new ArrayList<>();
        stocks.add(stockDAO.findStock(StocksEnum.WORLD.getSymbol()));
        stocks.add(stockDAO.findStock(StocksEnum.EMERGING.getSymbol()));
        stocks.add(stockDAO.findStock(StocksEnum.SMALL200.getSymbol()));

        List<Integer> weights = new ArrayList<>();
        weights.add(StocksEnum.WORLD.getWeight());
        weights.add(StocksEnum.EMERGING.getWeight());
        weights.add(StocksEnum.SMALL200.getWeight());

        ChartDataDTO data = chartBuilder.createAggregated(stocks, weights, Integer.valueOf(pointsStr), toDate(dateStr));

        return Response.status(Response.Status.OK).entity(data).build();
    }

    private Optional<LocalDate> toDate(String dateStr) {
        LocalDate since = null;
        if (!isEmpty(dateStr)) {
            since = LocalDate.parse(dateStr, ofPattern("ddMMyyyy", Locale.ENGLISH));
        }
        return Optional.ofNullable(since);
    }

    @GET
    @Produces(APPLICATION_XML)
    @Path("allInOneChartData/{date}/{points}")
    public Response getAllInOneChartData(@PathParam("date") String dateStr,
                                         @PathParam("points") String pointsStr) {

        List<Stock> all = stockDAO.findAllStocks();
        AllInOneChartDto data = chartBuilder.createAllInOne(all, Integer.valueOf(pointsStr), toDate(dateStr).get());
        return Response.status(Response.Status.OK).entity(data).build();
    }

    @GET
    @Produces(APPLICATION_XML)
    @Path("backup")
    public List<Stock> backup() {
        List<Stock> all = stockDAO.findAllStocks();
        logger.info("backup: " + stockStats(all));
        return all;
    }


    @POST
    @Consumes(APPLICATION_XML)
    @Path("restore")
    public Response restore(List<Stock> stocks) {

        if (!config.isWriteAccessEnabled()) {
            logger.warning("no write access!");

            return Response.status(Response.Status.FORBIDDEN).
                    entity("server missing -D" + Config.SWITCH_WRITE_ACCESS).build();
        }

        if (stocks == null || stocks.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("missing xml in body").build();
        }
        logger.info("restore: " + stockStats(stocks));

        List<Stock> peristentStocks = backup();
        peristentStocks.forEach(stock -> em.remove(stock));
        em.flush();

        stocks.forEach(s -> {
            s.getDayPrices().forEach(dp -> dp.setStock(s));
            s.getInstantPrices().forEach(ip -> ip.setStock(s));
        });
        stocks.forEach(stock -> em.persist(stock));
        em.flush();

        String msg = String.format("successfully removed %d and inserted %d", peristentStocks.size(), stocks.size());
        logger.info(msg);
        return Response.status(Response.Status.OK).build();
    }


    @GET
    @Produces(APPLICATION_XML)
    @Path("statistics")
    public List<StockKeyDataDto> statistics() {
        List<StockKeyDataDto> stockKeyData = keyDataBuilder.create();
        return stockKeyData;
    }

    @GET
    @Produces(TEXT_PLAIN)
    @Path("statisticsCsv")
    public String statisticsCsv() {
        List<StockKeyDataDto> stockKeyData = keyDataBuilder.create();
        return keyDataCsvBuilder.asCsv(stockKeyData);
    }


    public static String stockStats(List<Stock> all) {
        String stats = "";
        for (Stock stock : all) {
            stats += stock.toString() + "\n";
        }
        return (stats.length() == 0) ? "empty" : stats;
    }
}
