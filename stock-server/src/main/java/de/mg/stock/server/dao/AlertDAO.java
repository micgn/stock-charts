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

package de.mg.stock.server.dao;

import de.mg.stock.server.model.AlertMailStatus;
import de.mg.stock.server.model.Stock;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TemporalType;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import static de.mg.stock.server.util.DateConverters.toDate;

@Singleton
public class AlertDAO {

    private static Logger logger = Logger.getLogger(AlertDAO.class.getName());

    @PersistenceContext(name = "stock")
    private EntityManager em;


    public Optional<Date> getLastAlertSent() {
        AlertMailStatus status = getAlertMailStatus();
        return Optional.ofNullable(status.getLastAlertSent());
    }

    public void setLastAlertSent(Date date) {
        AlertMailStatus status = getAlertMailStatus();
        if (status == null) {
            status = new AlertMailStatus();
            status = em.merge(status);
        }
        status.setLastAlertSent(date);
    }

    private AlertMailStatus getAlertMailStatus() {
        return (AlertMailStatus) em.createQuery("from " + AlertMailStatus.class.getSimpleName()).getSingleResult();
    }
}
