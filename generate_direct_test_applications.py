#!/usr/bin/env python3
"""
Generate test applications directly in Firestore for admin testing
"""
import json
import random
from datetime import datetime, timedelta
from google.cloud import firestore
import os

# Set up Firebase credentials
os.environ['GOOGLE_APPLICATION_CREDENTIALS'] = '/Users/willgraham/Desktop/keys/firebase-service-account.json'

# Firestore configuration
PROJECT_ID = "connect-ea4c2"
db = firestore.Client(project=PROJECT_ID)

# Test data
FIRST_NAMES = ["Emma", "Liam", "Olivia", "Noah", "Ava", "Ethan", "Sophia", "Mason", "Isabella", "William"]
LAST_NAMES = ["Smith", "Johnson", "Brown", "Taylor", "Anderson", "Thomas", "Jackson", "White", "Harris", "Martin"]
LOCATIONS = ["London", "Manchester", "Birmingham", "Liverpool", "Bristol", "Sheffield", "Leeds", "Edinburgh", "Cardiff", "Glasgow"]
INDUSTRIES = ["Technology", "Healthcare", "Finance", "Marketing", "Education", "Fashion & Beauty", "Engineering", "Law", "Media", "Consulting"]
JOB_TITLES = ["Software Engineer", "Marketing Manager", "Doctor", "Teacher", "Consultant", "Designer", "Analyst", "Developer", "Manager", "Specialist"]
GENDERS = ["Male", "Female", "Non-binary"]
STATUSES = ["PENDING_APPROVAL", "APPROVED", "REJECTED"]

def generate_connect_id():
    """Generate a random 12-digit ConnectID"""
    return str(random.randint(100000000000, 999999999999))

def generate_email(first_name, last_name):
    """Generate a test email"""
    domains = ["test.com", "example.com", "demo.com"]
    return f"{first_name.lower()}.{last_name.lower()}@{random.choice(domains)}"

def generate_phone():
    """Generate a UK phone number"""
    return f"0{random.randint(1000000000, 9999999999)}"

def generate_birth_date():
    """Generate a birth date for someone aged 22-35"""
    age = random.randint(22, 35)
    birth_year = datetime.now().year - age
    birth_month = random.randint(1, 12)
    birth_day = random.randint(1, 28)
    return f"{birth_year}-{birth_month:02d}-{birth_day:02d}"

def generate_firestore_timestamp():
    """Generate a Firestore timestamp for submission"""
    days_ago = random.randint(1, 30)
    submission_time = datetime.now() - timedelta(days=days_ago)
    return submission_time

def generate_application():
    """Generate a single test application"""
    first_name = random.choice(FIRST_NAMES)
    last_name = random.choice(LAST_NAMES)
    connect_id = generate_connect_id()
    status = random.choice(STATUSES)
    submitted_at = generate_firestore_timestamp()
    
    application = {
        "connectId": connect_id,
        "firstName": first_name,
        "lastName": last_name,
        "email": generate_email(first_name, last_name),
        "dateOfBirth": generate_birth_date(),
        "gender": random.choice(GENDERS),
        "location": random.choice(LOCATIONS),
        "jobTitle": random.choice(JOB_TITLES),
        "industry": random.choice(INDUSTRIES),
        "phoneNumber": generate_phone(),
        "photoUrls": [
            "https://picsum.photos/400/600?random=" + str(random.randint(1, 1000)),
            "https://picsum.photos/400/600?random=" + str(random.randint(1, 1000)),
            "https://picsum.photos/400/600?random=" + str(random.randint(1, 1000))
        ],
        "bio": f"Test bio for {first_name} {last_name}",
        "whyJoinReason": "I'm interested in meeting new people and finding meaningful connections.",
        "status": status,
        "submittedAt": submitted_at
    }
    
    # Add review fields based on status
    if status == "APPROVED":
        application["reviewedAt"] = submitted_at + timedelta(hours=random.randint(1, 48))
        application["approvedAt"] = application["reviewedAt"]
        application["reviewedBy"] = "183600102436"  # Admin ConnectID
        application["reviewNotes"] = "Approved - good profile"
    elif status == "REJECTED":
        application["reviewedAt"] = submitted_at + timedelta(hours=random.randint(1, 48))
        application["rejectedAt"] = application["reviewedAt"]
        application["reviewedBy"] = "183600102436"  # Admin ConnectID
        application["rejectionReason"] = "Profile does not meet our community standards"
        application["reviewNotes"] = "Rejected - needs improvement"
    
    return connect_id, application

def create_application_in_firestore(connect_id, application_data):
    """Create an application document in Firestore"""
    try:
        doc_ref = db.collection('applicationSubmissions').document(connect_id)
        doc_ref.set(application_data)
        return True
    except Exception as e:
        print(f"❌ Error creating application {connect_id}: {e}")
        return False

def main():
    print("🚀 Generating 10 test applications directly in Firestore...")
    
    successful = 0
    failed = 0
    
    # Generate different types of applications
    statuses_to_create = ["PENDING_APPROVAL"] * 4 + ["APPROVED"] * 3 + ["REJECTED"] * 3
    random.shuffle(statuses_to_create)
    
    for i, target_status in enumerate(statuses_to_create):
        print(f"\n📝 Creating {target_status} application {i+1}/10...")
        
        connect_id, application = generate_application()
        application["status"] = target_status  # Override with target status
        
        if create_application_in_firestore(connect_id, application):
            print(f"✅ Successfully created {target_status} application for {application['firstName']} {application['lastName']} (ID: {connect_id})")
            successful += 1
        else:
            failed += 1
    
    print(f"\n🎉 Complete! Successfully created {successful} applications, {failed} failed.")
    print("Applications are now available in the admin dashboard for testing!")
    print(f"Created: ~4 PENDING, ~3 APPROVED, ~3 REJECTED applications")

if __name__ == "__main__":
    main()