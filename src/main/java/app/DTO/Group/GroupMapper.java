package app.DTO.Group;

import app.Database.DatabaseType;
import app.Database.Group;
import app.Database.GroupMembership;
import app.DTO.User.UserMapper;
import app.Service.GlobalShortCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GroupMapper {

    private final UserMapper userMapper;
    private final GlobalShortCodeService globalShortCodeService;

    @Autowired
    public GroupMapper(UserMapper userMapper, GlobalShortCodeService globalShortCodeService) {
        this.userMapper = userMapper;
        this.globalShortCodeService = globalShortCodeService;
    }

    public GroupDTO toDTO(Group group) {
        if (group == null) {
            return null;
        }

        GroupDTO dto = new GroupDTO();
        dto.setCommunityCode(globalShortCodeService.getShortCode(DatabaseType.COMMUNITY, group.getCommunity().getId()));
        dto.setCommunityName(group.getCommunity().getName());
        dto.setName(group.getName());
        dto.setDescription(group.getDescription());
        dto.setOnlyAdminsCanChat(group.getOnlyAdminsCanChat());
        dto.setCreatedBy(userMapper.toDTO(group.getCreatedBy()));
        dto.setCreatedAt(group.getCreatedAt());
        dto.setUpdatedAt(group.getUpdatedAt());

        // Set counts
        dto.setMemberCount(group.getMemberships() != null ? group.getMemberships().size() : 0);
        dto.setEventCount(group.getEvents() != null ? group.getEvents().size() : 0);

        dto.setGroupCode(globalShortCodeService.getShortCode(DatabaseType.GROUP, group.getId()));

        return dto;
    }

    public GroupMembershipDTO toMembershipDTO(GroupMembership membership) {
        if (membership == null) {
            return null;
        }

        GroupMembershipDTO dto = new GroupMembershipDTO();
        dto.setId(membership.getId());
        dto.setUser(userMapper.toDTO(membership.getUser()));
        dto.setGroupCode(globalShortCodeService.getShortCode(DatabaseType.GROUP, membership.getGroup().getId()));
        dto.setGroupName(membership.getGroup().getName());
        dto.setRole(membership.getRole());
        dto.setStatus(membership.getStatus());
        dto.setJoinedAt(membership.getJoinedAt());

        return dto;
    }
}
