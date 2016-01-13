package de.mg.stock.server.update;

import de.mg.stock.server.model.InstantPrice;
import de.mg.stock.server.util.DateConverters;
import de.mg.stock.server.util.HttpUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;

import static de.mg.stock.server.util.DateConverters.toLocalDate;
import static de.mg.stock.server.util.NumberUtils.toLong;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Singleton
class StockUpdateFromYahooInstant {

    private static Logger logger = Logger.getLogger(StockUpdateFromYahooInstant.class.getName());

    @Inject
    private HttpUtil httpUtil;

    public InstantPrice get(String symbol) {

        LocalDateTime fetchTime = LocalDateTime.now();

        /*
            b2=ask (real time)
            b3=bid (real time)
            a=ask
            b=bid
            g=day min
            h=day max
            d1=last trade date
            t1=last trade time
         */
        String response = httpUtil.get("http://download.finance.yahoo.com/d/quotes.csv?f=b2b3abghd1t1&s=" + symbol);
        if (isEmpty(response)) {
            logger.warning("nothing received for " + symbol);
            return null;
        }

        String[] lines = response.split(System.getProperty("line.separator"));
        if (lines.length != 1) {
            logger.warning("received " + lines.length + " for " + symbol);
            return null;
        }

        InstantPrice result = new InstantPrice();

        StringTokenizer tok = new StringTokenizer(lines[0], ",");

        Long askReal = toLong(tok.nextToken());
        Long bidReal = toLong(tok.nextToken());
        Long ask = toLong(tok.nextToken());
        Long bid = toLong(tok.nextToken());
        Long dayMin = toLong(tok.nextToken());
        Long dayMax = toLong(tok.nextToken());

        String dateTimeStr = tok.nextToken() + " " + tok.nextToken();
        dateTimeStr = dateTimeStr.replaceAll("\"", "");
        LocalDateTime tradeTime = DateConverters.toLocalDateTime(dateTimeStr, "MM/dd/yyyy hh:mma", Locale.ENGLISH);
        if (tradeTime == null) {
            logger.warning("invalid date + time for " + symbol + ": " + dateTimeStr);
            return null;
        }

        result.setTime(tradeTime);
        result.setFetchedAt(fetchTime);
        result.setAsk(askReal != null ? askReal : ask);
        result.setBid(bidReal != null ? bidReal : bid);
        result.setDayMax(dayMax);
        result.setDayMin(dayMin);

        if (result.getAsk() == null && result.getBid() == null && result.getDayMax() == null && result.getDayMin() == null) {
            logger.info("no meaningful values for " + symbol);
            return null;
        }

        return result;
    }


}
