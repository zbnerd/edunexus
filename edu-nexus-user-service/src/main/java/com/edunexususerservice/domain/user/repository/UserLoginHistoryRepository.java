package com.edunexususerservice.domain.user.repository;

import com.edunexususerservice.domain.user.entity.UserLoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserLoginHistoryRepository extends JpaRepository<UserLoginHistory, Integer> {
}
