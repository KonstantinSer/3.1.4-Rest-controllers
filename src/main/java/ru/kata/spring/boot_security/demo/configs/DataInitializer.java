package ru.kata.spring.boot_security.demo.configs;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.kata.spring.boot_security.demo.DAO.RoleRepository;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.UserService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private RoleRepository roleRepository;

    private UserService userService;
@Autowired
    public DataInitializer(RoleRepository roleRepository, UserService userService) {
        this.roleRepository = roleRepository;
        this.userService = userService;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 1. Сохраняем роли, если их нет
        if (!roleRepository.existsByName("ROLE_USER")) {
            roleRepository.save(new Role("ROLE_USER"));
        }
        if (!roleRepository.existsByName("ROLE_ADMIN")) {
            roleRepository.save(new Role("ROLE_ADMIN"));
        }

        // 2. Проверяем админа
        if (userService.findByUsername("admin@mail.com") == null) {
            User admin = new User();
            admin.setFirstName("Admin");
            admin.setLastName("Adminov");
            admin.setEmail("admin@mail.com");
            admin.setAge(30);
            admin.setPassword("admin");

            // 3. Достаем роли по отдельности, чтобы не запутаться
            Role rAdmin = roleRepository.findByName("ROLE_ADMIN");
            Role rUser = roleRepository.findByName("ROLE_USER");

            Set<Role> adminRoles = new HashSet<>();
            adminRoles.add(rAdmin);
            adminRoles.add(rUser);

            admin.setRoles(adminRoles); // Теперь тут точно ДВЕ роли

            userService.save(admin);
        }
    }
    }