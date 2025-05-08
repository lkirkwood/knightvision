#!/usr/bin/env sh


runtime=docker
command -v podman && runtime=podman

$runtime build -t knightvision -f test-server.Containerfile .
$runtime run -it -p 8080:80 knightvision
