#!/usr/bin/env python3
"""
Test discovery API endpoints for user 397286402299
"""

import requests
import json
from datetime import datetime

def test_discovery_api():
    """Test discovery API endpoints"""
    
    base_url = "http://localhost:8080"
    user_id = "397286402299"
    
    print(f"🔍 Testing Discovery API for user: {user_id}")
    print("=" * 60)
    
    # First, let's try to login or get a token for this user
    print("\n1️⃣ Attempting to get authentication token...")
    
    # Check if we can find this user's email in our test data
    # Based on our generated data, this should be one of the test emails
    test_email = f"1049@test.com"  # This user was the 50th generated (1000 + 49)
    test_password = "TestPassword123!"
    
    print(f"   Trying email: {test_email}")
    
    # Try to register this user first (in case they don't exist in auth)
    register_data = {
        "email": test_email,
        "password": test_password,
        "firstName": "Ben",
        "lastName": "Gonzalez",
        "dateOfBirth": "1992-01-01",
        "gender": "Male",
        "location": "Portland, OR"
    }
    
    try:
        register_response = requests.post(f"{base_url}/api/auth/register", json=register_data, timeout=10)
        print(f"   Register response: {register_response.status_code}")
        if register_response.status_code == 409:
            print("   ✅ User already exists (expected)")
        elif register_response.status_code == 201:
            print("   ✅ User registered successfully")
        else:
            print(f"   ⚠️  Unexpected register response: {register_response.text}")
    except Exception as e:
        print(f"   ❌ Register failed: {e}")
    
    # Now try to login
    login_data = {
        "email": test_email,
        "password": test_password
    }
    
    try:
        login_response = requests.post(f"{base_url}/api/auth/login", json=login_data, timeout=10)
        print(f"   Login response: {login_response.status_code}")
        
        if login_response.status_code == 200:
            login_result = login_response.json()
            access_token = login_result.get('accessToken')
            print("   ✅ Login successful")
            print(f"   Token (first 20 chars): {access_token[:20]}...")
        else:
            print(f"   ❌ Login failed: {login_response.text}")
            
            # Try creating a test token manually using the test endpoint
            print("\n   🔧 Trying test token endpoint...")
            test_token_response = requests.post(f"{base_url}/api/auth/test/generate-token", 
                                              json={"userId": user_id}, timeout=10)
            if test_token_response.status_code == 200:
                token_result = test_token_response.json()
                access_token = token_result.get('accessToken')
                print("   ✅ Test token generated")
                print(f"   Token (first 20 chars): {access_token[:20]}...")
            else:
                print(f"   ❌ Test token failed: {test_token_response.text}")
                return
                
    except Exception as e:
        print(f"   ❌ Login failed: {e}")
        return
    
    # Now test the discovery endpoints
    headers = {
        "Authorization": f"Bearer {access_token}",
        "Content-Type": "application/json"
    }
    
    print("\n2️⃣ Testing discovery status endpoint...")
    try:
        status_response = requests.get(f"{base_url}/api/discovery/matches/status", headers=headers, timeout=10)
        print(f"   Status response: {status_response.status_code}")
        if status_response.status_code == 200:
            status_data = status_response.json()
            print("   ✅ Status retrieved successfully")
            print(f"   Status data: {json.dumps(status_data, indent=2)}")
        else:
            print(f"   ❌ Status failed: {status_response.text}")
    except Exception as e:
        print(f"   ❌ Status request failed: {e}")
    
    print("\n3️⃣ Testing today's matches endpoint...")
    try:
        matches_response = requests.get(f"{base_url}/api/discovery/matches/today", headers=headers, timeout=10)
        print(f"   Matches response: {matches_response.status_code}")
        if matches_response.status_code == 200:
            matches_data = matches_response.json()
            print("   ✅ Matches retrieved successfully")
            print(f"   Success: {matches_data.get('success', False)}")
            print(f"   Message: {matches_data.get('message', 'No message')}")
            print(f"   Users count: {len(matches_data.get('users', []))}")
            print(f"   Batch ID: {matches_data.get('batchId', 'None')}")
            print(f"   Total count: {matches_data.get('totalCount', 0)}")
            print(f"   End of matches: {matches_data.get('endOfMatches', False)}")
            
            if matches_data.get('users'):
                print("   📋 Sample matches:")
                for i, user in enumerate(matches_data['users'][:3]):
                    print(f"     {i+1}. {user.get('connectId', 'Unknown')} - {user.get('firstName', 'Unknown')} {user.get('lastName', 'Unknown')}")
        else:
            print(f"   ❌ Matches failed: {matches_response.text}")
    except Exception as e:
        print(f"   ❌ Matches request failed: {e}")
    
    print("\n4️⃣ Testing matches countdown endpoint...")
    try:
        countdown_response = requests.get(f"{base_url}/api/discovery/matches/countdown", headers=headers, timeout=10)
        print(f"   Countdown response: {countdown_response.status_code}")
        if countdown_response.status_code == 200:
            countdown_data = countdown_response.json()
            print("   ✅ Countdown retrieved successfully")
            print(f"   Countdown data: {json.dumps(countdown_data, indent=2)}")
        else:
            print(f"   ❌ Countdown failed: {countdown_response.text}")
    except Exception as e:
        print(f"   ❌ Countdown request failed: {e}")
    
    print("\n" + "=" * 60)
    print("🔧 API TEST SUMMARY:")
    print("Check above results to see where the discovery flow is breaking")

def main():
    test_discovery_api()

if __name__ == "__main__":
    main()