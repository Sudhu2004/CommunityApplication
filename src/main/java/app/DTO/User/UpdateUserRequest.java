package app.DTO.User;

import jakarta.validation.constraints.Size;

public class UpdateUserRequest {

    @Size(min = 2, max = 255, message = "Name must be between 2 and 255 characters")
    private String name;

    @Size(max = 20, message = "Phone must be less than 20 characters")
    private String phone;

    private String profilePhotoUrl;

    // Constructors
    public UpdateUserRequest() {}

    public UpdateUserRequest(String name, String phone, String profilePhotoUrl) {
        this.name = name;
        this.phone = phone;
        this.profilePhotoUrl = profilePhotoUrl;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getProfilePhotoUrl() { return profilePhotoUrl; }
    public void setProfilePhotoUrl(String profilePhotoUrl) { this.profilePhotoUrl = profilePhotoUrl; }
}
