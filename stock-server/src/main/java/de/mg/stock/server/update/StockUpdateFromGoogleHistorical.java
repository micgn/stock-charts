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
import java.util.*;
import java.util.logging.Logger;

import static de.mg.stock.server.util.DateConverters.toLocalDate;
import static de.mg.stock.server.util.NumberUtils.toLong;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Singleton
public class StockUpdateFromGoogleHistorical implements StockUpdaterHistorical {

    private static Logger logger = Logger.getLogger(StockUpdateFromGoogleHistorical.class.getName());

    @Inject
    private HttpUtil httpUtil;

    @Inject
    private DateTimeProvider dateTimeProvider;

    @Override
    public List<DayPrice> get(String symbol) {

        String shortSymbol = symbol.substring(0, symbol.lastIndexOf('.'));

        LocalDateTime fetchTime = dateTimeProvider.now();

        String response = httpUtil.get("http://www.google.com/finance/historical?output=csv&startdate=Jan+1%2C+2000&q=" + shortSymbol);
        if (isEmpty(response)) {
            logger.warning("nothing received for " + shortSymbol);
            return Collections.emptyList();
        }

        String[] lines = response.split(System.getProperty("line.separator"));
        if (lines.length < 2) {
            logger.warning("no lines received for " + shortSymbol);
            return Collections.emptyList();
        }
        if (!lines[0].contains("Date,Open,High,Low,Close,Volume")) {
            logger.warning("wrong header for " + shortSymbol + ": " + lines[0]);
            return Collections.emptyList();
        }

        List<DayPrice> result = new ArrayList<>();

        for (int i = 1; i < lines.length; i++) {
            StringTokenizer tok = new StringTokenizer(lines[i], ",");
            LocalDate date = toLocalDate(tok.nextToken(), "dd-MMMM-yy", Locale.ENGLISH);
            DayPrice dayPrice = new DayPrice(date);
            dayPrice.setFetchedAt(fetchTime);

            // skip
            tok.nextToken();

            dayPrice.setMax(toLong(tok.nextToken()));
            dayPrice.setMin(toLong(tok.nextToken()));

            Long close = toLong(tok.nextToken());
            if (dayPrice.getMax() == null)
                dayPrice.setMax(close);
            if (dayPrice.getMin() == null)
                dayPrice.setMin(close);

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
