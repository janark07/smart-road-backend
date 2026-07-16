package com.smartroad.backend.dto;

public class AmbulanceLoginRequest {

    private String email;
    private String password;

    public AmbulanceLoginRequest() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}