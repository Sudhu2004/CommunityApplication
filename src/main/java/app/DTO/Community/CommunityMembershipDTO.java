package app.DTO.Community;

import app.DTO.User.UserDTO;
import app.Database.MemberRole;
import java.time.LocalDateTime;
import java.util.UUID;

public class CommunityMembershipDTO {
    private UUID id;
    private UserDTO user;
    private UUID communityId;
    private MemberRole role;
    private LocalDateTime joinedAt;

    // Constructors
    public CommunityMembershipDTO() {}

    public CommunityMembershipDTO(UUID id, UserDTO user, UUID communityId,
                                  MemberRole role, LocalDateTime joinedAt) {
        this.id = id;
        this.user = user;
        this.communityId = communityId;
        this.role = role;
        this.joinedAt = joinedAt;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UserDTO getUser() { return user; }
    public void setUser(UserDTO user) { this.user = user; }

    public UUID getCommunityId() { return communityId; }
    public void setCommunityId(UUID communityId) { this.communityId = communityId; }

    public MemberRole getRole() { return role; }
    public void setRole(MemberRole role) { this.role = role; }

    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }
}
