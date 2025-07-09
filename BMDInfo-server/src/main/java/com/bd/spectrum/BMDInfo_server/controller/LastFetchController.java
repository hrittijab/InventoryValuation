package com.bd.spectrum.BMDInfo_server.controller;

import com.bd.spectrum.BMDInfo_server.model.LastFetch;
import com.bd.spectrum.BMDInfo_server.service.LastFetchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/last-fetch")
public class LastFetchController {

    @Autowired
    private LastFetchService lastFetchService;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy, hh:mm a");

    @PostMapping
    public void saveFetchData() {
        lastFetchService.saveLastFetch();
    }

    @PutMapping("/{id}")
    public void update(@PathVariable UUID id, @RequestBody LastFetch lastFetch) {
        lastFetchService.updateLastFetch(id, lastFetch.getLastFetchDataAt());
    }

    @GetMapping("/{id}")
    public LastFetch get(@PathVariable UUID id) {
        return lastFetchService.getLastFetch(id);
    }

    public record LastFetchResponse(String lastFetchDataAt) {}

    @GetMapping
    public List<LastFetchResponse> getAll() {
        List<LastFetch> all = lastFetchService.getAll();
        return all.stream()
                .map(lf -> new LastFetchResponse(lf.getLastFetchDataAt().format(formatter)))
                .collect(Collectors.toList());
    }

    @DeleteMapping
    public void DeleteAll(){
        lastFetchService.deleteAll();
    }
}
