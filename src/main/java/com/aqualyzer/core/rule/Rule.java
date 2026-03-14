package com.aqualyzer.core.rule;

import com.aqualyzer.core.enums.QualityRating;

import java.util.Objects;

public abstract class Rule {
    public QualityRating apply(Double min, Double max, Double value){

        if (value == null) { return QualityRating.Unknown; }

        if (Objects.equals(min, max)) { return (value.equals(min)) ? QualityRating.Good : QualityRating.Critical; }

        var midpoint = (min + max) / 2; // ergibt mittelpunkt des toleranzbereichs
        var halfRange = (max - min) / 2; // halbe differenz zwischen max und min
        var deviation = Math.abs(value - midpoint) / halfRange; // distanz vom mittelpunkt des tol.-bereichs

        if (deviation <= 0.33) {
            return QualityRating.Good;
        }

        if (deviation <= 0.66) {
            return QualityRating.OK;
        }

        if (deviation <= 1.0) {
            return QualityRating.Risk;
        }

        if (deviation <= 2) {
            return QualityRating.Poor;
        }

        return QualityRating.Critical;
    }
}

