package app.DTO.User;

import app.Database.DatabaseType;
import app.Service.GlobalShortCodeService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

public class UserDTO {
    private String email;
    private String name;
    private String phone;
    private String profilePhotoUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isActive;
    private String userCode;

    @Autowired
    private GlobalShortCodeService globalShortCodeService;

    // Constructors
    public UserDTO() {}

    public UserDTO(String email, String name, String phone,
                   String profilePhotoUrl, LocalDateTime createdAt, LocalDateTime updatedAt, Boolean isActive, String userCode) {
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.profilePhotoUrl = profilePhotoUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isActive = isActive;
        this.userCode = userCode;
    }

    // Getters and Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getProfilePhotoUrl() { return profilePhotoUrl; }
    public void setProfilePhotoUrl(String profilePhotoUrl) { this.profilePhotoUrl = profilePhotoUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }
}
