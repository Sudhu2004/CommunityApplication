package app.Service;

import app.DTO.Group.AddMemberRequest;
import app.DTO.Group.*;
import app.Database.*;
import app.DTO.Group.GroupMapper;
import app.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GroupService {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMembershipRepository membershipRepository;

    @Autowired
    private CommunityRepository communityRepository;

    @Autowired
    private CommunityMembershipRepository communityMembershipRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupMapper groupMapper;

    /**
     * Create a new group
     */
    @Transactional
    public GroupDTO createGroup(UUID creatorId, CreateGroupRequest request) {
        // Find the creator
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + creatorId));

        // Find the community
        Community community = communityRepository.findById(request.getCommunityId())
                .orElseThrow(() -> new RuntimeException("Community not found with id: " + request.getCommunityId()));

        // Check if user is a member of the community
        if (!communityMembershipRepository.existsByUserIdAndCommunityId(creator.getId(), community.getId())) {
            throw new RuntimeException("You must be a member of the community to create a group");
        }

        // Create group
        Group group = new Group();
        group.setCommunity(community);
        group.setName(request.getName());
        group.setDescription(request.getDescription());
        group.setCreatedBy(creator);

        // Save group
        Group savedGroup = groupRepository.save(group);

        // Automatically add creator as OWNER
        GroupMembership creatorMembership = new GroupMembership();
        creatorMembership.setGroup(savedGroup);
        creatorMembership.setUser(creator);
        creatorMembership.setRole(MemberRole.OWNER);
        membershipRepository.save(creatorMembership);

        return groupMapper.toDTO(savedGroup);
    }

    /**
     * Get group by ID
     */
    public GroupDTO getGroupById(UUID groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));

        return groupMapper.toDTO(group);
    }

    /**
     * Get all groups in a community
     */
    public List<GroupDTO> getGroupsByCommunity(UUID communityId) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new RuntimeException("Community not found with id: " + communityId));

        List<Group> groups = groupRepository.findByCommunityIdOrderByCreatedAtDesc(communityId);
        return groups.stream()
                .map(groupMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get groups created by a user
     */
    public List<GroupDTO> getGroupsByCreator(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        List<Group> groups = groupRepository.findByCreatedBy(user);
        return groups.stream()
                .map(groupMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get groups where user is a member
     */
    public List<GroupDTO> getUserGroups(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        List<GroupMembership> memberships = membershipRepository.findByUser(user);
        return memberships.stream()
                .map(membership -> groupMapper.toDTO(membership.getGroup()))
                .collect(Collectors.toList());
    }

    /**
     * Update group
     */
    @Transactional
    public GroupDTO updateGroup(UUID groupId, UUID userId, UpdateGroupRequest request) {
        // Find group
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));

        // Check authorization (must be OWNER or ADMIN)
        if (!canManageGroup(userId, groupId)) {
            throw new RuntimeException("You don't have permission to update this group");
        }

        // Update fields if provided
        if (request.getName() != null && !request.getName().isEmpty()) {
            group.setName(request.getName());
        }

        if (request.getDescription() != null) {
            group.setDescription(request.getDescription());
        }

        // Save updated group
        Group updatedGroup = groupRepository.save(group);
        return groupMapper.toDTO(updatedGroup);
    }

    /**
     * Delete group
     */
    @Transactional
    public void deleteGroup(UUID groupId, UUID userId) {
        // Find group
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));

        // Check authorization (must be OWNER)
        if (!isOwner(userId, groupId)) {
            throw new RuntimeException("Only the owner can delete this group");
        }

        // Delete group (cascade will handle memberships)
        groupRepository.delete(group);
    }

    /**
     * Add member to group
     */
    @Transactional
    public GroupMembershipDTO addMember(UUID groupId, UUID requesterId, AddMemberRequest request) {
        // Find group
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));

        // Check authorization (must be OWNER or ADMIN of the group)
        if (!canManageGroup(requesterId, groupId)) {
            throw new RuntimeException("You don't have permission to add members");
        }

        // Find user to add
        User userToAdd = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getUserId()));

        // Check if user is a member of the community
        if (!communityMembershipRepository.existsByUserIdAndCommunityId(userToAdd.getId(), group.getCommunity().getId())) {
            throw new RuntimeException("User must be a member of the community to join this group");
        }

        // Check if already a member
        if (membershipRepository.existsByUserIdAndGroupId(userToAdd.getId(), group.getId())) {
            throw new RuntimeException("User is already a member of this group");
        }

        // Create membership
        GroupMembership membership = new GroupMembership();
        membership.setGroup(group);
        membership.setUser(userToAdd);
        membership.setRole(request.getRole());

        // Save membership
        GroupMembership savedMembership = membershipRepository.save(membership);
        return groupMapper.toMembershipDTO(savedMembership);
    }

    /**
     * Get all members of a group
     */
    public List<GroupMembershipDTO> getGroupMembers(UUID groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));

        List<GroupMembership> memberships = membershipRepository.findByGroup(group);
        return memberships.stream()
                .map(groupMapper::toMembershipDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get user's membership in a group
     */
    public GroupMembershipDTO getUserMembership(UUID groupId, UUID userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        GroupMembership membership = membershipRepository.findByUserAndGroup(user, group)
                .orElseThrow(() -> new RuntimeException("User is not a member of this group"));

        return groupMapper.toMembershipDTO(membership);
    }

    /**
     * Update member role
     */
    @Transactional
    public GroupMembershipDTO updateMemberRole(UUID groupId, UUID requesterId,
                                               UUID memberId, MemberRole newRole) {
        // Find group
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));

        // Check authorization (must be OWNER)
        if (!isOwner(requesterId, groupId)) {
            throw new RuntimeException("Only the owner can change member roles");
        }

        // Find user to update
        User memberUser = userRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + memberId));

        // Find membership
        GroupMembership membership = membershipRepository.findByUserAndGroup(memberUser, group)
                .orElseThrow(() -> new RuntimeException("User is not a member of this group"));

        // Don't allow changing owner's role
        if (membership.getRole() == MemberRole.OWNER) {
            throw new RuntimeException("Cannot change the owner's role");
        }

        // Update role
        membership.setRole(newRole);
        GroupMembership updatedMembership = membershipRepository.save(membership);

        return groupMapper.toMembershipDTO(updatedMembership);
    }

    /**
     * Remove member from group
     */
    @Transactional
    public void removeMember(UUID groupId, UUID requesterId, UUID memberId) {
        // Find group
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));

        // Check authorization (must be OWNER or ADMIN)
        if (!canManageGroup(requesterId, groupId)) {
            throw new RuntimeException("You don't have permission to remove members");
        }

        // Find user to remove
        User memberUser = userRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + memberId));

        // Find membership
        GroupMembership membership = membershipRepository.findByUserAndGroup(memberUser, group)
                .orElseThrow(() -> new RuntimeException("User is not a member of this group"));

        // Don't allow removing the owner
        if (membership.getRole() == MemberRole.OWNER) {
            throw new RuntimeException("Cannot remove the owner from the group");
        }

        // Delete membership
        membershipRepository.delete(membership);
    }

    /**
     * Search groups in a community
     */
    public List<GroupDTO> searchGroupsInCommunity(UUID communityId, String searchTerm) {
        // Verify community exists
        communityRepository.findById(communityId)
                .orElseThrow(() -> new RuntimeException("Community not found with id: " + communityId));

        List<Group> groups = groupRepository.searchByNameInCommunity(communityId, searchTerm);
        return groups.stream()
                .map(groupMapper::toDTO)
                .collect(Collectors.toList());
    }

    // ========== HELPER METHODS ==========

    private boolean isOwner(UUID userId, UUID groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return membershipRepository.findByUserAndGroup(user, group)
                .map(membership -> membership.getRole() == MemberRole.OWNER)
                .orElse(false);
    }

    private boolean canManageGroup(UUID userId, UUID groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return membershipRepository.findByUserAndGroup(user, group)
                .map(membership -> membership.getRole() == MemberRole.OWNER ||
                        membership.getRole() == MemberRole.ADMIN)
                .orElse(false);
    }
}
