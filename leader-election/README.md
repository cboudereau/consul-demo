# leader-election

https://www.consul.io/docs/dynamic-app-config/sessions
https://learn.hashicorp.com/tutorials/consul/application-leader-elections
https://www.consul.io/api-docs/session

Automatic leader election based on serf health check (consul agent)

Because existing system is only based on 2 dc instead of 3, 4 consul servers are setup (2 per DC) intead of 3 [which has no impact](https://www.consul.io/docs/architecture/consensus#deployment_table)

## Demo

```bash
./up.sh
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
docker compose exec -it --index 1 service supervisorctl
```

Status
```bash
status
```

Health check demo
```bash
stop app
```

Start the container
```bash
docker start leader-election-service-1
```

```bash
docker compose exec -it --index 1 service supervisorctl
```

The container is exited to be sure that both consul and app are up an running.

Stop the healthcheck for the demo
```bash
stop healthcheck
```

Kill java app (leader)
```bash
signal SIGKILL app
```

After the service health check has been in CRITICAL status, the next instance is elected

Start java app (old leader)
```bash
start app
```

Kill consul agent
```bash
signal SIGKILL consul
```

Start again consul agent
```bash
start consul
```

Service deregistration
```bash
stop consul
```

```bash
start consul
```

```bash
stop app
```

```bash
start app
```

Kill/Start consul-server leader
```bash
docker kill leader-election-consul-server-1-1
```

leader election impact for a little time the batch the time to elect a new leader while loosing a non leader consul-server has no impact.