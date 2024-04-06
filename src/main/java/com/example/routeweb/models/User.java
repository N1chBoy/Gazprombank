package com.example.routeweb.models;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    //@Column(name = "id")
    private Long id_user;

    @Column(name = "username")
    private String login_user;

    @Column(name = "password")
    private String password_user;

    @ElementCollection(targetClass = roleEnum.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    private Set<roleEnum> roles;

    public User(String login_user, String password_user, Set<roleEnum> roles) {
        this.login_user = login_user;
        this.password_user = password_user;
        this.roles = roles;
    }

    public User() {

    }

    public Long getId_user() {
        return id_user;
    }

    public void setId_user(Long id_user) {
        this.id_user = id_user;
    }

    public String getLogin_user() {
        return login_user;
    }

    public void setLogin_user(String login_user) {
        this.login_user = login_user;
    }

    public String getPassword_user() {
        return password_user;
    }

    public void setPassword_user(String password_user) {
        this.password_user = password_user;
    }

    public Set<roleEnum> getRoles() {
        return roles;
    }

    public void setRoles(Set<roleEnum> roles) {
        this.roles = roles;
    }
}

