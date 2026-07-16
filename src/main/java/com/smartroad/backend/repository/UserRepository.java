package com.smartroad.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartroad.backend.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail(String email);

    User findByFullName(String fullName);

}