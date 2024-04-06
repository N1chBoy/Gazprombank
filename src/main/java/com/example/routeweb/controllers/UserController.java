package com.example.routeweb.controllers;

import com.example.routeweb.models.User;
import com.example.routeweb.models.roleEnum;
import com.example.routeweb.repositiries.UserRepo;
import com.example.routeweb.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Collections;
import java.util.List;

@Controller
public class UserController {

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepo userRepo;

    @Autowired
    private UserService userService;

    @GetMapping("/users")
    public String listUsers(Model model) {
        List<User> users = userService.getAllUsers(); // Получаем всех пользователей
        model.addAttribute("users", users); // Добавляем список пользователей в модель
        return "users"; // Возвращаем имя представления, которое должно быть отображено
    }

    @GetMapping("/addUser")
    public String showAddClassForm(Model model) {
        User user = new User();
        model.addAttribute("user", user);
        return "addUser";
    }

    @PostMapping("/addUser")
    private String reg(
            @Valid @ModelAttribute("users") User user,
            Model model,
            BindingResult bindingResult
    ) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("errors", bindingResult.getAllErrors());
            return "addUser";
        }

        // Устанавливаем роль и шифруем пароль
        user.setRoles(Collections.singleton(roleEnum.EMPLOYEE));
        user.setPassword_user(passwordEncoder.encode(user.getPassword_user()));
        userRepo.save(user);

        return "redirect:/users";
    }
}
