package app.DTO.Community;

import app.DTO.User.UserDTO;
import app.Database.DatabaseType;
import app.Database.MemberRole;
import app.Database.MembershipStatus;
import app.Service.GlobalShortCodeService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.UUID;

public class CommunityMembershipDTO {
    private UUID id;
    private UserDTO user;
    private MemberRole role;
    private MembershipStatus status;
    private LocalDateTime joinedAt;
    private String communityCode;

    @Autowired
    private GlobalShortCodeService globalShortCodeService;

    // Constructors
    public CommunityMembershipDTO() {}

    public CommunityMembershipDTO(UUID id, UserDTO user,
                                  MemberRole role, LocalDateTime joinedAt, String communityCode) {
        this.id = id;
        this.user = user;
        this.role = role;
        this.joinedAt = joinedAt;
        this.communityCode = communityCode;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UserDTO getUser() { return user; }
    public void setUser(UserDTO user) { this.user = user; }

    public MemberRole getRole() { return role; }
    public void setRole(MemberRole role) { this.role = role; }

    public MembershipStatus getStatus() { return status; }
    public void setStatus(MembershipStatus status) { this.status = status; }

    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }

    public String getCommunityCode() {
        return communityCode;
    }

    public void setCommunityCode(String communityCode) {
        this.communityCode = communityCode;
    }
}
