package app.DTO.Group;

import app.DTO.User.UserDTO;
import app.Database.MemberRole;
import java.time.LocalDateTime;
import java.util.UUID;

public class GroupMembershipDTO {
    private UUID id;
    private UserDTO user;
    private UUID groupId;
    private String groupName;
    private MemberRole role;
    private LocalDateTime joinedAt;

    // Constructors
    public GroupMembershipDTO() {}

    public GroupMembershipDTO(UUID id, UserDTO user, UUID groupId, String groupName,
                              MemberRole role, LocalDateTime joinedAt) {
        this.id = id;
        this.user = user;
        this.groupId = groupId;
        this.groupName = groupName;
        this.role = role;
        this.joinedAt = joinedAt;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UserDTO getUser() { return user; }
    public void setUser(UserDTO user) { this.user = user; }

    public UUID getGroupId() { return groupId; }
    public void setGroupId(UUID groupId) { this.groupId = groupId; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public MemberRole getRole() { return role; }
    public void setRole(MemberRole role) { this.role = role; }

    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }
}
