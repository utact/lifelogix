# --- 1단계: Gradle을 사용하여 프로젝트 빌드 ---
# Gradle 8.5와 Java 17을 포함한 이미지를 빌드 환경으로 사용
FROM gradle:8.5.0-jdk17 AS build

# 작업 디렉토리 설정
WORKDIR /home/gradle/project

# Gradle 빌드에 필요한 파일들만 먼저 복사하여 Docker 캐시 활용 극대화
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

# 소스 코드 전체 복사
COPY src ./src

# Gradle Wrapper에 실행 권한 부여
RUN chmod +x ./gradlew

# Gradle을 사용하여 프로젝트를 빌드하고 실행 가능한 jar 파일 생성
RUN ./gradlew build --no-daemon


# --- 2단계: 빌드된 jar 파일을 실행 ---
# 안정적인 Eclipse Temurin JRE 이미지 사용
FROM eclipse-temurin:17-jre-jammy

# 이전 빌드 단계에서 생성된 jar 파일을 실행 환경으로 복사
# build/libs/lifelogix-0.0.1-SNAPSHOT.jar 파일을 app.jar 라는 이름으로 복사
COPY --from=build /home/gradle/project/build/libs/lifelogix-0.0.1-SNAPSHOT.jar app.jar

# 애플리케이션 실행 명령어
# 서버가 시작될 때 "java -jar /app.jar" 명령어가 실행
ENTRYPOINT ["java", "-jar", "/app.jar"]