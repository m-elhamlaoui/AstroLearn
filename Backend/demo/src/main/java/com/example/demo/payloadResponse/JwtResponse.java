package com.example.demo.payloadResponse;


import lombok.Data;

import java.util.List;
@Data

public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private List<String> roles;
    private String email;

    public JwtResponse(String accessToken, Long id, List<String> roles, String email) {
        this.token = accessToken;
        this.id = id;
        this.email = email;
        this.roles = roles;
    }

    public String getAccessToken() {
        return token;
    }

    public void setAccessToken(String accessToken) {
        this.token = accessToken;
    }

    public String getTokenType() {
        return type;
    }

    public void setTokenType(String tokenType) {
        this.type = tokenType;
    }

}