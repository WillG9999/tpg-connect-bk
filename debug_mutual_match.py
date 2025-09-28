#!/usr/bin/env python3
"""
Debug mutual match detection logic
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
    
    # From the logs, these users were involved
    user1_id = "346492379800"  # This user liked 986949226439
    user2_id = "986949226439"  # Check if this user liked 346492379800 back
    
    print(f"🔍 DEBUGGING MUTUAL MATCH DETECTION")
    print(f"👤 User 1: {user1_id}")
    print(f"👤 User 2: {user2_id}")
    print("=" * 60)
    
    # Check userActions for both users
    for user_id in [user1_id, user2_id]:
        print(f"\n🔍 USER ACTIONS FOR: {user_id}")
        print("-" * 40)
        
        try:
            user_actions_ref = db.collection('userActions').document(user_id)
            user_actions_doc = user_actions_ref.get()
            
            if user_actions_doc.exists:
                data = user_actions_doc.to_dict()
                likes = data.get('likes', [])
                liked_by = data.get('likedBy', [])
                matches = data.get('matches', [])
                
                print(f"✅ UserActions document exists")
                print(f"   📤 Likes (users this user liked): {likes}")
                print(f"   📥 Liked By (users who liked this user): {liked_by}")
                print(f"   💕 Matches: {matches}")
                
                # Check if mutual like exists
                other_user = user2_id if user_id == user1_id else user1_id
                
                has_liked = other_user in likes
                is_liked_by = other_user in liked_by
                
                print(f"   🤔 Has liked {other_user}: {has_liked}")
                print(f"   🤔 Is liked by {other_user}: {is_liked_by}")
                
                if has_liked and is_liked_by:
                    print(f"   🎉 MUTUAL MATCH SHOULD BE DETECTED!")
                elif has_liked:
                    print(f"   ⏳ User liked {other_user}, waiting for reciprocation")
                elif is_liked_by:
                    print(f"   ⏳ User is liked by {other_user}, needs to like back")
                else:
                    print(f"   ❌ No mutual interaction yet")
                    
            else:
                print(f"❌ No userActions document found")
        except Exception as e:
            print(f"❌ Error checking userActions: {e}")
    
    print("\n" + "=" * 60)
    print("🧮 MUTUAL MATCH ANALYSIS:")
    
    try:
        # Get both user action documents
        user1_doc = db.collection('userActions').document(user1_id).get()
        user2_doc = db.collection('userActions').document(user2_id).get()
        
        user1_data = user1_doc.to_dict() if user1_doc.exists else {}
        user2_data = user2_doc.to_dict() if user2_doc.exists else {}
        
        user1_likes = user1_data.get('likes', [])
        user1_liked_by = user1_data.get('likedBy', [])
        user2_likes = user2_data.get('likes', [])
        user2_liked_by = user2_data.get('likedBy', [])
        
        # Check mutual like logic as in UserActionsService
        user1_likes_user2 = user2_id in user1_likes
        user2_likes_user1 = user1_id in user2_likes
        
        # Check the actual logic from UserActionsService.addLikeAction()
        # When user1 likes user2, we check if user2 is in user1's likedBy array
        user1_to_user2_mutual = user2_id in user1_liked_by  # This is the check in addLikeAction
        user2_to_user1_mutual = user1_id in user2_liked_by  # This is the check in addLikeAction
        
        print(f"📊 User1 ({user1_id}) likes User2 ({user2_id}): {user1_likes_user2}")
        print(f"📊 User2 ({user2_id}) likes User1 ({user1_id}): {user2_likes_user1}")
        print(f"📊 User1's likedBy contains User2: {user1_to_user2_mutual}")
        print(f"📊 User2's likedBy contains User1: {user2_to_user1_mutual}")
        
        if user1_likes_user2 and user2_likes_user1:
            print(f"✅ BOTH USERS LIKE EACH OTHER!")
            if user1_to_user2_mutual or user2_to_user1_mutual:
                print(f"✅ Mutual match detection should work")
            else:
                print(f"❌ BUG: likedBy arrays not properly updated!")
        else:
            print(f"⏳ Mutual like not complete yet")
            
    except Exception as e:
        print(f"❌ Error in mutual match analysis: {e}")

if __name__ == "__main__":
    main()