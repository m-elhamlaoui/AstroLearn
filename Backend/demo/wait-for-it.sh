#!/usr/bin/env bash

# wait-for-it.sh is a pure bash script for waiting on the availability of a host and port.

set -e

host="$1"
shift
port="$1"
shift

cmd="$@"

echo "Waiting for $host:$port to be available..."

while ! nc -z "$host" "$port"; do
  sleep 1
done

echo "$host:$port is available. Starting the app..."
exec $cmd