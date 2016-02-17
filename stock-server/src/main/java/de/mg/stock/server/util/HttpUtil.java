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
