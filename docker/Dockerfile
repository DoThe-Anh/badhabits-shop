# =============================================================================
# Multi-stage Dockerfile cho Spring Boot app
#
# Build context khi `docker build`/`compose build` phải là PROJECT ROOT
# (cấp cha của docker/), vì Dockerfile COPY backend/ qua tương đối từ context.
# Compose đã set `context: ..` để xử lý chuyện này.
# =============================================================================

# ---------- Stage 1: build jar bằng Maven ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

# Copy pom.xml trước để Docker cache layer dependency download.
# Khi pom không đổi, layer này hit cache → dependency không tải lại.
COPY backend/pom.xml ./pom.xml
RUN mvn -B dependency:go-offline

# Copy source
COPY backend/src ./src

# Build jar (skip tests để build nhanh — CI/CD nên có stage test riêng)
RUN mvn -B -DskipTests package

# ---------- Stage 2: runtime — chỉ JRE + jar ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

# Tạo non-root user — best practice security
RUN groupadd -r app && useradd -r -g app app

COPY --from=build /workspace/target/badhabits-store-1.0.0.jar app.jar

# Mount point cho ảnh upload — bind volume từ host
RUN mkdir -p /uploads && chown app:app /uploads

USER app

EXPOSE 8080

# JVM options dev-friendly. Production có thể override qua JAVA_TOOL_OPTIONS env.
ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
