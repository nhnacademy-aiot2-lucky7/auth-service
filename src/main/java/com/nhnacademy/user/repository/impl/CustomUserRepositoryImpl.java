package com.nhnacademy.user.repository.impl;

import com.nhnacademy.user.domain.QUser;
import com.nhnacademy.user.domain.User;
import com.nhnacademy.user.dto.UserResponse;
import com.nhnacademy.user.repository.CustomUserRepository;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.Optional;

/**
 * 사용자에 대한 커스텀 조회 기능을 구현한 클래스입니다.
 * <p>
 * QueryDSL을 사용하여 사용자 정보를 조회하고, DTO(UserResponse)로 반환합니다.
 */
public class CustomUserRepositoryImpl extends QuerydslRepositorySupport implements CustomUserRepository {

    /**
     * 기본 생성자 - QuerydslRepositorySupport에 User 엔티티 클래스를 설정합니다.
     */
    public CustomUserRepositoryImpl() {
        super(User.class);
    }

    /**
     * 사용자 번호(userNo)를 기준으로 사용자 정보를 조회합니다.
     *
     * @param userNo 사용자 번호
     * @return 조회된 사용자 정보를 담은 {@link UserResponse}, 존재하지 않을 경우 Optional.empty()
     */
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

    /**
     * 사용자 이메일(userEmail)을 기준으로 사용자 정보를 조회합니다.
     *
     * @param userEmail 사용자 이메일
     * @return 조회된 사용자 정보를 담은 {@link UserResponse}, 존재하지 않을 경우 Optional.empty()
     */
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