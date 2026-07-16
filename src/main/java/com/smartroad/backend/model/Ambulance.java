package com.smartroad.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "ambulances")
public class Ambulance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String driverName;

    @Column(unique = true)
    private String email;

    private String phone;

    private String password;

    @Column(unique = true)
    private String vehicleNumber;

    private String vehicleType;

    private String hospitalName;

    private Double latitude;

    private Double longitude;

    private boolean online;

    private boolean available;

    public Ambulance() {
    }

    public Ambulance(Long id,
                      String driverName,
                      String email,
                      String phone,
                      String password,
                      String vehicleNumber,
                      String vehicleType,
                      String hospitalName,
                      Double latitude,
                      Double longitude,
                      boolean online,
                      boolean available) {

        this.id = id;
        this.driverName = driverName;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.vehicleNumber = vehicleNumber;
        this.vehicleType = vehicleType;
        this.hospitalName = hospitalName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.online = online;
        this.available = available;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    // Later we'll replace plain passwords with BCrypt hashing.
    public void setPassword(String password) {
        this.password = password;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}