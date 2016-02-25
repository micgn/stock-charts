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

package de.mg.stock.dto;

/**
 * the current implementation provides a fixed set of stocks
 */
public enum StocksEnum {

    WORLD("SWDA.SW", "World", 70),
    EMERGING("EUNM.DE", "Emerging", 20),
    SMALL200("EXSE.DE", "Small 200", 10);

    private String symbol, name;

    // the stock's percentage for calculating aggregated charts data
    private int weight;

    public static StocksEnum of(String symbol) {
        if (WORLD.symbol.equals(symbol)) return WORLD;
        else if (EMERGING.symbol.equals(symbol)) return EMERGING;
        else if (SMALL200.symbol.equals(symbol)) return SMALL200;
        else return null;
    }

    StocksEnum(String symbol, String name, int weight) {
        this.symbol = symbol;
        this.name = name;
        this.weight = weight;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public int getWeight() {
        return weight;
    }
}
