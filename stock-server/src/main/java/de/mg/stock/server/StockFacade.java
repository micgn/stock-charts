package de.mg.stock.server;

import de.mg.stock.dto.ChartDataDTO;
import de.mg.stock.server.model.Stock;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Logger;

import static javax.ejb.TransactionAttributeType.REQUIRED;
import static javax.ws.rs.core.MediaType.*;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Stateless
@TransactionAttribute(REQUIRED)
@Path("/")
@SuppressWarnings("unused")
public class StockFacade {

    private static Logger logger = Logger.getLogger(StockFacade.class.getName());

    @PersistenceContext
    private EntityManager em;

    @Inject
    private ChartBuilder chartBuilder;

    @Inject
    private StockDAO stockDAO;

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

        ChartDataDTO data = chartBuilder.createAggregated(stock, Integer.valueOf(pointsStr), toDate(dateStr), Boolean.valueOf(percentagesStr));

        return Response.status(Response.Status.OK).entity(data).build();
    }


    @GET
    @Produces(APPLICATION_XML)
    @Path("aggregatedChartData/{date}/{points}")
    public Response getAggregatedChartData(@PathParam("date") String dateStr,
                                           @PathParam("points") String pointsStr) {

        List<Stock> stocks = stockDAO.findAllStocks();

        // TODO remove hard coded
        stocks.sort((s1, s2) -> s1.getSymbol().compareTo(s2.getSymbol()));
        List<Integer> weights = new ArrayList<>();
        weights.add(20);
        weights.add(10);
        weights.add(80);

        ChartDataDTO data = chartBuilder.createAggregated(stocks, weights, Integer.valueOf(pointsStr), toDate(dateStr));

        return Response.status(Response.Status.OK).entity(data).build();
    }

    private Optional<LocalDate> toDate(String dateStr) {
        LocalDate since = null;
        if (!isEmpty(dateStr)) {
            since = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("ddMMyyyy", Locale.ENGLISH));
        }
        return (since == null) ? Optional.empty() : Optional.of(since);
    }

    @GET
    @Produces(APPLICATION_XML)
    @Path("backup")
    public List<Stock> backup() {
        List<Stock> all = stockDAO.findAllStocks();
        return all;
    }

    @POST
    @Consumes(APPLICATION_XML)
    @Path("restore")
    public Response restore(List<Stock> stocks) {
        if (stocks == null || stocks.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("missing xml in body").build();
        }

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

}
