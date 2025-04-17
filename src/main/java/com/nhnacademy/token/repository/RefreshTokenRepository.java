package com.nhnacademy.token.repository;

import com.nhnacademy.token.domain.RefreshToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * RefreshTokenRepository 인터페이스는 RefreshToken 엔티티에 대한 CRUD 작업을 제공합니다.
 * <p>
 * 이 인터페이스는 Spring Data JPA에서 제공하는 {@link CrudRepository}를 확장하여
 * Redis에서 RefreshToken을 저장, 조회, 삭제 등의 작업을 처리합니다.
 * </p>
 */
@Repository
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {

}
