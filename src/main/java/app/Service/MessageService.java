package app.Service;

import app.Database.*;
import app.DTO.Message.CreateMessageRequest;
import app.DTO.Message.MessageDTO;
import app.DTO.Message.MessageMapper;
import app.Repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private CommunityRepository communityRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private CommunityMembershipRepository communityMembershipRepository;

    @Autowired
    private GroupMembershipRepository groupMembershipRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private GlobalShortCodeService globalShortCodeService;

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public MessageDTO createMessage(CreateMessageRequest request, String userCode) {
        User sender = userService.getUserByShortCode(userCode);
        Event event = null;
        Community community = null;

        // Determine context
        if (request.getEventCode() != null) {
            UUID eventId = globalShortCodeService.getUUIDfromShortCode(DatabaseType.EVENTS, request.getEventCode());
            event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new RuntimeException("Event not found"));
            
            // Check if chat is restricted
            checkChatPermissions(sender, event.getCommunity(), event.getGroup());
        } else if (request.getCommunityCode() != null) {
            UUID communityId = globalShortCodeService.getUUIDfromShortCode(DatabaseType.COMMUNITY, request.getCommunityCode());
            community = communityRepository.findById(communityId)
                    .orElseThrow(() -> new RuntimeException("Community not found"));
            
            checkChatPermissions(sender, community, null);
        } else {
            throw new RuntimeException("Message must have a context (Event or Community)");
        }

        Message message = messageMapper.toEntity(request, event, community, null, sender);
        Message savedMessage = messageRepository.save(message);

        entityManager.flush();
        entityManager.refresh(savedMessage);
        
        MessageDTO dto = messageMapper.toDTO(savedMessage);
        broadcastMessage(dto);
        
        return dto;
    }

    private void checkChatPermissions(User user, Community community, Group group) {
        boolean onlyAdminsCanChat = false;
        MemberRole role = null;

        if (group != null) {
            // Even if it's an event in a group, we check group permissions if onlyAdminsCanChat is set on the group
            onlyAdminsCanChat = group.getOnlyAdminsCanChat();
            role = groupMembershipRepository.findByUserIdAndGroupIdAndStatus(user.getId(), group.getId(), MembershipStatus.ACCEPTED)
                    .map(GroupMembership::getRole)
                    .orElse(null);
        } else if (community != null) {
            onlyAdminsCanChat = community.getOnlyAdminsCanChat();
            role = communityMembershipRepository.findByUserIdAndCommunityIdAndStatus(user.getId(), community.getId(), MembershipStatus.ACCEPTED)
                    .map(CommunityMembership::getRole)
                    .orElse(null);
        }

        if (role == null) {
            throw new RuntimeException("You must be an accepted member to chat here");
        }

        if (onlyAdminsCanChat && role == MemberRole.MEMBER) {
            throw new RuntimeException("Only admins can chat in this space");
        }
    }

    private void broadcastMessage(MessageDTO dto) {
        if (dto.getEventCode() != null) {
            messagingTemplate.convertAndSend("/topic/event/" + dto.getEventCode() + "/messages", dto);
        } else if (dto.getCommunityCode() != null) {
            messagingTemplate.convertAndSend("/topic/community/" + dto.getCommunityCode() + "/messages", dto);
        }
    }

    public MessageDTO getMessageById(UUID messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        return messageMapper.toDTO(message);
    }

    public Page<MessageDTO> getMessagesByEventCode(String eventCode, int page, int size) {
        UUID eventId = globalShortCodeService.getUUIDfromShortCode(DatabaseType.EVENTS, eventCode);
        Pageable pageable = PageRequest.of(page, size);
        Page<Message> messages = messageRepository.findByEventIdOrderByCreatedAtDesc(eventId, pageable);
        return messages.map(messageMapper::toDTO);
    }

    public List<MessageDTO> getAllMessagesByEventCode(String eventCode) {
        UUID eventId = globalShortCodeService.getUUIDfromShortCode(DatabaseType.EVENTS, eventCode);
        List<Message> messages = messageRepository.findByEventIdOrderByCreatedAtAsc(eventId);
        return messageMapper.toDTOList(messages);
    }

    public Page<MessageDTO> getMessagesByCommunityCode(String communityCode, int page, int size) {
        UUID communityId = globalShortCodeService.getUUIDfromShortCode(DatabaseType.COMMUNITY, communityCode);
        Pageable pageable = PageRequest.of(page, size);
        Page<Message> messages = messageRepository.findByCommunityIdOrderByCreatedAtDesc(communityId, pageable);
        return messages.map(messageMapper::toDTO);
    }

    public List<MessageDTO> getAllMessagesByCommunityCode(String communityCode) {
        UUID communityId = globalShortCodeService.getUUIDfromShortCode(DatabaseType.COMMUNITY, communityCode);
        List<Message> messages = messageRepository.findByCommunityIdOrderByCreatedAtAsc(communityId);
        return messageMapper.toDTOList(messages);
    }

    public List<MessageDTO> getMessagesByEventCodeAndUserCode(String eventCode, String userCode) {
        UUID eventId = globalShortCodeService.getUUIDfromShortCode(DatabaseType.EVENTS, eventCode);
        User user = userService.getUserByShortCode(userCode);
        List<Message> messages = messageRepository.findByEventIdAndSenderIdOrderByCreatedAtDesc(eventId, user.getId());
        return messageMapper.toDTOList(messages);
    }

    public Long getMessageCountByEventCode(String eventCode) {
        UUID eventId = globalShortCodeService.getUUIDfromShortCode(DatabaseType.EVENTS, eventCode);
        return messageRepository.countByEventId(eventId);
    }

    public Long getMessageCountByCommunityCode(String communityCode) {
        UUID communityId = globalShortCodeService.getUUIDfromShortCode(DatabaseType.COMMUNITY, communityCode);
        return messageRepository.countByCommunityId(communityId);
    }

    @Transactional
    public void deleteMessage(UUID messageId, String userCode) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        User user = userService.getUserByShortCode(userCode);

        // Only the sender can delete their message
        if (!message.getSender().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to delete this message");
        }

        messageRepository.delete(message);
    }

    @Transactional
    public void deleteMessagesByEventId(UUID eventId) {
        messageRepository.deleteByEventId(eventId);
    }
}
