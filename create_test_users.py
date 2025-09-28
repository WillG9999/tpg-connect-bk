#!/usr/bin/env python3
"""
Create test users for mutual matching
"""

import requests
import json

def main():
    base_url = "http://localhost:8080"
    
    # Test users for mutual matching
    test_users = [
        {
            "connectId": "346492379800",
            "email": "user1@test.com",
            "password": "TestPassword123!",
            "confirmPassword": "TestPassword123!",
            "firstName": "Alice",
            "lastName": "Johnson",
            "dateOfBirth": "1995-03-15",
            "gender": "Female",
            "location": "San Francisco, CA"
        },
        {
            "connectId": "523214175859", 
            "email": "user2@test.com",
            "password": "TestPassword123!",
            "confirmPassword": "TestPassword123!",
            "firstName": "Bob",
            "lastName": "Smith",
            "dateOfBirth": "1993-07-22",
            "gender": "Male",
            "location": "San Francisco, CA"
        }
    ]
    
    print("🔐 CREATING TEST USERS FOR MUTUAL MATCHING")
    print("=" * 60)
    
    for user in test_users:
        print(f"\n👤 Creating user: {user['firstName']} {user['lastName']}")
        print(f"   📧 Email: {user['email']}")
        print(f"   🔑 Password: {user['password']}")
        print(f"   🆔 Connect ID: {user['connectId']}")
        
        # Register user
        try:
            register_response = requests.post(f"{base_url}/api/auth/register", json=user, timeout=10)
            print(f"   📝 Register response: {register_response.status_code}")
            
            if register_response.status_code == 201:
                print("   ✅ User registered successfully")
            elif register_response.status_code == 409:
                print("   ✅ User already exists (expected)")
            else:
                print(f"   ⚠️  Unexpected response: {register_response.text}")
                
        except Exception as e:
            print(f"   ❌ Registration failed: {e}")
    
    print("\n" + "=" * 60)
    print("🎯 TEST USERS READY!")
    print("\n📱 DEVICE 1 LOGIN:")
    print(f"   📧 Email: {test_users[0]['email']}")
    print(f"   🔑 Password: {test_users[0]['password']}")
    print(f"   👤 User: {test_users[0]['firstName']} {test_users[0]['lastName']}")
    
    print("\n📱 DEVICE 2 LOGIN:")
    print(f"   📧 Email: {test_users[1]['email']}")
    print(f"   🔑 Password: {test_users[1]['password']}")
    print(f"   👤 User: {test_users[1]['firstName']} {test_users[1]['lastName']}")
    
    print("\n🚀 TESTING STEPS:")
    print("1. Login on Device 1 with Alice's credentials")
    print("2. Go to Discovery and like Bob")
    print("3. Login on Device 2 with Bob's credentials") 
    print("4. Go to Discovery and like Alice")
    print("5. Check for 'It's a Match!' notification")
    print("6. Verify conversation is available in chat")

if __name__ == "__main__":
    main()