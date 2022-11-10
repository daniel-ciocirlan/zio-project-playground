## server

TODO add notes about server submodule

## app

This submodule is a ScalaJS app. To run in, you should `sbt> app/fastOptJS` (or
`~fastOptJS` if you want watch for changes).

You should then `docker-compose up nginx` which will host the files from a
web server, and be accessible from `http://localhost:8080`.