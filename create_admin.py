#!/usr/bin/env python3
import json
import requests

# Create admin user using the API
admin_data = {
    "email": "admin@connect.com", 
    "password": "admin123"
}

try:
    response = requests.post(
        "http://localhost:8080/api/admin/setup/create-admin",
        headers={"Content-Type": "application/json"},
        json=admin_data
    )
    
    if response.status_code == 200:
        result = response.json()
        print("✅ Admin user created successfully!")
        print(f"📧 Email: {result.get('email')}")
        print(f"🆔 ConnectID: {result.get('connectId')}")
    else:
        print(f"❌ Failed to create admin user: {response.status_code}")
        print(f"Response: {response.text}")

except requests.exceptions.ConnectionError:
    print("❌ Could not connect to backend server. Make sure it's running on localhost:8080")
except Exception as e:
    print(f"❌ Error: {e}")