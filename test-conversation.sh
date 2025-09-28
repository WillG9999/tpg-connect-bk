#!/bin/bash

echo "Testing conversation creation between two users..."

# Test user IDs (adjust these to actual user IDs from your database)
USER1="346492379800"
USER2="986949226439"

echo "Creating conversation between $USER1 and $USER2"

# Test the conversation creation endpoint
curl -X POST http://localhost:8080/api/conversations/create \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -d "{
    \"connectId1\": \"$USER1\",
    \"connectId2\": \"$USER2\",
    \"matchId\": \"test_match_123\"
  }" \
  -v

echo "Test completed"