# 빌더 이미지
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# settings.xml 먼저 복사
COPY .m2/settings.xml /root/.m2/settings.xml

# 프로젝트 복사
COPY . .

RUN ./mvnw clean package -DskipTests