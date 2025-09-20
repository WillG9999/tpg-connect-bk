# 🗄️ Connect Dating App - Simplified Database Schema

## 📋 Overview

This document defines the simplified database schema for the Connect dating application backend. The schema supports authentication, user profiles, matching algorithms, safety features, and notifications.

**Database Type:** MongoDB (NoSQL) with relational patterns  
**Total Collections:** 9 core collections  
**Primary Keys:** 12-digit ConnectID (numeric string format)  
**Messaging:** Handled by Firebase (not stored in database)

---

## 🔐 1. USERS Collection

**Purpose:** Core authentication and user account management  
**Collection:** `users`

### Schema Structure:
```javascript
{
  _id: ObjectId,
  connectId: String,             // 12-digit numeric ID - Primary Key (e.g., "123456789012")
  username: String,              // Unique username (optional)
  email: String,                 // Unique email address
  passwordHash: String,          // BCrypt hashed password
  role: String,                  // "USER" | "ADMIN" | "MODERATOR"
  active: Boolean,               // Account status (soft delete)
  emailVerified: Boolean,        // Email verification status
  createdAt: DateTime,           // Account creation timestamp
  updatedAt: DateTime,           // Last update timestamp
  lastLoginAt: DateTime,         // Last login timestamp
  emailVerifiedAt: DateTime,     // Email verification timestamp
  deletedAt: DateTime,           // Soft deletion timestamp
  lastLoginDevice: String        // Device type for last login
}
```

### ConnectID Generation:
- **Format:** 12-digit numeric string (e.g., "123456789012")
- **Range:** 100000000000 to 999999999999
- **Generated:** At user registration
- **Uniqueness:** Must be unique across entire system

### Indexes:
```javascript
// Primary indexes
{ "email": 1 }                    // Unique index for email lookup
{ "connectId": 1 }                // Unique index for ConnectID lookup
{ "username": 1 }                 // Unique index for username lookup (sparse)
{ "active": 1 }                   // Filter active users
{ "createdAt": 1 }                // Time-based queries

// Compound indexes
{ "email": 1, "active": 1 }       // Login queries
{ "emailVerified": 1, "active": 1 }  // Verified active users
```

### Constraints:
- **connectId**: Unique, required, 12-digit numeric string
- **email**: Unique, required, email format
- **passwordHash**: Required for authentication
- **role**: Enum ["USER", "ADMIN", "MODERATOR"]
- **active**: Default true
- **emailVerified**: Default false

---

## 👤 2. USER_PROFILES Collection

**Purpose:** Complete dating profile information and preferences  
**Collection:** `user_profiles`

