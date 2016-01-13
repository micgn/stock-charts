package de.mg.stock.dto;

public enum StocksEnum {
    WORLD("SWDA.SW", "World"), EMERGING("EUNM.DE", "Emerging"), SMALL200("EXSE.DE", "Small 200");

    private String symbol, name;

    StocksEnum(String symbol, String name) {
        this.symbol = symbol;
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }
}
