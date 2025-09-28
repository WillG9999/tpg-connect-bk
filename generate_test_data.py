#!/usr/bin/env python3
"""
Generate 50 realistic test user profiles based on the real 299335545418 account structure
"""

import json
import random
from datetime import datetime, timedelta
import uuid

# Based on the real 299335545418 account structure
def generate_test_profiles(count=50):
    profiles = []
    
    # Sample data pools
    first_names_male = ["Alex", "Ben", "Chris", "David", "Ethan", "Felix", "Gabriel", "Henry", "Ian", "Jake", "Kyle", "Liam", "Marcus", "Nathan", "Oliver", "Paul", "Quinn", "Ryan", "Sam", "Tyler"]
    first_names_female = ["Alice", "Bella", "Chloe", "Diana", "Emma", "Fiona", "Grace", "Hannah", "Iris", "Julia", "Kate", "Luna", "Maya", "Nina", "Olivia", "Paige", "Quinn", "Rachel", "Sofia", "Tessa"]
    
    last_names = ["Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis", "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson", "Thomas", "Taylor", "Moore", "Jackson", "Martin"]
    
    locations = ["San Francisco, CA", "New York, NY", "Los Angeles, CA", "Chicago, IL", "Boston, MA", "Seattle, WA", "Austin, TX", "Denver, CO", "Portland, OR", "Miami, FL"]
    
    companies = ["Google", "Apple", "Microsoft", "Meta", "Amazon", "Netflix", "Tesla", "Spotify", "Uber", "Airbnb", "Stripe", "Figma", "Notion", "Discord", "TikTok"]
    
    job_titles = ["Software Engineer", "Product Manager", "Designer", "Data Scientist", "Marketing Manager", "Sales Executive", "Consultant", "Teacher", "Doctor", "Lawyer", "Architect", "Chef", "Photographer", "Writer", "Artist"]
    
    universities = ["Stanford", "Harvard", "MIT", "UC Berkeley", "UCLA", "NYU", "Columbia", "Yale", "Princeton", "Oxford", "Cambridge", "University of Chicago", "Northwestern", "Duke", "Caltech"]
    
    heights = ["5'4\"", "5'5\"", "5'6\"", "5'7\"", "5'8\"", "5'9\"", "5'10\"", "5'11\"", "6'0\"", "6'1\"", "6'2\"", "6'3\"", "6'4\"", "6'5\"", "6'6\""]
    
    ethnicities = ["White", "Asian", "Hispanic", "Black", "Native American", "Middle Eastern", "Mixed", "Pacific Islander"]
    
    education_levels = ["High School", "Bachelor's Degree", "Master's Degree", "PhD", "Professional Degree"]
    
    dating_intentions = ["Serious relationship", "Casual dating", "Marriage", "Open to anything", "Friendship first"]
    
    relationship_types = ["Monogamous", "Open", "Exploring", "Polyamorous"]
    
    politics = ["Liberal", "Conservative", "Moderate", "Progressive", "Independent", "Apolitical"]
    
    religions = ["Christian", "Jewish", "Muslim", "Hindu", "Buddhist", "Agnostic", "Atheist", "Spiritual"]
    
    zodiac_signs = ["Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo", "Libra", "Scorpio", "Sagittarius", "Capricorn", "Aquarius", "Pisces"]
    
    interests = ["Travel", "Music", "Movies", "Reading", "Sports", "Cooking", "Photography", "Art", "Technology", "Fitness", "Dancing", "Gaming", "Fashion", "Food", "Nature"]
    
    # Question pools for prompts
    written_questions = ["Weird fact about me", "My simple pleasures", "I'm convinced that", "The way to my heart is", "I'm looking for", "My ideal weekend"]
    poll_questions = ["Best way to spend money", "Perfect date idea", "Biggest turn off", "Dream vacation", "Favorite season", "Morning person or night owl"]
    
    for i in range(count):
        # Generate connectId (12 digits starting with 2 or 3)
        connect_id = str(random.choice([2, 3])) + ''.join([str(random.randint(0, 9)) for _ in range(11)])
        
        # Choose gender and corresponding data
        gender = random.choice(["Male", "Female"])
        if gender == "Male":
            first_name = random.choice(first_names_male)
            interested_in = random.choice(["Women", "Men", "Everyone"])
            pronouns = "he/him"
            profile_gender = "Man"
        else:
            first_name = random.choice(first_names_female)
            interested_in = random.choice(["Men", "Women", "Everyone"])
            pronouns = "she/her"
            profile_gender = "Woman"
        
        # Generate birth date (ages 22-35)
        age = random.randint(22, 35)
        birth_date = datetime.now() - timedelta(days=age*365 + random.randint(0, 365))
        
        # Generate photos (3-6 photos)
        num_photos = random.randint(3, 6)
        photos = []
        for j in range(num_photos):
            photo_id = str(uuid.uuid4())
            photo = {
                "id": photo_id,
                "isPrimary": j == 0,
                "order": j + 1,
                "url": f"https://storage.googleapis.com/connect-ea4c2.firebasestorage.app/profile_photos/{connect_id}/photo_{random.randint(1000000000000, 9999999999999)}_{photo_id[:8]}.jpg?GoogleAccessId=firebase-adminsdk-fbsvc@connect-ea4c2.iam.gserviceaccount.com&Expires={random.randint(1759507400, 1759507500)}&Signature=example_signature_{photo_id[:16]}"
            }
            photos.append(photo)
        
        # Generate written prompts (1-3)
        num_written = random.randint(1, 3)
        written_prompts = []
        used_questions = random.sample(written_questions, num_written)
        for question in used_questions:
            written_prompts.append({
                "question": question,
                "answer": f"Sample answer for {question.lower()}"
            })
        
        # Generate poll prompts (1-2)
        num_polls = random.randint(1, 2)
        poll_prompts = []
        used_poll_questions = random.sample(poll_questions, num_polls)
        for question in used_poll_questions:
            options = ["Option A", "Option B", "Option C"]
            poll_prompts.append({
                "question": question,
                "description": question,
                "options": options,
                "selectedOption": random.choice(options)
            })
        
        # Create timestamp
        now = datetime.now()
        created_at = {
            "chronology": {
                "calendarType": "iso8601",
                "id": "ISO",
                "isoBased": True
            },
            "dayOfMonth": now.day,
            "dayOfWeek": now.strftime("%A").upper(),
            "dayOfYear": now.timetuple().tm_yday,
            "hour": now.hour,
            "minute": now.minute,
            "month": now.strftime("%B").upper(),
            "monthValue": now.month,
            "nano": random.randint(10000000, 99999999),
            "second": now.second,
            "year": now.year
        }
        
        profile = {
            "active": True,
            "connectId": connect_id,
            "createdAt": created_at,
            "dateOfBirth": birth_date.strftime("%Y-%m-%d"),
            "email": f"{i+1000}@test.com",
            "firstName": first_name,
            "gender": gender,
            "interests": random.sample(interests, random.randint(3, 8)),
            "isOnline": random.choice([True, False]),
            "isPremium": random.choice([True, False]),
            "isVerified": random.choice([True, False]),
            "lastActive": None if random.random() < 0.3 else created_at,
            "lastName": random.choice(last_names),
            "location": random.choice(locations),
            "photos": photos,
            "pollPrompts": poll_prompts,
            "profile": {
                "children": random.choice(["No kids", "1 child", "2 children", "3+ children", "Want kids"]),
                "company": random.choice(companies),
                "datingIntentions": random.choice(dating_intentions),
                "educationLevel": random.choice(education_levels),
                "ethnicity": random.choice(ethnicities),
                "familyPlans": random.choice(["Want kids", "Don't want kids", "Open to kids", "Have kids"]),
                "gender": profile_gender,
                "height": random.choice(heights),
                "hometown": random.choice(locations).split(",")[0],
                "interestedIn": interested_in,
                "jobTitle": random.choice(job_titles),
                "languages": ["English"] + random.sample(["Spanish", "French", "German", "Italian", "Portuguese", "Chinese", "Japanese"], random.randint(0, 2)),
                "pets": random.choice(["Dog person", "Cat person", "Both", "No pets", "Want pets"]),
                "politics": random.choice(politics),
                "pronouns": pronouns,
                "relationshipType": random.choice(relationship_types),
                "religiousBeliefs": random.choice(religions),
                "sexuality": "Straight" if interested_in != "Everyone" else random.choice(["Straight", "Bisexual", "Pansexual"]),
                "university": random.choice(universities),
                "zodiacSign": random.choice(zodiac_signs)
            },
            "subscriptionType": None if random.random() < 0.7 else random.choice(["premium_monthly", "premium_yearly"]),
            "updatedAt": created_at,
            "version": random.randint(1, 25),
            "writtenPrompts": written_prompts
        }
        
        profiles.append(profile)
    
    return profiles