### Schema Structure:
```javascript
{
  _id: ObjectId,
  connectId: String,             // 12-digit ConnectID - Primary Key
  userId: String,                // Foreign Key -> users.connectId
  
  // Basic Profile Info
  firstName: String,             // First name
  lastName: String,              // Last name
  name: String,                  // Full display name
  age: Number,                   // Calculated from dateOfBirth
  dateOfBirth: Date,             // Birth date (18+ validation)
  bio: String,                   // Profile bio (max 500 chars)
  location: String,              // City, State/Country
  
  // Photos (Embedded Array)
  photos: [
    {
      id: String,                // Photo ID
      url: String,               // Image URL
      isPrimary: Boolean,        // Primary profile photo
      order: Number              // Display order (1-6)
    }
  ],
  
  // Interests & Lifestyle
  interests: [String],           // Array of interest tags
  
  // Detailed Profile (Embedded Object)
  profile: {
    pronouns: String,            // "he/him", "she/her", "they/them"
    gender: String,              // "Man", "Woman", "Non-binary", etc.
    sexuality: String,           // "Straight", "Gay", "Bisexual", etc.
    interestedIn: String,        // "Men", "Women", "Everyone"
    jobTitle: String,            // Professional title
    company: String,             // Company name
    university: String,          // University name
    educationLevel: String,      // "High School", "Bachelor's", etc.
    religiousBeliefs: String,    // Religious preference
    hometown: String,            // Hometown
    politics: String,            // Political leaning
    languages: [String],         // Spoken languages
    datingIntentions: String,    // "Casual", "Serious", "Marriage"
    relationshipType: String,    // "Monogamous", "Non-monogamous"
    height: String,              // "5'8\"", "175cm"
    ethnicity: String,           // Ethnic background
    children: String,            // "No kids", "Have kids", etc.
    familyPlans: String,         // "Want kids", "Don't want kids"
    pets: String,                // "Dog person", "Cat person", etc.
    zodiacSign: String           // Astrological sign
  },
  
  // Written Prompts (Embedded Array)
  writtenPrompts: [
    {
      question: String,          // Prompt question
      answer: String             // User's answer
    }
  ],
  
  // Poll Prompts (Embedded Array) 
  pollPrompts: [
    {
      question: String,          // Poll question
      description: String,       // Poll description
      options: [String]          // Poll options
    }
  ],
  
  // Field Visibility Settings (Embedded Object)
  fieldVisibility: {
    jobTitle: Boolean,           // Show job title
    university: Boolean,         // Show university
    religiousBeliefs: Boolean,   // Show religion
    politics: Boolean,           // Show politics
    height: Boolean,             // Show height
    ethnicity: Boolean,          // Show ethnicity
    // ... other visibility flags
  },
  
  // User Preferences (Embedded Object)
  preferences: {
    preferredGender: String,     // "men", "women", "both"
    minAge: Number,              // Minimum age preference (18+)
    maxAge: Number,              // Maximum age preference
    maxDistance: Number,         // Max distance in miles/km
    minHeight: Number,           // Min height preference (inches)
    maxHeight: Number,           // Max height preference (inches)
    datingIntention: String,     // Preferred dating intention
    drinkingPreference: String,  // Drinking preference
    smokingPreference: String,   // Smoking preference
    religionImportance: String,  // Religion importance level
    wantsChildren: Boolean       // Wants children
  },
  
  // Metadata
  active: Boolean,               // Profile status
  createdAt: DateTime,           // Profile creation
  updatedAt: DateTime,           // Last profile update
  lastActive: DateTime,          // Last app activity
  version: Number                // Profile version for updates
}
```

### Indexes:
```javascript
// Primary indexes
{ "userId": 1 }                  // Unique index - one profile per user
{ "connectId": 1 }               // ConnectID lookup
{ "active": 1 }                  // Active profiles only
{ "location": 1 }                // Location-based matching
{ "age": 1 }                     // Age-based filtering

// Matching algorithm indexes
{ "active": 1, "age": 1, "location": 1 }
{ "preferences.preferredGender": 1, "profile.gender": 1 }
{ "preferences.minAge": 1, "preferences.maxAge": 1 }

// Search indexes
{ "interests": 1 }               // Interest-based matching
{ "profile.jobTitle": "text", "profile.university": "text", "bio": "text" }
```

### Constraints:
- **userId**: Unique foreign key to users.connectId
- **connectId**: Unique ConnectID
- **age**: Minimum 18, calculated from dateOfBirth
- **photos**: Maximum 6 photos, at least 1 required
- **bio**: Maximum 500 characters
- **preferences.minAge/maxAge**: Valid age ranges (18-100)

---

## 💕 3. MATCHES Collection

**Purpose:** User matches and relationship status  
**Collection:** `matches`

### Schema Structure:
```javascript
{
  _id: ObjectId,
  connectId: String,             // 12-digit ConnectID - Primary Key
  user1Id: String,               // Foreign Key -> users.connectId (alphabetically first)
  user2Id: String,               // Foreign Key -> users.connectId (alphabetically second)
  matchedAt: DateTime,           // When mutual like occurred
  status: String,                // "ACTIVE" | "UNMATCHED" | "BLOCKED_BY_USER1" | "BLOCKED_BY_USER2" | "REPORTED"
  lastActivityAt: DateTime,      // Last interaction timestamp
  
  // Match metadata - SIMPLIFIED (no super likes/boosts)
  user1ActionAt: DateTime,       // When user1 liked
  user2ActionAt: DateTime,       // When user2 liked
  
  // Moderation
  reportedBy: String,            // ConnectID who reported (if any)
  reportedAt: DateTime,          // Report timestamp
  adminNotes: String             // Admin moderation notes
}
```

