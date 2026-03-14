package com.aqualyzer.backend.repositories;

import com.aqualyzer.core.model.WaterMeasurement;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.util.List;

@ApplicationScoped
public class WaterMeasurementsRepository {

    @Inject
    EntityManager em;

    public List<WaterMeasurement> listAll() {
        return em.createQuery("FROM WaterMeasurement", WaterMeasurement.class).getResultList();
    }

    public void save(WaterMeasurement wm) {
        if (wm.getId() == null) {
            em.persist(wm);
        } else {
            em.merge(wm);
        }
    }

    public void delete(WaterMeasurement wm) {
        em.remove(em.contains(wm) ? wm : em.merge(wm));
    }
}
