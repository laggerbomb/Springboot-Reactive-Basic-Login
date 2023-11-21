package com.example.springbootbasiclogin.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@Table(name="users")
public class User {

    // define fields
    @Id
    @Column("id")
    private int id;

    @Column("first_name")
    private String firstName;

    @Column("last_name")
    private String lastName;

    @Column("email")
    private String email;

    @Column("about")
    private String about;

    @Column("roles")
    private String roles;

    @Column("languages")
    private String languages;

    @Column("skills")
    private String skills;

    @Column("projects_experiences")
    private String projectsAndExperiences;

    @Column("assignments")
    private String assignments;

    @Column("profile_pic")
    private String profilePic;
}
