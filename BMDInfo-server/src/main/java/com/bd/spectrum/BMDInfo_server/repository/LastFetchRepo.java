package com.bd.spectrum.BMDInfo_server.repository;

import com.bd.spectrum.BMDInfo_server.model.LastFetch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LastFetchRepo extends JpaRepository<LastFetch, UUID> {
}
