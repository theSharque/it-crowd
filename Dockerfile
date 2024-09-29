FROM ollama/ollama:latest
EXPOSE 8080

# Install Java
RUN apt update
RUN apt install -y curl git openjdk-17-jre

# Install AI
RUN ollama pull deepseek-coder-v2:latest

# Install application
COPY build/libs/it-crowd-0.0.1-SNAPSHOT.jar application.jar
ENTRYPOINT java -jar ./application.jar
