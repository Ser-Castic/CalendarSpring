package com.theironyard.entities;

import javax.persistence.*;
import java.util.List;

/**
 * Created by Sam on 1/25/17
 */

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue
    int id;

    @Column(nullable = false, unique = true) // Cannot have two users with the same name
    String name;

    public User() {
    }

    public User(String name) {
        this.name = name;
    }
}
