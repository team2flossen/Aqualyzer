package com.aqualyzer.core.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "measures")
public class WaterMeasurement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, name = "timestamp")
    private Date timestamp;

    @Column(nullable = false, name = "name")
    private String name;

    @Column
    private String comment;

    @Column(name = "ph_value")
    @Min(0) @Max(14)
    private Double phValue;

    @Column(name = "temperature")
    @Min(-40) @Max(100)
    private Double temperature;

    @Column(name = "o2concentration")
    @Min(0) @Max(20)
    private Double o2concentration;

    @Column(name = "psu")
    @Min(0) @Max(80)
    private Double psu;

    public WaterMeasurement() {}

    /** Für den manuellen Erfassen-Dialog — nimmt ein Date-Objekt direkt entgegen. */
    public WaterMeasurement(Date timestamp, String name, Double psu, Double phValue,
                            Double temperature, Double o2concentration) {
        this.timestamp = timestamp;
        this.name = name;
        this.phValue = phValue;
        this.temperature = temperature;
        this.o2concentration = o2concentration;
        this.psu = psu;
    }

    /** Für den CSV-Import — parst den Zeitstempel aus dem Dateiformat "dd.MM.yyyy HH:mm". */
    public WaterMeasurement(String timestamp, String name, Double psu, Double phValue,
                            Double temperature, Double o2concentration) throws ParseException {
        this.timestamp = new SimpleDateFormat("dd.MM.yyyy HH:mm").parse(timestamp);
        this.name = name;
        this.phValue = phValue;
        this.temperature = temperature;
        this.o2concentration = o2concentration;
        this.psu = psu;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Double getPhValue() { return phValue; }
    public void setPhValue(Double phValue) { this.phValue = phValue; }

    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }

    public Double getO2concentration() { return o2concentration; }
    public void setO2concentration(Double o2concentration) { this.o2concentration = o2concentration; }

    public Double getPsu() { return psu; }
    public void setPsu(Double psu) { this.psu = psu; }
}
