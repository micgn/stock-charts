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

package de.mg.stock.server.update;

import de.mg.stock.server.model.DayPrice;
import de.mg.stock.server.util.DateTimeProvider;
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
public class StockUpdateFromYahooHistorical implements StockUpdaterHistorical {

    private static Logger logger = Logger.getLogger(StockUpdateFromYahooHistorical.class.getName());

    @Inject
    private HttpUtil httpUtil;

    @Inject
    private DateTimeProvider dateTimeProvider;

    @Override
    public List<DayPrice> get(String symbol) {

        LocalDateTime fetchTime = dateTimeProvider.now();

        String response = httpUtil.get("https://ichart.yahoo.com/table.csv?s=" + symbol);
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

    public void setHttpUtil(HttpUtil httpUtil) {
        this.httpUtil = httpUtil;
    }

    public void setDateTimeProvider(DateTimeProvider dateTimeProvider) {
        this.dateTimeProvider = dateTimeProvider;
    }
}