def main():
    print("🚀 Generating 50 test user profiles based on real 299335545418 structure...")
    
    profiles = generate_test_profiles(50)
    
    # Save to JSON file
    output_file = "/Users/willgraham/Desktop/Connect/ConnectBackend/test_profiles_50.json"
    with open(output_file, 'w') as f:
        json.dump(profiles, f, indent=2)
    
    print(f"✅ Generated {len(profiles)} test profiles")
    print(f"📁 Saved to: {output_file}")
    
    # Print summary
    print("\n📊 Profile Summary:")
    males = sum(1 for p in profiles if p['gender'] == 'Male')
    females = sum(1 for p in profiles if p['gender'] == 'Female')
    print(f"   👨 Males: {males}")
    print(f"   👩 Females: {females}")
    
    locations = {}
    for p in profiles:
        loc = p['location']
        locations[loc] = locations.get(loc, 0) + 1
    print(f"   🌍 Locations: {len(locations)} different cities")
    
    ages = []
    for p in profiles:
        birth_year = int(p['dateOfBirth'].split('-')[0])
        age = datetime.now().year - birth_year
        ages.append(age)
    print(f"   🎂 Age range: {min(ages)}-{max(ages)} years")
    
    print(f"\n🔢 Sample ConnectIDs:")
    for i in range(min(5, len(profiles))):
        print(f"   {profiles[i]['connectId']} - {profiles[i]['firstName']} {profiles[i]['lastName']}")

if __name__ == "__main__":
    main()