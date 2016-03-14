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

package de.mg.stock.server;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import java.util.logging.Logger;

@Singleton
public class Config {

    public static final String SWITCH_WRITE_ACCESS = "writeAccessEnabled";
    public static final String MAIL_HOST = "mailHost";
    public static final String MAIL_FROM = "mailFrom";
    public static final String MAIL_TO = "mailTo";

    private static Logger logger = Logger.getLogger(Config.class.getName());

    private boolean writeAccessEnabled = false;
    private String smtpHost, alertMailFrom, alertMailTo;

    @PostConstruct
    void initialize() {
        writeAccessEnabled = System.getProperty(SWITCH_WRITE_ACCESS) != null;
        smtpHost = System.getProperty(MAIL_HOST);
        alertMailFrom = System.getProperty(MAIL_FROM);
        alertMailTo = System.getProperty(MAIL_TO);

        logger.info("Configuration initialized:\n" + this);
    }

    public boolean isWriteAccessEnabled() {
        return writeAccessEnabled;
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    public String getAlertMailFrom() {
        return alertMailFrom;
    }

    public String getAlertMailTo() {
        return alertMailTo;
    }

    @Override
    public String toString() {
        return "Config{" +
                "writeAccessEnabled=" + writeAccessEnabled +
                ", smtpHost='" + smtpHost + '\'' +
                ", alertMailFrom='" + alertMailFrom + '\'' +
                ", alertMailTo='" + alertMailTo + '\'' +
                '}';
    }
}
