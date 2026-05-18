package app.DTO.Group;

import app.Database.MemberRole;
import jakarta.validation.constraints.NotNull;

public class AddMemberRequest {

    @NotNull(message = "User code is required")
    private String userCode;

    @NotNull(message = "Role is required")
    private MemberRole role;

    // Constructors
    public AddMemberRequest() {}

    public AddMemberRequest(String userCode, MemberRole role) {
        this.userCode = userCode;
        this.role = role;
    }

    // Getters and Setters
    public String getUserCode() { return userCode; }
    public void setUserCode(String userCode) { this.userCode = userCode; }

    public MemberRole getRole() { return role; }
    public void setRole(MemberRole role) { this.role = role; }
}