### Indexes:
```javascript
// Primary lookups
{ "user1Id": 1, "user2Id": 1 }    // Unique compound index
{ "user1Id": 1, "status": 1 }     // User's matches
{ "user2Id": 1, "status": 1 }     // User's matches
{ "connectId": 1 }                // Match lookup

// Time-based queries
{ "matchedAt": 1 }                // Recent matches
{ "lastActivityAt": 1 }           // Active matches
{ "status": 1, "lastActivityAt": 1 }  // Active recent matches
```

### Constraints:
- **user1Id + user2Id**: Unique compound key (user1Id < user2Id alphabetically)
- **status**: Enum ["ACTIVE", "UNMATCHED", "BLOCKED_BY_USER1", "BLOCKED_BY_USER2", "REPORTED"]
- **connectId**: Unique match identifier

---

## 👍 4. USER_ACTIONS Collection

**Purpose:** User like/pass actions for matching algorithm  
**Collection:** `user_actions`

### Schema Structure:
```javascript
{
  _id: ObjectId,
  connectId: String,             // 12-digit ConnectID - Primary Key
  userId: String,                // Foreign Key -> users.connectId (user performing action)
  targetUserId: String,          // Foreign Key -> users.connectId (user being acted upon)
  action: String,                // "LIKE" | "PASS" | "DISLIKE" (NO SUPER_LIKE)
  batchDate: String,             // Date string "YYYY-MM-DD" for batch processing
  timestamp: DateTime,           // Action timestamp
  processed: Boolean,            // Batch processing status
  
  // Action context
  source: String,                // "DISCOVERY" | "SEARCH"
  matchSetId: String,            // Foreign Key -> match_sets.connectId
  
  // Result tracking
  resultedInMatch: Boolean,      // Did this action create a match?
  matchId: String,               // Foreign Key -> matches.connectId (if match created)
  
  // Metadata
  deviceType: String,            // "iOS" | "Android" | "Web"
  appVersion: String             // App version when action taken
}
```

### Indexes:
```javascript
// Primary queries
{ "userId": 1, "timestamp": -1 }         // User's action history
{ "targetUserId": 1, "action": 1 }       // Actions received by user
{ "userId": 1, "targetUserId": 1 }       // Unique user pair actions

// Batch processing
{ "batchDate": 1, "processed": 1 }       // Daily batch processing
{ "matchSetId": 1 }                      // Match set actions

// Algorithm queries  
{ "userId": 1, "action": 1, "timestamp": -1 }     // User's likes/passes
{ "action": 1, "timestamp": -1 }                   // All likes for analytics
{ "resultedInMatch": 1, "timestamp": -1 }          // Match success rate
```

### Constraints:
- **userId + targetUserId**: Unique compound key (one action per user pair)
- **action**: Enum ["LIKE", "PASS", "DISLIKE"] (NO super like/boost features)
- **batchDate**: ISO date string format
- **source**: Enum ["DISCOVERY", "SEARCH"] (NO boost source)

---

## 📅 5. MATCH_SETS Collection

**Purpose:** Daily batches of potential matches for users  
**Collection:** `match_sets`

### Schema Structure:
```javascript
{
  _id: ObjectId,
  connectId: String,             // 12-digit ConnectID - Primary Key
  userId: String,                // Foreign Key -> users.connectId
  date: Date,                    // Date for this match set (YYYY-MM-DD)
  userIds: [String],             // Array of potential match ConnectIDs
  status: String,                // "PENDING" | "ACTIVE" | "COMPLETED" | "EXPIRED"
  
  // Timing
  createdAt: DateTime,           // Match set creation time
  expiresAt: DateTime,           // Match set expiration time (24 hours)
  completedAt: DateTime,         // When user finished all actions
  
  // Progress tracking
  totalUsers: Number,            // Total users in set
  actionsSubmitted: Number,      // Actions taken by user
  matchesFound: Number,          // Matches created from this set
  
  // Algorithm metadata
  algorithmVersion: String,      // Matching algorithm version
  filters: {
    ageRange: [Number, Number],  // Age filter applied
    maxDistance: Number,         // Distance filter applied
    preferences: Object          // Other preference filters
  },
  
  // Performance tracking
  viewTime: Number,              // Total time spent viewing (seconds)
  avgTimePerProfile: Number      // Average time per profile (seconds)
}
```

