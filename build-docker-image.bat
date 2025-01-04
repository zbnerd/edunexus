#!/bin/bash

docker build -t edu-nexus-discovery:latest -f edu-nexus-discovery/Dockerfile .

docker build -t edu-nexus-course-service:latest -f edu-nexus-course-service/Dockerfile .

docker build -t edu-nexus-enrollment-service:latest -f edu-nexus-enrollment-service/Dockerfile .
docker build -t edu-nexus-playback-service:latest -f edu-nexus-playback-service/Dockerfile .
docker build -t edu-nexus-file-manage-service:latest -f edu-nexus-file-manage-service/Dockerfile .
docker build -t edu-nexus-user-service:latest -f edu-nexus-user-service/Dockerfile .
docker build -t edu-nexus-graphql:latest -f edu-nexus-graphql/Dockerfile .