package app.Service;

import app.DTO.Group.AddMemberRequest;
import app.DTO.Group.*;
import app.Database.*;
import app.DTO.Group.GroupMapper;
import app.Repository.*;
import jakarta.validation.Valid;
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

    @Autowired
    private UserService userService;

    @Autowired
    private GlobalShortCodeService globalShortCodeService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private ActivityService activityService;

    /**
     * Create a new group
     */
    @Transactional
    public GroupDTO createGroup(String userCode, CreateGroupRequest request) {
        // Find the creator
        User creator = userService.getUserByShortCode(userCode);

        // Find the community
        UUID communityId = globalShortCodeService.getUUIDfromShortCode(DatabaseType.COMMUNITY, request.getCommunityCode());
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new RuntimeException("Community not found with code: " + request.getCommunityCode()));

        // Check if user is a member of the community (ACCEPTED)
        if (!communityMembershipRepository.existsByUserIdAndCommunityIdAndStatus(creator.getId(), community.getId(), MembershipStatus.ACCEPTED)) {
            throw new RuntimeException("You must be an active member of the community to create a group");
        }

        // Only Community Owner or Admin can create groups
        if (!isCommunityAdminOrOwner(creator.getId(), community.getId())) {
            throw new RuntimeException("Only community owners or admins can create groups");
        }

        // Create group
        Group group = new Group();
        group.setCommunity(community);
        group.setName(request.getName());
        group.setDescription(request.getDescription());
        group.setCreatedBy(creator);

        // Save group
        Group savedGroup = groupRepository.save(group);

        // Generate short code
        globalShortCodeService.generateAndReserve(DatabaseType.GROUP, savedGroup.getId());

        // Automatically add creator as OWNER
        GroupMembership creatorMembership = new GroupMembership();
        creatorMembership.setGroup(savedGroup);
        creatorMembership.setUser(creator);
        creatorMembership.setRole(MemberRole.OWNER);
        creatorMembership.setStatus(MembershipStatus.ACCEPTED);
        membershipRepository.save(creatorMembership);

        // Activity Message
        activityService.record(
                DatabaseType.COMMUNITY,
                community.getId(),
                "New Group '" + group.getName() + "' was created by " + creator.getName()
        );
        return groupMapper.toDTO(savedGroup);
    }

    /**
     * Get group by Code
     */
    public GroupDTO getGroupByCode(String groupCode, String userCode) {
        Group group = getGroupEntityByCode(groupCode);
        User user = userService.getUserByShortCode(userCode);
        
        if (!canViewGroups(user.getId(), group.getCommunity().getId())) {
            throw new RuntimeException("You don't have permission to view this group");
        }
        
        return groupMapper.toDTO(group);
    }

    /**
     * Get all groups in a community
     */
    public List<GroupDTO> getGroupsByCommunity(String communityCode, String userCode) {
        UUID communityId = globalShortCodeService.getUUIDfromShortCode(DatabaseType.COMMUNITY, communityCode);
        User user = userService.getUserByShortCode(userCode);

        if (!canViewGroups(user.getId(), communityId)) {
            throw new RuntimeException("You must be a member of the community to see its groups");
        }

        List<Group> groups = groupRepository.findByCommunityIdOrderByCreatedAtDesc(communityId);
        return groups.stream()
                .map(groupMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get groups created by a user
     */
    public List<GroupDTO> getGroupsByCreator(String userCode) {
        User user = userService.getUserByShortCode(userCode);

        List<Group> groups = groupRepository.findByCreatedBy(user);
        return groups.stream()
                .map(groupMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get groups where user is a member
     */
    public List<GroupDTO> getUserGroups(String userCode) {
        User user = userService.getUserByShortCode(userCode);

        List<GroupMembership> memberships = membershipRepository.findByUserIdAndStatus(user.getId(), MembershipStatus.ACCEPTED);
        return memberships.stream()
                .map(membership -> groupMapper.toDTO(membership.getGroup()))
                .collect(Collectors.toList());
    }

    /**
     * Update group
     */
    @Transactional
    public GroupDTO updateGroup(String groupCode, String userCode, UpdateGroupRequest request) {
        // Find group
        Group group = getGroupEntityByCode(groupCode);
        User user = userService.getUserByShortCode(userCode);

        // Check authorization (must be OWNER or ADMIN)
        if (!canManageGroup(user.getId(), group.getId())) {
            throw new RuntimeException("You don't have permission to update this group");
        }

        // Update fields if provided
        if (request.getName() != null && !request.getName().isEmpty()) {
            group.setName(request.getName());
            activityService.record(
                    DatabaseType.GROUP,
                    group.getId(),
                    "Name was modified"
            );
        }

        if (request.getDescription() != null) {
            group.setDescription(request.getDescription());
            activityService.record(
                    DatabaseType.GROUP,
                    group.getId(),
                    "Description was modified"
            );
        }

        // Save updated group
        Group updatedGroup = groupRepository.save(group);
        return groupMapper.toDTO(updatedGroup);
    }

    /**
     * Delete group
     */
    @Transactional
    public void deleteGroup(String groupCode, String userCode) {
        Group group = getGroupEntityByCode(groupCode);
        User user = userService.getUserByShortCode(userCode);

        // Community Owner can delete any group
        boolean isCommOwner = isCommunityOwner(user.getId(), group.getCommunity().getId());
        // Community Admin can delete groups they manage
        boolean isCommAdmin = isCommunityAdmin(user.getId(), group.getCommunity().getId());
        // Group Owner can delete their group
        boolean isGrpOwner = isOwner(user.getId(), group.getId());

        if (isCommOwner || (isCommAdmin && canManageGroup(user.getId(), group.getId())) || isGrpOwner) {
            // Activity Message
            activityService.record(
                    DatabaseType.COMMUNITY,
                    group.getCommunity().getId(),
                    "Group '" + group.getName() + "' was deleted"
            );
            groupRepository.delete(group);
        } else {
            throw new RuntimeException("You don't have permission to delete this group");
        }
    }

    /**
     * Request to join group
     */
    @Transactional
    public GroupMembershipDTO requestToJoin(String groupCode, String userCode) {
        Group group = getGroupEntityByCode(groupCode);
        User user = userService.getUserByShortCode(userCode);

        // Pre-requisite: Must be in community
        if (!communityMembershipRepository.existsByUserIdAndCommunityIdAndStatus(user.getId(), group.getCommunity().getId(), MembershipStatus.ACCEPTED)) {
            throw new RuntimeException("You must be a member of the community to join this group");
        }

        membershipRepository.findByUserIdAndGroupId(user.getId(), group.getId())
                .ifPresent(m -> {
                    throw new RuntimeException("Membership already exists with status: " + m.getStatus());
                });

        GroupMembership membership = new GroupMembership();
        membership.setGroup(group);
        membership.setUser(user);
        membership.setRole(MemberRole.MEMBER);
        membership.setStatus(MembershipStatus.PENDING_APPROVAL);

        GroupMembership saved = membershipRepository.save(membership);
        return groupMapper.toMembershipDTO(saved);
    }

    /**
     * Approve group join request
     */
    @Transactional
    public GroupMembershipDTO approveRequest(String groupCode, String requesterCode, String targetUserCode) {
        Group group = getGroupEntityByCode(groupCode);
        User admin = userService.getUserByShortCode(requesterCode);

        if (!canManageGroup(admin.getId(), group.getId())) {
            throw new RuntimeException("Unauthorized to approve requests");
        }

        User userToApprove = userService.getUserByShortCode(targetUserCode);
        GroupMembership membership = membershipRepository.findByUserIdAndGroupIdAndStatus(
                userToApprove.getId(), group.getId(), MembershipStatus.PENDING_APPROVAL)
                .orElseThrow(() -> new RuntimeException("No pending join request found"));

        membership.setStatus(MembershipStatus.ACCEPTED);
        
        // Auto-promotion: Community Admin/Owner becomes Group Admin
        if (isCommunityAdminOrOwner(userToApprove.getId(), group.getCommunity().getId())) {
            membership.setRole(MemberRole.ADMIN);
        }

        GroupMembership saved = membershipRepository.save(membership);
        
        // Activity Message
        activityService.record(
                DatabaseType.GROUP,
                group.getId(),
                userToApprove.getName() + " joined the group"
        );
        return groupMapper.toMembershipDTO(saved);
    }

    /**
     * Reject join request
     */
    @Transactional
    public void rejectRequest(String groupCode, String requesterCode, String targetUserCode) {
        Group group = getGroupEntityByCode(groupCode);
        User admin = userService.getUserByShortCode(requesterCode);

        if (!canManageGroup(admin.getId(), group.getId())) {
            throw new RuntimeException("Unauthorized to reject requests");
        }

        User userToReject = userService.getUserByShortCode(targetUserCode);
        GroupMembership membership = membershipRepository.findByUserIdAndGroupIdAndStatus(
                userToReject.getId(), group.getId(), MembershipStatus.PENDING_APPROVAL)
                .orElseThrow(() -> new RuntimeException("No pending join request found"));

        membershipRepository.delete(membership);
    }

    /**
     * Invite member to group
     */
    @Transactional
    public GroupMembershipDTO inviteMember(String groupCode, String requesterCode, AddMemberRequest request) {
        Group group = getGroupEntityByCode(groupCode);
        User admin = userService.getUserByShortCode(requesterCode);

        if (!canManageGroup(admin.getId(), group.getId())) {
            throw new RuntimeException("Unauthorized to invite members");
        }

        User userToInvite = userService.getUserByShortCode(request.getUserCode());

        // Pre-requisite: Must be in community
        if (!communityMembershipRepository.existsByUserIdAndCommunityIdAndStatus(userToInvite.getId(), group.getCommunity().getId(), MembershipStatus.ACCEPTED)) {
            throw new RuntimeException("User must be a member of the community first");
        }

        membershipRepository.findByUserIdAndGroupId(userToInvite.getId(), group.getId())
                .ifPresent(m -> {
                    throw new RuntimeException("Membership already exists with status: " + m.getStatus());
                });

        GroupMembership membership = new GroupMembership();
        membership.setGroup(group);
        membership.setUser(userToInvite);
        membership.setRole(request.getRole());
        membership.setStatus(MembershipStatus.PENDING_INVITATION);

        GroupMembership saved = membershipRepository.save(membership);
        return groupMapper.toMembershipDTO(saved);
    }

    /**
     * Accept invitation to group
     */
    @Transactional
    public GroupMembershipDTO acceptInvitation(String groupCode, String userCode) {
        Group group = getGroupEntityByCode(groupCode);
        User user = userService.getUserByShortCode(userCode);

        GroupMembership membership = membershipRepository.findByUserIdAndGroupIdAndStatus(
                user.getId(), group.getId(), MembershipStatus.PENDING_INVITATION)
                .orElseThrow(() -> new RuntimeException("No pending invitation found"));

        membership.setStatus(MembershipStatus.ACCEPTED);
        
        // Auto-promotion: Community Admin/Owner becomes Group Admin
        if (isCommunityAdminOrOwner(user.getId(), group.getCommunity().getId())) {
            membership.setRole(MemberRole.ADMIN);
        }

        GroupMembership saved = membershipRepository.save(membership);
        
        // Activity Message
        activityService.record(
                DatabaseType.GROUP,
                group.getId(),
                user.getName() + " joined the group"
        );
        return groupMapper.toMembershipDTO(saved);
    }

    /**
     * Get all members of a group
     */
    public List<GroupMembershipDTO> getGroupMembers(String groupCode) {
        Group group = getGroupEntityByCode(groupCode);

        List<GroupMembership> memberships = membershipRepository.findByGroupId(group.getId());
        return memberships.stream()
                .map(groupMapper::toMembershipDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get user's membership in a group
     */
    public GroupMembershipDTO getUserMembership(String groupCode, String userCode) {
        Group group = getGroupEntityByCode(groupCode);
        User user = userService.getUserByShortCode(userCode);

        GroupMembership membership = membershipRepository.findByUserIdAndGroupId(user.getId(), group.getId())
                .orElseThrow(() -> new RuntimeException("User is not a member of this group"));

        return groupMapper.toMembershipDTO(membership);
    }

    /**
     * Update member role
     */
    @Transactional
    public GroupMembershipDTO updateMemberRole(String groupCode, String requesterCode,
                                               String memberCode, MemberRole newRole) {
        Group group = getGroupEntityByCode(groupCode);
        User requester = userService.getUserByShortCode(requesterCode);

        if (!isOwner(requester.getId(), group.getId())) {
            throw new RuntimeException("Only the owner can change member roles");
        }

        User memberUser = userService.getUserByShortCode(memberCode);
        GroupMembership membership = membershipRepository.findByUserIdAndGroupIdAndStatus(
                memberUser.getId(), group.getId(), MembershipStatus.ACCEPTED)
                .orElseThrow(() -> new RuntimeException("User is not an active member of this group"));

        if (membership.getRole() == MemberRole.OWNER && newRole != MemberRole.OWNER) {
            checkLastOwner(group.getId());
        }

        membership.setRole(newRole);
        GroupMembership updatedMembership = membershipRepository.save(membership);

        // Activity Message
        activityService.record(
                DatabaseType.GROUP,
                group.getId(),
                requester.getName() + " made " + memberUser.getName() + " " + newRole
        );

        return groupMapper.toMembershipDTO(updatedMembership);
    }

    /**
     * Directly add a member to the group (bypasses invite/approval flow)
     */
    @Transactional
    public GroupMembershipDTO addMember(String groupCode, String requesterCode, AddMemberRequest request) {
        Group group = getGroupEntityByCode(groupCode);
        User requester = userService.getUserByShortCode(requesterCode);

        if (!canManageGroup(requester.getId(), group.getId())) {
            throw new RuntimeException("Unauthorized to add members directly");
        }

        User userToAdd = userService.getUserByShortCode(request.getUserCode());

        // Pre-requisite: Must be in community
        if (!communityMembershipRepository.existsByUserIdAndCommunityIdAndStatus(
                userToAdd.getId(), group.getCommunity().getId(), MembershipStatus.ACCEPTED)) {
            throw new RuntimeException("User must be a member of the community first");
        }

        membershipRepository.findByUserIdAndGroupId(userToAdd.getId(), group.getId())
                .ifPresent(m -> {
                    throw new RuntimeException("Membership already exists with status: " + m.getStatus());
                });

        GroupMembership membership = new GroupMembership();
        membership.setGroup(group);
        membership.setUser(userToAdd);
        membership.setStatus(MembershipStatus.ACCEPTED);

        // Auto-promotion: Community Admin/Owner becomes Group Admin
        membership.setRole(isCommunityAdminOrOwner(userToAdd.getId(), group.getCommunity().getId())
                ? MemberRole.ADMIN
                : request.getRole());

        GroupMembership saved = membershipRepository.save(membership);

        activityService.record(
                DatabaseType.GROUP,
                group.getId(),
                requester.getName() + " added " + userToAdd.getName() + " to the group"
        );

        return groupMapper.toMembershipDTO(saved);
    }

    /**
     * Remove member from group
     */
    @Transactional
    public void removeMember(String groupCode, String requesterCode, String memberCode) {
        Group group = getGroupEntityByCode(groupCode);
        User requester = userService.getUserByShortCode(requesterCode);
        User memberUser = userService.getUserByShortCode(memberCode);

        GroupMembership membership = membershipRepository.findByUserIdAndGroupId(
                memberUser.getId(), group.getId())
                .orElseThrow(() -> new RuntimeException("Membership not found"));

        if (requester.getId().equals(memberUser.getId())) {
            if (membership.getRole() == MemberRole.OWNER) {
                checkLastOwner(group.getId());
            }
        } else {
            if (!canManageGroup(requester.getId(), group.getId())) {
                throw new RuntimeException("Unauthorized to remove members");
            }
            if (membership.getRole() == MemberRole.OWNER) {
                throw new RuntimeException("Cannot remove the owner");
            }
        }

        membershipRepository.delete(membership);
        
        if (membership.getStatus() == MembershipStatus.ACCEPTED) {
            // Activity Message
            activityService.record(
                    DatabaseType.GROUP,
                    group.getId(),
                    memberUser.getName() + " left the group"
            );
        }
    }

    /**
     * Search groups in a community
     */
    public List<GroupDTO> searchGroupsInCommunity(String communityCode, String searchTerm, String userCode) {
        UUID communityId = globalShortCodeService.getUUIDfromShortCode(DatabaseType.COMMUNITY, communityCode);
        User user = userService.getUserByShortCode(userCode);

        if (!canViewGroups(user.getId(), communityId)) {
            throw new RuntimeException("Unauthorized to search groups in this community");
        }

        List<Group> groups = groupRepository.searchByNameInCommunity(communityId, searchTerm);
        return groups.stream()
                .map(groupMapper::toDTO)
                .collect(Collectors.toList());
    }

    // ========== HELPER METHODS ==========

    private void checkLastOwner(UUID groupId) {
        List<GroupMembership> owners = membershipRepository.findByGroupIdAndRole(groupId, MemberRole.OWNER);
        if (owners.size() <= 1) {
            throw new RuntimeException("Group must have at least one owner");
        }
    }

    private Group getGroupEntityByCode(String groupCode) {
        UUID groupId = globalShortCodeService.getUUIDfromShortCode(DatabaseType.GROUP, groupCode);
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found with code: " + groupCode));
    }

    private boolean isOwner(UUID userId, UUID groupId) {
        return membershipRepository.findByUserIdAndGroupIdAndStatus(userId, groupId, MembershipStatus.ACCEPTED)
                .map(m -> m.getRole() == MemberRole.OWNER)
                .orElse(false);
    }

    private boolean canManageGroup(UUID userId, UUID groupId) {
        return membershipRepository.findByUserIdAndGroupIdAndStatus(userId, groupId, MembershipStatus.ACCEPTED)
                .map(m -> m.getRole() == MemberRole.OWNER || m.getRole() == MemberRole.ADMIN)
                .orElse(false);
    }

    private boolean isCommunityOwner(UUID userId, UUID communityId) {
        return communityMembershipRepository.findByUserIdAndCommunityIdAndStatus(userId, communityId, MembershipStatus.ACCEPTED)
                .map(m -> m.getRole() == MemberRole.OWNER)
                .orElse(false);
    }

    private boolean isCommunityAdmin(UUID userId, UUID communityId) {
        return communityMembershipRepository.findByUserIdAndCommunityIdAndStatus(userId, communityId, MembershipStatus.ACCEPTED)
                .map(m -> m.getRole() == MemberRole.ADMIN)
                .orElse(false);
    }

    private boolean isCommunityAdminOrOwner(UUID userId, UUID communityId) {
        return communityMembershipRepository.findByUserIdAndCommunityIdAndStatus(userId, communityId, MembershipStatus.ACCEPTED)
                .map(m -> m.getRole() == MemberRole.OWNER || m.getRole() == MemberRole.ADMIN)
                .orElse(false);
    }

    private boolean canViewGroups(UUID userId, UUID communityId) {
        // Community Admin/Owner can always see groups
        if (isCommunityAdminOrOwner(userId, communityId)) return true;
        
        // Others must be active members of the community
        return communityMembershipRepository.existsByUserIdAndCommunityIdAndStatus(userId, communityId, MembershipStatus.ACCEPTED);
    }
}
