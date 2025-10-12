# Connect Dating Application - Complete Documentation

## 📋 Application Overview

**Connect** is a comprehensive dating application built with a modern tech stack consisting of a Spring Boot backend and Flutter frontend. The application provides full dating app functionality including user registration, profile management, photo uploads, matching algorithms, messaging, premium subscriptions, and robust safety features.

### Key Features
- **User Authentication** - JWT-based auth with email verification
- **Profile Management** - Detailed profiles with photos, prompts, and preferences  
- **Discovery & Matching** - Swipe-based matching with daily batches
- **Conversations** - Real-time messaging between matched users
- **Premium Features** - Subscription-based premium tiers
- **Safety Features** - User reporting, blocking, and safety controls
- **Push Notifications** - Firebase messaging integration

---

## 🏗️ Architecture Overview

### Backend: Spring Boot Application
- **Framework**: Spring Boot 3.5.5
- **Java Version**: 21
- **Database**: Firebase Firestore (NoSQL)
- **Authentication**: JWT tokens with BCrypt password hashing
- **File Storage**: Google Cloud Storage via Firebase
- **Email Service**: Multi-provider (SendGrid, AWS SES, SMTP)
- **API Documentation**: OpenAPI/Swagger UI
- **Testing**: JUnit, Cucumber BDD tests

### Frontend: Flutter Application  
- **Framework**: Flutter 3.9+ with Dart
- **State Management**: Provider pattern
- **HTTP Client**: Dio for API communication
- **Storage**: Flutter Secure Storage + Hive for caching
- **Photo Handling**: Image picker with upload capabilities
- **UI**: Material Design with custom components

### Database: Firebase Firestore
- **Type**: NoSQL Document Database
- **Collections**: 8 core collections (users, userProfiles, matches, conversations, etc.)
- **ID Pattern**: 12-digit ConnectID numeric strings
- **Real-time**: Native Firestore listeners for live updates

---

## 🔧 Backend Components

### Core Application Structure
```
src/main/java/com/tpg/connect/
├── ConnectApplication.java          # Main Spring Boot application
├── config/                          # Configuration classes
├── controllers/                     # REST API endpoints
├── services/                        # Business logic layer
├── repository/                      # Data access layer
├── model/                          # Data models and DTOs
├── utilities/                       # Utility classes
└── exceptions/                      # Custom exception handling
```

### Key Configuration Classes
- **`FirebaseConfig.java`** - Firebase/GCP integration setup
- **`SecurityConfig.java`** - Spring Security and CORS configuration  
- **`JwtConfig.java`** - JWT token configuration and validation
- **`EmailConfig.java`** - Multi-provider email service setup
- **`CacheConfig.java`** - Application caching configuration
- **`WebMvcConfig.java`** - Web MVC and CORS configuration

### Controller Layer
- **`AuthController.java`** - Authentication endpoints (register, login, password reset)
- **`ProfileController.java`** - User profile management
- **`DiscoveryController.java`** - Match discovery and swiping
- **`MatchController.java`** - Match management and messaging
- **`ConversationController.java`** - Conversation management
- **`SafetyController.java`** - User safety and reporting
- **`SubscriptionController.java`** - Premium subscription management
- **`NotificationController.java`** - Push notification management

### Service Layer
- **`AuthenticationService.java`** - User authentication and JWT management
- **`UserService.java`** - User profile and data management
- **`MatchService.java`** - Matching algorithm and match management
- **`DiscoveryService.java`** - User discovery and filtering
- **`EmailService.java`** - Multi-provider email sending
- **`CloudStorageService.java`** - Firebase Storage integration
- **`NotificationService.java`** - Push notification handling
- **`SubscriptionService.java`** - Premium subscription logic
- **`SafetyService.java`** - User safety and reporting

### Data Models

#### Core User Models
- **`User.java`** - Core authentication user entity
- **`UserProfile.java`** - Detailed profile information
- **`CompleteUserProfile.java`** - Combined user data for API responses
- **`Photo.java`** - User photo metadata
- **`UserPreferences.java`** - App preferences and discovery settings

#### Matching Models  
- **`Match.java`** - Match relationship between users
- **`PotentialMatch.java`** - Discovery candidate user data
- **`UserAction.java`** - Like/pass actions in discovery
- **`MatchSet.java`** - Daily batch of potential matches

