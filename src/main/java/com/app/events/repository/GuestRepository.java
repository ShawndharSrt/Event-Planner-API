package com.app.events.repository;

import com.app.events.model.Guest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GuestRepository extends MongoRepository<Guest, String> {

    Optional<Guest> findByGuestId(String id);

    long countByCreatedBy(String userId);
}
