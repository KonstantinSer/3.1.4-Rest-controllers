package ru.kata.spring.boot_security.demo.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.UserService;

import java.security.Principal;

@Controller
public class UserControllers {

    private final UserService userService;

    @Autowired
    public UserControllers(UserService userService) {
        this.userService = userService;
    }

    @GetMapping()
    public String login() {
        return "login";
    }

    @GetMapping("/user")
    public String userPage(Model model, Principal principal) {
        String email = principal.getName();

        User currentUser = userService.findByEmail(email);

        model.addAttribute("user", currentUser);

        return "user";
    }
}

