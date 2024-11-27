package com.core.repository;

import com.core.model.Genie;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GenieRepository extends JpaRepository<Genie, Long> {
    Optional<Genie> findByEmail(String email);

    @Query("SELECT g FROM Genie g LEFT JOIN FETCH g.wishes WHERE g.email = :email")
    Optional<Genie> findByEmailWithWishes(@Param("email") String email);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM wish_user WHERE wish_id = :wishId AND user_id = :genieId", nativeQuery = true)
    void deleteWishUserRelation(@Param("wishId") Long wishId, @Param("genieId") Long genieId);

    @Modifying
    @Transactional
    @Query(value="delete from genie where email=:email",nativeQuery = true)
    void deleteGenieByEmail(@Param("email") String email);

}
