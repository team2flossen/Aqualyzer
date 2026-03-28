package com.aqualyzer.backend.repositories;

import com.aqualyzer.core.model.Fish;
import com.aqualyzer.core.model.Result;
import com.aqualyzer.core.model.WaterMeasurement;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ResultRepository {

    @Inject
    EntityManager em;

    public List<Result> findByFish(Fish fish) {
        return em.createQuery(
                "FROM Result r WHERE r.fish = :fish", Result.class)
                .setParameter("fish", fish)
                .getResultList();
    }

    public Optional<Result> findByMeasurementAndFish(WaterMeasurement measurement, Fish fish) {
        return em.createQuery(
                "FROM Result r WHERE r.measurement = :measurement AND r.fish = :fish", Result.class)
                .setParameter("measurement", measurement)
                .setParameter("fish", fish)
                .getResultStream()
                .findFirst();
    }

    public void save(Result result) {
        if (result.getId() == null) {
            em.persist(result);
        } else {
            em.merge(result);
        }
    }

    public void deleteByMeasurement(WaterMeasurement measurement) {
        em.createQuery("DELETE FROM Result r WHERE r.measurement = :measurement")
                .setParameter("measurement", measurement)
                .executeUpdate();
    }

    public void deleteByFish(Fish fish) {
        em.createQuery("DELETE FROM Result r WHERE r.fish = :fish")
                .setParameter("fish", fish)
                .executeUpdate();
    }
}
