package com.nhnacademy.user.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_no")
    @Comment("사용자-번호")
    private Long userNo;

    @Column(name = "user_name", nullable = false, length = 50)
    @Comment("사용자-이름")
    private String userName;

    @Column(name = "user_email", nullable = false, length = 100)
    @Comment("사용자-이메일")
    private String userEmail;

    @Column(nullable = false, length = 100)
    @Comment("사용자-비밀번호")
    private String userPassword;

    public User() {
    }

    private User(String userName, String userEmail, String userPassword) {
        this.userName = userName;
        this.userEmail = userEmail;
        this.userPassword = userPassword;
    }

    public static User ofNewUser(String userName, String userEmail, String userPassword) {
        return new User(userName, userEmail, userPassword);
    }

    public Long getUserNo() {
        return userNo;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getUserPassword() {
        return userPassword;
    }

    @Override
    public String toString() {
        return "User{" +
                "userNo=" + userNo +
                ", userName='" + userName + '\'' +
                ", userEmail='" + userEmail + '\'' +
                ", userPassword='" + userPassword + '\'' +
                '}';
    }
}
