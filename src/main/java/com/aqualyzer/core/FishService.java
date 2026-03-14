package com.aqualyzer.core;

import com.aqualyzer.backend.repositories.FishRepository;
import com.aqualyzer.core.model.Fish;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class FishService {

    @Inject
    FishRepository fishRepo;

    @Transactional
    public List<Fish> getAll() {
        return fishRepo.listAll();
    }

    @Transactional
    public void addFish(Fish fish) {
        fishRepo.save(fish);
    }

    @Transactional
    public void deleteFish(Fish fish) {
        fishRepo.delete(fish);
    }
}
