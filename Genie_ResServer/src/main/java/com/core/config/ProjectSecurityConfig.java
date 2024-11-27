package com.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class ProjectSecurityConfig {



    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception{

        // to accept jwt tokens and convert it into authentication object
        JwtAuthenticationConverter jwtAuthenticationConvertor = new JwtAuthenticationConverter();
        jwtAuthenticationConvertor.setJwtGrantedAuthoritiesConverter(new RoleConvertor());

        http.authorizeHttpRequests((req)->req.requestMatchers("/registerGenie","/registerWishmaker","/getWishes","/error").permitAll()
                .requestMatchers("/insertWish","/getWishmaker","/getAllWishesByWishMakerEmail").hasRole("WISHMAKER")
                .requestMatchers("/getGenie","/getAllWishByGenieEmail","/acceptWish").hasRole("GENIE"));
        http.sessionManagement(sc->sc.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // since not using the UI application !!
        http.csrf(sdf->sdf.disable());

        http.oauth2ResourceServer(orc->orc.jwt(jwt->jwt.jwtAuthenticationConverter(jwtAuthenticationConvertor)));
        return http.build();
    }


    // to encode the password in the database
    // bydefault, password is stored in bcrypt format
    @Bean
    public PasswordEncoder passwordEncoder(){
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
