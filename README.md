# Conway's Game of Life in Clojure

Small example application.

## Requirements

- [Java JDK](https://www.oracle.com/java/technologies/javase-downloads.html) version 11 or higher
- [Leiningen](https://leiningen.org/)

## Useful commands

Run tests when files are changed

    lein kaocha --watch

Start a REPL

    lein repl

Build an uberjar for distribution

    lein uberjar

Upgrade dependencies

    lein ancient upgrade :all :check-clojure :no-tests
