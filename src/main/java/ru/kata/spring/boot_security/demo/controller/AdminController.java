package ru.kata.spring.boot_security.demo.controller;


import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.kata.spring.boot_security.demo.DAO.RoleRepository;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.UserService;

import javax.validation.Valid;
import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final RoleRepository roleRepository;


    public AdminController(UserService userService, RoleRepository roleRepository) {

        this.userService = userService;
        this.roleRepository = roleRepository;
    }

    @GetMapping()
    public String adminPage(Model model, Principal principal) {
        User currentUser = userService.findByEmail(principal.getName());
        model.addAttribute("currentUser", currentUser);

        model.addAttribute("users", userService.findAll());

        model.addAttribute("newUser", new User());

        model.addAttribute("allRoles", roleRepository.findAll());

        return "admin";
    }

    @GetMapping("/addNewUser")
    public String showAddUserForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("allRoles", roleRepository.findAll());
        return "users-info";
    }

    @PostMapping("/addNewUser")
    public String addUser(@ModelAttribute @Valid User user,
                          @RequestParam(value = "roleIds", required = false) List<Long> roleIds) {

        Set<Role> roles;

        if (roleIds != null && !roleIds.isEmpty()) {

            roles = new HashSet<>(roleRepository.findAllById(roleIds));
        } else {

            Role defaultRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("Default role ROLE_USER not found"));
            roles = Set.of(defaultRole);
        }

        user.setRoles(roles);
        userService.save(user);
        return "redirect:/admin";
    }

    @GetMapping("/updateInfo")
    public String showUpdateForm(@RequestParam("id") Long id, Model model) {
        User user = userService.findById(id);
        model.addAttribute("user", user);
        model.addAttribute("allRoles", roleRepository.findAll());

        return "redirect:/admin";
    }

    @PostMapping("/updateInfo")
    public String updateUser(@ModelAttribute User user,
                             @RequestParam(value = "roleIds", required = false) List<Long> roleIds) {

        User existingUser = userService.findById(user.getId());

        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            user.setPassword(existingUser.getPassword());
        } else {
            user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        }

        if (roleIds != null && !roleIds.isEmpty()) {
            user.setRoles(new HashSet<>(roleRepository.findAllById(roleIds)));
        } else {
            roleRepository.findByName("ROLE_USER")
                    .ifPresent(r -> user.setRoles(Set.of(r)));
        }

        userService.save(user);
        return "redirect:/admin";
    }

    @PostMapping("/deleteUser")
    public String deleteUser(@RequestParam("id") Long id) {
        userService.delete(id);
        return "redirect:/admin";
    }

}
