# Splunk

## config
host from path : \/data\/([^_]+).*

## field extractor

http://localhost:8000/en-US/app/search/field_extractor?sourcetype=testapp

## queries
index="testapp" | stats count by host | mcollect index="testapp_metrics"