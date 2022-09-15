# spring-integration

## Demo

```bash
docker compose down --remove-orphans -v --rmi local && docker compose up
```

Analyze the database
```bash
docker compose exec -it database psql -Upostgres
```

List databases
```
\l+
```

Select orders database
```
 \c orders
```

Show relations
```
\d+ 
```

Describe table orders
```sql
SELECT table_name, column_name, data_type FROM information_schema.columns WHERE table_name = 'orders';
```

List data
```sql
SELECT * FROM orders;
```

Observe the logs leader and stop it

```bash
docker kill spring-integration-batch-<Id Of the leader>
```

```bash
docker start spring-integration-batch-<Id Of the leader>
```

The lock is released since the (consul agent) serf Health check is ko.

Exec into the leader
```bash
docker compose exec -it --index 1 batch supervisorctl
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
docker start spring-integration-batch-1
```

```bash
docker compose exec -it --index 1 batch supervisorctl
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

After the service health check has been in CRITICAL stauts, the next instance is elected

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

Kill/Start consul-server leader
```bash
docker kill spring-integration-consul-server-1-1
```

leader election impact for a little time the batch the time to elect a new leader while loosing a non leader consul-server has no impact.