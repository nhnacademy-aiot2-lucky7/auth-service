# 빌더 이미지
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# GHCR 인증용 변수 선언
ARG GITHUB_ACTOR
ARG GITHUB_TOKEN
ENV GITHUB_ACTOR=$GITHUB_ACTOR
ENV GITHUB_TOKEN=$GITHUB_TOKEN

# settings.xml 먼저 복사
COPY .m2/settings.xml /root/.m2/settings.xml

# 프로젝트 복사
COPY . .

RUN ./mvnw clean package -DskipTests