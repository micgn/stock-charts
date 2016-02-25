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

package de.mg.stock.charts;


import de.mg.stock.dto.AllInOneChartDto;
import de.mg.stock.dto.ChartDataDTO;
import de.mg.stock.dto.StocksEnum;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

class ChartsRestClient {

    static final ChartsRestClient INSTANCE = new ChartsRestClient();

    private ChartsRestClient() {
    }

    ChartDataDTO getChartData(StocksEnum stock, int points) {
        String path = String.format("chartData/%s/%d", stock.getSymbol(), points);
        return retrieve(ChartDataDTO.class, path);
    }

    ChartDataDTO getChartDataSince(StocksEnum stock, LocalDate since, boolean percentages, int points) {
        String sinceStr = since.format(DateTimeFormatter.ofPattern("ddMMyyyy", Locale.ENGLISH));
        String path = String.format("chartDataSince/%s/%s/%s/%d", stock.getSymbol(), (percentages) ? "true" : "false", sinceStr, points);
        return retrieve(ChartDataDTO.class, path);
    }

    ChartDataDTO getAggregatedChartData(LocalDate since, int points) {
        String sinceStr = since.format(DateTimeFormatter.ofPattern("ddMMyyyy", Locale.ENGLISH));
        String path = String.format("aggregatedChartData/%s/%d", sinceStr, points);
        return retrieve(ChartDataDTO.class, path);
    }

    AllInOneChartDto getAllInOneChartData(LocalDate since, int points) {
        String sinceStr = since.format(DateTimeFormatter.ofPattern("ddMMyyyy", Locale.ENGLISH));
        String path = String.format("allInOneChartData/%s/%d", sinceStr, points);
        return retrieve(AllInOneChartDto.class, path);
    }

    String getBackup() {
        String backupStr = webtarget().path("backup").request().accept(MediaType.APPLICATION_XML).get(String.class);
        return backupStr;
    }

    boolean restoreBackup(String backup) {
        Response r = webtarget().path("restore").request(MediaType.TEXT_PLAIN).post(Entity.entity(backup, MediaType.APPLICATION_XML));
        return r.getStatus() == 200;
    }

    private <T> T retrieve(Class<T> resultClass, String path) {
        WebTarget target = webtarget();
        return target.path(path).request()
                .accept(MediaType.APPLICATION_XML).get(resultClass);
    }

    private WebTarget webtarget() {
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

            }}, new java.security.SecureRandom());
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        Client client = ClientBuilder.newBuilder().sslContext(sslContext).hostnameVerifier((var1, var2) -> true).build();

        HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic(ChartsMain.BASIC_AUTH_LOGIN, ChartsMain.BASIC_AUTH_PASSWORD);
        client.register(feature);

        URI baseUri = UriBuilder.fromUri(ChartsMain.serverUrl).build();
        WebTarget target = client.target(baseUri);
        return target;
    }

}
