FROM docker.elastic.co/logstash/logstash-oss:8.8.0

RUN bin/logstash-plugin install logstash-output-loki

USER logstash

RUN mkdir -p /tmp/logstash-input