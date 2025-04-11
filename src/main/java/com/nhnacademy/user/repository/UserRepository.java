package com.nhnacademy.user.repository;

import com.nhnacademy.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * User 엔티티에 대한 데이터 접근을 담당하는 리포지토리 인터페이스입니다.
 * <p>
 * Spring Data JPA에서 제공하는 기본 CRUD 기능을 포함하며, {@link CustomUserRepository}를 확장하여
 * QueryDSL 기반의 커스텀 조회 기능도 제공합니다.
 */
public interface UserRepository extends JpaRepository<User, Long>, CustomUserRepository {

    /**
     * 주어진 이메일을 가진 사용자가 존재하는지 여부를 확인합니다.
     *
     * @param userEmail 사용자 이메일
     * @return 존재하면 true, 그렇지 않으면 false
     */
    boolean existsByUserEmail(String userEmail);
}