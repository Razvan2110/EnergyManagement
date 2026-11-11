package com.mycompany.userservice.services;

import com.mycompany.userservice.dto.UserRequestDTO;
import com.mycompany.userservice.dto.UserResponseDTO;
import java.util.List;

public interface UserService {
    UserResponseDTO createUser(UserRequestDTO userDto);
    UserResponseDTO getUserById(Long id);
    List<UserResponseDTO> getAllUsers();
    UserResponseDTO updateUser(Long id, UserRequestDTO userDto);
    void deleteUser(Long id);
}