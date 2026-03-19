package ru.kata.spring.boot_security.demo.controller;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.UserService;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {

        this.userService = userService;
    }

    @GetMapping()
    public String adminPage(Model model, Principal principal) {
        User currentUser = userService.findByEmail(principal.getName());
        model.addAttribute("currentUser", currentUser);

        model.addAttribute("users", userService.findAll());

        model.addAttribute("newUser", new User());

        model.addAttribute("allRoles", userService.getAllRoles());

        return "admin";
    }

    @GetMapping("/addNewUser")
    public String showAddUserForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("allRoles", userService.getAllRoles());
        return "update-user";
    }

    @PostMapping("/addNewUser")
    public String addUser(@ModelAttribute @Valid User user,
                          @RequestParam(value = "roleIds", required = false) List<Long> roleIds) {
        userService.newUser(user, roleIds);
        return "redirect:/admin";
    }

    @GetMapping("/updateUser")
    public String updateForm(@RequestParam("id") Long id, Model model) {
        User user = userService.findById(id);
        model.addAttribute("user", user);
        model.addAttribute("allRoles", userService.getAllRoles());

        return "redirect:/admin";
    }

    @PostMapping("/updateUser")
    public String updateUser(@ModelAttribute User user,
                             @RequestParam(value = "roleIds", required = false) List<Long> roleIds) {
        userService.updateUser(user, roleIds);
        return "redirect:/admin";
    }


    @PostMapping("/deleteUser")
    public String deleteUser(@RequestParam("id") Long id) {
        userService.delete(id);
        return "redirect:/admin";
    }

}