### Indexes:
```javascript
// Primary queries
{ "userId": 1, "date": -1 }             // User's match sets by date
{ "userId": 1, "status": 1 }            // User's active/pending sets
{ "status": 1, "expiresAt": 1 }         // Expired sets cleanup
{ "connectId": 1 }                      // Match set lookup

// Algorithm queries
{ "date": 1, "status": 1 }              // Daily match set analytics
{ "userId": 1, "status": 1, "date": -1 } // User's recent active sets
```

### Constraints:
- **userId + date**: Unique compound key (one match set per user per day)
- **status**: Enum ["PENDING", "ACTIVE", "COMPLETED", "EXPIRED"]
- **userIds**: Array of ConnectIDs (max 20-50 users per set)
- **date**: Date-only format

---

## 🚫 6. BLOCKED_USERS Collection

**Purpose:** User blocking relationships for safety  
**Collection:** `blocked_users`

### Schema Structure:
```javascript
{
  _id: ObjectId,
  connectId: String,             // 12-digit ConnectID - Primary Key
  userId: String,                // Foreign Key -> users.connectId (user who blocked)
  blockedUserId: String,         // Foreign Key -> users.connectId (user who was blocked)
  reason: String,                // Reason for blocking
  blockedAt: DateTime,           // Block timestamp
  status: String,                // "ACTIVE" | "REMOVED"
  
  // Block context
  source: String,                // "PROFILE" | "MATCH" | "REPORT"
  matchId: String,               // Foreign Key -> matches.connectId (if from match)
  
  // Block details
  blockType: String,             // "USER_INITIATED" | "ADMIN_ENFORCED" | "SYSTEM_SAFETY"
  severity: String,              // "LOW" | "MEDIUM" | "HIGH" | "CRITICAL"
  
  // Admin moderation
  reviewedBy: String,            // Admin ConnectID who reviewed block
  reviewedAt: DateTime,          // Review timestamp
  adminNotes: String,            // Admin notes
  
  // Unblock tracking
  unblockedAt: DateTime,         // Unblock timestamp
  unblockReason: String,         // Reason for unblocking
  unblockedBy: String            // ConnectID who unblocked (user or admin)
}
```

### Indexes:
```javascript
// Primary queries
{ "userId": 1, "status": 1 }            // User's active blocks
{ "blockedUserId": 1, "status": 1 }     // Users who blocked this user
{ "userId": 1, "blockedUserId": 1 }     // Specific block relationship
{ "connectId": 1 }                      // Block lookup

// Safety queries
{ "blockType": 1, "severity": 1 }       // Admin safety monitoring
{ "status": 1, "blockedAt": -1 }        // Recent blocks
{ "reviewedBy": 1, "reviewedAt": -1 }   // Admin review tracking
```

### Constraints:
- **userId + blockedUserId**: Unique compound key
- **status**: Enum ["ACTIVE", "REMOVED"]
- **source**: Enum ["PROFILE", "MATCH", "REPORT"]
- **blockType**: Enum ["USER_INITIATED", "ADMIN_ENFORCED", "SYSTEM_SAFETY"]
- **severity**: Enum ["LOW", "MEDIUM", "HIGH", "CRITICAL"]

---

## 🛡️ 7. USER_REPORTS Collection

**Purpose:** Safety reports and content moderation  
**Collection:** `user_reports`

