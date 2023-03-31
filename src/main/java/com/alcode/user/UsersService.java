package com.alcode.user;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class UsersService {
    private final UserRepository userRepository;

    public UsersService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserEntity createUser(Long chatId, String firstName, String lastName) {
        Optional<UserEntity> byId = userRepository.findById(chatId);
        if (byId.isEmpty()) {
            UserEntity entity = new UserEntity();
            entity.setChatId(chatId);
            entity.setFirstName(firstName);
            entity.setLastName(lastName);
            entity.setRole(Role.ROLE_USER);
            entity.setRegisterAt(LocalDateTime.now());

            userRepository.save(entity);
            return entity;
        }

        return byId.get();
    }

    public Iterable<UserEntity> getAll() {
        return userRepository.findAll();
    }

    public Role getRoleByChatId(Long chatId) {
        Optional<UserEntity> userByChatId = userRepository.getUserByChatId(chatId);
        if (userByChatId.isEmpty()) {
            return null;
        }
        UserEntity entity = userByChatId.get();
        return entity.getRole();
    }

    public List<Long> getChatIdByRole(Role role) {
        List<UserEntity> usersByRole = userRepository.findByRole(role);
        List<Long> result = new ArrayList<>();
        for (UserEntity entity : usersByRole) {
            result.add(entity.getChatId());
        }

        return result;
    }

    public Boolean changeRole(Long chatId, Role role) {
        Optional<UserEntity> userByChatId = userRepository.getUserByChatId(chatId);
        if (userByChatId.isEmpty()) {
            throw new UserNotFoundException("There is no user by this id");
        }
        UserEntity entity = userByChatId.get();
        entity.setRole(role);
        userRepository.save(entity);
        return true;
    }

}
