package app.DTO.Group;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public class CreateGroupRequest {

    @NotNull(message = "Community ID is required")
    private UUID communityId;

    @NotBlank(message = "Group name is required")
    @Size(min = 2, max = 255, message = "Name must be between 2 and 255 characters")
    private String name;

    @Size(max = 1000, message = "Description must be less than 1000 characters")
    private String description;

    // Constructors
    public CreateGroupRequest() {}

    public CreateGroupRequest(UUID communityId, String name, String description) {
        this.communityId = communityId;
        this.name = name;
        this.description = description;
    }

    // Getters and Setters
    public UUID getCommunityId() { return communityId; }
    public void setCommunityId(UUID communityId) { this.communityId = communityId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
