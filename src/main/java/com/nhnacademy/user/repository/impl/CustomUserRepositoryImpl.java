package com.nhnacademy.user.repository.impl;

import com.nhnacademy.user.domain.QUser;
import com.nhnacademy.user.domain.User;
import com.nhnacademy.user.dto.UserResponse;
import com.nhnacademy.user.repository.CustomUserRepository;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.Optional;

public class CustomUserRepositoryImpl extends QuerydslRepositorySupport implements CustomUserRepository {

    public CustomUserRepositoryImpl(){super(User.class);}

    @Override
    public Optional<UserResponse> findUserResponseByUserNo(Long userNo) {

        JPAQuery<UserResponse> query = new JPAQuery<>(getEntityManager());

        QUser qUser = QUser.user;

        return Optional.ofNullable(query
                .select(Projections.constructor(
                        UserResponse.class,
                        qUser.userRole,
                        qUser.userNo,
                        qUser.userName,
                        qUser.userEmail
                ))
                .from(qUser)
                .where(qUser.userNo.eq(userNo))
                .fetchOne());
    }

    @Override
    public Optional<UserResponse> findUserResponseByUserEmail(String userEmail) {

        JPAQuery<UserResponse> query = new JPAQuery<>(getEntityManager());

        QUser qUser = QUser.user;

        return Optional.ofNullable(query
                .select(Projections.constructor(
                        UserResponse.class,
                        qUser.userRole,
                        qUser.userNo,
                        qUser.userName,
                        qUser.userEmail
                ))
                .from(qUser)
                .where(qUser.userEmail.eq(userEmail))
                .fetchOne());
    }
}
