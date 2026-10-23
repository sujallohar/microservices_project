package com.govasset.identity.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serializable;
import java.util.Set;

@RedisHash("users")
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @Indexed
    private String username;

    private String passwordHash;
    private String fullName;
    private String email;
    private String department;
    private Set<Role> roles;
    private boolean active;
    private long createdAt;

    public User() {
    }

    public User(String id, String username, String passwordHash, String fullName, String email,
                String department, Set<Role> roles, boolean active, long createdAt) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.email = email;
        this.department = department;
        this.roles = roles;
        this.active = active;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public Set<Role> getRoles() { return roles; }
    public void setRoles(Set<Role> roles) { this.roles = roles; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String username;
        private String passwordHash;
        private String fullName;
        private String email;
        private String department;
        private Set<Role> roles;
        private boolean active = true;
        private long createdAt = System.currentTimeMillis();

        public Builder id(String id) { this.id = id; return this; }
        public Builder username(String username) { this.username = username; return this; }
        public Builder passwordHash(String passwordHash) { this.passwordHash = passwordHash; return this; }
        public Builder fullName(String fullName) { this.fullName = fullName; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder department(String department) { this.department = department; return this; }
        public Builder roles(Set<Role> roles) { this.roles = roles; return this; }
        public Builder active(boolean active) { this.active = active; return this; }
        public Builder createdAt(long createdAt) { this.createdAt = createdAt; return this; }

        public User build() {
            return new User(id, username, passwordHash, fullName, email, department, roles, active, createdAt);
        }
    }
}
