package com.hannomed.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "employees")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    private String role;

    @Column(name = "fcm_token")
    private String fcmToken;

    @Column(name = "profile_photo_url")
    private String profilePhotoUrl;

    private String position;

    @Column(name = "tour_number")
    private String tourNumber;

    private String standort;

    @Column(name = "deleted_at")
    private java.time.LocalDateTime deletedAt;
}