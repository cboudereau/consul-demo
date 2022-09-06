#!/bin/sh
until wget -q -O- http://consul-server-1:8500/v1/status/leader | grep 8300; do
  echo "Waiting for consul-server to start"
  sleep 1
done

curl -X PUT -d primary http://consul-server-1:8500/v1/kv/sybase/dc \
&& curl -X PUT -d connStringPrimary http://consul-server-1:8500/v1/kv/sybase/primary/connectionString \
&& curl -X PUT -d connStringSecondary http://consul-server-1:8500/v1/kv/sybase/secondary/connectionString