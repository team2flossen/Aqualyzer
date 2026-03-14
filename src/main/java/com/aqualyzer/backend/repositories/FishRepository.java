package com.aqualyzer.backend.repositories;

import com.aqualyzer.core.model.Fish;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.util.List;

@ApplicationScoped
public class FishRepository {

    @Inject
    EntityManager em;

    public List<Fish> listAll() {
        return em.createQuery("FROM Fish", Fish.class).getResultList();
    }

    public void save(Fish fish) {
        if (fish.getId() == null) {
            em.persist(fish);
        } else {
            em.merge(fish);
        }
    }

    public void delete(Fish fish) {
        em.remove(em.contains(fish) ? fish : em.merge(fish));
    }
}
