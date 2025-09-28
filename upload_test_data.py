#!/usr/bin/env python3
"""
Upload test data directly to Firebase using the Admin SDK
"""

import json
import firebase_admin
from firebase_admin import credentials, firestore
from datetime import datetime
import sys

def initialize_firebase():
    """Initialize Firebase Admin SDK"""
    try:
        # Check if already initialized
        firebase_admin.get_app()
        print("✅ Firebase already initialized")
    except ValueError:
        # Initialize Firebase
        cred = credentials.Certificate("/Users/willgraham/Desktop/keys/firebase-service-account.json")
        firebase_admin.initialize_app(cred)
        print("🔥 Firebase initialized")
    
    return firestore.client()

def upload_test_profiles():
    """Upload test profiles to Firestore"""
    
    # Initialize Firebase
    db = initialize_firebase()
    
    # Load test data
    json_file = "/Users/willgraham/Desktop/Connect/ConnectBackend/test_profiles_50.json"
    with open(json_file, 'r') as f:
        profiles = json.load(f)
    
    print(f"📖 Loaded {len(profiles)} profiles from {json_file}")
    
    # Upload in batches
    batch_size = 10
    total_uploaded = 0
    
    for i in range(0, len(profiles), batch_size):
        batch = profiles[i:i + batch_size]
        batch_num = (i // batch_size) + 1
        
        print(f"📦 Processing batch {batch_num} with {len(batch)} profiles...")
        
        # Create Firestore batch
        firestore_batch = db.batch()
        
        for profile in batch:
            connect_id = profile['connectId']
            
            # Clean up the profile data for Firestore
            cleaned_profile = clean_profile_for_firestore(profile)
            
            # Add to batch
            doc_ref = db.collection('user_profiles').document(connect_id)
            firestore_batch.set(doc_ref, cleaned_profile)
            
            print(f"  ✅ Prepared: {connect_id} - {profile['firstName']} {profile['lastName']}")
        
        # Commit batch
        try:
            firestore_batch.commit()
            total_uploaded += len(batch)
            print(f"🔥 Batch {batch_num} uploaded successfully")
            print(f"📊 Progress: {total_uploaded}/{len(profiles)} profiles uploaded")
        except Exception as e:
            print(f"❌ Error uploading batch {batch_num}: {e}")
            return False
    
    print(f"🎉 Successfully uploaded {total_uploaded} test profiles to Firestore!")
    return True

def clean_profile_for_firestore(profile):
    """Clean up profile data to be compatible with Firestore"""
    cleaned = {}
    
    for key, value in profile.items():
        if key in ['createdAt', 'updatedAt', 'lastActive']:
            # Convert timestamp objects to simple timestamps
            if value and isinstance(value, dict):
                # Convert to Firestore timestamp
                cleaned[key] = firestore.SERVER_TIMESTAMP
            else:
                cleaned[key] = value
        else:
            cleaned[key] = value
    
    return cleaned

def main():
    print("🚀 Starting test data upload to Firebase...")
    
    try:
        success = upload_test_profiles()
        if success:
            print("✅ Test data upload completed successfully!")
            sys.exit(0)
        else:
            print("❌ Test data upload failed!")
            sys.exit(1)
    except Exception as e:
        print(f"💥 Unexpected error: {e}")
        sys.exit(1)

if __name__ == "__main__":
    main()