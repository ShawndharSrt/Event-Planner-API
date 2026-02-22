package com.app.events.repository;

import com.app.events.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);

    Optional<User> findTopByOrderByUserIdDesc();

    Optional<User> findByResetToken(String resetToken);

    java.util.List<User> findByUserIdIn(java.util.Collection<String> userIds);

    Optional<User> findByUserId(String userId);
}
