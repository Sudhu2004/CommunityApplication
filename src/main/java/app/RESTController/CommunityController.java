package app.RESTController;

import app.DTO.Community.CommunityDTO;
import app.DTO.Community.CommunityMembershipDTO;
import app.DTO.Community.CreateCommunityRequest;
import app.DTO.Community.UpdateCommunityRequest;
import app.DTO.Group.AddMemberRequest;
import app.Database.MemberRole;
import app.Service.CommunityService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/communities")
public class CommunityController {

    @Autowired
    private CommunityService communityService;

    /**
     * POST /api/communities
     * Create a new community
     */
    @PostMapping
    public ResponseEntity<CommunityDTO> createCommunity(
            @Valid @RequestBody CreateCommunityRequest request,
            @RequestHeader("User-Id") UUID userId) {
        CommunityDTO community = communityService.createCommunity(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(community);
    }

    /**
     * GET /api/communities/{communityId}
     * Get a community by ID
     */
    @GetMapping("/{communityId}")
    public ResponseEntity<CommunityDTO> getCommunity(@PathVariable UUID communityId) {
        CommunityDTO community = communityService.getCommunityById(communityId);
        return ResponseEntity.ok(community);
    }

    /**
     * GET /api/communities
     * Get all communities
     */
    @GetMapping
    public ResponseEntity<List<CommunityDTO>> getAllCommunities() {
        List<CommunityDTO> communities = communityService.getAllCommunities();
        return ResponseEntity.ok(communities);
    }

    /**
     * GET /api/communities/search?q={searchTerm}
     * Search communities by name
     */
    @GetMapping("/search")
    public ResponseEntity<List<CommunityDTO>> searchCommunities(
            @RequestParam("q") String searchTerm) {
        List<CommunityDTO> communities = communityService.searchCommunities(searchTerm);
        return ResponseEntity.ok(communities);
    }

    /**
     * GET /api/communities/created-by/{userId}
     * Get communities created by a user
     */
    @GetMapping("/created-by/{userId}")
    public ResponseEntity<List<CommunityDTO>> getCommunitiesByCreator(
            @PathVariable UUID userId) {
        List<CommunityDTO> communities = communityService.getCommunitiesByCreator(userId);
        return ResponseEntity.ok(communities);
    }

    /**
     * GET /api/communities/user/{userId}
     * Get communities where user is a member
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CommunityDTO>> getUserCommunities(
            @PathVariable UUID userId) {
        List<CommunityDTO> communities = communityService.getUserCommunities(userId);
        return ResponseEntity.ok(communities);
    }

    /**
     * PUT /api/communities/{communityId}
     * Update a community
     */
    @PutMapping("/{communityId}")
    public ResponseEntity<CommunityDTO> updateCommunity(
            @PathVariable UUID communityId,
            @Valid @RequestBody UpdateCommunityRequest request,
            @RequestHeader("User-Id") UUID userId) {
        CommunityDTO community = communityService.updateCommunity(communityId, userId, request);
        return ResponseEntity.ok(community);
    }

    /**
     * DELETE /api/communities/{communityId}
     * Delete a community
     */
    @DeleteMapping("/{communityId}")
    public ResponseEntity<Void> deleteCommunity(
            @PathVariable UUID communityId,
            @RequestHeader("User-Id") UUID userId) {
        communityService.deleteCommunity(communityId, userId);
        return ResponseEntity.noContent().build();
    }

    // ========== MEMBERSHIP ENDPOINTS ==========

    /**
     * POST /api/communities/{communityId}/members
     * Add a member to the community
     */
    @PostMapping("/{communityId}/members")
    public ResponseEntity<CommunityMembershipDTO> addMember(
            @PathVariable UUID communityId,
            @Valid @RequestBody AddMemberRequest request,
            @RequestHeader("User-Id") UUID userId) {
        CommunityMembershipDTO membership = communityService.addMember(communityId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(membership);
    }

    /**
     * GET /api/communities/{communityId}/members
     * Get all members of a community
     */
    @GetMapping("/{communityId}/members")
    public ResponseEntity<List<CommunityMembershipDTO>> getCommunityMembers(
            @PathVariable UUID communityId) {
        List<CommunityMembershipDTO> members = communityService.getCommunityMembers(communityId);
        return ResponseEntity.ok(members);
    }

    /**
     * GET /api/communities/{communityId}/members/{userId}
     * Get a specific user's membership in the community
     */
    @GetMapping("/{communityId}/members/{userId}")
    public ResponseEntity<CommunityMembershipDTO> getUserMembership(
            @PathVariable UUID communityId,
            @PathVariable UUID userId) {
        CommunityMembershipDTO membership = communityService.getUserMembership(communityId, userId);
        return ResponseEntity.ok(membership);
    }

    /**
     * PUT /api/communities/{communityId}/members/{memberId}/role
     * Update a member's role
     */
    @PutMapping("/{communityId}/members/{memberId}/role")
    public ResponseEntity<CommunityMembershipDTO> updateMemberRole(
            @PathVariable UUID communityId,
            @PathVariable UUID memberId,
            @RequestBody MemberRole newRole,
            @RequestHeader("User-Id") UUID userId) {
        CommunityMembershipDTO membership = communityService.updateMemberRole(
                communityId, userId, memberId, newRole);
        return ResponseEntity.ok(membership);
    }

    /**
     * DELETE /api/communities/{communityId}/members/{memberId}
     * Remove a member from the community
     */
    @DeleteMapping("/{communityId}/members/{memberId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable UUID communityId,
            @PathVariable UUID memberId,
            @RequestHeader("User-Id") UUID userId) {
        communityService.removeMember(communityId, userId, memberId);
        return ResponseEntity.noContent().build();
    }
}
