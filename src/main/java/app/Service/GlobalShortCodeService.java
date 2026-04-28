package app.Service;

import app.Database.DatabaseType;
import app.Database.GlobalShortCode;
import app.Repository.GlobalShortCodeRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class GlobalShortCodeService {

    private final String CHAR_POOL = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private final int CODE_LENGTH = 10;

    private SecureRandom random = new SecureRandom();

    @Autowired
    private GlobalShortCodeRepository globalShortCodeRepository;

    // 🔹 1. Generate & reserve globally unique code
    @Transactional
    public String generateAndReserve(DatabaseType type, UUID referenceId) {

        while (true) {
            String code = generateCode();

            try {
                GlobalShortCode entity = new GlobalShortCode();
                entity.setCode(code);
                entity.setType(type);
                entity.setReferenceId(referenceId);
                entity.setCreatedAt(LocalDateTime.now());

                globalShortCodeRepository.save(entity);

                return code;

            } catch (DataIntegrityViolationException e) {
                // Collision → retry
            }
        }
    }

    public void deleteCode(String code) {
        globalShortCodeRepository.deleteByCode(code);
    }

    public String getShortCode(DatabaseType type, UUID referenceId) {
        GlobalShortCode globalShortCode = globalShortCodeRepository.findByTypeAndReferenceId(type, referenceId).get();

        return globalShortCode.getCode();
    }

    public UUID getUUIDfromShortCode(DatabaseType type, String code) {
        GlobalShortCode globalShortCode = globalShortCodeRepository.findByCodeAndType(code, type).get();
        return globalShortCode.getReferenceId();
    }

    private String generateCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);

        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = random.nextInt(CHAR_POOL.length());
            sb.append(CHAR_POOL.charAt(index));
        }

        return sb.toString();
    }
}