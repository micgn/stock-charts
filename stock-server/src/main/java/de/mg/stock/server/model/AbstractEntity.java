package de.mg.stock.server.model;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;

import static javax.persistence.GenerationType.AUTO;

@MappedSuperclass
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AbstractEntity {

    @Id
    @GeneratedValue(strategy = AUTO)
    @XmlTransient
    private Long id;

    @Version
    @XmlTransient
    private Long version;

}
