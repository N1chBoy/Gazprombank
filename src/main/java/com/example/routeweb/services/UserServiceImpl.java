package com.example.routeweb.services;

import com.example.routeweb.models.User;
import com.example.routeweb.repositiries.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepo userRepository; // Предполагаем, что у вас есть JPA репозиторий

    @Autowired
    public UserServiceImpl(UserRepo userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll(); // Предполагаем, что метод findAll() существует в UserRepository
    }

    @Override
    public User addUser(User user) {
        return userRepository.save(user); // Сохраняем пользователя в базу данных
    }
}