#### Conversation Models
- **`Conversation.java`** - Chat conversation metadata
- **`Message.java`** - Individual chat messages
- **`ConversationSummary.java`** - Conversation overview data

#### Safety Models
- **`SafetyBlock.java`** - Admin safety restrictions
- **`UserReport.java`** - User-generated safety reports
- **`BlockedUser.java`** - User blocking relationships

### Repository Layer
All repositories follow the Firebase implementation pattern:
- Interface definitions with standard CRUD operations
- Firebase-specific implementations in `/impl/` package
- Custom queries for complex data retrieval
- Caching integration for frequently accessed data

---

## 📱 Frontend Components

### Flutter Application Structure
```
lib/
├── main.dart                       # Application entry point
├── core/                          # Core utilities and constants
├── data/                          # Data layer (models, repositories)
├── business/                      # Business logic layer
├── presentation/                  # UI layer (pages, widgets)
└── service/                       # Service layer (API clients)
```

### Key Components

#### Data Layer
- **`models/`** - Dart data models matching backend DTOs
- **`requests/`** - API request/response models
- **`repositories/`** - Local data caching and management

#### Business Layer  
- **`services/`** - Business logic services
- **`use_cases/`** - Specific user action implementations

#### Presentation Layer
- **`pages/`** - Screen/page widgets
- **`widgets/`** - Reusable UI components
- **`auth/`** - Authentication flow screens

#### Service Layer
- **`client/`** - HTTP client configuration
- **`controllers/`** - State management controllers

### Key Services
- **`AuthService`** - Authentication state management
- **`UserService`** - User profile management
- **`MatchService`** - Match discovery and management
- **`PhotoUploadService`** - Image upload handling
- **`CacheService`** - Local data caching
- **`NotificationService`** - Push notification handling

---

## 🗄️ Database Schema (Firebase Firestore)

### Core Collections

#### 1. `/users/{connectId}`
Primary user authentication and account data
```javascript
{
  connectId: "123456789012",
  email: "user@example.com", 
  role: "USER",
  active: true,
  emailVerified: true,
  createdAt: Timestamp,
  updatedAt: Timestamp,
  fcmTokens: [...] // Push notification tokens
}
```

#### 2. `/userProfiles/{connectId}` 
Detailed user profile information
```javascript
{
  connectId: "123456789012",
  firstName: "John",
  lastName: "Doe",
  dateOfBirth: "1995-01-15",
  bio: "Software developer...",
  photos: [...],
  writtenPrompts: [...],
  pollPrompts: [...],
  preferences: {...}
}
```

#### 3. `/matches/{matchId}`
Match relationships between users
```javascript
{
  matchId: "match_uuid",
  user1Id: "123456789012",
  user2Id: "987654321098", 
  matchedAt: Timestamp,
  isActive: true,
  lastMessageAt: Timestamp,
  conversationId: "conv_uuid"
}
```

#### 4. `/conversations/{conversationId}`
Chat conversations between matched users
```javascript
{
  conversationId: "conv_uuid",
  participantIds: ["123456789012", "987654321098"],
  lastMessage: {...},
  lastMessageAt: Timestamp,
  isActive: true,
  messages: [...] // Subcollection
}
```

#### 5. `/userActions/{actionId}`
User like/pass actions for analytics
```javascript
{
  actionId: "action_uuid",
  userId: "123456789012",
  targetUserId: "987654321098",
  action: "LIKE", // LIKE, PASS, SUPER_LIKE
  createdAt: Timestamp,
  batchId: "batch_uuid"
}
```

#### 6. `/matchSets/{batchId}`
Daily batches of potential matches
```javascript
{
  batchId: "batch_uuid", 
  userId: "123456789012",
  potentialMatches: [...],
  createdAt: Timestamp,
  expiresAt: Timestamp,
  viewed: false
}
```

#### 7. `/notifications/{notificationId}`
User notifications and messages
```javascript
{
  notificationId: "notif_uuid",
  userId: "123456789012",
  type: "NEW_MATCH",
  title: "New Match!",
  message: "You have a new match",
  isRead: false,
  createdAt: Timestamp
}
```

