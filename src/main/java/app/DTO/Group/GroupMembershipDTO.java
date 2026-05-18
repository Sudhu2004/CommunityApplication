package app.DTO.Group;

import app.DTO.User.UserDTO;
import app.Database.MemberRole;
import app.Database.MembershipStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public class GroupMembershipDTO {
    private UUID id;
    private UserDTO user;
    private String groupCode;
    private String groupName;
    private MemberRole role;
    private MembershipStatus status;
    private LocalDateTime joinedAt;

    // Constructors
    public GroupMembershipDTO() {}

    public GroupMembershipDTO(UUID id, UserDTO user, String groupCode, String groupName,
                              MemberRole role, LocalDateTime joinedAt) {
        this.id = id;
        this.user = user;
        this.groupCode = groupCode;
        this.groupName = groupName;
        this.role = role;
        this.joinedAt = joinedAt;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UserDTO getUser() { return user; }
    public void setUser(UserDTO user) { this.user = user; }

    public String getGroupCode() { return groupCode; }
    public void setGroupCode(String groupCode) { this.groupCode = groupCode; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public MemberRole getRole() { return role; }
    public void setRole(MemberRole role) { this.role = role; }

    public MembershipStatus getStatus() { return status; }
    public void setStatus(MembershipStatus status) { this.status = status; }

    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }
}
