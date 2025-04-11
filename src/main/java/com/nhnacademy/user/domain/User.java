package com.nhnacademy.user.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

/**
 * 사용자 정보를 나타내는 JPA 엔티티 클래스입니다.
 * <p>
 * 회원의 이름, 이메일, 비밀번호, 권한, 가입/수정/탈퇴 일자 등의 정보를 관리합니다.
 */
@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor
public class User {

    /**
     * 사용자 역할(권한)을 나타내는 열거형입니다.
     */
    public enum Role {
        USER,
        ADMIN
    }

    /**
     * 사용자 고유 번호 (Primary Key)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_no", nullable = false, unique = true, updatable = false)
    @Comment("사용자-번호")
    private Long userNo;

    /**
     * 사용자 역할 (USER 또는 ADMIN)
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false, updatable = false)
    @Comment("사용자-역할")
    private Role userRole;

    /**
     * 사용자 이름
     */
    @NotNull
    @Column(name = "user_name", nullable = false, length = 45)
    @Comment("사용자-이름")
    private String userName;

    /**
     * 사용자 이메일 (중복 불가)
     */
    @Column(name = "user_email", unique = true, nullable = false, length = 45)
    @Comment("사용자-이메일")
    private String userEmail;

    /**
     * 사용자 비밀번호
     */
    @Column(name = "user_password", nullable = false, length = 45)
    @Comment("사용자-비밀번호")
    private String userPassword;

    /**
     * 가입일자 (생성 시 자동 설정)
     */
    @Column(nullable = false, updatable = false)
    @Comment("가입일자")
    private LocalDateTime createdAt;

    /**
     * 정보 수정일자 (수정 시 자동 갱신)
     */
    @Comment("수정일자")
    private LocalDateTime updatedAt;

    /**
     * 탈퇴일자
     */
    @Comment("탈퇴일자")
    private LocalDateTime withdrawalAt;

    /**
     * 엔티티 저장 전 호출되어 가입일자를 설정합니다.
     */
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * 엔티티 업데이트 전 호출되어 수정일자를 설정합니다.
     */
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 현재 시점을 탈퇴일자로 설정합니다.
     */
    public void withdrawalAt() {
        this.withdrawalAt = LocalDateTime.now();
    }

    /**
     * 새로운 사용자 객체를 생성합니다. (생성자)
     *
     * @param userRole     사용자 권한
     * @param userName     이름
     * @param userEmail    이메일
     * @param userPassword 비밀번호
     */
    private User(Role userRole, String userName, String userEmail, String userPassword) {
        this.userRole = userRole;
        this.userName = userName;
        this.userEmail = userEmail;
        this.userPassword = userPassword;
    }

    /**
     * 모든 정보를 지정하여 새로운 사용자 객체를 생성합니다.
     *
     * @param userRole     사용자 권한
     * @param userName     이름
     * @param userEmail    이메일
     * @param userPassword 비밀번호
     * @return 생성된 사용자 객체
     */
    public static User ofNewUser(Role userRole, String userName, String userEmail, String userPassword) {
        return new User(userRole, userName, userEmail, userPassword);
    }

    /**
     * 기본 권한(USER)으로 새로운 사용자 객체를 생성합니다.
     *
     * @param userName     이름
     * @param userEmail    이메일
     * @param userPassword 비밀번호
     * @return 생성된 사용자 객체
     */
    public static User ofNewUser(String userName, String userEmail, String userPassword) {
        return new User(Role.USER, userName, userEmail, userPassword);
    }

    /**
     * 사용자 이름과 이메일을 업데이트합니다.
     *
     * @param userName  새로운 이름
     * @param userEmail 새로운 이메일
     */
    public void update(String userName, String userEmail) {
        this.userName = userName;
        this.userEmail = userEmail;
    }

    /**
     * 비밀번호를 변경합니다.
     *
     * @param userPassword 새로운 비밀번호
     */
    public void changePassword(String userPassword) {
        this.userPassword = userPassword;
    }
}