### Schema Structure:
```javascript
{
  _id: ObjectId,
  connectId: String,             // 12-digit ConnectID - Primary Key
  reporterId: String,            // Foreign Key -> users.connectId (user making report)
  reportedUserId: String,        // Foreign Key -> users.connectId (user being reported)
  
  // Report details
  reasons: [String],             // Array of report reasons
  description: String,           // Detailed description from reporter
  evidenceUrls: [String],        // URLs to screenshots/evidence
  
  // Context
  context: String,               // "PROFILE" | "MATCH" | "PHOTO"
  matchId: String,               // Foreign Key -> matches.connectId (if match-related)
  
  // Timestamps
  reportedAt: DateTime,          // Report submission time
  reviewedAt: DateTime,          // Admin review time
  resolvedAt: DateTime,          // Resolution time
  
  // Status tracking
  status: String,                // "PENDING" | "UNDER_REVIEW" | "RESOLVED" | "DISMISSED" | "ACTION_TAKEN"
  priority: String,              // "LOW" | "MEDIUM" | "HIGH" | "URGENT"
  
  // Moderation
  reviewedBy: String,            // Admin ConnectID who reviewed
  adminNotes: String,            // Internal admin notes
  actionTaken: String,           // Action taken ("WARNING" | "SUSPEND" | "BAN" | "CONTENT_REMOVED")
  
  // Follow-up
  followUpRequired: Boolean,     // Needs follow-up action
  escalated: Boolean,            // Escalated to senior moderator
  escalatedTo: String,           // Senior moderator ConnectID
  escalatedAt: DateTime          // Escalation timestamp
}
```

### Indexes:
```javascript
// Moderation workflow
{ "status": 1, "priority": 1, "reportedAt": 1 }    // Admin queue
{ "reviewedBy": 1, "reviewedAt": -1 }              // Admin workload
{ "reporterId": 1, "reportedAt": -1 }              // Reporter history
{ "connectId": 1 }                                 // Report lookup

// User safety
{ "reportedUserId": 1, "status": 1 }               // Reports against user
{ "reportedUserId": 1, "reportedAt": -1 }          // Report timeline
{ "context": 1, "status": 1 }                      // Report type analysis

// Escalation tracking
{ "escalated": 1, "escalatedTo": 1 }               // Escalated reports
{ "followUpRequired": 1, "status": 1 }             // Follow-up needed
```

### Constraints:
- **reasons**: Array of predefined reason codes
- **status**: Enum ["PENDING", "UNDER_REVIEW", "RESOLVED", "DISMISSED", "ACTION_TAKEN"]
- **priority**: Enum ["LOW", "MEDIUM", "HIGH", "URGENT"]
- **context**: Enum ["PROFILE", "MATCH", "PHOTO"]
- **actionTaken**: Enum ["WARNING", "SUSPEND", "BAN", "CONTENT_REMOVED"]

---

## 🔔 8. NOTIFICATIONS Collection

**Purpose:** Notification history and tracking (Firebase handles delivery)  
**Collection:** `notifications`

### Schema Structure:
```javascript
{
  _id: ObjectId,
  connectId: String,             // 12-digit ConnectID - Primary Key
  userId: String,                // Foreign Key -> users.connectId
  
  // Notification content
  type: String,                  // Notification type (see enum below)
  title: String,                 // Notification title
  message: String,               // Notification message
  data: Object,                  // Additional data payload
  
  // Status
  status: String,                // "PENDING" | "SENT" | "DELIVERED" | "FAILED" | "CANCELLED"
  priority: String,              // "LOW" | "NORMAL" | "HIGH" | "URGENT"
  
  // Timestamps
  createdAt: DateTime,           // Notification creation
  sentAt: DateTime,              // Sent timestamp (via Firebase)
  deliveredAt: DateTime,         // Delivered timestamp
  readAt: DateTime,              // Read timestamp
  
  // User interaction
  isRead: Boolean,               // Read status
  isSent: Boolean,               // Sent status
  clicked: Boolean,              // User clicked notification
  clickedAt: DateTime,           // Click timestamp
  
  // Content
  actionUrl: String,             // Deep link URL
  imageUrl: String,              // Notification image
  
  // Expiration
  expiresAt: DateTime,           // Notification expiry time
  isExpired: Boolean,            // Expiration status
  
  // Grouping
  groupId: String,               // Group related notifications
  
  // A/B Testing
  campaign: String,              // Campaign identifier
  variant: String                // A/B test variant
}
```

