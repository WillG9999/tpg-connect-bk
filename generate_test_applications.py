#!/usr/bin/env python3
"""
Generate test applications for admin testing
"""
import requests
import json
import random
import time
from datetime import datetime, timedelta

# Server configuration
BASE_URL = "http://10.248.182.45:8080"
APPLY_ENDPOINT = f"{BASE_URL}/api/applications/submit"

# Test data
FIRST_NAMES = ["Emma", "Liam", "Olivia", "Noah", "Ava", "Ethan", "Sophia", "Mason", "Isabella", "William"]
LAST_NAMES = ["Smith", "Johnson", "Brown", "Taylor", "Anderson", "Thomas", "Jackson", "White", "Harris", "Martin"]
LOCATIONS = ["London", "Manchester", "Birmingham", "Liverpool", "Bristol", "Sheffield", "Leeds", "Edinburgh", "Cardiff", "Glasgow"]
INDUSTRIES = ["Technology", "Healthcare", "Finance", "Marketing", "Education", "Fashion & Beauty", "Engineering", "Law", "Media", "Consulting"]
JOB_TITLES = ["Software Engineer", "Marketing Manager", "Doctor", "Teacher", "Consultant", "Designer", "Analyst", "Developer", "Manager", "Specialist"]
GENDERS = ["Male", "Female", "Non-binary"]

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

def generate_application():
    """Generate a single test application"""
    first_name = random.choice(FIRST_NAMES)
    last_name = random.choice(LAST_NAMES)
    
    application = {
        "connectId": generate_connect_id(),
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
            "https://picsum.photos/400/600?random=1",
            "https://picsum.photos/400/600?random=2",
            "https://picsum.photos/400/600?random=3"
        ],
        "bio": f"Test bio for {first_name} {last_name}",
        "whyJoinReason": "I'm interested in meeting new people and finding meaningful connections."
    }
    
    return application

def submit_application(application):
    """Submit an application to the server"""
    try:
        headers = {"Content-Type": "application/json"}
        response = requests.post(APPLY_ENDPOINT, json=application, headers=headers)
        
        if response.status_code == 200 or response.status_code == 201:
            print(f"✅ Successfully created application for {application['firstName']} {application['lastName']} (ID: {application['connectId']})")
            return True
        else:
            print(f"❌ Failed to create application for {application['firstName']} {application['lastName']}: {response.status_code} - {response.text}")
            return False
    except Exception as e:
        print(f"❌ Error submitting application: {e}")
        return False

def main():
    print("🚀 Generating 10 test applications for admin testing...")
    
    successful = 0
    failed = 0
    
    for i in range(10):
        print(f"\n📝 Creating application {i+1}/10...")
        application = generate_application()
        
        if submit_application(application):
            successful += 1
        else:
            failed += 1
        
        # Small delay to avoid overwhelming the server
        time.sleep(0.5)
    
    print(f"\n🎉 Complete! Successfully created {successful} applications, {failed} failed.")
    print("Applications are now available in the admin dashboard for testing!")

if __name__ == "__main__":
    main()