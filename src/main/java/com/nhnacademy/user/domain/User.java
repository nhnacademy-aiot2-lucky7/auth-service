package com.nhnacademy.user.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor
public class User {

    public enum Role{
        USER,
        ADMIN
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_no")
    @Comment("사용자-번호")
    private Long userNo;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "user_role")
    @Comment("사용자-역할")
    private Role userRole;

    @NotNull
    @Column(name = "user_name", nullable = false, length = 45)
    @Comment("사용자-이름")
    private String userName;

    @Column(name = "user_email", unique = true, nullable = false, length = 45)
    @Comment("사용자-이메일")
    private String userEmail;

    @Column(name = "user_password", nullable = false, length = 45)
    @Comment("사용자-비밀번호")
    private String userPassword;


    @Column(name = "user_token", unique = true)
    @Comment("refresh-token")
    private String refreshToken;

    @Column(nullable = false, updatable = false)
    @Comment("가입일자")
    private LocalDateTime createdAt;

    @Comment("수정일자")
    private LocalDateTime updatedAt;

    @Comment("탈퇴일자")
    private LocalDateTime withdrawalAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void withdrawalAt(){
        this.withdrawalAt = LocalDateTime.now();
    }

    private User(Role userRole, String userName, String userEmail, String userPassword) {
        this.userRole = userRole;
        this.userName = userName;
        this.userEmail = userEmail;
        this.userPassword = userPassword;
    }

    public static User ofNewUser(Role userRole, String userName, String userEmail, String userPassword) {
        return new User(userRole, userName, userEmail, userPassword);
    }

    public static User ofNewUser(String userName, String userEmail, String userPassword) {
        return new User(Role.USER, userName, userEmail, userPassword);
    }

    public void update(String userName, String userEmail){
        this.userName = userName;
        this.userEmail = userEmail;
    }

    public void changePassword(String userPassword){
        this.userPassword = userPassword;
    }
}
