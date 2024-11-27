package com.core.repository;

import com.core.model.Wishmaker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WishmakerRepository extends JpaRepository<Wishmaker, Long> {
    Optional<Wishmaker> findByEmail(String email); // <1>

}
