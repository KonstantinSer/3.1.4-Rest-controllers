package ru.kata.spring.boot_security.demo.service;


import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kata.spring.boot_security.demo.DAO.RoleRepository;
import ru.kata.spring.boot_security.demo.DAO.UserRepository;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final RoleRepository roleRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public void newUser(User user, List<Long> roleIds) {

        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        } else {
            if (user.getId() == null) {
                throw new IllegalArgumentException("Пароль обязателен для нового пользователя");
            }
        }

        Set<Role> roles;
        if (roleIds != null && !roleIds.isEmpty()) {
            roles = new HashSet<>(roleRepository.findAllById(roleIds));
        } else {
            Role defaultRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("Default role ROLE_USER not found"));
            roles = new HashSet<>();
            roles.add(defaultRole);
        }

        user.setRoles(roles);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        userRepository.deleteById(id);
    }


    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        Hibernate.initialize(user.getRoles());
        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        User user = userRepository.findByEmail(email);
        if (user != null) {
            return user;
        } else {
            throw new UsernameNotFoundException("User not found");
        }
    }


    @Override
    @Transactional
    public void updateUser(User user, List<Long> roleIds) {
        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден с id: " + user.getId()));

        existingUser.setEmail(user.getEmail());
        existingUser.setAge(user.getAge());
        existingUser.setLastName(user.getLastName());
        existingUser.setFirstName(user.getFirstName());

        Set<Role> roles;

        if (roleIds != null && !roleIds.isEmpty()) {
            roles = new HashSet<>(roleRepository.findAllById(roleIds));
        } else {
            Role defaultRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("Default role ROLE_USER not found"));
            roles = new HashSet<>();
            roles.add(defaultRole);
        }
        existingUser.setRoles(roles);

        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            existingUser.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        }

        userRepository.save(existingUser);

    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }
}

