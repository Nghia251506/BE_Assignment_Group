package com.tns.newscrawler.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ADMIN, EDITOR, USER...
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    // Quản trị viên, Biên tập viên...
    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String description;


    // ManyToMany với Permission qua role_permission
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "permistion_role",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();
}
