package com.aqualyzer.core;

import com.aqualyzer.backend.repositories.ResultRepository;
import com.aqualyzer.core.model.Fish;
import com.aqualyzer.core.model.Result;
import com.aqualyzer.core.model.WaterMeasurement;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ResultService {

    @Inject
    ResultRepository repo;

    @Transactional
    public List<Result> getByFish(Fish fish) {
        return repo.findByFish(fish);
    }

    @Transactional
    public Optional<Result> getByMeasurementAndFish(WaterMeasurement measurement, Fish fish) {
        return repo.findByMeasurementAndFish(measurement, fish);
    }

    @Transactional
    public void save(Result result) {
        repo.save(result);
    }

    @Transactional
    public void deleteByMeasurement(WaterMeasurement measurement) {
        repo.deleteByMeasurement(measurement);
    }

    @Transactional
    public void deleteByFish(Fish fish) {
        repo.deleteByFish(fish);
    }
}
