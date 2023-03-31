package com.alcode.user;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor

@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    private Long chatId;

    @Column
    private String firstName;

    @Column
    private String lastName;

    @Column
    private LocalDateTime registerAt;

    @Enumerated(value = EnumType.STRING)
    @Column
    private Role role;
}
