package com.edunexususerservice.domain.user.repository;

import com.edunexususerservice.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.loginHistories WHERE u.email = :email")
    Optional<User> findByEmail(@Param("email") String email);
}