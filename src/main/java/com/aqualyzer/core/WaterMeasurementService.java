package com.aqualyzer.core;

import com.aqualyzer.backend.repositories.WaterMeasurementsRepository;
import com.aqualyzer.core.model.WaterMeasurement;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class WaterMeasurementService {

    @Inject
    WaterMeasurementsRepository repo;

    @Transactional
    public List<WaterMeasurement> getAll() {
        return repo.listAll();
    }

    @Transactional
    public void addMeasurement(WaterMeasurement measurement) {
        repo.save(measurement);
    }

    @Transactional
    public void deleteMeasurement(WaterMeasurement measurement) {
        repo.delete(measurement);
    }
}
