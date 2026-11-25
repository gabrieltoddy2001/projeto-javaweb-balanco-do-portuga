# ---------- Etapa 1: compila o .war com Maven ----------
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B package -DskipTests

# ---------- Etapa 2: roda no Payara Micro (Jakarta EE 10, mesma base do GlassFish) ----------
FROM payara/micro:6.2025.11-jdk21

# Pasta de dados da aplicação (banco SQLite + fotos dos veículos)
ENV DB_PATH=/opt/payara/dados/balancodoportuga.db \
    UPLOAD_DIR=/opt/payara/dados/uploads/veiculos \
    DEMO_MODE=true \
    PORT=8080

USER root
RUN mkdir -p /opt/payara/dados/uploads/veiculos && chown -R payara:payara /opt/payara/dados
USER payara

# Fotos usadas pelos dados de demonstração
COPY --chown=payara:payara demo/veiculos/ /opt/payara/dados/uploads/veiculos/
COPY --chown=payara:payara --from=build /app/target/BalancoDoPortugaWeb-1.0-SNAPSHOT.war /opt/payara/deployments/ROOT.war

EXPOSE 8080

# Memória ajustada para caber no plano gratuito do Render (512 MB)
ENTRYPOINT ["sh", "-c", "exec java -Xms96m -Xmx300m -XX:MaxMetaspaceSize=160m -XX:+UseSerialGC -Xss512k -jar /opt/payara/payara-micro.jar --nocluster --port ${PORT} --contextroot / --deploy /opt/payara/deployments/ROOT.war"]
