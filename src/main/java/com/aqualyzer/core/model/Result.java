package com.aqualyzer.core.model;

import com.aqualyzer.core.enums.QualityRating;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "results", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"measurement_id", "fish_id"})
})
public class Result {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "measurement_id", nullable = false)
    private WaterMeasurement measurement;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fish_id", nullable = false)
    private Fish fish;

    @Enumerated(EnumType.STRING)
    @Column(name = "ph_rating")
    private QualityRating phRating = QualityRating.Unknown;

    @Enumerated(EnumType.STRING)
    @Column(name = "temperature_rating")
    private QualityRating temperatureRating = QualityRating.Unknown;

    @Enumerated(EnumType.STRING)
    @Column(name = "oxygen_rating")
    private QualityRating oxygenRating = QualityRating.Unknown;

    @Enumerated(EnumType.STRING)
    @Column(name = "salinity_rating")
    private QualityRating salinityRating = QualityRating.Unknown;

    public Result() {}

    public Result(WaterMeasurement measurement, Fish fish) {
        this.measurement = measurement;
        this.fish = fish;
    }

    public UUID getId() { return id; }

    public WaterMeasurement getMeasurement() { return measurement; }
    public void setMeasurement(WaterMeasurement measurement) { this.measurement = measurement; }

    public Fish getFish() { return fish; }
    public void setFish(Fish fish) { this.fish = fish; }

    public QualityRating getPhRating() { return phRating; }
    public void setPhRating(QualityRating phRating) { this.phRating = phRating; }

    public QualityRating getTemperatureRating() { return temperatureRating; }
    public void setTemperatureRating(QualityRating temperatureRating) { this.temperatureRating = temperatureRating; }

    public QualityRating getOxygenRating() { return oxygenRating; }
    public void setOxygenRating(QualityRating oxygenRating) { this.oxygenRating = oxygenRating; }

    public QualityRating getSalinityRating() { return salinityRating; }
    public void setSalinityRating(QualityRating salinityRating) { this.salinityRating = salinityRating; }
}
