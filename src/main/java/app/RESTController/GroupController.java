package app.RESTController;

import app.DTO.Group.AddMemberRequest;
import app.DTO.Group.*;
import app.Database.MemberRole;
import app.Service.GroupService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    @Autowired
    private GroupService groupService;

    /**
     * POST /api/groups
     * Create a new group
     */
    @PostMapping
    public ResponseEntity<GroupDTO> createGroup(
            @Valid @RequestBody CreateGroupRequest request,
            @RequestHeader("User-Id") UUID userId) {
        GroupDTO group = groupService.createGroup(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(group);
    }

    /**
     * GET /api/groups/{groupId}
     * Get a group by ID
     */
    @GetMapping("/{groupId}")
    public ResponseEntity<GroupDTO> getGroup(@PathVariable UUID groupId) {
        GroupDTO group = groupService.getGroupById(groupId);
        return ResponseEntity.ok(group);
    }

    /**
     * GET /api/groups/community/{communityId}
     * Get all groups in a community
     */
    @GetMapping("/community/{communityId}")
    public ResponseEntity<List<GroupDTO>> getGroupsByCommunity(
            @PathVariable UUID communityId) {
        List<GroupDTO> groups = groupService.getGroupsByCommunity(communityId);
        return ResponseEntity.ok(groups);
    }

    /**
     * GET /api/groups/community/{communityId}/search?q={searchTerm}
     * Search groups in a community
     */
    @GetMapping("/community/{communityId}/search")
    public ResponseEntity<List<GroupDTO>> searchGroupsInCommunity(
            @PathVariable UUID communityId,
            @RequestParam("q") String searchTerm) {
        List<GroupDTO> groups = groupService.searchGroupsInCommunity(communityId, searchTerm);
        return ResponseEntity.ok(groups);
    }

    /**
     * GET /api/groups/created-by/{userId}
     * Get groups created by a user
     */
    @GetMapping("/created-by/{userId}")
    public ResponseEntity<List<GroupDTO>> getGroupsByCreator(
            @PathVariable UUID userId) {
        List<GroupDTO> groups = groupService.getGroupsByCreator(userId);
        return ResponseEntity.ok(groups);
    }

    /**
     * GET /api/groups/user/{userId}
     * Get groups where user is a member
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<GroupDTO>> getUserGroups(
            @PathVariable UUID userId) {
        List<GroupDTO> groups = groupService.getUserGroups(userId);
        return ResponseEntity.ok(groups);
    }

    /**
     * PUT /api/groups/{groupId}
     * Update a group
     */
    @PutMapping("/{groupId}")
    public ResponseEntity<GroupDTO> updateGroup(
            @PathVariable UUID groupId,
            @Valid @RequestBody UpdateGroupRequest request,
            @RequestHeader("User-Id") UUID userId) {
        GroupDTO group = groupService.updateGroup(groupId, userId, request);
        return ResponseEntity.ok(group);
    }

    /**
     * DELETE /api/groups/{groupId}
     * Delete a group
     */
    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteGroup(
            @PathVariable UUID groupId,
            @RequestHeader("User-Id") UUID userId) {
        groupService.deleteGroup(groupId, userId);
        return ResponseEntity.noContent().build();
    }

    // ========== MEMBERSHIP ENDPOINTS ==========

    /**
     * POST /api/groups/{groupId}/members
     * Add a member to the group
     */
    @PostMapping("/{groupId}/members")
    public ResponseEntity<GroupMembershipDTO> addMember(
            @PathVariable UUID groupId,
            @Valid @RequestBody AddMemberRequest request,
            @RequestHeader("User-Id") UUID userId) {
        GroupMembershipDTO membership = groupService.addMember(groupId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(membership);
    }

    /**
     * GET /api/groups/{groupId}/members
     * Get all members of a group
     */
    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<GroupMembershipDTO>> getGroupMembers(
            @PathVariable UUID groupId) {
        List<GroupMembershipDTO> members = groupService.getGroupMembers(groupId);
        return ResponseEntity.ok(members);
    }

    /**
     * GET /api/groups/{groupId}/members/{userId}
     * Get a specific user's membership in the group
     */
    @GetMapping("/{groupId}/members/{userId}")
    public ResponseEntity<GroupMembershipDTO> getUserMembership(
            @PathVariable UUID groupId,
            @PathVariable UUID userId) {
        GroupMembershipDTO membership = groupService.getUserMembership(groupId, userId);
        return ResponseEntity.ok(membership);
    }

    /**
     * PUT /api/groups/{groupId}/members/{memberId}/role
     * Update a member's role
     */
    @PutMapping("/{groupId}/members/{memberId}/role")
    public ResponseEntity<GroupMembershipDTO> updateMemberRole(
            @PathVariable UUID groupId,
            @PathVariable UUID memberId,
            @RequestBody MemberRole newRole,
            @RequestHeader("User-Id") UUID userId) {
        GroupMembershipDTO membership = groupService.updateMemberRole(
                groupId, userId, memberId, newRole);
        return ResponseEntity.ok(membership);
    }

    /**
     * DELETE /api/groups/{groupId}/members/{memberId}
     * Remove a member from the group
     */
    @DeleteMapping("/{groupId}/members/{memberId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable UUID groupId,
            @PathVariable UUID memberId,
            @RequestHeader("User-Id") UUID userId) {
        groupService.removeMember(groupId, userId, memberId);
        return ResponseEntity.noContent().build();
    }
}
