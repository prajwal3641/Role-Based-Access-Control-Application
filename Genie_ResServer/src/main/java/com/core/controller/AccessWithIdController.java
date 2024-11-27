package com.core.controller;

import com.core.model.Genie;
import com.core.model.Wish;
import com.core.model.Wishmaker;
import com.core.repository.GenieRepository;
import com.core.repository.WishRepository;
import com.core.repository.WishmakerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AccessWithIdController {


    private final WishRepository wishRepository;
    private final WishmakerRepository wishmakerRepository;
    private final GenieRepository genieRepository;

    @GetMapping("/getGenie")
    @PreAuthorize("#email == authentication.principal.claims['sub']")
    public ResponseEntity<Genie> getGenie(@RequestParam String email) {
        Genie genie = genieRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Genie not found"));

        return ResponseEntity.ok().body(genie);
    }

    @GetMapping("/getWishmaker")
    @PreAuthorize("#email == authentication.principal.claims['sub']")
    public ResponseEntity<Wishmaker> getWishmaker(@RequestParam String email) {
        return ResponseEntity.ok().body(wishmakerRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("Wishmaker not found")));
    }

    @GetMapping("/getWishes")
    public ResponseEntity<Iterable<Wish>> getWishes() {
        List<Wish> wishes = wishRepository.findAll();
        return ResponseEntity.ok().body(wishes);
    }

    @GetMapping("/getAllWishesByWishMakerEmail")
    @PreAuthorize("#email == Authentication.principal.claims['sub']")
    public ResponseEntity<Iterable<Wish>> getWishesByWishMakerEmail(@RequestParam String email) {
        Wishmaker wishmaker = wishmakerRepository.findByEmail(email).get();
//        System.out.println(wishmaker.getWishes());

        return ResponseEntity.ok().body(wishRepository.findByWishmakerId(wishmaker.getId()));
    }

    @GetMapping("/getAllWishByGenieEmail")
    @PreAuthorize("#email == Authentication.principal.claims['sub']")
    public ResponseEntity<Iterable<Wish>> getWishByGenieId(@RequestParam String email) {
        Genie genie = genieRepository.findByEmail(email).get();
        return ResponseEntity.ok().body(wishRepository.getWishByGenieId(genie.getId()));
    }

    @GetMapping("/getWishmakerByWishId")
    public ResponseEntity<Wishmaker> getWishmakerByWishId(@RequestParam Long wishId) {
        Wish wish = wishRepository.findById(wishId).get();
        if(wish != null){
            return ResponseEntity.ok().body(wish.getWishmaker());
        }else{
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

    }
}
