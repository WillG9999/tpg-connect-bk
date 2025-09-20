# Profile Backend Reorganization

This document outlines the reorganization of the backend profile functionality to better align with frontend data requirements and provide comprehensive profile management.

## Summary of Changes

The backend has been reorganized to group all profile-related functionality together, providing a unified and comprehensive profile management system that matches the frontend's User model structure.

## New Structure

### 1. Unified Profile Controller (`/api/profile`)

**File:** `src/main/java/com/tpg/connect/controllers/profile/ProfileController.java`

A comprehensive controller that handles all profile operations:

- **GET `/api/profile`** - Get current user profile
- **PUT `/api/profile`** - Update complete profile  
- **PUT `/api/profile/basic`** - Update basic info (name, bio, location, interests)
- **POST `/api/profile/photos`** - Upload a new photo
- **DELETE `/api/profile/photos/{photoId}`** - Remove a specific photo
- **PUT `/api/profile/photos`** - Update all photos
- **PUT `/api/profile/prompts/written`** - Update written prompts
- **PUT `/api/profile/prompts/polls`** - Update poll prompts
- **PUT `/api/profile/visibility`** - Update field visibility settings
- **PUT `/api/profile/preferences`** - Update user preferences

### 2. Comprehensive Profile Service

**File:** `src/main/java/com/tpg/connect/services/ProfileManagementService.java`

A unified service that handles all profile management operations:

- Complete profile retrieval and updates
- Photo management (add, remove, reorder)
- Written and poll prompt management
- Field visibility settings
- User preferences management
- Profile validation and business rules
- Caching support for performance

### 3. Updated DTOs

**Files:**
- `src/main/java/com/tpg/connect/model/api/ProfileUpdateRequest.java`
- `src/main/java/com/tpg/connect/model/api/ProfileUpdateResponse.java`
- `src/main/java/com/tpg/connect/model/dto/UpdateProfileRequest.java`
- `src/main/java/com/tpg/connect/model/dto/PhotoUploadRequest.java`

Simplified DTOs that match the frontend User model structure:

```java
// ProfileUpdateRequest includes all fields from frontend User model:
- Basic info: name, age, bio, location, interests
- Identity: pronouns, gender, sexuality, interestedIn
- Virtues: jobTitle, company, university, educationLevel, etc.
- Vitals: height, ethnicity, children, familyPlans, pets, zodiacSign
- Photos: List of photo URLs
- Written/Poll prompts
- Field visibility settings
- User preferences
```

### 4. Enhanced Validation

**File:** `src/main/java/com/tpg/connect/config/ValidationConfig.java`

Added validation configuration to ensure data integrity:

- Profile validation with business rules
- Photo count validation (1-6 photos)
- Age validation (18-100)
- URL validation for photos
- Field size and format validation

## Frontend Alignment

The reorganized backend now perfectly matches the frontend data requirements:

### Frontend User Model
```dart
class User {
  // Basic info
  final String name;
  final int age;
  final String bio;
  final List<String> photos;
  final String location;
  final List<String> interests;
  
  // Identity, Virtues, Vitals
  final String pronouns, gender, sexuality, interestedIn;
  final String jobTitle, company, university, educationLevel;
  final String height, ethnicity, children, familyPlans;
  
  // Prompts and visibility
  final List<Map<String, String>> writtenPrompts;
  final List<Map<String, dynamic>> pollPrompts;
  final Map<String, bool>? fieldVisibility;
}
```

### Backend CompleteUserProfile
```java
@Document(collection = "users")
public class CompleteUserProfile {
  // Exact same structure as frontend
  private String name;
  private int age;
  private String bio;
  private List<Photo> photos;
  private String location;
  private List<String> interests;
  
  private UserProfile profile; // Contains identity, virtues, vitals
  private List<WrittenPrompt> writtenPrompts;
  private List<PollPrompt> pollPrompts;
  private FieldVisibility fieldVisibility;
  private UserPreferences preferences;
}
```

## Benefits of Reorganization

### 1. **Unified Profile Management**
- All profile operations in one place
- Single service for all profile functionality
- Consistent validation and business rules

### 2. **Frontend Alignment**
- DTOs match frontend User model exactly
- Single API call can update complete profile
- Simplified integration between frontend and backend

### 3. **Improved Performance**
- Caching support with automatic cache invalidation
- Batch operations for efficiency
- Optimized data retrieval

### 4. **Better Organization**
- Clear separation of concerns
- Profile functionality grouped together
- Easier maintenance and development

### 5. **Enhanced Validation**
- Comprehensive validation rules
- Business logic enforcement
- Data integrity guarantees

## Migration Notes

### Deprecated Components
- `UserController` is now deprecated (kept for backward compatibility)
- `UserProfileService` functionality moved to `ProfileManagementService`

### New Endpoints
All new profile endpoints are under `/api/profile` path with comprehensive OpenAPI documentation.

### Backward Compatibility
The old `UserController` endpoints are still available but marked as deprecated. They will be removed in future versions.

## Usage Examples

### Complete Profile Update
```java
PUT /api/profile
{
  "name": "John Doe",
  "age": 28,
  "bio": "Looking for meaningful connections",
  "pronouns": "he/him",
  "jobTitle": "Software Engineer",
  "photos": ["https://example.com/photo1.jpg"],
  "writtenPrompts": [{"prompt": "My ideal Sunday", "answer": "Hiking and coffee"}],
  "fieldVisibility": {"jobTitle": true, "politics": false}
}
```

### Photo Management
```java
POST /api/profile/photos
{
  "photoUrl": "https://example.com/new-photo.jpg",
  "isPrimary": false
}
```

### Field Visibility Update
```java
PUT /api/profile/visibility
{
  "jobTitle": true,
  "politics": false,
  "religiousBeliefs": false
}
```

This reorganization provides a solid foundation for comprehensive profile management that scales with the application's needs while maintaining excellent frontend-backend alignment.