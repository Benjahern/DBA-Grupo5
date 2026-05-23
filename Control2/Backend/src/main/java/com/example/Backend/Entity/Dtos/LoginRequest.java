package com.example.Backend.Entity.Dtos;

import lombok.Data;

@Data
public class LoginRequest {

    private String username;
    private String password;
}
