package de.mg.stock.server.util;


import org.apache.commons.io.IOUtils;

import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class HttpUtil {

    private static final Logger log = Logger.getLogger(HttpUtil.class.getName());

    public String get(String urlStr) {

        try {
            URL url = new URL(urlStr);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream response = connection.getInputStream();
                return IOUtils.toString(response);
            } else {
                log.log(Level.WARNING, "retrieving " + urlStr + " returned " + connection.getResponseCode());
                return null;
            }
        } catch (IOException e) {
            log.log(Level.WARNING, "retrieving " + urlStr, e);
            return null;
        }
    }
}
