export CONSUL_VERSION=1.15.2

docker compose down --remove-orphans -v --rmi local && docker compose up