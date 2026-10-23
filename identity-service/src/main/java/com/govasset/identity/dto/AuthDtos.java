package com.govasset.identity.dto;

import com.govasset.identity.model.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public class AuthDtos {

    public static class LoginRequest {
        @NotBlank(message = "Username cannot be blank")
        private String username;

        @NotBlank(message = "Password cannot be blank")
        private String password;

        public LoginRequest() {}
        public LoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class RegisterRequest {
        @NotBlank(message = "Username is required")
        private String username;

        @NotBlank(message = "Password is required")
        private String password;

        @NotBlank(message = "Full name is required")
        private String fullName;

        private String email;
        private String department;

        @NotEmpty(message = "At least one role is required")
        private Set<Role> roles;

        public RegisterRequest() {}

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        public Set<Role> getRoles() { return roles; }
        public void setRoles(Set<Role> roles) { this.roles = roles; }
    }

    public static class AuthResponse {
        private String token;
        private String tokenType;
        private long expiresInMs;
        private String userId;
        private String username;
        private String fullName;
        private String department;
        private Set<Role> roles;

        public AuthResponse() {}

        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        public String getTokenType() { return tokenType; }
        public void setTokenType(String tokenType) { this.tokenType = tokenType; }
        public long getExpiresInMs() { return expiresInMs; }
        public void setExpiresInMs(long expiresInMs) { this.expiresInMs = expiresInMs; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        public Set<Role> getRoles() { return roles; }
        public void setRoles(Set<Role> roles) { this.roles = roles; }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private AuthResponse res = new AuthResponse();
            public Builder token(String t) { res.token = t; return this; }
            public Builder tokenType(String tt) { res.tokenType = tt; return this; }
            public Builder expiresInMs(long exp) { res.expiresInMs = exp; return this; }
            public Builder userId(String id) { res.userId = id; return this; }
            public Builder username(String u) { res.username = u; return this; }
            public Builder fullName(String f) { res.fullName = f; return this; }
            public Builder department(String d) { res.department = d; return this; }
            public Builder roles(Set<Role> r) { res.roles = r; return this; }
            public AuthResponse build() { return res; }
        }
    }

    public static class TokenValidationResponse {
        private boolean valid;
        private String userId;
        private String username;
        private String department;
        private Set<String> roles;
        private String message;

        public TokenValidationResponse() {}

        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        public Set<String> getRoles() { return roles; }
        public void setRoles(Set<String> roles) { this.roles = roles; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private TokenValidationResponse res = new TokenValidationResponse();
            public Builder valid(boolean v) { res.valid = v; return this; }
            public Builder userId(String id) { res.userId = id; return this; }
            public Builder username(String u) { res.username = u; return this; }
            public Builder department(String d) { res.department = d; return this; }
            public Builder roles(Set<String> r) { res.roles = r; return this; }
            public Builder message(String m) { res.message = m; return this; }
            public TokenValidationResponse build() { return res; }
        }
    }

    public static class LogoutRequest {
        @NotBlank(message = "Token is required")
        private String token;
        public LogoutRequest() {}
        public LogoutRequest(String token) { this.token = token; }
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
    }

    public static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;

        public ApiResponse() {}
        public ApiResponse(boolean success, String message, T data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public T getData() { return data; }
        public void setData(T data) { this.data = data; }

        public static <T> ApiResponse<T> ok(String message, T data) {
            return new ApiResponse<>(true, message, data);
        }

        public static <T> ApiResponse<T> fail(String message) {
            return new ApiResponse<>(false, message, null);
        }
    }
}
