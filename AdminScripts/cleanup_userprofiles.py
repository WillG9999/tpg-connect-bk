#!/usr/bin/env python3
"""
Firebase Firestore Collection Cleanup Script
Deletes all documents from 'userProfiles' collection except admin@connect.com

Usage: python3 cleanup_userprofiles.py
"""

import firebase_admin
from firebase_admin import credentials, firestore
import json
import os
import sys

def initialize_firebase():
    """Initialize Firebase Admin SDK with service account"""
    # Path to your Firebase service account key
    service_account_path = "/Users/willgraham/Desktop/keys/firebase-service-account.json"
    
    if not os.path.exists(service_account_path):
        print(f"❌ Error: Service account file not found at {service_account_path}")
        print("Please ensure the Firebase service account JSON file exists at the specified path.")
        sys.exit(1)
    
    try:
        # Initialize Firebase Admin SDK
        cred = credentials.Certificate(service_account_path)
        firebase_admin.initialize_app(cred)
        
        # Get Firestore client
        db = firestore.client()
        print("✅ Firebase initialized successfully")
        return db
    except Exception as e:
        print(f"❌ Error initializing Firebase: {e}")
        sys.exit(1)

def get_user_profiles(db):
    """Get all documents from userProfiles collection"""
    try:
        collection_ref = db.collection('userAuth')
        docs = collection_ref.stream()
        
        profiles = []
        for doc in docs:
            data = doc.to_dict()
            profiles.append({
                'id': doc.id,
                'email': data.get('email', 'NO_EMAIL'),
                'firstName': data.get('firstName', 'NO_NAME'),
                'lastName': data.get('lastName', ''),
                'data': data
            })
        
        return profiles
    except Exception as e:
        print(f"❌ Error getting user profiles: {e}")
        return []

def delete_profile(db, profile_id, email):
    """Delete a specific profile document"""
    try:
        db.collection('userAuth').document(profile_id).delete()
        print(f"🗑️  Deleted profile: {profile_id} ({email})")
        return True
    except Exception as e:
        print(f"❌ Error deleting profile {profile_id}: {e}")
        return False

def main():
    """Main cleanup function"""
    print("🧹 Firebase Firestore Cleanup Script")
    print("📄 Collection: userProfiles")
    print("🔒 Preserving: admin@connect.com")
    print("=" * 50)
    
    # Initialize Firebase
    db = initialize_firebase()
    
    # Get all user profiles
    print("\n📋 Fetching all user profiles...")
    profiles = get_user_profiles(db)
    
    if not profiles:
        print("ℹ️  No profiles found in userProfiles collection")
        return
    
    print(f"📊 Found {len(profiles)} total profiles")
    
    # Filter profiles to delete (all except admin@connect.com)
    admin_email = "admin@connect.com"
    profiles_to_delete = []
    admin_profiles = []
    
    for profile in profiles:
        if profile['email'] == admin_email:
            admin_profiles.append(profile)
        else:
            profiles_to_delete.append(profile)
    
    print(f"🔒 Found {len(admin_profiles)} admin profile(s) to preserve:")
    for admin in admin_profiles:
        print(f"   - {admin['id']} ({admin['firstName']} {admin['lastName']} - {admin['email']})")
    
    print(f"🗑️  Found {len(profiles_to_delete)} profile(s) to delete:")
    for profile in profiles_to_delete:
        print(f"   - {profile['id']} ({profile['firstName']} {profile['lastName']} - {profile['email']})")
    
    if not profiles_to_delete:
        print("✅ No profiles to delete. Admin profile is the only one present.")
        return
    
    # Confirmation prompt
    print(f"\n⚠️  WARNING: This will permanently delete {len(profiles_to_delete)} profile(s)!")
    print("This action cannot be undone.")
    
    while True:
        confirm = input("\nDo you want to proceed? (yes/no): ").lower().strip()
        if confirm in ['yes', 'y']:
            break
        elif confirm in ['no', 'n']:
            print("❌ Operation cancelled by user")
            return
        else:
            print("Please enter 'yes' or 'no'")
    
    # Delete profiles
    print(f"\n🗑️  Deleting {len(profiles_to_delete)} profiles...")
    deleted_count = 0
    failed_count = 0
    
    for profile in profiles_to_delete:
        if delete_profile(db, profile['id'], profile['email']):
            deleted_count += 1
        else:
            failed_count += 1
    
    # Summary
    print("\n" + "=" * 50)
    print("📊 CLEANUP SUMMARY")
    print("=" * 50)
    print(f"✅ Successfully deleted: {deleted_count} profiles")
    print(f"❌ Failed to delete: {failed_count} profiles")
    print(f"🔒 Preserved admin profiles: {len(admin_profiles)}")
    print(f"📈 Total profiles processed: {len(profiles)}")
    
    if failed_count > 0:
        print(f"\n⚠️  {failed_count} profiles failed to delete. Check the error messages above.")
    else:
        print(f"\n🎉 Cleanup completed successfully!")
        print(f"🔒 Only admin@connect.com profile(s) remain in the userProfiles collection.")

if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("\n\n❌ Operation cancelled by user (Ctrl+C)")
        sys.exit(1)
    except Exception as e:
        print(f"\n❌ Unexpected error: {e}")
        sys.exit(1)