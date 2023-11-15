package com.eventplanner.mappers;

import com.eventplanner.dtos.UserDTO;
import com.eventplanner.entities.Users;

public class UsersMapper {

    public UserDTO toDTO(Users user)
    {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(user.getUsername());
        userDTO.setEmail(user.getEmail());
        userDTO.setFirstname(user.getFirstname());
        userDTO.setLastname(user.getLastname());
        return userDTO;
    }

    public Users update(UserDTO updatedUser, Users user)
    {
        user.setEmail(updatedUser.getEmail());
        user.setUsername(updatedUser.getUsername());
        user.setFirstname(updatedUser.getFirstname());
        user.setLastname(updatedUser.getLastname());
        return user;
    }


}
