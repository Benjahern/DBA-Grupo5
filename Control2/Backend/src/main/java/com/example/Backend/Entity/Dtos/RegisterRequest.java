package com.example.Backend.Entity.Dtos;

import lombok.Data;

@Data
public class RegisterRequest {

    private String username;
    private String email;
    private String password;
    private Double latitude;
    private Double longitude;

}
