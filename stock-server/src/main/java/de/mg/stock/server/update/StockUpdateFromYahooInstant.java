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

import de.mg.stock.server.model.InstantPrice;
import de.mg.stock.server.util.DateConverters;
import de.mg.stock.server.util.HttpUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import static de.mg.stock.server.util.NumberUtils.toLong;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Singleton
public class StockUpdateFromYahooInstant {

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
            logger.info("no meaningful values for " + symbol + ": " + response);
            return null;
        }

        // since at times we receive instant data from long ago:
        if (result.getTime().isBefore(LocalDate.now().atStartOfDay())) {
            return null;
        }

        // only for debugging
        if (false)
            logger.info("retrieved: " + response + "\nparsed: " + result.toString());

        return result;
    }


}
