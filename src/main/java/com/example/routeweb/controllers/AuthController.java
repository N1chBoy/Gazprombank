package com.example.routeweb.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {
    @GetMapping("/authorization")
    public String HomeUser(Model model) {
        return "authorization";
    }
}