package com.aqualyzer.ui.viewmodels;

import com.aqualyzer.core.enums.QualityRating;
import com.aqualyzer.core.model.Fish;
import com.aqualyzer.core.model.WaterMeasurement;

public class ResultListViewModel {
    private Fish fish;
    private final WaterMeasurement measurement;

    private QualityRating phRating = QualityRating.Unknown;
    private QualityRating temperatureRating = QualityRating.Unknown;
    private QualityRating oxygenRating = QualityRating.Unknown;
    private QualityRating salinityRating = QualityRating.Unknown;

    public ResultListViewModel(WaterMeasurement measurement) {
        this.measurement = measurement;
    }

    public Fish getFish() { return fish; }
    public void setFish(Fish fish) { this.fish = fish; }

    public WaterMeasurement getMeasurement() { return measurement; }

    public QualityRating getPhRating() { return phRating; }
    public void setPhRating(QualityRating phRating) { this.phRating = phRating; }

    public QualityRating getTemperatureRating() { return temperatureRating; }
    public void setTemperatureRating(QualityRating temperatureRating) { this.temperatureRating = temperatureRating; }

    public QualityRating getOxygenRating() { return oxygenRating; }
    public void setOxygenRating(QualityRating oxygenRating) { this.oxygenRating = oxygenRating; }

    public QualityRating getSalinityRating() { return salinityRating; }
    public void setSalinityRating(QualityRating salinityRating) { this.salinityRating = salinityRating; }
}
