FROM ollama/ollama:latest

RUN apt update
RUN apt install -y curl git openjdk-17-jre
COPY build/libs/it-crowd-0.0.1-SNAPSHOT.jar application.jar
COPY run_in_docker.sh run_in_docker.sh
RUN chmod 777 ./run_in_docker.sh

ENTRYPOINT ./run_in_docker.sh
EXPOSE 8080
