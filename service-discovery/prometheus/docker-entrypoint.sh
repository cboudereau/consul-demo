#!/bin/sh
consul agent -data-dir /consul/data -node ${SERVICE_NAME}-$(hostname) ${CONSUL_ARGS} &
until wget -q -O- http://localhost:8500/v1/status/leader | grep 8300; do
  echo "Waiting for local consul agent to start"
  sleep 1
done

echo "Consul agent started"

su -l -s /bin/sh - nobody -c '/bin/prometheus --config.file=/etc/prometheus/prometheus.yml --storage.tsdb.path=/prometheus --web.console.libraries=/usr/share/prometheus/console_libraries --web.console.templates=/usr/share/prometheus/consoles'