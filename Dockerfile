# ----------------------------------------------------
# STAGE 1: BUILD (Compila o JAR usando o Maven)
# ----------------------------------------------------
# Usamos a imagem oficial do Maven com o JDK (Java Development Kit)
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Define o diretório de trabalho dentro do container
WORKDIR /app

# Copia os arquivos de configuração (pom.xml) para otimizar o cache do Docker
COPY pom.xml .
# O Railway/Render fará o build a partir do GitHub, então copiamos o código fonte
COPY src /app/src

# Comando de Build do Maven (gera o JAR)
# skipTests para acelerar o processo de build da imagem final
RUN mvn clean install -DskipTests

# ----------------------------------------------------
# STAGE 2: EXECUÇÃO (Runtime)
# ----------------------------------------------------
# Usamos uma imagem base leve que só tem o JRE (Java Runtime Environment)
FROM eclipse-temurin:21-jre-alpine

# Define o diretório onde a aplicação estará
WORKDIR /app

# Copia o JAR compilado da primeira etapa para a etapa de execução
COPY --from=build /app/target/*.jar app.jar

# Define a porta que o Spring Boot usa
EXPOSE 8080

# Comando de inicialização da aplicação (entrypoint)
# Inicia o JAR e configura a memória para evitar OutOfMemoryErrors em containers pequenos
ENTRYPOINT ["java", "-Xmx300m", "-jar", "app.jar"]