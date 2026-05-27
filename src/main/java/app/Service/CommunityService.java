package app.Service;

import app.DTO.Community.CommunityDTO;
import app.DTO.Community.CommunityMembershipDTO;
import app.DTO.Community.CreateCommunityRequest;
import app.DTO.Community.UpdateCommunityRequest;
import app.DTO.Group.AddMemberRequest;
import app.Database.*;
import app.DTO.Community.CommunityMapper;
import app.Repository.CommunityMembershipRepository;
import app.Repository.CommunityRepository;
import app.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CommunityService {

    @Autowired
    private CommunityRepository communityRepository;

    @Autowired
    private CommunityMembershipRepository membershipRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommunityMapper communityMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private GlobalShortCodeService globalShortCodeService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private app.Repository.GroupMembershipRepository groupMembershipRepository;

    /**
     * Create a new community
     */
    @Transactional
    public CommunityDTO createCommunity(String userCode, CreateCommunityRequest request) {
        // Find the creator
        User creator = userService.getUserByShortCode(userCode);

        // Create community
        Community community = new Community();
        community.setName(request.getName());
        community.setDescription(request.getDescription());
        community.setCreatedBy(creator);

        // Save community
        Community savedCommunity = communityRepository.save(community);

        globalShortCodeService.generateAndReserve(DatabaseType.COMMUNITY, savedCommunity.getId());

        // Automatically add creator as OWNER
        CommunityMembership creatorMembership = new CommunityMembership();
        creatorMembership.setCommunity(savedCommunity);
        creatorMembership.setUser(creator);
        creatorMembership.setRole(MemberRole.OWNER);
        membershipRepository.save(creatorMembership);

        // Activity Message
        activityService.record(
                DatabaseType.COMMUNITY,
                community.getId(),
                community.getName() + " created by " + creator.getName()
        );

        return communityMapper.toDTO(savedCommunity);
    }

    /**
     * Get community by Code
     */
    public CommunityDTO getCommunityByCode(String communityCode) {
        Community community = getCommunityEntityByCode(communityCode);
        return communityMapper.toDTO(community);
    }

    /**
     * Get all communities
     */
    public List<CommunityDTO> getAllCommunities() {
        List<Community> communities = communityRepository.findAllByOrderByCreatedAtDesc();
        return communities.stream()
                .map(communityMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get communities created by a user
     */
    public List<CommunityDTO> getCommunitiesByCreator(String userCode) {
        User user = userService.getUserByShortCode(userCode);

        List<Community> communities = communityRepository.findByCreatedBy(user);
        return communities.stream()
                .map(communityMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get communities where user is a member
     */
    public List<CommunityDTO> getUserCommunities(String userCode) {
        User user = userService.getUserByShortCode(userCode);

        List<CommunityMembership> memberships = membershipRepository.findByUser(user);
        return memberships.stream()
                .map(membership -> communityMapper.toDTO(membership.getCommunity()))
                .collect(Collectors.toList());
    }

    /**
     * Update community
     */
    @Transactional
    public CommunityDTO updateCommunity(String communityCode, String userCode, UpdateCommunityRequest request) {
        // Find community
        Community community = getCommunityEntityByCode(communityCode);
        User user = userService.getUserByShortCode(userCode);

        // Check authorization (must be OWNER or ADMIN)
        if (!canManageCommunity(user.getId(), community.getId())) {
            throw new RuntimeException("You don't have permission to update this community");
        }

        // Update fields if provided
        if (request.getName() != null && !request.getName().isEmpty()) {
            community.setName(request.getName());
            // Activity Message
            activityService.record(
                    DatabaseType.COMMUNITY,
                    community.getId(),
                    "Name was modified"
            );
        }

        if (request.getDescription() != null) {
            community.setDescription(request.getDescription());
            // Activity Message
            activityService.record(
                    DatabaseType.COMMUNITY,
                    community.getId(),
                    "Description was modified"
            );
        }

        // Save updated community
        Community updatedCommunity = communityRepository.save(community);

        return communityMapper.toDTO(updatedCommunity);
    }

    /**
     * Delete community
     */
    @Transactional
    public void deleteCommunity(String communityCode, String userCode) {
        // Find community
        Community community = getCommunityEntityByCode(communityCode);
        User user = userService.getUserByShortCode(userCode);

        // Check authorization (must be OWNER)
        if (!isOwner(user.getId(), community.getId())) {
            throw new RuntimeException("Only the owner can delete this community");
        }

        // Delete community (cascade will handle memberships and groups)
        communityRepository.delete(community);

        // Activity Message
        activityService.record(
                DatabaseType.COMMUNITY,
                community.getId(),
                community.getName() + " was deleted"
        );
    }

    /**
     * Request to join community
     */
    @Transactional
    public CommunityMembershipDTO requestToJoin(String communityCode, String userCode) {
        Community community = getCommunityEntityByCode(communityCode);
        User user = userService.getUserByShortCode(userCode);

        // Check if already a member or pending
        membershipRepository.findByUserIdAndCommunityId(user.getId(), community.getId())
                .ifPresent(m -> {
                    throw new RuntimeException("Membership already exists with status: " + m.getStatus());
                });

        CommunityMembership membership = new CommunityMembership();
        membership.setCommunity(community);
        membership.setUser(user);
        membership.setRole(MemberRole.MEMBER);
        membership.setStatus(MembershipStatus.PENDING_APPROVAL);

        CommunityMembership saved = membershipRepository.save(membership);
        return communityMapper.toMembershipDTO(saved);
    }

    /**
     * Approve join request
     */
    @Transactional
    public CommunityMembershipDTO approveRequest(String communityCode, String requesterCode, String targetUserCode) {
        Community community = getCommunityEntityByCode(communityCode);
        User admin = userService.getUserByShortCode(requesterCode);

        if (!canManageCommunity(admin.getId(), community.getId())) {
            throw new RuntimeException("Unauthorized to approve requests");
        }

        User userToApprove = userService.getUserByShortCode(targetUserCode);
        CommunityMembership membership = membershipRepository.findByUserIdAndCommunityIdAndStatus(
                userToApprove.getId(), community.getId(), MembershipStatus.PENDING_APPROVAL)
                .orElseThrow(() -> new RuntimeException("No pending join request found for this user"));

        membership.setStatus(MembershipStatus.ACCEPTED);
        CommunityMembership saved = membershipRepository.save(membership);

        // Activity Message
        activityService.record(
                DatabaseType.COMMUNITY,
                globalShortCodeService.getUUIDfromShortCode(DatabaseType.COMMUNITY, communityCode),
                userToApprove.getName() + " joined the community"
        );
        return communityMapper.toMembershipDTO(saved);
    }

    /**
     * Reject join request
     */
    @Transactional
    public void rejectRequest(String communityCode, String requesterCode, String targetUserCode) {
        Community community = getCommunityEntityByCode(communityCode);
        User admin = userService.getUserByShortCode(requesterCode);

        if (!canManageCommunity(admin.getId(), community.getId())) {
            throw new RuntimeException("Unauthorized to reject requests");
        }

        User userToReject = userService.getUserByShortCode(targetUserCode);
        CommunityMembership membership = membershipRepository.findByUserIdAndCommunityIdAndStatus(
                userToReject.getId(), community.getId(), MembershipStatus.PENDING_APPROVAL)
                .orElseThrow(() -> new RuntimeException("No pending join request found for this user"));

        membershipRepository.delete(membership);
    }

    /**
     * Invite member (formerly addMember)
     */
    @Transactional
    public CommunityMembershipDTO inviteMember(String communityCode, String requesterCode, AddMemberRequest request) {
        Community community = getCommunityEntityByCode(communityCode);
        User admin = userService.getUserByShortCode(requesterCode);

        if (!canManageCommunity(admin.getId(), community.getId())) {
            throw new RuntimeException("Unauthorized to invite members");
        }

        User userToInvite = userService.getUserByShortCode(request.getUserCode());

        membershipRepository.findByUserIdAndCommunityId(userToInvite.getId(), community.getId())
                .ifPresent(m -> {
                    throw new RuntimeException("Membership already exists with status: " + m.getStatus());
                });

        CommunityMembership membership = new CommunityMembership();
        membership.setCommunity(community);
        membership.setUser(userToInvite);
        membership.setRole(request.getRole());
        membership.setStatus(MembershipStatus.PENDING_INVITATION);

        CommunityMembership saved = membershipRepository.save(membership);
        return communityMapper.toMembershipDTO(saved);
    }

    /**
     * Accept invitation
     */
    @Transactional
    public CommunityMembershipDTO acceptInvitation(String communityCode, String userCode) {
        Community community = getCommunityEntityByCode(communityCode);
        User user = userService.getUserByShortCode(userCode);

        CommunityMembership membership = membershipRepository.findByUserIdAndCommunityIdAndStatus(
                user.getId(), community.getId(), MembershipStatus.PENDING_INVITATION)
                .orElseThrow(() -> new RuntimeException("No pending invitation found"));

        membership.setStatus(MembershipStatus.ACCEPTED);
        CommunityMembership saved = membershipRepository.save(membership);

        // Activity Message
        activityService.record(
                DatabaseType.COMMUNITY,
                globalShortCodeService.getUUIDfromShortCode(DatabaseType.COMMUNITY, communityCode),
                user.getName() + " joined the community"
        );

        return communityMapper.toMembershipDTO(saved);
    }

    /**
     * Get all members of a community
     */
    public List<CommunityMembershipDTO> getCommunityMembers(String communityCode) {
        Community community = getCommunityEntityByCode(communityCode);

        List<CommunityMembership> memberships = membershipRepository.findByCommunityId(community.getId());
        return memberships.stream()
                .map(communityMapper::toMembershipDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all pending requests (Admin only)
     */
    public List<CommunityMembershipDTO> getPendingRequests(String communityCode, String userCode) {
        Community community = getCommunityEntityByCode(communityCode);
        User user = userService.getUserByShortCode(userCode);

        if (!canManageCommunity(user.getId(), community.getId())) {
            throw new RuntimeException("Unauthorized to view pending requests");
        }

        return membershipRepository.findByCommunityIdAndStatus(community.getId(), MembershipStatus.PENDING_APPROVAL)
                .stream().map(communityMapper::toMembershipDTO).collect(Collectors.toList());
    }

    /**
     * Get user's membership in a community
     */
    public CommunityMembershipDTO getUserMembership(String communityCode, String userCode) {
        Community community = getCommunityEntityByCode(communityCode);
        User user = userService.getUserByShortCode(userCode);

        CommunityMembership membership = membershipRepository.findByUserIdAndCommunityId(user.getId(), community.getId())
                .orElseThrow(() -> new RuntimeException("User is not a member of this community"));

        return communityMapper.toMembershipDTO(membership);
    }

    /**
     * Update member role
     */
    @Transactional
    public CommunityMembershipDTO updateMemberRole(String communityCode, String requesterCode,
                                                   String memberCode, MemberRole newRole) {
        Community community = getCommunityEntityByCode(communityCode);
        User requester = userService.getUserByShortCode(requesterCode);

        if (!isOwner(requester.getId(), community.getId())) {
            throw new RuntimeException("Only the owner can change member roles");
        }

        User memberUser = userService.getUserByShortCode(memberCode);
        CommunityMembership membership = membershipRepository.findByUserIdAndCommunityIdAndStatus(
                memberUser.getId(), community.getId(), MembershipStatus.ACCEPTED)
                .orElseThrow(() -> new RuntimeException("User is not an active member of this community"));

        if (membership.getRole() == MemberRole.OWNER && newRole != MemberRole.OWNER) {
            checkLastOwner(community.getId());
        }

        membership.setRole(newRole);
        CommunityMembership updatedMembership = membershipRepository.save(membership);

        // Activity Message
        activityService.record(
                DatabaseType.COMMUNITY,
                community.getId(),
                requester.getName() + " made " + memberUser.getName() + " " + newRole
        );

        return communityMapper.toMembershipDTO(updatedMembership);
    }

    /**
     * Remove member from community
     */
    @Transactional
    public void removeMember(String communityCode, String requesterCode, String memberCode) {
        Community community = getCommunityEntityByCode(communityCode);
        User requester = userService.getUserByShortCode(requesterCode);

        User memberUser = userService.getUserByShortCode(memberCode);
        CommunityMembership membership = membershipRepository.findByUserIdAndCommunityId(
                memberUser.getId(), community.getId())
                .orElseThrow(() -> new RuntimeException("Membership not found"));

        // If self-leaving
        if (requester.getId().equals(memberUser.getId())) {
            if (membership.getRole() == MemberRole.OWNER) {
                checkLastOwner(community.getId());
            }
        } else {
            // If being removed by someone else
            if (!canManageCommunity(requester.getId(), community.getId())) {
                throw new RuntimeException("Unauthorized to remove members");
            }
            if (membership.getRole() == MemberRole.OWNER) {
                throw new RuntimeException("Cannot remove the owner");
            }
        }

        membershipRepository.delete(membership);
        
        // Cascade removal to all groups in this community
        List<GroupMembership> groupMemberships = groupMembershipRepository.findByUserIdAndCommunityId(memberUser.getId(), community.getId());
        for (GroupMembership gm : groupMemberships) {
            handleGroupMembershipRemoval(gm, community);
        }

        if (membership.getStatus() == MembershipStatus.ACCEPTED) {
            // Activity Message
            activityService.record(
                    DatabaseType.COMMUNITY,
                    globalShortCodeService.getUUIDfromShortCode(DatabaseType.COMMUNITY, communityCode),
                    memberUser.getName() + " left the community"
            );
        }
    }

    /**
     * Search communities by name
     */
    public List<CommunityDTO> searchCommunities(String searchTerm) {
        List<Community> communities = communityRepository.searchByName(searchTerm);
        return communities.stream()
                .map(communityMapper::toDTO)
                .collect(Collectors.toList());
    }

    // ========== HELPER METHODS ==========

    private void checkLastOwner(UUID communityId) {
        List<CommunityMembership> owners = membershipRepository.findByCommunityIdAndRole(communityId, MemberRole.OWNER);
        if (owners.size() <= 1) {
            throw new RuntimeException("Community must have at least one owner. Please appoint a new owner before leaving or changing role.");
        }
    }

    private Community getCommunityEntityByCode(String communityCode) {
        UUID communityId = globalShortCodeService.getUUIDfromShortCode(DatabaseType.COMMUNITY, communityCode);
        return communityRepository.findById(communityId)
                .orElseThrow(() -> new RuntimeException("Community not found with code: " + communityCode));
    }

    private boolean isOwner(UUID userId, UUID communityId) {
        return membershipRepository.findByUserIdAndCommunityIdAndStatus(userId, communityId, MembershipStatus.ACCEPTED)
                .map(m -> m.getRole() == MemberRole.OWNER)
                .orElse(false);
    }

    private boolean canManageCommunity(UUID userId, UUID communityId) {
        return membershipRepository.findByUserIdAndCommunityIdAndStatus(userId, communityId, MembershipStatus.ACCEPTED)
                .map(m -> m.getRole() == MemberRole.OWNER || m.getRole() == MemberRole.ADMIN)
                .orElse(false);
    }

    private void handleGroupMembershipRemoval(GroupMembership gm, Community community) {
        if (gm.getRole() == MemberRole.OWNER) {
            List<GroupMembership> otherOwners = groupMembershipRepository.findByGroupIdAndRole(gm.getGroup().getId(), MemberRole.OWNER);
            if (otherOwners.size() <= 1) {
                // Last owner - promote someone else or community owner
                promoteNewGroupOwner(gm.getGroup(), community, gm.getUser());
            }
        }
        groupMembershipRepository.delete(gm);
    }

    private void promoteNewGroupOwner(Group group, Community community, User leavingUser) {
        // 1. Try to find a group admin
        List<GroupMembership> admins = groupMembershipRepository.findByGroupIdAndRole(group.getId(), MemberRole.ADMIN);
        if (!admins.isEmpty()) {
            GroupMembership newOwner = admins.get(0);
            newOwner.setRole(MemberRole.OWNER);
            groupMembershipRepository.save(newOwner);
            return;
        }

        // 2. Try to find any other group member
        List<GroupMembership> members = groupMembershipRepository.findByGroupIdAndStatus(group.getId(), MembershipStatus.ACCEPTED);
        for (GroupMembership m : members) {
            if (!m.getUser().getId().equals(leavingUser.getId())) {
                m.setRole(MemberRole.OWNER);
                groupMembershipRepository.save(m);
                return;
            }
        }

        // 3. No one left in group? Transfer to community owner
        List<CommunityMembership> commOwners = membershipRepository.findByCommunityIdAndRole(community.getId(), MemberRole.OWNER);
        if (!commOwners.isEmpty()) {
            User commOwner = commOwners.get(0).getUser();
            
            // Check if community owner is already in the group
            groupMembershipRepository.findByUserIdAndGroupId(commOwner.getId(), group.getId())
                .ifPresentOrElse(
                    existingGm -> {
                        existingGm.setRole(MemberRole.OWNER);
                        existingGm.setStatus(MembershipStatus.ACCEPTED);
                        groupMembershipRepository.save(existingGm);
                    },
                    () -> {
                        GroupMembership newMembership = new GroupMembership();
                        newMembership.setGroup(group);
                        newMembership.setUser(commOwner);
                        newMembership.setRole(MemberRole.OWNER);
                        newMembership.setStatus(MembershipStatus.ACCEPTED);
                        groupMembershipRepository.save(newMembership);
                    }
                );
        }
    }
}
