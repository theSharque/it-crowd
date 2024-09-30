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

* Run IT-Crowd
* Setup a GIT project you want to include (access to clone, push new branch)
* Wait...
* IT-Crowd clone your project to local folder
* Get all python functions/Java methods
* Use AI to optimize the method
* Check if it is applicable (just simple check changes)
* Create new branch nad push it into your git..(..hub, ..lab, ..any)
* All what you need is to create a PR/MR and review changes

### Currently available modules:
* Project (git) module - allow to download source code from remote GIT
* Java Junior dev - create an optimized version for each method bigger than 5 lines
* Python Junior dev - create an optimized version for each function bigger than 5 lines
* Internal chat (actually just a log on the screen)
* Generate new branch and create a commit with message
* Push branch to review
* Create single-installation dockerfile

### Future plan:
* Create docker-compose module file
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
