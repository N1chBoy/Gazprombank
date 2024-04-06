package com.example.routeweb.services;

import com.example.routeweb.models.User;

import java.util.List;

public interface UserService {
    List<User> getAllUsers();
    User addUser(User user);
}
