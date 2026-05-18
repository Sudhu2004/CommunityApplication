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
            @RequestHeader("userCode") String userCode) {
        GroupDTO group = groupService.createGroup(userCode, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(group);
    }

    /**
     * GET /api/groups/{groupCode}
     * Get a group by ID
     */
    @GetMapping("/{groupCode}")
    public ResponseEntity<GroupDTO> getGroup(
            @PathVariable String groupCode,
            @RequestHeader("userCode") String userCode) {
        GroupDTO group = groupService.getGroupByCode(groupCode, userCode);
        return ResponseEntity.ok(group);
    }

    /**
     * GET /api/groups/community/{communityCode}
     * Get all groups in a community
     */
    @GetMapping("/community/{communityCode}")
    public ResponseEntity<List<GroupDTO>> getGroupsByCommunity(
            @PathVariable String communityCode,
            @RequestHeader("userCode") String userCode) {
        List<GroupDTO> groups = groupService.getGroupsByCommunity(communityCode, userCode);
        return ResponseEntity.ok(groups);
    }

    /**
     * GET /api/groups/community/{communityCode}/search?q={searchTerm}
     * Search groups in a community
     */
    @GetMapping("/community/{communityCode}/search")
    public ResponseEntity<List<GroupDTO>> searchGroupsInCommunity(
            @PathVariable String communityCode,
            @RequestParam("q") String searchTerm,
            @RequestHeader("userCode") String userCode) {
        List<GroupDTO> groups = groupService.searchGroupsInCommunity(communityCode, searchTerm, userCode);
        return ResponseEntity.ok(groups);
    }

    /**
     * GET /api/groups/created-by/{userCode}
     * Get groups created by a user
     */
    @GetMapping("/created-by/{userCode}")
    public ResponseEntity<List<GroupDTO>> getGroupsByCreator(
            @PathVariable String userCode) {
        List<GroupDTO> groups = groupService.getGroupsByCreator(userCode);
        return ResponseEntity.ok(groups);
    }

    /**
     * GET /api/groups/user/{userCode}
     * Get groups where user is a member
     */
    @GetMapping("/user/{userCode}")
    public ResponseEntity<List<GroupDTO>> getUserGroups(
            @PathVariable String userCode) {
        List<GroupDTO> groups = groupService.getUserGroups(userCode);
        return ResponseEntity.ok(groups);
    }

    /**
     * PUT /api/groups/{groupCode}
     * Update a group
     */
    @PutMapping("/{groupCode}")
    public ResponseEntity<GroupDTO> updateGroup(
            @PathVariable String groupCode,
            @Valid @RequestBody UpdateGroupRequest request,
            @RequestHeader("userCode") String userCode) {
        GroupDTO group = groupService.updateGroup(groupCode, userCode, request);
        return ResponseEntity.ok(group);
    }

    /**
     * DELETE /api/groups/{groupCode}
     * Delete a group
     */
    @DeleteMapping("/{groupCode}")
    public ResponseEntity<Void> deleteGroup(
            @PathVariable String groupCode,
            @RequestHeader("userCode") String userCode) {
        groupService.deleteGroup(groupCode, userCode);
        return ResponseEntity.noContent().build();
    }

    // ========== MEMBERSHIP ENDPOINTS ==========

    /**
     * POST /api/groups/{groupCode}/request-join
     * Request to join group
     */
    @PostMapping("/{groupCode}/request-join")
    public ResponseEntity<GroupMembershipDTO> requestToJoin(
            @PathVariable String groupCode,
            @RequestHeader("userCode") String userCode) {
        GroupMembershipDTO membership = groupService.requestToJoin(groupCode, userCode);
        return ResponseEntity.status(HttpStatus.CREATED).body(membership);
    }

    /**
     * POST /api/groups/{groupCode}/approve/{targetUserCode}
     * Approve join request
     */
    @PostMapping("/{groupCode}/approve/{targetUserCode}")
    public ResponseEntity<GroupMembershipDTO> approveRequest(
            @PathVariable String groupCode,
            @PathVariable String targetUserCode,
            @RequestHeader("userCode") String userCode) {
        GroupMembershipDTO membership = groupService.approveRequest(groupCode, userCode, targetUserCode);
        return ResponseEntity.ok(membership);
    }

    /**
     * POST /api/groups/{groupCode}/members
     * Directly add a member to the group
     */
    @PostMapping("/{groupCode}/members")
    public ResponseEntity<GroupMembershipDTO> addMember(
            @PathVariable String groupCode,
            @Valid @RequestBody AddMemberRequest request,
            @RequestHeader("userCode") String userCode) {
        GroupMembershipDTO membership = groupService.addMember(groupCode, userCode, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(membership);
    }

    /**
     * POST /api/groups/{groupCode}/reject/{targetUserCode}
     * Reject join request
     */
    @PostMapping("/{groupCode}/reject/{targetUserCode}")
    public ResponseEntity<Void> rejectRequest(
            @PathVariable String groupCode,
            @PathVariable String targetUserCode,
            @RequestHeader("userCode") String userCode) {
        groupService.rejectRequest(groupCode, userCode, targetUserCode);
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/groups/{groupCode}/invite
     * Invite a member to the group
     */
    @PostMapping("/{groupCode}/invite")
    public ResponseEntity<GroupMembershipDTO> inviteMember(
            @PathVariable String groupCode,
            @Valid @RequestBody AddMemberRequest request,
            @RequestHeader("userCode") String userCode) {
        GroupMembershipDTO membership = groupService.inviteMember(groupCode, userCode, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(membership);
    }

    /**
     * POST /api/groups/{groupCode}/accept-invite
     * Accept group invitation
     */
    @PostMapping("/{groupCode}/accept-invite")
    public ResponseEntity<GroupMembershipDTO> acceptInvitation(
            @PathVariable String groupCode,
            @RequestHeader("userCode") String userCode) {
        GroupMembershipDTO membership = groupService.acceptInvitation(groupCode, userCode);
        return ResponseEntity.ok(membership);
    }

    /**
     * GET /api/groups/{groupCode}/members
     * Get all members of a group
     */
    @GetMapping("/{groupCode}/members")
    public ResponseEntity<List<GroupMembershipDTO>> getGroupMembers(
            @PathVariable String groupCode) {
        List<GroupMembershipDTO> members = groupService.getGroupMembers(groupCode);
        return ResponseEntity.ok(members);
    }

    /**
     * GET /api/groups/{groupCode}/members/{userCode}
     * Get a specific user's membership in the group
     */
    @GetMapping("/{groupCode}/members/{userCode}")
    public ResponseEntity<GroupMembershipDTO> getUserMembership(
            @PathVariable String groupCode,
            @PathVariable String userCode) {
        GroupMembershipDTO membership = groupService.getUserMembership(groupCode, userCode);
        return ResponseEntity.ok(membership);
    }

    /**
     * PUT /api/groups/{groupCode}/members/{memberCode}/role
     * Update a member's role
     */
    @PutMapping("/{groupCode}/members/{memberCode}/role")
    public ResponseEntity<GroupMembershipDTO> updateMemberRole(
            @PathVariable String groupCode,
            @PathVariable String memberCode,
            @RequestBody MemberRole newRole,
            @RequestHeader("userCode") String userCode) {
        GroupMembershipDTO membership = groupService.updateMemberRole(
                groupCode, userCode, memberCode, newRole);
        return ResponseEntity.ok(membership);
    }

    /**
     * DELETE /api/groups/{groupCode}/members/{memberCode}
     * Remove a member from the group
     */
    @DeleteMapping("/{groupCode}/members/{memberCode}")
    public ResponseEntity<Void> removeMember(
            @PathVariable String groupCode,
            @PathVariable String memberCode,
            @RequestHeader("userCode") String userCode) {
        groupService.removeMember(groupCode, userCode, memberCode);
        return ResponseEntity.noContent().build();
    }
}
