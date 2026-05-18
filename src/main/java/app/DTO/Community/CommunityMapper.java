package app.DTO.Community;

import app.Database.Community;
import app.Database.CommunityMembership;
import app.DTO.User.UserMapper;
import app.Database.DatabaseType;
import app.Service.GlobalShortCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommunityMapper {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private GlobalShortCodeService globalShortCodeService;

    public CommunityDTO toDTO(Community community) {
        if (community == null) {
            return null;
        }

        CommunityDTO dto = new CommunityDTO();
        dto.setName(community.getName());
        dto.setDescription(community.getDescription());
        dto.setOnlyAdminsCanChat(community.getOnlyAdminsCanChat());
        dto.setCreatedBy(userMapper.toDTO(community.getCreatedBy()));
        dto.setCreatedAt(community.getCreatedAt());
        dto.setUpdatedAt(community.getUpdatedAt());

        // Set counts
        dto.setMemberCount(community.getMemberships() != null ? community.getMemberships().size() : 0);
        dto.setGroupCount(community.getGroups() != null ? community.getGroups().size() : 0);

        dto.setCommunityCode(globalShortCodeService.getShortCode(DatabaseType.COMMUNITY, community.getId()));

        return dto;
    }

    public CommunityMembershipDTO toMembershipDTO(CommunityMembership membership) {
        if (membership == null) {
            return null;
        }

        CommunityMembershipDTO dto = new CommunityMembershipDTO();
        dto.setId(membership.getId());
        dto.setUser(userMapper.toDTO(membership.getUser()));
        dto.setRole(membership.getRole());
        dto.setStatus(membership.getStatus());
        dto.setJoinedAt(membership.getJoinedAt());
        dto.setCommunityCode(globalShortCodeService.getShortCode(DatabaseType.COMMUNITY, membership.getCommunity().getId()));

        return dto;
    }
}
