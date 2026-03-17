package ru.kata.spring.boot_security.demo.controller;


import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.UserService;

import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping()
    public String adminPage(Model model, Principal principal) {
        // 1. Данные для шапки
        User currentUser = userService.findByUsername(principal.getName());
        model.addAttribute("currentUser", currentUser);

        // 2. Данные для таблицы (Вкладка 1)
        model.addAttribute("users", userService.findAll());

        // 3. Пустой объект для формы (Вкладка 2)
        model.addAttribute("newUser", new User());

        // 4. Роли для выпадающего списка
        model.addAttribute("allRoles", userService.getAllRoles());

        return "admin"; // Имя файла в templates
    }


    @GetMapping("/addNewUser")
    public String showAddUserForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("allRoles", userService.getAllRoles());
        return "users-info";
    }
    @PostMapping("/addNewUser")
    public String saveNewUser(@ModelAttribute("newUser") User user,
                              @RequestParam("roleIds") List<Long> roleIds) {

        // Получаем все роли из базы через твой метод
        List<Role> allRoles = userService.getAllRoles();

        // Фильтруем список: оставляем только те роли, чьи ID совпали с выбранными в форме
        Set<Role> selectedRoles = allRoles.stream()
                .filter(role -> roleIds.contains(role.getId()))
                .collect(Collectors.toSet());

        // Устанавливаем готовый Set объектов Role пользователю
        user.setRoles(selectedRoles);

        // Сохраняем (пароль должен захешироваться внутри userService.save)
        userService.save(user);

        return "redirect:/admin";
    }


    @GetMapping("/updateInfo")
    public String showUpdateForm(@RequestParam("id") Long id, Model model) {
        User user = userService.findById(id);
        model.addAttribute("user", user);
        return "users-info";  // можно использовать тот же шаблон или отдельный
    }

    @PostMapping("/updateInfo")
    public String updateUser(@ModelAttribute User user) {
        userService.updateUser(user);
        return "redirect:/admin";
    }


    @PostMapping("/deleteUser")
    public String deleteUser(@RequestParam("id") Long id) {
        userService.delete(id);
        return "redirect:/admin";
    }
}
