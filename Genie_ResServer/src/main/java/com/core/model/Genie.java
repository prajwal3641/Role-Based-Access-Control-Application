package com.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
public class Genie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String email;

    @Column(name="phone_no")
    private String phoneNo;

    private String city;

    private String description;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String pwd;

    @JsonIgnore
    private String role;


    @ManyToMany(mappedBy = "registeredUsers", cascade = {CascadeType.PERSIST,CascadeType.MERGE})
    private Set<Wish> wishes = new HashSet<>();

}
