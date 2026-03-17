package ru.kata.spring.boot_security.demo.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.kata.spring.boot_security.demo.DAO.RoleRepository;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.UserService;

import java.util.Set;

@Controller
public class UserControllers {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private RoleRepository roleRepository;


    @Autowired
    public UserControllers(UserService userService, PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;

    }

    @GetMapping("/register")
    public String getRegistrationPage(Model model) {
        model.addAttribute("username", new User());
        return "register";
    }

    @PostMapping("/register")
    public String creatUserAccount(@RequestParam String username,
                                   @RequestParam String lastname,
                                   @RequestParam String password,
                                   @RequestParam Integer age,
                                   @RequestParam String email) {
        String hashedPassword = passwordEncoder.encode(password);
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Role ROLE_USER not found!"));

        User user = new User(username, lastname, hashedPassword, age, email, Set.of(userRole));
        userService.save(user);
        return "redirect:/login";
    }

    @GetMapping()
    public String login() {
        return "login";
    }

    @GetMapping("/user")
    public String userPage(Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User currentUser = userService.findByUsername(username);

        model.addAttribute("user", currentUser);

        return "user";
    }
}
