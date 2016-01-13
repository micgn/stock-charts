package de.mg.stock.server.model;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import java.time.LocalDateTime;
import java.util.Date;

import static de.mg.stock.server.util.DateConverters.toDate;
import static de.mg.stock.server.util.DateConverters.toLocalDateTime;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class AbstractPrice extends AbstractEntity {

    @XmlTransient
    @JoinColumn(nullable = false)
    @ManyToOne(optional = false)
    protected Stock stock;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fetchedAt;

    public Stock getStock() {
        return stock;
    }

    public void setStock(Stock stock) {
        this.stock = stock;
    }

    public LocalDateTime getFetchedAt() {
        return toLocalDateTime(fetchedAt);
    }

    public void setFetchedAt(LocalDateTime timestamp) {
        this.fetchedAt = toDate(timestamp);
    }
}