#### 8. `/subscriptions/{subscriptionId}`
Premium subscription data
```javascript
{
  subscriptionId: "sub_uuid",
  userId: "123456789012", 
  planId: "premium_monthly",
  status: "ACTIVE",
  startDate: Timestamp,
  endDate: Timestamp,
  autoRenew: true
}
```

---

## 🔌 API Documentation

### Authentication Endpoints
```
POST /api/auth/register          # User registration
POST /api/auth/login             # User login  
POST /api/auth/logout            # User logout
POST /api/auth/refresh-token     # Token refresh
POST /api/auth/forgot-password   # Password reset request
POST /api/auth/reset-password    # Password reset completion
POST /api/auth/change-password   # Password change
DELETE /api/auth/delete-account  # Account deletion
GET  /api/auth/verify-email      # Email verification
POST /api/auth/resend-verification # Resend verification email
```

### User Profile Endpoints
```
GET  /api/profile                # Get current user profile
PUT  /api/profile                # Update complete profile
PUT  /api/profile/basic          # Update basic info
POST /api/profile/photos         # Upload new photo
DELETE /api/profile/photos/{id}  # Remove photo
PUT  /api/profile/photos         # Reorder photos
PUT  /api/profile/prompts        # Update written prompts
PUT  /api/profile/polls          # Update poll responses
PUT  /api/profile/visibility     # Update field visibility
PUT  /api/profile/preferences    # Update app preferences
```

### Discovery & Matching Endpoints  
```
GET  /api/discovery/matches      # Get potential matches
POST /api/discovery/like         # Like a user
POST /api/discovery/pass         # Pass on a user
GET  /api/discovery/settings     # Get discovery preferences
PUT  /api/discovery/settings     # Update discovery preferences
GET  /api/matches                # Get current matches
GET  /api/matches/daily          # Get daily match batch
POST /api/matches/actions        # Create match action
DELETE /api/matches/{id}         # Unmatch user
```

### Conversation Endpoints
```
GET  /api/conversations          # Get all conversations
GET  /api/conversations/{id}/messages # Get conversation messages
POST /api/conversations/{id}/messages # Send message
DELETE /api/conversations/{id}   # End conversation
```

### Safety Endpoints
```
POST /api/safety/block           # Block user
DELETE /api/safety/block/{id}    # Unblock user  
GET  /api/safety/blocked         # Get blocked users
POST /api/safety/report          # Report user
GET  /api/safety/blocks          # Get safety restrictions
POST /api/safety/blocks          # Create safety restriction
PUT  /api/safety/blocks/{id}     # Update safety restriction
DELETE /api/safety/blocks/{id}   # Remove safety restriction
```

---

## 🔐 Authentication & Security

### JWT Token System
- **Access Token**: Short-lived (1 hour) for API authentication
- **Refresh Token**: Long-lived (7 days) for token renewal
- **Secret Key**: Configurable JWT signing secret
- **Header Format**: `Authorization: Bearer <token>`

### Password Security
- **Hashing**: BCrypt with salt rounds
- **Validation**: Minimum 8 characters, mixed case, numbers, symbols
- **Reset**: Secure token-based password reset via email
- **Change**: Requires current password verification

### Email Verification
- **Registration**: Email verification required for account activation
- **Tokens**: Secure verification tokens with expiration
- **Resend**: Users can request new verification emails

### CORS Configuration
- **Development**: Permissive CORS for localhost development
- **Production**: Restricted CORS for specific domains
- **Headers**: Standard CORS headers with authentication support

### Firebase Security
- **Service Account**: JSON key file for backend authentication
- **Rules**: Firestore security rules for data access control
- **Storage**: Secure file upload with authentication

---

## 🏗️ Build & Deployment

### Backend Build (Maven)

#### Dependencies
- **Spring Boot**: 3.5.5 (Web, Security, Validation, Actuator)
- **Google Cloud**: Spring Cloud GCP integration
- **Firebase**: Admin SDK for database and storage
- **JWT**: JSON Web Token implementation
- **Email**: SendGrid, AWS SES, SMTP support
- **Testing**: JUnit, Cucumber, Rest Assured

#### Build Commands
```bash
# Development build
mvn clean compile

# Run tests
mvn test

# Run with bld profile  
mvn spring-boot:run -Dspring-boot.run.profiles=bld

# Package application
mvn clean package

# Run packaged JAR
java -Dspring.profiles.active=prod -jar target/Connect-0.0.1-SNAPSHOT.jar
```

