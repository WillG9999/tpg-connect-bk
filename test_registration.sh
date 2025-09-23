#!/bin/bash

echo "=== Testing Connect Backend Registration ==="
echo "Starting server..."

# Start the server in background
nohup ./mvnw spring-boot:run > server.log 2>&1 &
SERVER_PID=$!

echo "Server started with PID: $SERVER_PID"
echo "Waiting for server to start..."

# Wait for server to be ready
for i in {1..30}; do
    if curl -s http://localhost:8080/actuator/health >/dev/null 2>&1; then
        echo "Server is ready!"
        break
    fi
    echo "Waiting... ($i/30)"
    sleep 2
done

# Test registration endpoint
echo ""
echo "=== Testing Registration Endpoint ==="

REGISTRATION_DATA='{
  "email": "test@example.com",
  "password": "TestPass123!",
  "confirmPassword": "TestPass123!",
  "firstName": "John",
  "lastName": "Doe",
  "dateOfBirth": "1995-01-15",
  "gender": "Male",
  "location": "New York"
}'

echo "Sending registration request..."
echo "Data: $REGISTRATION_DATA"

RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d "$REGISTRATION_DATA")

echo ""
echo "Response:"
echo "$RESPONSE" | jq '.' 2>/dev/null || echo "$RESPONSE"

echo ""
echo "=== Checking Server Logs ==="
tail -20 server.log

echo ""
echo "=== Cleanup ==="
kill $SERVER_PID
echo "Server stopped"

rm -f server.log