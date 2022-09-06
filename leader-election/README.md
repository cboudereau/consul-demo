# leader-election

https://www.consul.io/docs/dynamic-app-config/sessions
https://learn.hashicorp.com/tutorials/consul/application-leader-elections
https://www.consul.io/api-docs/session

Automatic leader election based on serf health check (consul agent)

Because existing system is only based on 2 dc instead of 3, 4 consul servers are setup (2 per DC) intead of 3 [which has no impact](https://www.consul.io/docs/architecture/consensus#deployment_table)

## Demo

```bash
docker compose down --remove-orphans -v --rmi local && docker compose up
```

Observe the logs leader and stop it

```bash
docker kill leader-election-service-<Id Of the leader>
```

```bash
docker start leader-election-service-<Id Of the leader>
```

The lock is released since the (consul agent) serf Health check is ko.

Exec into the leader
```bash
docker compose exec -it --index 1 service sh
```

Kill java app (leader)
```bash
killall -9 java dumb-init
```

After the service health check has been in CRITICAL stauts, the next instance is elected

Start java app (old leader)
```bash
dumb-init java -jar /app/app.jar &
```

Kill consul agent
```bash
killall -9 consul
```

Start again consul agent
```bash
consul agent -data-dir /consul/data -node ${SERVICE_NAME}-$(hostname) ${CONSUL_ARGS} &
```

Kill/Start consul-server leader
```bash
docker kill leader-election-consul-server-1-1
```

leader election impact for a little time the batch the time to elect a new leader while loosing a non leader consul-server has no impact.