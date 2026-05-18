package app.DTO.Community;
import app.DTO.User.UserDTO;
import app.Database.DatabaseType;
import app.Service.GlobalShortCodeService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

public class CommunityDTO {
    private String name;
    private String description;
    private Boolean onlyAdminsCanChat;
    private UserDTO createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int memberCount;
    private int groupCount;
    private String communityCode;

    @Autowired
    private GlobalShortCodeService globalShortCodeService;

    // Constructors
    public CommunityDTO() {}

    public CommunityDTO(String name, String description, UserDTO createdBy,
                        LocalDateTime createdAt, LocalDateTime updatedAt,
                        int memberCount, int groupCount, String communityCode) {
        this.name = name;
        this.description = description;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.memberCount = memberCount;
        this.groupCount = groupCount;
        this.communityCode = communityCode;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getOnlyAdminsCanChat() { return onlyAdminsCanChat; }
    public void setOnlyAdminsCanChat(Boolean onlyAdminsCanChat) { this.onlyAdminsCanChat = onlyAdminsCanChat; }

    public UserDTO getCreatedBy() { return createdBy; }
    public void setCreatedBy(UserDTO createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public int getMemberCount() { return memberCount; }
    public void setMemberCount(int memberCount) { this.memberCount = memberCount; }

    public int getGroupCount() { return groupCount; }
    public void setGroupCount(int groupCount) { this.groupCount = groupCount; }

    public String getCommunityCode() {
        return communityCode;
    }

    public void setCommunityCode(String communityCode) {
        this.communityCode = communityCode;
    }
}
