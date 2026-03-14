package com.aqualyzer.core.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.UUID;

@Entity
@Table(name = "fish")
public class Fish {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "scientific_name")
    private String scientificName;

    @Column
    private String comment;

    @Column(name = "ph_min", nullable = false)
    @Min(0) @Max(14)
    private Double phMin;

    @Column(name = "ph_max", nullable = false)
    @Min(0) @Max(14)
    private Double phMax;

    @Column(name = "temp_min", nullable = false)
    @Min(-40) @Max(100)
    private Double tempMin;

    @Column(name = "temp_max", nullable = false)
    @Min(-40) @Max(100)
    private Double tempMax;

    @Column(name = "psu_min")
    @Min(0) @Max(80)
    private Double psuMin;

    @Column(name = "psu_max")
    @Min(0) @Max(80)
    private Double psuMax;

    @Column(name = "o2concentration_min")
    @Min(0) @Max(20)
    private Double o2ConcentrationMin;

    @Column(name = "o2concentration_max")
    @Min(0) @Max(20)
    private Double o2ConcentrationMax;

    public Fish() {}

    public Fish(String name, Double psuMin, Double psuMax, Double phMin, Double phMax,
                Double tempMin, Double tempMax, Double o2ConcentrationMin, Double o2ConcentrationMax) {
        this.name = name;
        this.phMin = phMin;
        this.phMax = phMax;
        this.tempMin = tempMin;
        this.tempMax = tempMax;
        this.psuMin = psuMin;
        this.psuMax = psuMax;
        this.o2ConcentrationMin = o2ConcentrationMin;
        this.o2ConcentrationMax = o2ConcentrationMax;
    }

    public UUID getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getScientificName() { return scientificName; }
    public void setScientificName(String scientificName) { this.scientificName = scientificName; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Double getPhMin() { return phMin; }
    public void setPhMin(Double phMin) { this.phMin = phMin; }

    public Double getPhMax() { return phMax; }
    public void setPhMax(Double phMax) { this.phMax = phMax; }

    public Double getTempMin() { return tempMin; }
    public void setTempMin(Double tempMin) { this.tempMin = tempMin; }

    public Double getTempMax() { return tempMax; }
    public void setTempMax(Double tempMax) { this.tempMax = tempMax; }

    public Double getPsuMin() { return psuMin; }
    public void setPsuMin(Double psuMin) { this.psuMin = psuMin; }

    public Double getPsuMax() { return psuMax; }
    public void setPsuMax(Double psuMax) { this.psuMax = psuMax; }

    public Double getO2ConcentrationMin() { return o2ConcentrationMin; }
    public void setO2ConcentrationMin(Double o2ConcentrationMin) { this.o2ConcentrationMin = o2ConcentrationMin; }

    public Double getO2ConcentrationMax() { return o2ConcentrationMax; }
    public void setO2ConcentrationMax(Double o2ConcentrationMax) { this.o2ConcentrationMax = o2ConcentrationMax; }

    @Override
    public String toString() { return name; }
}
