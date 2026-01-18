package app.DTO.Community;

import app.Database.Community;
import app.Database.CommunityMembership;
import app.DTO.User.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommunityMapper {

    @Autowired
    private UserMapper userMapper;

    public CommunityDTO toDTO(Community community) {
        if (community == null) {
            return null;
        }

        CommunityDTO dto = new CommunityDTO();
        dto.setId(community.getId());
        dto.setName(community.getName());
        dto.setDescription(community.getDescription());
        dto.setCreatedBy(userMapper.toDTO(community.getCreatedBy()));
        dto.setCreatedAt(community.getCreatedAt());
        dto.setUpdatedAt(community.getUpdatedAt());

        // Set counts
        dto.setMemberCount(community.getMemberships() != null ? community.getMemberships().size() : 0);
        dto.setGroupCount(community.getGroups() != null ? community.getGroups().size() : 0);

        return dto;
    }

    public CommunityMembershipDTO toMembershipDTO(CommunityMembership membership) {
        if (membership == null) {
            return null;
        }

        CommunityMembershipDTO dto = new CommunityMembershipDTO();
        dto.setId(membership.getId());
        dto.setUser(userMapper.toDTO(membership.getUser()));
        dto.setCommunityId(membership.getCommunity().getId());
        dto.setRole(membership.getRole());
        dto.setJoinedAt(membership.getJoinedAt());

        return dto;
    }
}
