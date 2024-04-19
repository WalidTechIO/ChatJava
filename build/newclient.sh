#!/usr/bin/env bash
docker run -e DISPLAY=$DISPLAY --network chat_default -v /tmp/.X11-unix:/tmp/.X11-unix walkloly/chat:client
