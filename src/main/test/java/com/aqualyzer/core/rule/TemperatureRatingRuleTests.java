package com.aqualyzer.core.rule;

import com.aqualyzer.core.enums.QualityRating;
import com.aqualyzer.core.model.Fish;
import com.aqualyzer.core.model.WaterMeasurement;
import com.aqualyzer.core.rule.TemperatureRatingRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TemperatureRatingRuleTests {

    private TemperatureRatingRule temperatureRatingRule;
    private Fish testFish;

    @BeforeEach
    void setUp() {
        temperatureRatingRule = new TemperatureRatingRule();
        testFish = new Fish("Goldfisch", 0.0, 5.0, 6.5, 7.5, 6.1, 9.8, 2.0, 12.0);
        testFish.setTempMin(20.0);
        testFish.setTempMax(25.0);
    }

    @Test
    void apply_temperatureNull_returnsUnknown() {
        // arrange
        Double temp = null;

        // act
        var rating = temperatureRatingRule.apply(testFish.getTempMin(), testFish.getTempMax(), temp);

        // assert
        assertSame(QualityRating.Unknown, rating);
    }

    @Test
    void apply_tempWithinRange_returnsGood() {
        // arrange
        var measure = new WaterMeasurement();
        measure.setTemperature(22.5);
        
        // act
        var rating = temperatureRatingRule.apply(testFish.getTempMin(), testFish.getTempMax(), measure.getTemperature());
        
        // assert
        assertSame(QualityRating.Good, rating);
    }

    @Test
    void apply_tempNearMargin_returnsOK() {

        // arrange
        var measure = new WaterMeasurement();
        measure.setTemperature(24.0);

        // act
        var rating = temperatureRatingRule.apply(testFish.getTempMin(), testFish.getTempMax(), measure.getTemperature());

        // assert
        assertSame(QualityRating.OK, rating);
    }

    @Test
    void apply_tempAtMargin_returnsRisk() {
        // arrange
        var measure = new WaterMeasurement();
        measure.setTemperature(25.0);

        // act
        var rating = temperatureRatingRule.apply(testFish.getTempMin(), testFish.getTempMax(), measure.getTemperature());

        // assert
        assertSame(QualityRating.Risk, rating);

    }

    @Test
    void apply_tempBeyondMargin_returnsPoor() {
        // arrange
        var measure = new WaterMeasurement();
        measure.setTemperature(27.0);

        // act
        var rating = temperatureRatingRule.apply(testFish.getTempMin(), testFish.getTempMax(), measure.getTemperature());

        // assert
        assertSame(QualityRating.Poor, rating);
    }


    @Test
    void apply_tempFarBeyondMargin_returnsCritical() {
        // arrange
        var measure = new WaterMeasurement();
        measure.setTemperature(100.0);

        // act
        var rating = temperatureRatingRule.apply(testFish.getTempMin(), testFish.getTempMax(), measure.getTemperature());

        // assert
        assertSame(QualityRating.Critical, rating);
    }
}
