# spring-integration

## Database intro

```bash
./up.sh
```

Analyze the database
```bash
docker compose exec -it database psql -Upostgres -dorders
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
## Demo

Observe the logs leader and stop it

```bash
docker kill spring-integration-batch-<Id Of the leader>
```

```bash
docker start spring-integration-batch-<Id Of the leader>
```

The lock is released since the (consul agent) serf Health check is ko.

Exec into the leader to manually stop/kill consul agent and/or the batch
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

At this step, the container is in status exited since one of the supervisor program is down

In the consul service dashboard, one service is down

Start the container
```bash
docker start spring-integration-batch-1
```

Once the container is started it should be healthy in the consul console

```bash
docker compose exec -it --index 1 batch supervisorctl
```

The container is exited to be sure that both consul and app are up an running.

Again, one service should be down and one up and running in consul

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

Here again a new leader is elected and one service is down in the consul console

Start again consul agent
```bash
start consul
```

What about consul cluster resilience ? 

Kill/Start consul-server leader
```bash
docker kill spring-integration-consul-server-1-1
```

leader election impact for a little time the batch the time to elect a new leader while loosing a non leader consul-server has no impact.