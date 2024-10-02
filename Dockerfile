FROM ollama/ollama:latest

RUN apt update
RUN apt install -y git openjdk-17-jre
COPY build/libs/it-crowd-0.0.1-SNAPSHOT.jar application.jar

RUN echo "ollama serve &" > ./runollama.sh
RUN echo "sleep 1" >> ./runollama.sh
RUN echo "java -jar ./application.jar" >> ./runollama.sh
RUN chmod +x ./runollama.sh

ENTRYPOINT ./runollama.sh
EXPOSE 8080
