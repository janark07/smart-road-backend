package com.smartroad.backend.controller;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.smartroad.backend.model.User;
import com.smartroad.backend.repository.UserRepository;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173")
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

    // LOGIN METHOD
    @PostMapping("/login")
    public User loginUser(@RequestBody User user) {

        User existingUser =
            userRepository.findByEmail(user.getEmail());
           
        if(existingUser != null) {
            System.out.println("Entered Password: " + user.getPassword());
            System.out.println("DB Password: " + existingUser.getPassword());
        }
        
        if (existingUser != null &&
            existingUser.getPassword().equals(user.getPassword())) {

            return existingUser;
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

