package de.mg.stock.dto;

import de.mg.stock.util.LocalDateTimeAdapter;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class ChartDataDTO implements Serializable {

    private String name;
    private LocalDateTime lastUpdate;
    private List<ChartItemDTO> items = new ArrayList<>();

    public ChartDataDTO() {
    }

    public ChartDataDTO(String name, LocalDateTime lastUpdate) {
        this.name = name;
        this.lastUpdate = lastUpdate;
    }

    public String getName() {
        return name;
    }

    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    public List<ChartItemDTO> getItems() {
        return items;
    }

    public void setItems(List<ChartItemDTO> items) {
        this.items = items;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    @Override
    public String toString() {
        return "ChartDataDTO{" +
                "name='" + name + '\'' +
                ", lastUpdate=" + lastUpdate +
                ", items=" + items +
                '}';
    }
}
