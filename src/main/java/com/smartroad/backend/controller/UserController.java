package com.smartroad.backend.controller;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartroad.backend.model.User;
import com.smartroad.backend.repository.UserRepository;

@RestController
@RequestMapping("/api/users")
public class UserController {
	
	public UserController() {
	    System.out.println("UserController Loaded");
	}

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public Object registerUser(@RequestBody User user) {

        User existingUser =
                userRepository.findByEmail(user.getEmail());

        if(existingUser != null) {
            return "Email already exists";
        }

        return userRepository.save(user);
    }

    // ADD HERE 👇
    @GetMapping("/test")
    public String test() {
        return "Login API Loaded";
    }

    // LOGIN METHOD with timeout to fail fast if DB is unreachable
    @PostMapping("/login")
    public User loginUser(@RequestBody User user) {
        try {
            java.util.concurrent.CompletableFuture<User> fut =
                    java.util.concurrent.CompletableFuture.supplyAsync(() -> userRepository.findByEmail(user.getEmail()));

            User existingUser = fut.get(3, java.util.concurrent.TimeUnit.SECONDS);

            if(existingUser != null) {
                System.out.println("Entered Password: " + user.getPassword());
                System.out.println("DB Password: " + existingUser.getPassword());
            }

            if (existingUser != null && existingUser.getPassword().equals(user.getPassword())) {
                return existingUser;
            }

        } catch (Exception e) {
            System.err.println("Login attempt failed or timed out: " + e.getMessage());
            // fall through and return null for failed login
        }

        return null;
    }
    
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
    }
    
    @PutMapping("/{id}/promote")
    public User promoteUser(@PathVariable Long id) {

        User user =
            userRepository.findById(id).orElse(null);

        if(user != null) {

            user.setRole("ADMIN");

            return userRepository.save(user);
        }

        return null;
    }
    
    @GetMapping("/{id}")
    public User getUserById(
            @PathVariable Long id) {

        return userRepository
                .findById(id)
                .orElse(null);
    }
    
    @PutMapping("/{id}")
    public User updateUser(
            @PathVariable Long id,
            @RequestBody User updatedUser) {

        User user =
            userRepository.findById(id)
                          .orElse(null);

        if(user != null) {

            user.setPhone(updatedUser.getPhone());
            user.setCity(updatedUser.getCity());

            return userRepository.save(user);
        }

        return null;
    }
    }

