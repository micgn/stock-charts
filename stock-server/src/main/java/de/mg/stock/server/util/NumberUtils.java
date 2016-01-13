package de.mg.stock.server.util;

public class NumberUtils {

    public static Long toLong(String s) {
        if (s == null) return null;

        if ("N/A".equals(s)) return null;

        int dotPos;
        if ((dotPos = s.indexOf('.')) != -1) {
            s = s.substring(0, dotPos + 3);
        }
        try {
            return Long.valueOf(s.replace(".", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
