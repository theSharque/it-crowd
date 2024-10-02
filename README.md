# it-crowd

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
* Add settings
* Add russian comment in commit
* Add temperature param to settings (0 - clear code, 5 - crazy idea code)


# Please wait for the model to pull complete before after run! IT TAKES A TIME! Check logs or chat messages from Moss

## Raw locally
1. You need ollama instructions: https://ollama.com/download
2. Pull deepseek-coder-v2 consider: `ollama pull deepseek-coder-v2:latest`
3. You need a Java-17
4. Build `./gradlew bootJar`
5. Run the code `java -jar ./build/libs/it-crowd-0.0.1-SNAPSHOT.jar`

## Docker run
Run in terminal:
1. `./gradlew bootJar`
2. `docker build . --tag=it-crowd`
3. `docker container run -p 8080:8080 it-crowds`

## Docker-compose run
Run in terminal:
1. `./gradlew bootJar`
2. `docker-compose up -d`

### Currently available modules:
* Project (git) module - allow to download source code from remote GIT
* Java Junior dev - create an optimized version for each method bigger than 5 lines
* Python Junior dev - create an optimized version for each function bigger than 5 lines
* Internal chat (actually just a log on the screen)
* Generate new branch and create a commit with message
* Push branch to review
* Single-installation dockerfile
* Docker-compose module file
* Temperature settings
* Automatic pull model if not exists
* Add more applicable model to the list

### Future plan:
* Support for JavaScript / TypeScript
* Support ruby
* Support C/C++
* Add QA junior unit-test writer

# Used:
* https://github.com/ollama/ollama
* Spring boot
* JGit
* Magic
* Love
* Coffee
* Chocolate
