package com.core.repository;

import com.core.model.Genie;
import com.core.model.Wish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface WishRepository extends JpaRepository<Wish, Long> {

    @Query(value="select *from wish where wishmaker_id=?",nativeQuery = true)
    List<Wish> findByWishmakerId(Long id); // <1>

    @Query(value="select w.* from wish w join wish_user wu on w.wish_id=wu.wish_id where wu.user_id=?",nativeQuery = true)
    Set<Wish> getWishByGenieId(Long id);

    @Query(value="SELECT g.* \n" +
            "FROM genie g \n" +
            "JOIN wish_user wu ON g.id = wu.user_id \n" +
            "JOIN wish w ON wu.wish_id = w.wish_id \n" +
            "WHERE w.wish_id = ?",nativeQuery = true)
    Set<Genie> getGenieByWishId(Long id); // <2>


}