#### Maven Profiles
- **`bld`** - Build/Development profile with enhanced logging and test data
- **`test`** - Testing profile for automated tests
- **`prod`** - Production profile with optimized settings

#### Build Plugins
- **Maven Compiler**: Java 21 compilation with annotation processing
- **Spring Boot Maven**: Application packaging and running
- **Surefire**: Unit test execution
- **Failsafe**: Integration test execution

### Frontend Build (Flutter)

#### Dependencies
- **Core**: Flutter SDK, Provider state management
- **UI**: Swipe cards, cached network images
- **HTTP**: Dio client for API communication
- **Storage**: Secure storage, Hive caching
- **Media**: Image picker and upload
- **Development**: Lints, build runner, code generation

#### Build Commands
```bash
# Get dependencies
flutter pub get

# Run code generation
flutter pub run build_runner build

# Run development
flutter run

# Build for release
flutter build apk --release           # Android
flutter build ios --release           # iOS  
flutter build macos --release         # macOS
flutter build web --release           # Web
```

### Testing Strategy

#### Backend Testing
- **Unit Tests**: JUnit for service and component testing
- **Integration Tests**: Spring Boot Test for full application testing
- **BDD Tests**: Cucumber for behavior-driven testing
- **API Tests**: Rest Assured for HTTP endpoint testing

#### Frontend Testing
- **Widget Tests**: Flutter widget testing framework
- **Unit Tests**: Dart unit testing
- **Integration Tests**: Flutter integration testing
- **Mock Data**: Test fixtures and factories

#### API Testing
- **Bruno Collection**: Comprehensive API request collections
- **Test Data**: Realistic test scenarios for all endpoints
- **Authentication**: Automated token management
- **Environment**: Development and staging environment configs

### Deployment

#### Backend Deployment
- **JAR Packaging**: Executable JAR with embedded Tomcat
- **Docker**: Containerization support with startup script
- **Cloud Deploy**: Google Cloud Run, AWS ECS, etc.
- **Environment**: Profile-based configuration

#### Frontend Deployment
- **Mobile**: App Store and Google Play distribution
- **Web**: Static hosting (Firebase Hosting, AWS S3)
- **Desktop**: Platform-specific installers

---

## 🧪 Development Practices

### Code Organization
- **Package Structure**: Clear separation of concerns (controller/service/repository)
- **Naming Conventions**: Descriptive names with consistent patterns
- **Configuration**: External configuration with profiles
- **Documentation**: Inline documentation and API specs

### API Design
- **REST Principles**: RESTful endpoint design
- **HTTP Status Codes**: Proper status code usage
- **Error Handling**: Consistent error response format
- **Validation**: Request validation with meaningful error messages

### Database Design
- **Document Structure**: Optimized for NoSQL querying
- **Indexing**: Proper indexing for query performance
- **Data Integrity**: Validation and consistency checks
- **Relationships**: Denormalized structure for read optimization

### Security Practices
- **Authentication**: JWT-based stateless authentication
- **Authorization**: Role-based access control
- **Data Validation**: Input sanitization and validation
- **Secret Management**: Secure configuration management

---

## ⚙️ Configuration

### Backend Configuration

#### Application Properties (`application.properties`)
```properties
# Basic Configuration
spring.application.name=freedom
server.port=8080

# JWT Configuration  
jwt.secret=mySecretKeyForJWTTokenGenerationThatIsLongEnough123456789ABCDEF
app.jwt.access-token-expiration=3600000
app.jwt.refresh-token-expiration=604800000

# Cache Configuration
spring.cache.type=simple
spring.cache.cache-names=userProfiles

# Jackson JSON Configuration
spring.jackson.serialization.write-dates-as-timestamps=false
spring.jackson.deserialization.fail-on-unknown-properties=false
```

#### Build/Development Profile (`application-bld.yaml`)
```yaml
# Firebase Configuration
firebase:
  config:
    path: /path/to/firebase-service-account.json
  database:
    url: https://connect-ea4c2-default-rtdb.firebaseio.com
  storage:
    bucket: connect-ea4c2.firebasestorage.app
  project:
    id: connect-ea4c2

# Development Features
app:
  dev:
    expose-reset-tokens: true
```

