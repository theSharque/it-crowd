# it-crowd

## Raw installation
1. You need ollama installed https://ollama.com/download
2. Pull deepseek-coder-v2 use command `ollama pull deepseek-coder-v2:latest`
3. You need to have Java-17
4. Run the code

## Docker run
Run in terminal:
* `docker build . --tag=it-crowd`
* `docker container run -p 8080:8080 it-crowds`

## Docker-compose run
Under construction (not usable yet :)

## Description
Project starts like a Just For Fun pet-project :) This IT-crowd will work for you for free, well almost for free.

### Currently available modules:
* Project (git) module - allow to download source code from remote GIT
* Java Junior dev - create an optimized version for each method bigger than 5 rows
* Add internal chat (actually just a log on the screen)
* Support for Python
* Generate new branch and create a commit with message
* Push branch to review
* Create single-installation dockerfile
* Create docker-compose module file

### Future plan:
* Support for JavaScript / TypeScript
* Add QA junior unit-test writer

# Used:
* https://github.com/ollama/ollama
* Spring boot
* JGit
* Magic
* Love
* Coffee
* Chocolate
