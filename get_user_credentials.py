#!/usr/bin/env python3
"""
Get login credentials for specific test users
"""

import os
import sys
from google.cloud import firestore
from google.oauth2 import service_account
import json

def main():
    # Initialize Firestore with service account
    key_path = "/Users/willgraham/Desktop/keys/firebase-service-account.json"
    
    if not os.path.exists(key_path):
        print(f"❌ Service account key not found at: {key_path}")
        return
    
    credentials = service_account.Credentials.from_service_account_file(key_path)
    db = firestore.Client(credentials=credentials, project="connect-ea4c2")
    
    print("🔐 GETTING TEST USER CREDENTIALS")
    print("=" * 60)
    
    # First, let's find any existing users
    try:
        users_ref = db.collection('users')
        users = users_ref.limit(10).stream()
        
        print("📋 EXISTING TEST USERS:")
        for doc in users:
            user_data = doc.to_dict()
            connect_id = doc.id
            email = user_data.get('email', 'Unknown')
            print(f"   🆔 {connect_id} - 📧 {email}")
        
    except Exception as e:
        print(f"❌ Error getting users: {e}")
    
    # Users we need credentials for
    test_users = ["346492379800", "523214175859"]
    
    print(f"\n🎯 SPECIFIC TEST USERS:")
    print("=" * 40)
    
    for user_id in test_users:
        print(f"\n👤 USER: {user_id}")
        
        # Get user profile to find name
        try:
            profile_doc = db.collection('userProfiles').document(user_id).get()
            if profile_doc.exists:
                profile_data = profile_doc.to_dict()
                first_name = profile_data.get('firstName', 'Unknown')
                last_name = profile_data.get('lastName', 'Unknown')
                print(f"   Name: {first_name} {last_name}")
            else:
                print(f"   ❌ No profile found")
                continue
        except Exception as e:
            print(f"   ❌ Error getting profile: {e}")
            continue
        
        # Get user auth info
        try:
            user_doc = db.collection('users').document(user_id).get()
            if user_doc.exists:
                user_data = user_doc.to_dict()
                email = user_data.get('email', 'Unknown')
                print(f"   📧 Email: {email}")
                
                # Standard test password for all generated users
                print(f"   🔑 Password: TestPassword123!")
                print(f"   🆔 Connect ID: {user_id}")
                
            else:
                print(f"   ❌ No user auth found")
        except Exception as e:
            print(f"   ❌ Error getting user auth: {e}")
    
    print("\n" + "=" * 60)
    print("📱 LOGIN INSTRUCTIONS:")
    print("1. Open the app on one device")
    print("2. Use the email and password above")
    print("3. Complete profile if needed")
    print("4. Go to Discovery and like the other user")
    print("5. Repeat on second device with the other user")
    print("6. Check for mutual match and conversation!")

if __name__ == "__main__":
    main()