#!/bin/sh
# This script updates the runtime configuration with environment variables

# Create config with the API URL from environment variable
echo "window.RUNTIME_CONFIG = {" > /app/public/config.js
echo "  API_URL: '${NEXT_PUBLIC_API_URL:-http://localhost:8088}'" >> /app/public/config.js
echo "};" >> /app/public/config.js

echo "Runtime config updated with API_URL: ${NEXT_PUBLIC_API_URL:-http://localhost:8088}"

# Start the Next.js application
exec "$@"
