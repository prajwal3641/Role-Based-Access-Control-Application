package com.core.controller;

import com.core.model.Genie;
import com.core.model.Wish;
import com.core.model.Wishmaker;
import com.core.model.users;
import com.core.repository.GenieRepository;
import com.core.repository.UserRepository;
import com.core.repository.WishRepository;
import com.core.repository.WishmakerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RegisterController {

    private final UserRepository userRepository;
    private final WishRepository wishRepository;
    private final WishmakerRepository wishmakerRepository;
    private final GenieRepository genieRepository;
    private final PasswordEncoder passwordEncoder;


    // Register as Genie(user)
    @PostMapping(value = "/registerGenie",consumes="application/json")
    public String registerGenie(@RequestBody Genie genie) {

        genie.setPwd(passwordEncoder.encode(genie.getPwd()));
        genie.setRole("ROLE_GENIE");

        // make a object of user
        users user = users.builder().name(genie.getName()).email(genie.getEmail()).role(genie.getRole()).pwd(genie.getPwd()).build();
        userRepository.save(user);
        genieRepository.save(genie);
        return "Genie Registered Successfully";
    }

    // Register as Wishmaker(jobprovider)
    @PostMapping("/registerWishmaker")
    public String registerWishMaker(@RequestBody Wishmaker wishmaker) {

        wishmaker.setPwd(passwordEncoder.encode(wishmaker.getPwd()));
        wishmaker.setRole("ROLE_WISHMAKER");

        // make a object of user for the wishmaker also
        users user = users.builder().name(wishmaker.getName()).email(wishmaker.getEmail()).role(wishmaker.getRole()).pwd(wishmaker.getPwd()).build();
        userRepository.save(user);
        wishmakerRepository.save(wishmaker);
        return "Wishmaker Registered Successfully";
    }

    @PostMapping("/insertWish")
    public String insertWish(@RequestBody Wish wish,Authentication authentication) {
        // Authentication object contains the logged in user
        Wishmaker wishmaker = wishmakerRepository.findByEmail(authentication.getName()).get();
        if(wishmaker != null){
            wish.setWishmaker(wishmaker);
            wishRepository.save(wish);
            return "Wish Registered Successfully";
        }else{
            return "Wishmaker not found";
        }
    }

    @PostMapping("/acceptWish")
    public ResponseEntity<Wish> acceptWish(@RequestParam Long wishId,Authentication authentication) {
        Wish wish = wishRepository.findById(wishId).orElseThrow(() -> new RuntimeException("Wish not found"));
        Genie genie = genieRepository.findByEmail(authentication.getName()).orElseThrow(() -> new RuntimeException("Genie not found"));
        wish.getRegisteredUsers().add(genie);
        wishRepository.save(wish);
        return ResponseEntity.ok().body(wish);
    }


}
