#!/usr/bin/env python3
"""
Create test scenario for mutual matching between two specific users
"""

import os
import sys
from google.cloud import firestore
from google.oauth2 import service_account
import json
from datetime import datetime

def main():
    # Initialize Firestore with service account
    key_path = "/Users/willgraham/Desktop/keys/firebase-service-account.json"
    
    if not os.path.exists(key_path):
        print(f"❌ Service account key not found at: {key_path}")
        return
    
    credentials = service_account.Credentials.from_service_account_file(key_path)
    db = firestore.Client(credentials=credentials, project="connect-ea4c2")
    
    user1_id = "346492379800"  # User 1
    user2_id = "523214175859"  # User 2
    
    print(f"🎯 Setting up mutual match test scenario")
    print(f"👤 User 1: {user1_id}")
    print(f"👤 User 2: {user2_id}")
    print("=" * 60)
    
    # 1. Clear any existing userActions for clean test
    print("1️⃣ CLEARING EXISTING USER ACTIONS...")
    try:
        # Clear user1 actions
        user1_actions_ref = db.collection('userActions').document(user1_id)
        user1_actions_ref.delete()
        print(f"✅ Cleared userActions for {user1_id}")
        
        # Clear user2 actions
        user2_actions_ref = db.collection('userActions').document(user2_id)
        user2_actions_ref.delete()
        print(f"✅ Cleared userActions for {user2_id}")
        
    except Exception as e:
        print(f"⚠️ Error clearing userActions: {e}")
    
    # 2. Create UserMatchPools for both users to see each other
    print("\n2️⃣ CREATING USER MATCH POOLS...")
    
    today = datetime.now().strftime("%Y-%m-%d")
    timestamp = datetime.now().isoformat() + "Z"
    
    # Create match pool for user1 to see user2
    user1_pool = {
        "connectId": user1_id,
        "lastUpdated": timestamp,
        "dailyEntries": [
            {
                "date": today,
                "algorithmVersion": "test-v1.0",
                "totalCandidatesEvaluated": 1,
                "matches": [
                    {
                        "matchConnectId": user2_id,
                        "compatibilityScore": 0.85,
                        "stabilityRank": 1,
                        "viewed": False,
                        "compatibilityHighlights": ["Highly compatible match", "Similar life goals"],
                        "commonFactors": ["Same geographic area", "Compatible relationship goals"]
                    }
                ]
            }
        ]
    }
    
    # Create match pool for user2 to see user1
    user2_pool = {
        "connectId": user2_id,
        "lastUpdated": timestamp,
        "dailyEntries": [
            {
                "date": today,
                "algorithmVersion": "test-v1.0",
                "totalCandidatesEvaluated": 1,
                "matches": [
                    {
                        "matchConnectId": user1_id,
                        "compatibilityScore": 0.85,
                        "stabilityRank": 1,
                        "viewed": False,
                        "compatibilityHighlights": ["Highly compatible match", "Similar life goals"],
                        "commonFactors": ["Same geographic area", "Compatible relationship goals"]
                    }
                ]
            }
        ]
    }
    
    try:
        # Write UserMatchPools for user1
        user1_pool_ref = db.collection('UserMatchPools').document(user1_id)
        user1_pool_ref.set(user1_pool)
        print(f"✅ Created UserMatchPools for {user1_id}")
        
        # Write UserMatchPools for user2
        user2_pool_ref = db.collection('UserMatchPools').document(user2_id)
        user2_pool_ref.set(user2_pool)
        print(f"✅ Created UserMatchPools for {user2_id}")
        
    except Exception as e:
        print(f"❌ Error creating UserMatchPools: {e}")
        return
    
    print("\n" + "=" * 60)
    print("🎯 TEST SCENARIO SETUP COMPLETE!")
    print(f"📱 Both users can now see each other in discovery")
    print(f"👆 User {user1_id} will see user {user2_id} as a potential match")
    print(f"👆 User {user2_id} will see user {user1_id} as a potential match")
    print(f"🔥 When both users LIKE each other, a mutual match should be detected!")
    print("\n📋 TESTING STEPS:")
    print("1. Login as user 346492379800 and like user 523214175859")
    print("2. Login as user 523214175859 and like user 346492379800")
    print("3. Check if mutual match is detected and conversation is created")

if __name__ == "__main__":
    main()