### Notification Types:
```javascript
// Enum values for type field
{
  "NEW_MATCH": "You have a new match!",
  "PROFILE_VIEW": "Someone viewed your profile",
  "POTENTIAL_MATCHES_READY": "Your potential matches are ready",
  "WELCOME": "Welcome to Connect!",
  "PROFILE_INCOMPLETE": "Complete your profile to get better matches",
  "ACCOUNT_VERIFICATION": "Please verify your account",
  "SAFETY_ALERT": "Important safety information",
  "SYSTEM_MAINTENANCE": "Scheduled maintenance notification"
}
```

### Indexes:
```javascript
// User notifications
{ "userId": 1, "createdAt": -1 }          // User's notification history
{ "userId": 1, "isRead": 1, "createdAt": -1 }  // Unread notifications
{ "connectId": 1 }                        // Notification lookup

// Delivery tracking
{ "status": 1, "createdAt": 1 }           // Delivery status monitoring
{ "type": 1, "createdAt": -1 }            // Notification type analytics

// Cleanup and expiration
{ "expiresAt": 1, "isExpired": 1 }        // Expired notification cleanup

// Campaign tracking
{ "campaign": 1, "variant": 1, "clicked": 1 }  // A/B test results
```

### Constraints:
- **type**: Enum of predefined notification types
- **status**: Enum ["PENDING", "SENT", "DELIVERED", "FAILED", "CANCELLED"]
- **priority**: Enum ["LOW", "NORMAL", "HIGH", "URGENT"]

---

## 🛡️ 9. SAFETY_BLOCKS Collection

**Purpose:** Admin-enforced safety blocks and automated safety actions  
**Collection:** `safety_blocks`

### Schema Structure:
```javascript
{
  _id: ObjectId,
  connectId: String,             // 12-digit ConnectID - Primary Key
  userId: String,                // Foreign Key -> users.connectId (user being blocked)
  
  // Block details
  blockType: String,             // "PROFILE_REVIEW" | "SUSPICIOUS_ACTIVITY" | "CONTENT_VIOLATION" | "ADMIN_ACTION"
  severity: String,              // "WARNING" | "TEMPORARY" | "PERMANENT"
  reason: String,                // Detailed reason for block
  
  // Timing
  blockedAt: DateTime,           // Block start time
  expiresAt: DateTime,           // Block expiration (null for permanent)
  duration: Number,              // Block duration in hours
  
  // Status
  status: String,                // "ACTIVE" | "EXPIRED" | "LIFTED" | "ESCALATED"
  isActive: Boolean,             // Block currently active
  
  // Admin information
  createdBy: String,             // Admin ConnectID who created block
  reviewedBy: String,            // Admin who reviewed/approved
  liftedBy: String,              // Admin who lifted block early
  
  // Evidence and context
  reportIds: [String],           // Related report ConnectIDs
  evidenceUrls: [String],        // Evidence screenshots/files
  context: String,               // Additional context
  
  // User impact
  restrictedFeatures: [String],  // Features blocked ("MATCHING" | "PROFILE_EDIT" | "LOGIN")
  warningMessage: String,        // Message shown to user
  
  // Appeals process
  appealSubmitted: Boolean,      // User submitted appeal
  appealedAt: DateTime,          // Appeal submission time
  appealReason: String,          // User's appeal reason
  appealReviewedBy: String,      // Admin ConnectID who reviewed appeal
  appealDecision: String,        // "UPHELD" | "OVERTURNED" | "REDUCED"
  appealReviewedAt: DateTime,    // Appeal review time
  
  // Automation
  triggeredBySystem: Boolean,    // Automatically triggered
  triggerRule: String,           // Automation rule that triggered block
  humanReviewRequired: Boolean,  // Needs human review
  
  // Notes
  adminNotes: String,            // Internal admin notes
  publicReason: String           // Reason shown to user
}
```

