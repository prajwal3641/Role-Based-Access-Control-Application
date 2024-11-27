package com.core.config;

import com.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.core.model.users;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GenieUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        users user = userRepository.findByEmail(username).orElseThrow(()-> new UsernameNotFoundException("User not founddd"));
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(user.getRole());
        List<GrantedAuthority> authorities = List.of(authority);
        return new User(user.getEmail(), user.getPwd(), authorities);
    }
}
