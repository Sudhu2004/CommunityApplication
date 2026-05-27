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
            @RequestHeader("userCode") String userCode) {
        CommunityDTO community = communityService.createCommunity(userCode, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(community);
    }

    /**
     * GET /api/communities/{communityCode}
     * Get a community by ID
     */
    @GetMapping("/{communityCode}")
    public ResponseEntity<CommunityDTO> getCommunity(@PathVariable String communityCode) {
        CommunityDTO community = communityService.getCommunityByCode(communityCode);
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
     * GET /api/communities/created-by/{userCode}
     * Get communities created by a user
     */
    @GetMapping("/created-by/{userCode}")
    public ResponseEntity<List<CommunityDTO>> getCommunitiesByCreator(
            @PathVariable String userCode) {
        List<CommunityDTO> communities = communityService.getCommunitiesByCreator(userCode);
        return ResponseEntity.ok(communities);
    }

    /**
     * GET /api/communities/user/{userCode}
     * Get communities where user is a member
     */
    @GetMapping("/user/{userCode}")
    public ResponseEntity<List<CommunityDTO>> getUserCommunities(
            @PathVariable String userCode) {
        List<CommunityDTO> communities = communityService.getUserCommunities(userCode);
        return ResponseEntity.ok(communities);
    }

    /**
     * PUT /api/communities/{communityCode}
     * Update a community
     */
    @PutMapping("/{communityCode}")
    public ResponseEntity<CommunityDTO> updateCommunity(
            @PathVariable String communityCode,
            @Valid @RequestBody UpdateCommunityRequest request,
            @RequestHeader("userCode") String userCode) {
        CommunityDTO community = communityService.updateCommunity(communityCode, userCode, request);
        return ResponseEntity.ok(community);
    }

    /**
     * DELETE /api/communities/{communityCode}
     * Delete a community
     */
    @DeleteMapping("/{communityCode}")
    public ResponseEntity<Void> deleteCommunity(
            @PathVariable String communityCode,
            @RequestHeader("userCode") String userCode) {
        communityService.deleteCommunity(communityCode, userCode);
        return ResponseEntity.noContent().build();
    }

    // ========== MEMBERSHIP ENDPOINTS ==========

    /**
     * POST /api/communities/{communityCode}/request-join
     * Request to join community
     */
    @PostMapping("/{communityCode}/request-join")
    public ResponseEntity<CommunityMembershipDTO> requestToJoin(
            @PathVariable String communityCode,
            @RequestHeader("userCode") String userCode) {
        CommunityMembershipDTO membership = communityService.requestToJoin(communityCode, userCode);
        return ResponseEntity.status(HttpStatus.CREATED).body(membership);
    }

    /**
     * POST /api/communities/{communityCode}/approve/{targetUserCode}
     * Approve join request
     */
    @PostMapping("/{communityCode}/approve/{targetUserCode}")
    public ResponseEntity<CommunityMembershipDTO> approveRequest(
            @PathVariable String communityCode,
            @PathVariable String targetUserCode,
            @RequestHeader("userCode") String userCode) {
        CommunityMembershipDTO membership = communityService.approveRequest(communityCode, userCode, targetUserCode);
        return ResponseEntity.ok(membership);
    }

    /**
     * POST /api/communities/{communityCode}/reject/{targetUserCode}
     * Reject join request
     */
    @PostMapping("/{communityCode}/reject/{targetUserCode}")
    public ResponseEntity<Void> rejectRequest(
            @PathVariable String communityCode,
            @PathVariable String targetUserCode,
            @RequestHeader("userCode") String userCode) {
        communityService.rejectRequest(communityCode, userCode, targetUserCode);
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/communities/{communityCode}/invite
     * Invite a member to the community
     */
    @PostMapping("/{communityCode}/invite")
    public ResponseEntity<CommunityMembershipDTO> inviteMember(
            @PathVariable String communityCode,
            @Valid @RequestBody AddMemberRequest request,
            @RequestHeader("userCode") String userCode) {
        CommunityMembershipDTO membership = communityService.inviteMember(communityCode, userCode, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(membership);
    }

    /**
     * POST /api/communities/{communityCode}/accept-invite
     * Accept community invitation
     */
    @PostMapping("/{communityCode}/accept-invite")
    public ResponseEntity<CommunityMembershipDTO> acceptInvitation(
            @PathVariable String communityCode,
            @RequestHeader("userCode") String userCode) {
        CommunityMembershipDTO membership = communityService.acceptInvitation(communityCode, userCode);
        return ResponseEntity.ok(membership);
    }

    /**
     * GET /api/communities/{communityCode}/pending-requests
     * Get all pending join requests
     */
    @GetMapping("/{communityCode}/pending-requests")
    public ResponseEntity<List<CommunityMembershipDTO>> getPendingRequests(
            @PathVariable String communityCode,
            @RequestParam String userCode) {
        List<CommunityMembershipDTO> requests = communityService.getPendingRequests(communityCode, userCode);
        return ResponseEntity.ok(requests);
    }

    /**
     * GET /api/communities/{communityCode}/members
     * Get all members of a community
     */
    @GetMapping("/{communityCode}/members")
    public ResponseEntity<List<CommunityMembershipDTO>> getCommunityMembers(
            @PathVariable String communityCode) {
        List<CommunityMembershipDTO> members = communityService.getCommunityMembers(communityCode);
        System.out.println("Members of Community:");

        for (CommunityMembershipDTO member : members) {
            System.out.println(member);
        }
        return ResponseEntity.ok(members);
    }

    /**
     * GET /api/communities/{communityCode}/members/{userCode}
     * Get a specific user's membership in the community
     */
    @GetMapping("/{communityCode}/members/{userCode}")
    public ResponseEntity<CommunityMembershipDTO> getUserMembership(
            @PathVariable String communityCode,
            @PathVariable String userCode) {
        CommunityMembershipDTO membership = communityService.getUserMembership(communityCode, userCode);
        return ResponseEntity.ok(membership);
    }

    /**
     * PUT /api/communities/{communityCode}/members/{memberCode}/role
     * Update a member's role
     */
    @PutMapping("/{communityCode}/members/{memberCode}/role")
    public ResponseEntity<CommunityMembershipDTO> updateMemberRole(
            @PathVariable String communityCode,
            @PathVariable String memberCode,
            @RequestBody MemberRole newRole,
            @RequestHeader("userCode") String userCode) {
        CommunityMembershipDTO membership = communityService.updateMemberRole(
                communityCode, userCode, memberCode, newRole);
        return ResponseEntity.ok(membership);
    }

    /**
     * DELETE /api/communities/{communityCode}/members/{memberCode}
     * Remove a member from the community
     */
    @DeleteMapping("/{communityCode}/members/{memberCode}")
    public ResponseEntity<Void> removeMember(
            @PathVariable String communityCode,
            @PathVariable String memberCode,
            @RequestHeader("userCode") String userCode) {
        communityService.removeMember(communityCode, userCode, memberCode);
        return ResponseEntity.noContent().build();
    }
}
