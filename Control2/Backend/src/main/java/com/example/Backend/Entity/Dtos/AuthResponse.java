package com.example.Backend.Entity.Dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class AuthResponse {

    @JsonIgnore
    private String token;
    private String username;

    public AuthResponse(String token, String username){
        this.token = token;
        this.username = username;
    }

}
