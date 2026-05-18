package app.DTO.Group;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateGroupRequest {

    @NotNull(message = "Community code is required")
    private String communityCode;

    @NotBlank(message = "Group name is required")
    @Size(min = 2, max = 255, message = "Name must be between 2 and 255 characters")
    private String name;

    @Size(max = 1000, message = "Description must be less than 1000 characters")
    private String description;

    // Constructors
    public CreateGroupRequest() {}

    public CreateGroupRequest(String communityCode, String name, String description) {
        this.communityCode = communityCode;
        this.name = name;
        this.description = description;
    }

    // Getters and Setters
    public String getCommunityCode() { return communityCode; }
    public void setCommunityCode(String communityCode) { this.communityCode = communityCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
