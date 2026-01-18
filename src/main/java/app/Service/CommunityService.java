package app.Service;

import app.DTO.Community.CommunityDTO;
import app.DTO.Community.CommunityMembershipDTO;
import app.DTO.Community.CreateCommunityRequest;
import app.DTO.Community.UpdateCommunityRequest;
import app.DTO.Group.AddMemberRequest;
import app.Database.Community;
import app.Database.CommunityMembership;
import app.Database.MemberRole;
import app.Database.User;
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

    /**
     * Create a new community
     */
    @Transactional
    public CommunityDTO createCommunity(UUID creatorId, CreateCommunityRequest request) {
        // Find the creator
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + creatorId));

        // Create community
        Community community = new Community();
        community.setName(request.getName());
        community.setDescription(request.getDescription());
        community.setCreatedBy(creator);

        // Save community
        Community savedCommunity = communityRepository.save(community);

        // Automatically add creator as OWNER
        CommunityMembership creatorMembership = new CommunityMembership();
        creatorMembership.setCommunity(savedCommunity);
        creatorMembership.setUser(creator);
        creatorMembership.setRole(MemberRole.OWNER);
        membershipRepository.save(creatorMembership);

        return communityMapper.toDTO(savedCommunity);
    }

    /**
     * Get community by ID
     */
    public CommunityDTO getCommunityById(UUID communityId) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new RuntimeException("Community not found with id: " + communityId));

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
    public List<CommunityDTO> getCommunitiesByCreator(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        List<Community> communities = communityRepository.findByCreatedBy(user);
        return communities.stream()
                .map(communityMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get communities where user is a member
     */
    public List<CommunityDTO> getUserCommunities(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        List<CommunityMembership> memberships = membershipRepository.findByUser(user);
        return memberships.stream()
                .map(membership -> communityMapper.toDTO(membership.getCommunity()))
                .collect(Collectors.toList());
    }

    /**
     * Update community
     */
    @Transactional
    public CommunityDTO updateCommunity(UUID communityId, UUID userId, UpdateCommunityRequest request) {
        // Find community
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new RuntimeException("Community not found with id: " + communityId));

        // Check authorization (must be OWNER or ADMIN)
        if (!canManageCommunity(userId, communityId)) {
            throw new RuntimeException("You don't have permission to update this community");
        }

        // Update fields if provided
        if (request.getName() != null && !request.getName().isEmpty()) {
            community.setName(request.getName());
        }

        if (request.getDescription() != null) {
            community.setDescription(request.getDescription());
        }

        // Save updated community
        Community updatedCommunity = communityRepository.save(community);
        return communityMapper.toDTO(updatedCommunity);
    }

    /**
     * Delete community
     */
    @Transactional
    public void deleteCommunity(UUID communityId, UUID userId) {
        // Find community
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new RuntimeException("Community not found with id: " + communityId));

        // Check authorization (must be OWNER)
        if (!isOwner(userId, communityId)) {
            throw new RuntimeException("Only the owner can delete this community");
        }

        // Delete community (cascade will handle memberships and groups)
        communityRepository.delete(community);
    }

    /**
     * Add member to community
     */
    @Transactional
    public CommunityMembershipDTO addMember(UUID communityId, UUID requesterId, AddMemberRequest request) {
        // Find community
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new RuntimeException("Community not found with id: " + communityId));

        // Check authorization (must be OWNER or ADMIN)
        if (!canManageCommunity(requesterId, communityId)) {
            throw new RuntimeException("You don't have permission to add members");
        }

        // Find user to add
        User userToAdd = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getUserId()));

        // Check if already a member
        if (membershipRepository.existsByUserIdAndCommunityId(userToAdd.getId(), community.getId())) {
            throw new RuntimeException("User is already a member of this community");
        }

        // Create membership
        CommunityMembership membership = new CommunityMembership();
        membership.setCommunity(community);
        membership.setUser(userToAdd);
        membership.setRole(request.getRole());

        // Save membership
        CommunityMembership savedMembership = membershipRepository.save(membership);
        return communityMapper.toMembershipDTO(savedMembership);
    }

    /**
     * Get all members of a community
     */
    public List<CommunityMembershipDTO> getCommunityMembers(UUID communityId) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new RuntimeException("Community not found with id: " + communityId));

        List<CommunityMembership> memberships = membershipRepository.findByCommunity(community);
        return memberships.stream()
                .map(communityMapper::toMembershipDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get user's membership in a community
     */
    public CommunityMembershipDTO getUserMembership(UUID communityId, UUID userId) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new RuntimeException("Community not found with id: " + communityId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        CommunityMembership membership = membershipRepository.findByUserAndCommunity(user, community)
                .orElseThrow(() -> new RuntimeException("User is not a member of this community"));

        return communityMapper.toMembershipDTO(membership);
    }

    /**
     * Update member role
     */
    @Transactional
    public CommunityMembershipDTO updateMemberRole(UUID communityId, UUID requesterId,
                                                   UUID memberId, MemberRole newRole) {
        // Find community
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new RuntimeException("Community not found with id: " + communityId));

        // Check authorization (must be OWNER)
        if (!isOwner(requesterId, communityId)) {
            throw new RuntimeException("Only the owner can change member roles");
        }

        // Find user to update
        User memberUser = userRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + memberId));

        // Find membership
        CommunityMembership membership = membershipRepository.findByUserAndCommunity(memberUser, community)
                .orElseThrow(() -> new RuntimeException("User is not a member of this community"));

        // Don't allow changing owner's role
        if (membership.getRole() == MemberRole.OWNER) {
            throw new RuntimeException("Cannot change the owner's role");
        }

        // Update role
        membership.setRole(newRole);
        CommunityMembership updatedMembership = membershipRepository.save(membership);

        return communityMapper.toMembershipDTO(updatedMembership);
    }

    /**
     * Remove member from community
     */
    @Transactional
    public void removeMember(UUID communityId, UUID requesterId, UUID memberId) {
        // Find community
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new RuntimeException("Community not found with id: " + communityId));

        // Check authorization (must be OWNER or ADMIN)
        if (!canManageCommunity(requesterId, communityId)) {
            throw new RuntimeException("You don't have permission to remove members");
        }

        // Find user to remove
        User memberUser = userRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + memberId));

        // Find membership
        CommunityMembership membership = membershipRepository.findByUserAndCommunity(memberUser, community)
                .orElseThrow(() -> new RuntimeException("User is not a member of this community"));

        // Don't allow removing the owner
        if (membership.getRole() == MemberRole.OWNER) {
            throw new RuntimeException("Cannot remove the owner from the community");
        }

        // Delete membership
        membershipRepository.delete(membership);
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

    private boolean isOwner(UUID userId, UUID communityId) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new RuntimeException("Community not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return membershipRepository.findByUserAndCommunity(user, community)
                .map(membership -> membership.getRole() == MemberRole.OWNER)
                .orElse(false);
    }

    private boolean canManageCommunity(UUID userId, UUID communityId) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new RuntimeException("Community not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return membershipRepository.findByUserAndCommunity(user, community)
                .map(membership -> membership.getRole() == MemberRole.OWNER ||
                        membership.getRole() == MemberRole.ADMIN)
                .orElse(false);
    }
}
