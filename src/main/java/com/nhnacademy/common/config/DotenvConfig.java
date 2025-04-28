package com.nhnacademy.common.config;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * DotenvConfig 클래스는 .env 파일의 환경 변수를 로드하는 설정 클래스입니다.
 * <p>
 * 이 클래스는 Spring의 환경 설정을 통해 .env 파일에 정의된 키-값 쌍을 로드하며,
 * Dotenv 라이브러리를 사용하여 .env 파일을 읽고 환경 변수로 로드합니다.
 * </p>
 */
@Slf4j
@Configuration
public class DotenvConfig {

    /**
     * Dotenv 객체를 Bean으로 등록하여 .env 파일의 환경 변수를 애플리케이션에서 사용할 수 있도록 제공합니다.
     * <p>
     * 이 메서드는 .env 파일을 로드하며, 파일이 없을 경우에도 무시하고 계속 진행됩니다.
     * 로드된 환경 변수는 로그로 출력되어 확인할 수 있습니다.
     * </p>
     *
     * @return Dotenv 객체
     */
    @Bean
    public Dotenv dotenv() {
        return Dotenv.configure()
                .filename(".env")
                .ignoreIfMissing()
                .load();
    }
}