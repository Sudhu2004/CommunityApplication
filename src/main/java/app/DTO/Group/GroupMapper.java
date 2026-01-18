package app.DTO.Group;

import app.Database.Group;
import app.Database.GroupMembership;
import app.DTO.User.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GroupMapper {

    private final UserMapper userMapper;

    @Autowired
    public GroupMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public GroupDTO toDTO(Group group) {
        if (group == null) {
            return null;
        }

        GroupDTO dto = new GroupDTO();
        dto.setId(group.getId());
        dto.setCommunityId(group.getCommunity().getId());
        dto.setCommunityName(group.getCommunity().getName());
        dto.setName(group.getName());
        dto.setDescription(group.getDescription());
        dto.setCreatedBy(userMapper.toDTO(group.getCreatedBy()));
        dto.setCreatedAt(group.getCreatedAt());
        dto.setUpdatedAt(group.getUpdatedAt());

        // Set counts
        dto.setMemberCount(group.getMemberships() != null ? group.getMemberships().size() : 0);
        dto.setEventCount(group.getEvents() != null ? group.getEvents().size() : 0);

        return dto;
    }

    public GroupMembershipDTO toMembershipDTO(GroupMembership membership) {
        if (membership == null) {
            return null;
        }

        GroupMembershipDTO dto = new GroupMembershipDTO();
        dto.setId(membership.getId());
        dto.setUser(userMapper.toDTO(membership.getUser()));
        dto.setGroupId(membership.getGroup().getId());
        dto.setGroupName(membership.getGroup().getName());
        dto.setRole(membership.getRole());
        dto.setJoinedAt(membership.getJoinedAt());

        return dto;
    }
}
