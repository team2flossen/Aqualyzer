package com.aqualyzer.core.model;

import com.aqualyzer.core.enums.QualityRating;
import java.time.LocalDateTime;

public class Result {

    private Fish fish;
    private WaterMeasurement measure;
    private QualityRating waterPSURating;
    private QualityRating temperatureRating;
    private QualityRating o2concentrationRating;
    private LocalDateTime timestamp;
    private QualityRating phRating;

    public Result() {}

    public Result(
            Fish fish,
            WaterMeasurement measure,
            QualityRating phRating,
            QualityRating waterPSURating,
            QualityRating temperatureRating,
            QualityRating o2concentrationRating,
            LocalDateTime timestamp
    ) {
        this.fish = fish;
        this.measure = measure;
        this.phRating = phRating;
        this.waterPSURating = waterPSURating;
        this.temperatureRating = temperatureRating;
        this.timestamp = timestamp;
        this.o2concentrationRating = o2concentrationRating;
    }

    public Fish getFish() { return fish; }
    public void setFish(Fish fish) { this.fish = fish; }

    public WaterMeasurement getMeasurement() { return measure; }
    public void setMeasurement(WaterMeasurement measure) { this.measure = measure; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public QualityRating getPhRating() { return phRating; }
    public void setPhRating(QualityRating phRating) { this.phRating = phRating; }

    public QualityRating getWaterPSURating() { return waterPSURating; }
    public void setWaterPSURating(QualityRating waterPSURating) { this.waterPSURating = waterPSURating; }

    public QualityRating getTemperatureRating() { return temperatureRating; }
    public void setTemperatureRating(QualityRating temperatureRating) { this.temperatureRating = temperatureRating; }

    public QualityRating getO2ConcentrationRating() { return o2concentrationRating; }
    public void setO2ConcentrationRating(QualityRating o2concentrationRating) { this.o2concentrationRating = o2concentrationRating; }
}