### Indexes:
```javascript
// Admin management
{ "userId": 1, "status": 1 }             // User's active blocks
{ "createdBy": 1, "blockedAt": -1 }      // Admin's block history
{ "status": 1, "expiresAt": 1 }          // Expiring blocks
{ "connectId": 1 }                       // Block lookup

// Safety monitoring
{ "blockType": 1, "severity": 1 }        // Block type analysis
{ "triggeredBySystem": 1, "humanReviewRequired": 1 }  // Automation review queue
{ "severity": 1, "blockedAt": -1 }       // Severity trend analysis

// Appeals process
{ "appealSubmitted": 1, "appealReviewedBy": 1 }  // Appeal queue
{ "appealDecision": 1, "appealReviewedAt": -1 }  // Appeal outcomes
```

### Constraints:
- **blockType**: Enum ["PROFILE_REVIEW", "SUSPICIOUS_ACTIVITY", "CONTENT_VIOLATION", "ADMIN_ACTION"]
- **severity**: Enum ["WARNING", "TEMPORARY", "PERMANENT"]
- **status**: Enum ["ACTIVE", "EXPIRED", "LIFTED", "ESCALATED"]
- **restrictedFeatures**: Array of feature codes (NO messaging restrictions - Firebase handles)
- **appealDecision**: Enum ["UPHELD", "OVERTURNED", "REDUCED"]

---

## 🔥 Firebase Integration

### What Firebase Handles:
- **Real-time messaging** between matched users
- **Push notification delivery** to devices
- **Message encryption** and security
- **Online/offline status** tracking
- **Typing indicators** for conversations
- **Message delivery receipts**

### What MongoDB Stores:
- **User profiles and preferences**
- **Match relationships and history**
- **User actions** (likes/passes) for algorithm
- **Safety and moderation data**
- **Notification history** for analytics
- **Admin tools and reporting**

### Integration Notes:
- Firebase FCM tokens managed by Firebase SDK
- Message history can be stored in Firebase (not MongoDB)
- MongoDB focus: user data, matching, safety, analytics
- Real-time features handled entirely by Firebase

---

## 📊 Database Statistics & Scaling

### Expected Data Volumes:
- **Users**: 100K - 1M users
- **User Profiles**: 1:1 with users
- **Matches**: ~5-10 matches per active user
- **User Actions**: ~20-50 actions per user per day
- **Notifications**: ~5-10 notifications per user per day

### Storage Estimates:
- **Total Collections**: 9 (simplified from 13)
- **Total Indexes**: ~45 indexes across all collections
- **Estimated Storage**: 30GB - 300GB for 1M users
- **Daily Growth**: ~500MB - 2GB per day at scale

### Performance Optimization:
1. **ConnectID Indexing**: Fast numeric lookups
2. **Compound Indexes**: Optimized for common query patterns
3. **Time-based Partitioning**: Consider partitioning by date for large collections
4. **Archive Strategy**: Archive old notifications after 6 months
5. **Caching**: Redis cache for frequently accessed user profiles
6. **Read Replicas**: For analytics and reporting queries

### Backup & Recovery:
- **Daily Backups**: Full database backup daily
- **Point-in-time Recovery**: 7-day window
- **Geographic Replication**: Multi-region for disaster recovery
- **Data Retention**: 7 years for user data, 1 year for notifications

---

## 🔧 Migration Strategy

### From Current UUID Schema:
1. **Phase 1**: Generate ConnectIDs for existing users
2. **Phase 2**: Update all foreign key references
3. **Phase 3**: Remove messaging collections (migrate to Firebase)
4. **Phase 4**: Remove premium/boost features
5. **Phase 5**: Update indexes and constraints

### ConnectID Generation Strategy:
```javascript
// Example ConnectID generation
function generateConnectId() {
  const min = 100000000000; // 12-digit minimum
  const max = 999999999999; // 12-digit maximum
  let connectId;
  
  do {
    connectId = Math.floor(Math.random() * (max - min + 1)) + min;
  } while (isConnectIdExists(connectId)); // Ensure uniqueness
  
  return connectId.toString();
}
```

This simplified database schema provides a clean foundation for the Connect dating application with ConnectID, Firebase integration, and removal of unnecessary premium features! 🚀