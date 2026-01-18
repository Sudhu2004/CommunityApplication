package app.DTO.Group;

import app.Database.MemberRole;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class AddMemberRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "Role is required")
    private MemberRole role;

    // Constructors
    public AddMemberRequest() {}

    public AddMemberRequest(UUID userId, MemberRole role) {
        this.userId = userId;
        this.role = role;
    }

    // Getters and Setters
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public MemberRole getRole() { return role; }
    public void setRole(MemberRole role) { this.role = role; }
}
