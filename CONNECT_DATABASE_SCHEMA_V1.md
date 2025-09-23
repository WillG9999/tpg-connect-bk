# 🔥 Connect Dating App - Firebase Firestore Schema

## 📋 Overview

This document defines the Firebase Firestore database schema for the Connect dating application. The schema leverages Firebase's real-time capabilities, authentication, cloud storage, and messaging services.

**Database Type:** Firebase Firestore (NoSQL Document Database)  
**Total Collections:** 8 core collections  
**Primary Keys:** 12-digit ConnectID (numeric string format)  
**Real-time Features:** Built-in with Firestore listeners  
**Authentication:** Firebase Auth integration  
**File Storage:** Firebase Storage for photos  
**Messaging:** Firestore real-time + FCM for push notifications  
**Notification Approach:** Data-driven (no notification audit trail)  
**Data Pattern:** Enhanced objects (update existing docs, don't create new ones)

---

## 🔐 1. users Collection

**Purpose:** Core user authentication and account management  
**Collection Path:** `/users/{connectId}`

### Document Structure:
```javascript
{
  connectId: "123456789012",        // 12-digit numeric ID - Document ID
  username: "johndoe",              // Unique username (optional)
  email: "john@example.com",        // Unique email address
  role: "USER",                     // "USER" | "ADMIN" | "MODERATOR"
  active: true,                     // Account status (soft delete)
  emailVerified: true,              // Email verification status
  createdAt: Timestamp,             // Account creation timestamp
  updatedAt: Timestamp,             // Last update timestamp
  lastLoginAt: Timestamp,           // Last login timestamp
  emailVerifiedAt: Timestamp,       // Email verification timestamp
  deletedAt: null,                  // Soft deletion timestamp
  lastLoginDevice: "iOS",           // Device type for last login
  
  // FCM Tokens for push notifications
  fcmTokens: [                      // Array of active device tokens
    {
      token: "fcm_token_123...",
      deviceType: "iOS",            // "iOS" | "Android" | "Web"
      deviceId: "device_001",
      addedAt: Timestamp,
      lastUsed: Timestamp,
      isActive: true
    }
  ]
}
```

### ConnectID Generation:
- **Format:** 12-digit numeric string (e.g., "123456789012")
- **Range:** 100000000000 to 999999999999
- **Generated:** At user registration via Firebase Auth trigger
- **Uniqueness:** Enforced by Firestore document ID
- **Firebase Auth Integration:** Custom claims include connectId

### Security Rules:
```javascript
// Firestore Security Rules
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{connectId} {
      allow read, write: if request.auth != null && request.auth.uid == connectId;
      allow read: if request.auth != null; // Other users can read basic info
    }
  }
}
```

### Indexes:
- **email**: Automatic composite index
- **username**: Single field index
- **active**: Single field index for filtering
- **createdAt**: Single field index for time-based queries

---

## 👤 2. user_profiles Collection

**Purpose:** Dating profiles, preferences, and photos  
**Collection Path:** `/user_profiles/{connectId}`

### Document Structure:
```javascript
{
  connectId: "123456789012",        // 12-digit ConnectID - Document ID
  userId: "123456789012",           // Reference to users collection
  
  // Basic Profile Info
  firstName: "John",                // First name
  lastName: "Doe",                  // Last name  
  name: "John Doe",                 // Full display name
  age: 28,                          // Calculated from dateOfBirth
  dateOfBirth: Timestamp,           // Birth date (18+ validation)
  location: "London, UK",           // City, Country
  
  // Email Verification Status
  emailVerified: true,              // Email address verified (synced from users collection)
  emailVerifiedAt: Timestamp,       // When email was verified
  
  // Photos with Prompts (Array of Objects)
  photos: [
    {
      id: "photo_001",              // Photo ID
      url: "gs://bucket/photos/photo_001.jpg", // Firebase Storage URL
      isPrimary: true,              // Primary profile photo
      order: 1,                     // Display order (1-6)
      prompts: [                    // Photo prompts/captions
        {
          id: "prompt_001",
          text: "My favorite travel spot",
          position: { x: 0.5, y: 0.8 }, // Position on photo (0-1)
          style: {
            backgroundColor: "rgba(0,0,0,0.7)",
            textColor: "#FFFFFF",
            fontSize: 14
          }
        }
      ]
    }
  ],
  
  // Interests & Lifestyle
  interests: ["Travel", "Fitness", "Music", "Cooking"],
  
  // Detailed Profile (Nested Object)
  profile: {
    pronouns: "he/him",             // "he/him", "she/her", "they/them"
    gender: "Man",                  // "Man", "Woman", "Non-binary", etc.
    sexuality: "Straight",          // "Straight", "Gay", "Bisexual", etc.
    interestedIn: "Women",          // "Men", "Women", "Everyone"
    jobTitle: "Software Engineer",  // Professional title
    company: "Tech Corp",           // Company name
    university: "Oxford",           // University name
    educationLevel: "Bachelor's",   // Education level
    religiousBeliefs: "Agnostic",   // Religious preference
    hometown: "Manchester",         // Hometown
    politics: "Liberal",            // Political leaning
    languages: ["English", "Spanish"], // Spoken languages
    datingIntentions: "Serious",    // "Casual", "Serious", "Marriage"
    relationshipType: "Monogamous", // "Monogamous", "Non-monogamous"
    height: "5'10\"",               // Height string
    ethnicity: "Mixed",             // Ethnic background
    children: "No kids",            // "No kids", "Have kids", etc.
    familyPlans: "Want kids",       // "Want kids", "Don't want kids"
    pets: "Dog person",             // "Dog person", "Cat person", etc.
    zodiacSign: "Leo"               // Astrological sign
  },
  
  // Written Prompts (Array of Objects)
  writtenPrompts: [
    {
      question: "My ideal Sunday is...",
      answer: "Brunch with friends followed by a long walk in the park"
    }
  ],
  
  // Poll Prompts (Array of Objects)
  pollPrompts: [
    {
      question: "Coffee or tea?",
      description: "What's your morning fuel?",
      options: ["Coffee", "Tea", "Both", "Neither"],
      selectedOption: "Coffee"      // User's selection
    }
  ],
  
  // Field Visibility Settings (Nested Object)
  fieldVisibility: {
    jobTitle: true,                 // Show job title
    university: true,               // Show university
    religiousBeliefs: false,        // Hide religion
    politics: false,                // Hide politics
    height: true,                   // Show height
    ethnicity: true                 // Show ethnicity
  },
  
  // User Preferences (Nested Object)
  preferences: {
    preferredGender: "women",       // "men", "women", "both"
    minAge: 22,                     // Minimum age preference (18+)
    maxAge: 35,                     // Maximum age preference
    maxDistance: 25,                // Max distance in miles
    minHeight: 64,                  // Min height preference (inches)
    maxHeight: 72,                  // Max height preference (inches)
    datingIntention: "Serious",     // Preferred dating intention
    drinkingPreference: "Social",   // Drinking preference
    smokingPreference: "Never",     // Smoking preference
    religionImportance: "Low",      // Religion importance level
    wantsChildren: true             // Wants children
  },
  
  // Notification Settings (Nested Object)
  notificationSettings: {
    pushEnabled: true,              // Overall push notifications
    newMatches: true,               // Notify for new matches
    messages: true,                 // Notify for new messages
    profileViews: false,            // Notify when profile viewed
    matchReminders: true,           // Remind about inactive matches
    
    // Quiet Hours
    quietHours: {
      enabled: true,                // Enable quiet hours
      start: "22:00",               // Start time (24hr format)
      end: "08:00"                  // End time (24hr format)
    },
    
    // Notification Types
    marketing: false,               // Marketing notifications
    safety: true,                   // Safety alerts
    systemUpdates: true             // App updates/maintenance
  },
  
  // Metadata
  active: true,                     // Profile status
  createdAt: Timestamp,             // Profile creation
  updatedAt: Timestamp,             // Last profile update
  lastActive: Timestamp,            // Last app activity
  version: 1                        // Profile version for updates
}
```

### Photo Prompts Structure:
```javascript
// Enhanced photo with multiple prompts
{
  id: "photo_002",
  url: "gs://bucket/photos/photo_002.jpg",
  isPrimary: false,
  order: 2,
  prompts: [
    {
      id: "prompt_002",
      text: "This is me hiking in Scotland",
      position: { x: 0.2, y: 0.9 },  // Bottom left
      style: {
        backgroundColor: "rgba(0,0,0,0.7)",
        textColor: "#FFFFFF",
        fontSize: 14
      }
    },
    {
      id: "prompt_003", 
      text: "🏔️",
      position: { x: 0.8, y: 0.1 },  // Top right
      style: {
        fontSize: 24
      }
    }
  ]
}
```

### Security Rules:
```javascript
match /user_profiles/{connectId} {
  allow read: if request.auth != null;
  allow write: if request.auth != null && request.auth.uid == connectId;
}
```

### Indexes:
- **active**: Single field
- **age**: Single field  
- **location**: Single field
- **interests**: Array-contains index
- **preferences.preferredGender**: Single field
- **profile.gender**: Single field
- **emailVerified**: Single field

---

## 💕 3. user_matches Collection

**Purpose:** Enhanced matches document per user (single doc with all matches)  
**Collection Path:** `/user_matches/{connectId}` (ONE document per user)

### Document Structure:
```javascript
// Document ID: user's connectId
{
  connectId: "123456789012",        // User's ConnectID - Document ID
  userId: "123456789012",           // User's ConnectID (redundant but clear)
  
  // Enhanced Matches List (Array of Match Objects)
  matches: [
    {
      connectId: "match_567890123456", // Unique match ID
      otherUserId: "567890123456",  // Other user's ConnectID
      otherUserName: "Sarah Johnson", // Other user's name
      otherUserPhoto: "gs://bucket/photos/sarah_001.jpg", // Their primary photo
      
      // Match timing
      matchedAt: Timestamp,         // When mutual like occurred
      myActionAt: Timestamp,        // When I liked them
      theirActionAt: Timestamp,     // When they liked me
      
      // Status and activity
      status: "ACTIVE",             // "ACTIVE" | "UNMATCHED" | "BLOCKED" | "REPORTED"
      lastActivityAt: Timestamp,    // Last interaction
      
      // Match context
      matchSource: "DISCOVERY",     // "DISCOVERY" | "SEARCH"
      matchSetId: "set_20240919_123", // Which batch this came from
      
      // Conversation tracking
      hasMessaged: false,           // Have we started chatting
      lastMessageAt: null,          // Last message timestamp
      lastMessageText: null,        // Preview of last message
      myLastRead: null,             // When I last read messages
      unreadCount: 0,               // My unread message count
      
      // Match quality metrics
      compatibilityScore: 0.85,     // Algorithm compatibility
      commonInterests: ["Travel", "Music"], // Shared interests
      distance: 3.2,                // Distance in miles
      
      // Moderation
      reportedBy: null,             // ConnectID who reported
      reportedAt: null,             // Report timestamp
      adminNotes: null              // Admin notes
    },
    {
      connectId: "match_789012345678",
      otherUserId: "789012345678",
      otherUserName: "Emma Wilson",
      otherUserPhoto: "gs://bucket/photos/emma_001.jpg",
      matchedAt: Timestamp,
      // ... rest of match data
    }
  ],
  
  // Match Summary Stats
  totalMatches: 2,                  // Total number of matches
  activeMatches: 2,                 // Currently active matches
  newMatches: 1,                    // Unviewed new matches
  conversationsStarted: 0,          // Matches with messages
  lastMatchAt: Timestamp,           // Most recent match time
  
  // Metadata
  createdAt: Timestamp,             // First match timestamp
  updatedAt: Timestamp,             // Last update timestamp
  version: 1                        // Document version
}
```

### Update Pattern:
```javascript
// When new match occurs - UPDATE existing document
await db.collection('user_matches').doc(userId).update({
  matches: FieldValue.arrayUnion({
    connectId: "match_" + otherUserId,
    otherUserId: otherUserId,
    otherUserName: otherUserName,
    // ... rest of match data
  }),
  totalMatches: FieldValue.increment(1),
  activeMatches: FieldValue.increment(1),
  newMatches: FieldValue.increment(1),
  lastMatchAt: FieldValue.serverTimestamp(),
  updatedAt: FieldValue.serverTimestamp()
});
```

### Security Rules:
```javascript
match /user_matches/{connectId} {
  allow read, write: if request.auth != null && request.auth.uid == connectId;
}
```

---

## 👍 4. user_activity Collection

**Purpose:** Enhanced activity document per user (single doc with all actions)  
**Collection Path:** `/user_activity/{connectId}` (ONE document per user)

### Document Structure:
```javascript
// Document ID: user's connectId
{
  connectId: "123456789012",        // User's ConnectID - Document ID
  userId: "123456789012",           // User's ConnectID (redundant but clear)
  
  // Enhanced Actions List (Array of Action Objects)
  actions: [
    {
      connectId: "action_567890123456_20240919", // Action ID
      targetUserId: "567890123456", // User being acted upon
      targetUserName: "Sarah Johnson", // Their name
      action: "LIKE",               // "LIKE" | "PASS" | "DISLIKE"
      timestamp: Timestamp,         // Action timestamp
      
      // Action context
      source: "DISCOVERY",          // "DISCOVERY" | "SEARCH"
      matchSetId: "set_20240919_123", // Related match set
      batchDate: "2024-09-19",      // Date string for processing
      
      // Result tracking
      resultedInMatch: true,        // Did this create a match?
      matchId: "match_567890123456", // Match ID if created
      
      // Target user context (for analytics)
      targetUserAge: 26,            // Their age at time of action
      targetUserLocation: "London, UK", // Their location
      distance: 3.2,                // Distance at time of action
      compatibilityScore: 0.85,     // Algorithm score
      
      // Metadata
      deviceType: "iOS",            // Device used
      appVersion: "1.0.0"           // App version
    },
    {
      connectId: "action_789012345678_20240919",
      targetUserId: "789012345678",
      targetUserName: "Emma Wilson",
      action: "PASS",
      timestamp: Timestamp,
      // ... rest of action data
    }
  ],
  
  // Daily Activity Summary (Object with date keys)
  dailySummary: {
    "2024-09-19": {
      totalActions: 15,             // Total actions taken today
      likes: 8,                     // Likes given today
      passes: 7,                    // Passes given today
      dislikes: 0,                  // Dislikes given today
      matches: 2,                   // Matches created today
      viewTime: 1800,               // Time spent swiping (seconds)
      batchesCompleted: 1           // Match sets completed
    },
    "2024-09-18": {
      // Previous day summary...
    }
  },
  
  // Overall Activity Stats
  totalActions: 150,                // Lifetime total actions
  totalLikes: 80,                   // Lifetime likes given
  totalPasses: 65,                  // Lifetime passes given
  totalDislikes: 5,                 // Lifetime dislikes given
  totalMatches: 12,                 // Lifetime matches created
  matchSuccessRate: 0.15,           // Matches per like ratio
  avgActionsPerDay: 15,             // Average daily actions
  
  // Recent Activity Tracking
  lastActionAt: Timestamp,          // Most recent action
  actionsToday: 15,                 // Actions taken today
  currentStreak: 5,                 // Days of consecutive activity
  longestStreak: 12,                // Longest activity streak
  
  // Metadata
  createdAt: Timestamp,             // First action timestamp
  updatedAt: Timestamp,             // Last update timestamp
  version: 1                        // Document version
}
```

### Update Pattern:
```javascript
// When user takes action - UPDATE existing document
await db.collection('user_activity').doc(userId).update({
  actions: FieldValue.arrayUnion({
    connectId: `action_${targetUserId}_${dateString}`,
    targetUserId: targetUserId,
    action: "LIKE",
    timestamp: FieldValue.serverTimestamp(),
    // ... rest of action data
  }),
  
  // Update counters
  totalActions: FieldValue.increment(1),
  totalLikes: FieldValue.increment(1),
  actionsToday: FieldValue.increment(1),
  
  // Update daily summary
  [`dailySummary.${dateString}.totalActions`]: FieldValue.increment(1),
  [`dailySummary.${dateString}.likes`]: FieldValue.increment(1),
  
  // Update metadata
  lastActionAt: FieldValue.serverTimestamp(),
  updatedAt: FieldValue.serverTimestamp()
});
```

### Security Rules:
```javascript
match /user_activity/{connectId} {
  allow read, write: if request.auth != null && request.auth.uid == connectId;
}
```

### Indexes:
- **userId**: Single field
- **lastActionAt**: Single field
- **totalActions**: Single field

---

## 📅 5. match_sets Collection

**Purpose:** Daily batches of potential matches with full match data  
**Collection Path:** `/match_sets/{setId}`

### Document Structure:
```javascript
{
  connectId: "set_20240919_123456789012", // Match set ID
  userId: "123456789012",            // User this set is for
  date: "2024-09-19",                // Date string
  status: "ACTIVE",                  // "PENDING" | "ACTIVE" | "COMPLETED"
  
  // Timing
  createdAt: Timestamp,              // Set creation time
  completedAt: null,                 // When user finished all actions
  
  // Match data (Array of potential matches with full profile data)
  potentialMatches: [
    {
      connectId: "567890123456",     // User's ConnectID
      name: "Sarah Johnson",         // User's name
      age: 26,                       // User's age
      photos: [                      // User's photos (first few)
        {
          url: "gs://bucket/photos/sarah_001.jpg",
          isPrimary: true,
          prompts: [
            {
              text: "My weekend adventure",
              position: { x: 0.5, y: 0.8 }
            }
          ]
        }
      ],
      location: "London, UK",        // User's location
      interests: ["Travel", "Art"],  // User's interests
      profile: {                     // Relevant profile fields
        jobTitle: "Graphic Designer",
        datingIntentions: "Serious"
      },
      distance: 3.2,                 // Distance in miles
      compatibilityScore: 0.85,      // Algorithm compatibility score
      commonInterests: ["Travel"],   // Shared interests
      algorithmReason: "High compatibility + shared interests"
    }
  ],
  
  // Progress tracking
  totalMatches: 15,                  // Total potential matches in set
  actionsSubmitted: 0,               // Actions taken by user
  matchesFound: 0,                   // Actual matches created
  
  // Algorithm metadata
  algorithmVersion: "v2.1",          // Matching algorithm version
  filters: {
    ageRange: [22, 35],              // Age filter applied
    maxDistance: 25,                 // Distance filter applied
    preferredGender: "women",        // Gender preference
    otherPreferences: {}             // Other filters applied
  },
  
  // Performance tracking
  viewTime: 0,                       // Total time spent viewing (seconds)
  avgTimePerProfile: 0               // Average time per profile
}
```

### Security Rules:
```javascript
match /match_sets/{setId} {
  allow read, write: if request.auth != null && 
    resource.data.userId == request.auth.uid;
}
```

---

## 🚫 6. blocked_users Collection

**Purpose:** User blocking relationships  
**Collection Path:** `/blocked_users/{blockId}`

### Document Structure:
```javascript
{
  connectId: "block_123456789012_567890123456", // Block ID
  userId: "123456789012",            // User who blocked
  blockedUserId: "567890123456",     // User who was blocked
  reason: "Inappropriate behavior",   // Reason for blocking
  blockedAt: Timestamp,              // Block timestamp
  status: "ACTIVE",                  // "ACTIVE" | "REMOVED"
  
  // Block context
  source: "MATCH",                   // "PROFILE" | "MATCH" | "REPORT"
  matchId: "match_123456789012_567890123456", // Related match if applicable
  
  // Block details
  blockType: "USER_INITIATED",       // "USER_INITIATED" | "ADMIN_ENFORCED" | "SYSTEM_SAFETY"
  severity: "MEDIUM",                // "LOW" | "MEDIUM" | "HIGH" | "CRITICAL"
  
  // Admin moderation
  reviewedBy: null,                  // Admin ConnectID who reviewed
  reviewedAt: null,                  // Review timestamp
  adminNotes: null,                  // Admin notes
  
  // Unblock tracking
  unblockedAt: null,                 // Unblock timestamp
  unblockReason: null,               // Reason for unblocking
  unblockedBy: null                  // Who unblocked
}
```

---

## 🛡️ 7. user_reports Collection

**Purpose:** Safety reports and content moderation  
**Collection Path:** `/user_reports/{reportId}`

### Document Structure:
```javascript
{
  connectId: "report_123456789012_001", // Report ID
  reporterId: "123456789012",        // User making report
  reportedUserId: "567890123456",    // User being reported
  
  // Report details
  reasons: ["Inappropriate photos", "Harassment"], // Array of reasons
  description: "User sent inappropriate messages", // Detailed description
  evidenceUrls: [                   // Evidence screenshots
    "gs://bucket/evidence/report_001_screenshot.jpg"
  ],
  
  // Context
  context: "MATCH",                  // "PROFILE" | "MATCH" | "PHOTO"
  matchId: "match_123456789012_567890123456", // Related match
  
  // Timestamps
  reportedAt: Timestamp,             // Report submission
  reviewedAt: null,                  // Admin review time
  resolvedAt: null,                  // Resolution time
  
  // Status tracking
  status: "PENDING",                 // "PENDING" | "UNDER_REVIEW" | "RESOLVED" | "DISMISSED" | "ACTION_TAKEN"
  priority: "HIGH",                  // "LOW" | "MEDIUM" | "HIGH" | "URGENT"
  
  // Moderation
  reviewedBy: null,                  // Admin ConnectID
  adminNotes: null,                  // Internal notes
  actionTaken: null,                 // "WARNING" | "SUSPEND" | "BAN" | "CONTENT_REMOVED"
  
  // Follow-up
  followUpRequired: false,           // Needs follow-up
  escalated: false,                  // Escalated to senior moderator
  escalatedTo: null,                 // Senior moderator ConnectID
  escalatedAt: null                  // Escalation timestamp
}
```

---

## 🛡️ 8. safety_blocks Collection

**Purpose:** Admin-enforced safety blocks  
**Collection Path:** `/safety_blocks/{blockId}`

### Document Structure:
```javascript
{
  connectId: "safety_123456789012_001", // Safety block ID
  userId: "123456789012",            // User being blocked
  
  // Block details
  blockType: "CONTENT_VIOLATION",    // "PROFILE_REVIEW" | "SUSPICIOUS_ACTIVITY" | "CONTENT_VIOLATION" | "ADMIN_ACTION"
  severity: "TEMPORARY",             // "WARNING" | "TEMPORARY" | "PERMANENT"
  reason: "Inappropriate profile photos", // Detailed reason
  
  // Timing
  blockedAt: Timestamp,              // Block start time
  expiresAt: Timestamp,              // Block expiration (null for permanent)
  duration: 168,                     // Block duration in hours (7 days)
  
  // Status
  status: "ACTIVE",                  // "ACTIVE" | "EXPIRED" | "LIFTED" | "ESCALATED"
  isActive: true,                    // Block currently active
  
  // Admin information
  createdBy: "admin_001",            // Admin ConnectID
  reviewedBy: "admin_001",           // Admin who reviewed
  liftedBy: null,                    // Admin who lifted early
  
  // Evidence and context
  reportIds: ["report_123456789012_001"], // Related reports
  evidenceUrls: [                   // Evidence files
    "gs://bucket/evidence/violation_001.jpg"
  ],
  context: "Multiple reports about inappropriate photos",
  
  // User impact
  restrictedFeatures: ["MATCHING", "PROFILE_EDIT"], // Blocked features
  warningMessage: "Your profile contains inappropriate content. Please review our community guidelines.",
  
  // Appeals process
  appealSubmitted: false,            // User appealed
  appealedAt: null,                  // Appeal time
  appealReason: null,                // Appeal reason
  appealReviewedBy: null,            // Admin who reviewed appeal
  appealDecision: null,              // "UPHELD" | "OVERTURNED" | "REDUCED"
  appealReviewedAt: null,            // Appeal review time
  
  // Automation
  triggeredBySystem: false,          // Auto-triggered
  triggerRule: null,                 // Automation rule
  humanReviewRequired: true,         // Needs human review
  
  // Notes
  adminNotes: "User uploaded explicit content in profile photos",
  publicReason: "Profile content violates community guidelines"
}
```

---

## 🔥 Firebase Integration Features

### Firebase Authentication
```javascript
// Custom claims for ConnectID
{
  uid: "firebase_auth_uid",
  connectId: "123456789012",
  role: "USER",
  emailVerified: true
}
```

### Firebase Storage Structure
```
gs://connect-app-photos/
├── profiles/
│   ├── 123456789012/
│   │   ├── photo_001.jpg
│   │   ├── photo_002.jpg
│   │   └── thumbnails/
│   │       ├── photo_001_thumb.jpg
│   │       └── photo_002_thumb.jpg
├── evidence/
│   ├── reports/
│   └── violations/
└── system/
    └── defaults/
```

### Enhanced Document Update Patterns

**Adding New Match:**
```javascript
// Update user_matches document (don't create new)
await db.collection('user_matches').doc(userId).set({
  matches: FieldValue.arrayUnion(newMatchObject),
  totalMatches: FieldValue.increment(1),
  newMatches: FieldValue.increment(1),
  lastMatchAt: FieldValue.serverTimestamp(),
  updatedAt: FieldValue.serverTimestamp()
}, { merge: true });
```

**Adding New Action:**
```javascript
// Update user_activity document (don't create new)
await db.collection('user_activity').doc(userId).set({
  actions: FieldValue.arrayUnion(newActionObject),
  totalActions: FieldValue.increment(1),
  [`dailySummary.${dateString}.likes`]: FieldValue.increment(1),
  lastActionAt: FieldValue.serverTimestamp(),
  updatedAt: FieldValue.serverTimestamp()
}, { merge: true });
```

### Data-Driven Notifications (No Audit Trail)

**Frontend Notification Flow:**
```javascript
// Check for new matches when app opens
const userMatchesRef = db.collection('user_matches').doc(connectId);
const snapshot = await userMatchesRef.get();
const data = snapshot.data();

if (data && data.newMatches > 0) {
  showNotification(`You have ${data.newMatches} new matches! 🎉`);
  
  // Mark as viewed
  await userMatchesRef.update({
    newMatches: 0,
    updatedAt: FieldValue.serverTimestamp()
  });
}

// Real-time listener for new matches
userMatchesRef.onSnapshot(doc => {
  const data = doc.data();
  if (data && data.newMatches > 0) {
    showInAppNotification(`New match! You have ${data.totalMatches} matches total.`);
  }
});
```

**FCM for Closed App Only:**
```javascript
// Cloud Function: Send push when new match added
exports.sendMatchPush = functions.firestore
  .document('user_matches/{userId}')
  .onUpdate(async (change, context) => {
    const before = change.before.data();
    const after = change.after.data();
    
    // Check if new match was added
    if (after.totalMatches > before.totalMatches) {
      const userId = context.params.userId;
      const userDoc = await admin.firestore().collection('users').doc(userId).get();
      const fcmTokens = userDoc.data().fcmTokens || [];
      
      if (fcmTokens.length > 0) {
        await admin.messaging().sendMulticast({
          notification: {
            title: "New Match! 🎉",
            body: `You have ${after.totalMatches} matches total!`
          },
          data: {
            type: "NEW_MATCH",
            action: "open_matches"
          },
          tokens: fcmTokens.map(t => t.token)
        });
      }
    }
  });
```

### Performance Optimization
1. **Enhanced Documents**: Single document per user for matches/activity
2. **Array Updates**: Use FieldValue.arrayUnion for adding items
3. **Atomic Counters**: Use FieldValue.increment for stats
4. **Efficient Queries**: Single document reads vs multiple document queries
5. **Data Locality**: All user matches/activity in one place
6. **Reduced Writes**: Update existing docs instead of creating new ones

### Security Rules Summary
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users can read/write their own data
    match /users/{connectId} {
      allow read, write: if request.auth != null && request.auth.uid == connectId;
      allow read: if request.auth != null; // Basic profile info readable by others
    }
    
    // User profiles readable by authenticated users
    match /user_profiles/{connectId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == connectId;
    }
    
    // Enhanced matches document per user
    match /user_matches/{connectId} {
      allow read, write: if request.auth != null && request.auth.uid == connectId;
    }
    
    // Enhanced activity document per user
    match /user_activity/{connectId} {
      allow read, write: if request.auth != null && request.auth.uid == connectId;
    }
    
    // Admin-only collections
    match /user_reports/{reportId} {
      allow create: if request.auth != null;
      allow read, write: if request.auth != null && 
        request.auth.token.role == 'ADMIN';
    }
  }
}
```

This enhanced Firebase Firestore schema uses the update pattern instead of creating new documents - much more efficient for user matches and activity tracking! 🚀