package app.DTO.User;

import app.Database.DatabaseType;
import app.Database.User;
import app.Service.GlobalShortCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.crypto.Data;

@Component
public class UserMapper {

    @Autowired
    private GlobalShortCodeService globalShortCodeService;

    public UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }

        UserDTO dto = new UserDTO();
        dto.setEmail(user.getEmail());
        dto.setName(user.getName());
        dto.setPhone(user.getPhone());
        dto.setProfilePhotoUrl(user.getProfilePhotoUrl());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        dto.setActive(user.getActive());
        dto.setUserCode(globalShortCodeService.getShortCode(DatabaseType.USER, user.getId()));
        return dto;
    }
}
