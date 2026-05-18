package app.DTO.Group;

import app.DTO.User.UserDTO;

import java.time.LocalDateTime;

public class GroupDTO {
    private String communityCode;
    private String communityName;
    private String name;
    private String description;
    private Boolean onlyAdminsCanChat;
    private UserDTO createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int memberCount;
    private int eventCount;
    private String groupCode;

    // Constructors
    public GroupDTO() {}

    public GroupDTO(String communityCode, String communityName, String name,
                    String description, UserDTO createdBy, LocalDateTime createdAt,
                    LocalDateTime updatedAt, int memberCount, int eventCount, String groupCode) {
        this.communityCode = communityCode;
        this.communityName = communityName;
        this.name = name;
        this.description = description;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.memberCount = memberCount;
        this.eventCount = eventCount;
        this.groupCode = groupCode;
    }

    // Getters and Setters
    public String getCommunityCode() { return communityCode; }
    public void setCommunityCode(String communityCode) { this.communityCode = communityCode; }

    public String getCommunityName() { return communityName; }
    public void setCommunityName(String communityName) { this.communityName = communityName; }

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

    public int getEventCount() { return eventCount; }
    public void setEventCount(int eventCount) { this.eventCount = eventCount; }

    public String getGroupCode() { return groupCode; }
    public void setGroupCode(String groupCode) { this.groupCode = groupCode; }
}
