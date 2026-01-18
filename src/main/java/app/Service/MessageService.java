package app.Service;

import app.Database.Event;
import app.Database.Message;
import app.Database.User;
import app.DTO.Message.CreateMessageRequest;
import app.DTO.Message.MessageDTO;
import app.DTO.Message.MessageMapper;
import app.Repository.EventRepository;
import app.Repository.MessageRepository;
import app.Repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    private UserRepository userRepository;

    @Autowired
    private MessageMapper messageMapper;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public MessageDTO createMessage(CreateMessageRequest request, UUID senderId) {
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new RuntimeException("Event not found"));

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Message message = messageMapper.toEntity(request, event, sender);
        Message savedMessage = messageRepository.save(message);

        // Flush to trigger @CreationTimestamp and @UpdateTimestamp
        entityManager.flush();

        entityManager.refresh(savedMessage);
        return messageMapper.toDTO(savedMessage);
    }

    public MessageDTO getMessageById(UUID messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        return messageMapper.toDTO(message);
    }

    public Page<MessageDTO> getMessagesByEventId(UUID eventId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Message> messages = messageRepository.findByEventIdOrderByCreatedAtDesc(eventId, pageable);
        return messages.map(messageMapper::toDTO);
    }

    public List<MessageDTO> getAllMessagesByEventId(UUID eventId) {
        List<Message> messages = messageRepository.findByEventIdOrderByCreatedAtAsc(eventId);
        return messageMapper.toDTOList(messages);
    }

    public List<MessageDTO> getMessagesByEventIdAndSenderId(UUID eventId, UUID senderId) {
        List<Message> messages = messageRepository.findByEventIdAndSenderIdOrderByCreatedAtDesc(eventId, senderId);
        return messageMapper.toDTOList(messages);
    }

    public Long getMessageCountByEventId(UUID eventId) {
        return messageRepository.countByEventId(eventId);
    }

    @Transactional
    public void deleteMessage(UUID messageId, UUID userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        // Only the sender can delete their message
        if (!message.getSender().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized to delete this message");
        }

        messageRepository.delete(message);
    }

    @Transactional
    public void deleteMessagesByEventId(UUID eventId) {
        messageRepository.deleteByEventId(eventId);
    }
}
