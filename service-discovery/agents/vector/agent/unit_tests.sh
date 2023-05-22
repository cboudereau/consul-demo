#! /bin/bash

if docker run --rm -v $(pwd):/app/vector_config/ timberio/vector:0.29.1-debian test --config-toml /app/vector_config/**/*.toml; then
    echo "all tests ... passed"
else
    echo "test failed! exit code : $?"
fi