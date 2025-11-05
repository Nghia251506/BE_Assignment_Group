package com.tns.newscrawler.repository;

import com.tns.newscrawler.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    long countByTenant_IdAndIsActiveTrue(Long tenantId);

    List<User> findByTenant_Id(Long tenantId);
}
