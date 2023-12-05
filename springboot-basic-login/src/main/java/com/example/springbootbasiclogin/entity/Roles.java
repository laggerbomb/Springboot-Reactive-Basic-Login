package com.example.springbootbasiclogin.entity;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@Table(name="roles")
public class Roles {

    @Id
    @Column("role_id")
    private int roleId;

    @Column("user_id")
    private int userId;

    @Column("role")
    private String role;
}
