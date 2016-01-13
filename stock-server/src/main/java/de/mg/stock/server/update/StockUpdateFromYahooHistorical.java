package de.mg.stock.server.update;

import de.mg.stock.server.model.DayPrice;
import de.mg.stock.server.util.HttpUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import static de.mg.stock.server.util.DateConverters.toLocalDate;
import static de.mg.stock.server.util.NumberUtils.toLong;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Singleton
class StockUpdateFromYahooHistorical implements StockUpdaterHistorical {

    private static Logger logger = Logger.getLogger(StockUpdateFromYahooHistorical.class.getName());

    @Inject
    private HttpUtil httpUtil;

    @Override
    public List<DayPrice> get(String symbol) {

        LocalDateTime fetchTime = LocalDateTime.now();

        String response = httpUtil.get("http://ichart.yahoo.com/table.csv?s=" + symbol);
        if (isEmpty(response)) {
            logger.warning("nothing received for " + symbol);
            return Collections.emptyList();
        }

        String[] lines = response.split(System.getProperty("line.separator"));
        if (lines.length < 2) {
            logger.warning("no lines received for " + symbol);
            return Collections.emptyList();
        }
        if (!"Date,Open,High,Low,Close,Volume,Adj Close".equals(lines[0])) {
            logger.warning("wrong header for " + symbol + ": " + lines[0]);
            return Collections.emptyList();
        }

        List<DayPrice> result = new ArrayList<>();

        for (int i = 1; i < lines.length; i++) {
            StringTokenizer tok = new StringTokenizer(lines[i], ",");
            LocalDate date = toLocalDate(tok.nextToken(), "yyyy-MM-dd");
            DayPrice dayPrice = new DayPrice(date);

            // skip
            tok.nextToken();

            dayPrice.setMax(toLong(tok.nextToken()));
            dayPrice.setMin(toLong(tok.nextToken()));

            dayPrice.setFetchedAt(fetchTime);

            if (dayPrice.getMax() != null && dayPrice.getMin() != null && dayPrice.getMax() != 0 && dayPrice.getMin() != 0)
                result.add(dayPrice);
        }

        return result;
    }

}
