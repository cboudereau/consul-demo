#!/bin/sh
consul agent -data-dir /consul/data -node ${SERVICE_NAME}-$(hostname) ${CONSUL_ARGS} &
until wget -q -O- http://localhost:8500/v1/status/leader | grep 8300; do
  echo "Waiting for local consul agent to start"
  sleep 1
done

dumb-init java -jar /app/app.jar &

tail -f /dev/null