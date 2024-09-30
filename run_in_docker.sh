echo "Run and pull model..."
ollama serve &
ollama pull deepseek-coder-v2
echo "Ollama started!"
java -jar ./application.jar
