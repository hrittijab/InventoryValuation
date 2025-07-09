package com.bd.spectrum.BMDInfo_server.service;

import com.bd.spectrum.BMDInfo_server.model.LastFetch;
import com.bd.spectrum.BMDInfo_server.repository.LastFetchRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class LastFetchService {

    @Autowired
    private LastFetchRepo lastFetchRepo;

    public void saveLastFetch(){
        this.deleteAll();
        LastFetch lastFetch = new LastFetch();
        lastFetchRepo.save(lastFetch);
    }

    public LastFetch getLastFetch(UUID id){
        return lastFetchRepo.findById(id).orElseThrow(null);
    }

    public void updateLastFetch(UUID id, LocalDateTime newLastFetchDataAt) {
        Optional<LastFetch> optionalLastFetch = lastFetchRepo.findById(id);
        if (optionalLastFetch.isPresent()) {
            LastFetch lastFetch = optionalLastFetch.get();
            lastFetch.setLastFetchDataAt(newLastFetchDataAt);
            lastFetchRepo.save(lastFetch); // This will update since ID is the same
        } else {
            throw new RuntimeException("LastFetch with ID " + id + " not found");
        }
    }

    public List<LastFetch> getAll(){
        return lastFetchRepo.findAll();
    }

    public void deleteAll(){
        lastFetchRepo.deleteAll();
    }

}
