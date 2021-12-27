# Empty Clojure Project

Something to get quickly started with Clojure for a code kata or similar.

## Requirements

- [Java JDK](https://www.oracle.com/java/technologies/javase-downloads.html) version 11 or higher
- [Leiningen](https://leiningen.org/)

### Recommended plugins

- Visual Studio Code:
    - [Calva](https://calva.io/)
    - [Parinfer](https://github.com/oakmac/vscode-parinfer)
- IntelliJ IDEA:
    - [Cursive](https://cursive-ide.com/)

## Useful commands

Run tests when files are changed

    lein kaocha --watch

Start a REPL

    lein repl

Build an uberjar for distribution

    lein uberjar
