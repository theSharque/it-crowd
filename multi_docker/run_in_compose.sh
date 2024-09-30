echo "Run and pull model..."
ollama serve &
sleep 5
ollama pull deepseek-coder-v2
echo "Ollama started!"
