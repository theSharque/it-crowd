echo "Run ollama..."
ollama serve &
sleep 1
echo "Pull model..."
ollama pull deepseek-coder-v2
echo "Model ready to use"
tail -f /dev/null