### Firebase Setup
1. **Create Firebase Project** - Create project in Firebase Console
2. **Enable Services** - Enable Firestore, Storage, Authentication, Messaging
3. **Generate Service Account** - Download service account JSON key
4. **Configure Path** - Set `firebase.config.path` to key file location
5. **Security Rules** - Configure Firestore and Storage security rules

### Frontend Configuration
- **API Base URL** - Configure backend API endpoint
- **Environment** - Development/staging/production environments
- **Firebase** - Firebase project configuration for push notifications
- **Storage** - Secure storage for authentication tokens

---

## 📁 Complete File Structure Reference

### Backend Structure
```
ConnectBackend/
├── pom.xml                          # Maven configuration
├── mvnw, mvnw.cmd                   # Maven wrapper scripts
├── start_up.sh                      # Docker startup script
├── test_registration.sh             # Registration testing script
├── bruno-collection/                # API testing collection
│   ├── Auth/                        # Authentication tests
│   ├── Profile/                     # Profile management tests  
│   ├── Discovery/                   # Match discovery tests
│   ├── Matches/                     # Match management tests
│   ├── Conversations/               # Conversation tests
│   ├── Premium/                     # Subscription tests
│   ├── Safety/                      # Safety feature tests
│   └── Notifications/               # Notification tests
├── src/main/
│   ├── java/com/tpg/connect/
│   │   ├── ConnectApplication.java  # Main application class
│   │   ├── config/                  # Configuration classes
│   │   ├── controllers/             # REST controllers
│   │   ├── services/                # Business logic services
│   │   ├── repository/              # Data access layer
│   │   ├── model/                   # Data models and DTOs
│   │   ├── utilities/               # Utility classes
│   │   └── exceptions/              # Exception handling
│   └── resources/
│       ├── application.properties   # Main configuration
│       ├── application-bld.yaml     # Build/Development profile
│       └── firebase/                # Firebase configuration
└── src/test/
    ├── java/                        # Java test files
    └── resources/                   # Test resources
        └── features/                # Cucumber feature files
```

### Frontend Structure  
```
Connect_CWA/
├── pubspec.yaml                     # Flutter dependencies
├── lib/
│   ├── main.dart                    # Application entry point
│   ├── core/                        # Core utilities
│   │   ├── constants/               # App constants
│   │   ├── providers/               # State providers
│   │   └── utils/                   # Utility functions
│   ├── data/                        # Data layer
│   │   ├── models/                  # Data models
│   │   ├── repositories/            # Data repositories
│   │   └── requests/                # API requests
│   ├── business/                    # Business logic
│   │   ├── services/                # Business services
│   │   └── use_cases/               # Use case implementations
│   ├── presentation/                # UI layer
│   │   ├── pages/                   # Screen widgets
│   │   └── widgets/                 # Reusable widgets
│   └── service/                     # Service layer
│       ├── client/                  # HTTP client
│       └── controllers/             # Controllers
├── android/                         # Android-specific files
├── ios/                            # iOS-specific files  
├── macos/                          # macOS-specific files
├── web/                            # Web-specific files
└── test/                           # Test files
```

---

## 🚀 Getting Started

### Prerequisites
- **Java 21** - OpenJDK or Oracle JDK
- **Maven 3.9+** - Build tool
- **Flutter 3.9+** - Frontend framework
- **Firebase Project** - Database and storage
- **IDE** - IntelliJ IDEA, VS Code, or similar

### Backend Setup
1. Clone repository
2. Configure Firebase service account key
3. Update `application-bld.yaml` with Firebase settings
4. Run `mvn clean install`
5. Start with `mvn spring-boot:run`
6. Access Swagger UI at `http://localhost:8080/swagger-ui.html`

### Frontend Setup  
1. Navigate to Flutter project directory
2. Run `flutter pub get`
3. Run `flutter pub run build_runner build`
4. Configure API endpoint in client configuration
5. Run `flutter run` for development

### Testing
1. **Backend**: Run `mvn test` for all tests
2. **API**: Import Bruno collection for endpoint testing
3. **Frontend**: Run `flutter test` for widget tests
4. **Integration**: Use test scripts for full workflow testing

This documentation provides a complete overview of the Connect dating application architecture, components, and development practices. The application demonstrates modern software engineering practices with proper separation of concerns, comprehensive testing, and security-focused design.