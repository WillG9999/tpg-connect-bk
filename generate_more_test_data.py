#!/usr/bin/env python3
import firebase_admin
from firebase_admin import credentials, firestore
import random
from datetime import datetime, timedelta

# Initialize Firebase Admin SDK
cred = credentials.Certificate("/Users/willgraham/Desktop/keys/firebase-service-account.json")
try:
    firebase_admin.get_app()
except ValueError:
    firebase_admin.initialize_app(cred)

db = firestore.client()

def generate_test_applications():
    # Sample data for generating test applications
    first_names = [
        "Oliver", "Emma", "Noah", "Charlotte", "James", "Amelia", "Benjamin", "Sophia", 
        "Lucas", "Isabella", "Henry", "Mia", "Alexander", "Evelyn", "Mason", "Harper",
        "Michael", "Luna", "Ethan", "Gianna", "Daniel", "Aria", "Jacob", "Ellie",
        "Logan", "Violet", "Jackson", "Scarlett", "Levi", "Madison"
    ]
    
    last_names = [
        "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis",
        "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson",
        "Thomas", "Taylor", "Moore", "Jackson", "Martin", "Lee", "Perez", "Thompson",
        "White", "Harris", "Sanchez", "Clark", "Ramirez", "Lewis", "Robinson"
    ]
    
    locations = [
        "London", "Manchester", "Birmingham", "Leeds", "Glasgow", "Liverpool", 
        "Newcastle", "Sheffield", "Bristol", "Edinburgh", "Leicester", "Coventry",
        "Bradford", "Cardiff", "Belfast", "Nottingham", "Hull", "Plymouth",
        "Stoke", "Wolverhampton", "Derby", "Southampton", "Portsmouth", "Brighton"
    ]
    
    job_titles = [
        "Software Engineer", "Marketing Manager", "Teacher", "Designer", "Doctor",
        "Lawyer", "Accountant", "Nurse", "Consultant", "Architect", "Writer",
        "Artist", "Chef", "Photographer", "Therapist", "Scientist", "Engineer",
        "Manager", "Developer", "Analyst", "Coordinator", "Specialist", "Director"
    ]
    
    industries = [
        "Technology", "Healthcare", "Education", "Marketing", "Finance", "Legal",
        "Engineering", "Arts & Entertainment", "Hospitality", "Consulting",
        "Architecture", "Media", "Science", "Non-profit", "Government", "Retail",
        "Manufacturing", "Transportation", "Real Estate", "Fashion & Beauty"
    ]
    
    genders = ["Male", "Female", "Non-binary"]
    
    # Generate 15 new test applications
    applications = []
    
    for i in range(15):
        first_name = random.choice(first_names)
        last_name = random.choice(last_names)
        gender = random.choice(genders)
        location = random.choice(locations)
        job_title = random.choice(job_titles)
        industry = random.choice(industries)
        
        # Generate a unique connectId
        connect_id = str(random.randint(100000000000, 999999999999))
        
        # Generate email
        email = f"{first_name.lower()}.{last_name.lower()}@newtest.com"
        
        # Generate birth date (21-35 years old)
        birth_year = random.randint(1988, 2002)
        birth_month = random.randint(1, 12)
        birth_day = random.randint(1, 28)
        date_of_birth = f"{birth_year}-{birth_month:02d}-{birth_day:02d}"
        
        # Generate phone number
        phone_number = f"0{random.randint(1000000000, 9999999999)}"[:11]
        
        # Generate submission date (within last 30 days)
        days_ago = random.randint(1, 30)
        submitted_at = datetime.now() - timedelta(days=days_ago)
        
        # Generate photo URLs
        photo_urls = []
        num_photos = random.randint(3, 6)
        for j in range(num_photos):
            photo_urls.append(f"https://picsum.photos/400/600?random={random.randint(1, 1000)}")
        
        # Assign status - mix of pending, approved, and rejected
        status_weights = [0.4, 0.4, 0.2]  # 40% pending, 40% approved, 20% rejected
        status_choice = random.choices(["PENDING_APPROVAL", "APPROVED", "REJECTED"], weights=status_weights)[0]
        
        application = {
            "connectId": connect_id,
            "firstName": first_name,
            "lastName": last_name,
            "email": email,
            "dateOfBirth": date_of_birth,
            "gender": gender,
            "location": location,
            "jobTitle": job_title,
            "industry": industry,
            "phoneNumber": phone_number,
            "bio": f"Test bio for {first_name} {last_name}",
            "whyJoinReason": "I'm interested in meeting new people and finding meaningful connections.",
            "photoUrls": photo_urls,
            "status": status_choice,
            "submittedAt": submitted_at
        }
        
        # Add status-specific fields
        if status_choice in ["APPROVED", "REJECTED"]:
            review_date = submitted_at + timedelta(hours=random.randint(2, 48))
            application["reviewedAt"] = review_date
            application["reviewedBy"] = "183600102436"  # Admin user ID
            
            if status_choice == "APPROVED":
                application["approvedAt"] = review_date
                application["reviewNotes"] = "Approved - good profile"
            else:  # REJECTED
                application["rejectedAt"] = review_date
                application["rejectionReason"] = random.choice([
                    "Profile does not meet our community standards",
                    "Incomplete application information",
                    "Photos do not meet quality requirements",
                    "Inappropriate content detected"
                ])
                application["reviewNotes"] = "Rejected - needs improvement"
        
        applications.append(application)
    
    return applications

def save_applications_to_firestore(applications):
    """Save applications directly to Firestore"""
    batch = db.batch()
    
    for app in applications:
        doc_ref = db.collection("applicationSubmissions").document(app["connectId"])
        batch.set(doc_ref, app)
    
    # Commit the batch
    batch.commit()
    print(f"✅ Successfully added {len(applications)} test applications to Firestore")

if __name__ == "__main__":
    print("🔄 Generating new test applications...")
    test_applications = generate_test_applications()
    
    print(f"📝 Generated {len(test_applications)} test applications:")
    
    # Count by status
    status_counts = {"PENDING_APPROVAL": 0, "APPROVED": 0, "REJECTED": 0}
    for app in test_applications:
        status_counts[app["status"]] += 1
        print(f"   {app['firstName']} {app['lastName']} ({app['status']})")
    
    print(f"\n📊 Status breakdown:")
    for status, count in status_counts.items():
        print(f"   {status}: {count}")
    
    print(f"\n💾 Saving to Firestore...")
    save_applications_to_firestore(test_applications)
    
    print(f"\n🎉 Test data generation complete!")
    print(f"   Total new applications: {len(test_applications)}")
    print(f"   Pending: {status_counts['PENDING_APPROVAL']}")
    print(f"   Approved: {status_counts['APPROVED']}")
    print(f"   Rejected: {status_counts['REJECTED']}")