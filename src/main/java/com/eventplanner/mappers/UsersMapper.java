package com.eventplanner.mappers;

import com.eventplanner.dtos.UserDTO;
import com.eventplanner.entities.User;
import org.springframework.stereotype.Component;

@Component
public class UsersMapper {

    public UserDTO toDTO(User user)
    {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(user.getUsername());
        userDTO.setEmail(user.getEmail());
        userDTO.setFirstname(user.getFirstname());
        userDTO.setLastname(user.getLastname());
        return userDTO;
    }

    public User update(UserDTO updatedUser, User user)
    {
        user.setEmail(updatedUser.getEmail());
        user.setUsername(updatedUser.getUsername());
        user.setFirstname(updatedUser.getFirstname());
        user.setLastname(updatedUser.getLastname());
        return user;
    }


}
