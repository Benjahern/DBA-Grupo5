package com.example.Backend.Entity.Dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class AuthResponse {

    @JsonIgnore
    private String token;
    private String username;
    private String role;

    public AuthResponse(String token, String username, String role){
        this.token = token;
        this.username = username;
        this.role = role;
    }

}
