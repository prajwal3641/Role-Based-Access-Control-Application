package com.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
public class Wish {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long wish_id;

    private String wish_name;
    private String description;

    private String status;

    private String location;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "wishmaker_id")
    @JsonIgnore
    private Wishmaker wishmaker;

    // saving/updating, one entity can save/update other , but deleting one cannnot delete other
    @ManyToMany(cascade = {CascadeType.PERSIST,CascadeType.MERGE},fetch = FetchType.EAGER)
    @JoinTable(
            name = "wish_user",
            joinColumns = @JoinColumn(name = "wish_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
//    @JsonIgnoreProperties("wishes")// Ignore the back-reference to prevent recursion
    // it Genie json will ignore the wishes field
    @JsonIgnore
    private Set<Genie> registeredUsers = new HashSet<>();

    public void removeGenie(Genie genie) {
        this.registeredUsers.remove(genie);
    }

}
