#!/bin/sh
set -e

host="data-analytics-app" # Target service name
port="8080"               # Target service port
timeout=20                # Max wait time in seconds

echo "Waiting for $host:$port to be ready..."

for i in $(seq $timeout); do
    nc -z $host $port && break || echo "Still waiting for $host..."
    sleep 1
done

if [ $i = $timeout ]; then
    echo "Timeout reached. $host not available."
    exit 1
fi

echo "$host is up - starting Nginx"
# Execute the main Nginx command passed as arguments to the script
exec "$@"
