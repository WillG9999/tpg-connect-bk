#!/usr/bin/env python3
"""
Debug script to check user match data in Firebase Firestore
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
    
    user_id = "523214175859"
    print(f"🔍 Debugging match data for user: {user_id}")
    print("=" * 60)
    
    # 1. Check userActions document
    print("1️⃣ CHECKING USER ACTIONS:")
    try:
        user_actions_ref = db.collection('userActions').document(user_id)
        user_actions_doc = user_actions_ref.get()
        
        if user_actions_doc.exists:
            data = user_actions_doc.to_dict()
            print(f"✅ UserActions document exists")
            print(f"   - Likes: {len(data.get('likes', []))} users")
            print(f"   - Passes: {len(data.get('passes', []))} users") 
            print(f"   - Matches: {len(data.get('matches', []))} users")
            print(f"   - Liked by: {len(data.get('likedBy', []))} users")
            print(f"   - Last updated: {data.get('lastUpdated', 'N/A')}")
            
            # Show details
            likes = data.get('likes', [])
            passes = data.get('passes', [])
            if likes:
                print(f"   - Liked users: {likes}")
            if passes:
                print(f"   - Passed users: {passes}")
        else:
            print("❌ No userActions document found")
    except Exception as e:
        print(f"❌ Error checking userActions: {e}")
    
    print("\n" + "=" * 60)
    
    # 2. Check UserMatchPools document
    print("2️⃣ CHECKING USER MATCH POOLS:")
    try:
        match_pools_ref = db.collection('UserMatchPools').document(user_id)
        match_pools_doc = match_pools_ref.get()
        
        if match_pools_doc.exists:
            data = match_pools_doc.to_dict()
            print(f"✅ UserMatchPools document exists")
            
            daily_entries = data.get('dailyEntries', [])
            print(f"   - Daily entries: {len(daily_entries)}")
            print(f"   - Last updated: {data.get('lastUpdated', 'N/A')}")
            
            total_matches = 0
            unviewed_matches = 0
            
            for i, entry in enumerate(daily_entries):
                date = entry.get('date', 'Unknown')
                matches = entry.get('matches', [])
                total_matches += len(matches)
                
                entry_unviewed = sum(1 for match in matches if not match.get('viewed', False))
                unviewed_matches += entry_unviewed
                
                print(f"   - Day {i+1} ({date}): {len(matches)} matches, {entry_unviewed} unviewed")
                
                # Show match details for latest entries
                if i < 2:  # Show first 2 entries in detail
                    for j, match in enumerate(matches):
                        match_id = match.get('matchConnectId', 'Unknown')
                        viewed = match.get('viewed', False)
                        score = match.get('compatibilityScore', 0)
                        print(f"     └ Match {j+1}: {match_id} (viewed: {viewed}, score: {score:.2f})")
            
            print(f"   - TOTAL: {total_matches} matches, {unviewed_matches} unviewed")
        else:
            print("❌ No UserMatchPools document found")
    except Exception as e:
        print(f"❌ Error checking UserMatchPools: {e}")
    
    print("\n" + "=" * 60)
    
    # 3. Analysis
    print("3️⃣ ANALYSIS:")
    try:
        # Get acted-on users from userActions
        user_actions_ref = db.collection('userActions').document(user_id)
        user_actions_doc = user_actions_ref.get()
        acted_on_users = set()
        
        if user_actions_doc.exists:
            data = user_actions_doc.to_dict()
            acted_on_users.update(data.get('likes', []))
            acted_on_users.update(data.get('passes', []))
        
        # Get available matches from UserMatchPools
        match_pools_ref = db.collection('UserMatchPools').document(user_id)
        match_pools_doc = match_pools_ref.get()
        available_matches = set()
        unviewed_available = []
        
        if match_pools_doc.exists:
            data = match_pools_doc.to_dict()
            daily_entries = data.get('dailyEntries', [])
            
            for entry in daily_entries:
                matches = entry.get('matches', [])
                for match in matches:
                    match_id = match.get('matchConnectId')
                    viewed = match.get('viewed', False)
                    if match_id:
                        available_matches.add(match_id)
                        if not viewed and match_id not in acted_on_users:
                            unviewed_available.append(match_id)
        
        print(f"   - Total users acted on: {len(acted_on_users)}")
        print(f"   - Total available matches: {len(available_matches)}")
        print(f"   - Unviewed, unacted matches: {len(unviewed_available)}")
        
        if unviewed_available:
            print(f"   - Available match IDs: {unviewed_available}")
        
        # Check overlap
        overlap = acted_on_users.intersection(available_matches)
        print(f"   - Overlap (acted + available): {len(overlap)}")
        
        if len(unviewed_available) == 0:
            print("   ⚠️  USER HAS EXHAUSTED ALL AVAILABLE MATCHES")
            print("   💡 Need to generate new daily matches or reset user actions for testing")
        else:
            print("   ✅ User has available matches - check filtering logic")
            
    except Exception as e:
        print(f"❌ Error in analysis: {e}")

if __name__ == "__main__":
    main()