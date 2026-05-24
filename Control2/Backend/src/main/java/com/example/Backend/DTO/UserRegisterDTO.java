package com.example.Backend.DTO;

import lombok.Data;

@Data
public class UserRegisterDTO {
    private String name;
    private String email;
    private String password;
    private Double latitude;
    private Double longitude;
}