# Firebase Configuration Setup

This directory contains Firebase service account credentials for the Connect application.

## Development Setup

### 1. Create Firebase Project
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project named `connect-dev`
3. Enable the following services:
   - Authentication
   - Firestore Database
   - Cloud Storage
   - Cloud Functions (optional)

### 2. Generate Service Account Key
1. In Firebase Console, go to Project Settings > Service Accounts
2. Click "Generate new private key"
3. Save the downloaded JSON file as `connect-dev-firebase-adminsdk.json`
4. Place it in this directory (`src/main/resources/firebase/`)

### 3. Configure Firebase Services

#### Firestore Database
1. Go to Firestore Database in Firebase Console
2. Create database in test mode (for development)
3. Create the following collections:
   - `users` - for user authentication data
   - `user_profiles` - for complete user profiles
   - `matches` - for user matches
   - `conversations` - for messaging
   - `notifications` - for push notifications

#### Cloud Storage
1. Go to Storage in Firebase Console
2. Set up with default security rules (for development)
3. Create the following folders:
   - `profile_photos/`
   - `uploads/`
   - `temp/`

#### Authentication
1. Go to Authentication in Firebase Console
2. Enable Email/Password authentication
3. Add authorized domains if needed for development

### 4. Environment Variables (Optional)
For production or additional security, you can use environment variables:

```bash
export FIREBASE_PROJECT_ID=connect-dev
export FIREBASE_STORAGE_BUCKET=connect-dev.appspot.com
export FIREBASE_DATABASE_URL=https://connect-dev-default-rtdb.firebaseio.com
```

### 5. Security Notes
- **NEVER** commit the service account JSON file to version control
- Add `*.json` to your `.gitignore` if not already present
- Use different projects for development, staging, and production
- Regularly rotate service account keys for production

### 6. Testing Connection
Once configured, you can test the Firebase connection by running:
```bash
mvn spring-boot:run -Dspring.profiles.active=bld
```

Check the logs for successful Firebase initialization.

## File Structure
```
firebase/
├── README.md (this file)
├── connect-dev-firebase-adminsdk.json (create this - do not commit!)
├── connect-staging-firebase-adminsdk.json (optional)
└── connect-prod-firebase-adminsdk.json (optional)
```

## Troubleshooting

### Common Issues:
1. **"Service account key not found"**: Ensure the JSON file path matches the configuration
2. **"Insufficient permissions"**: Check that the service account has the required roles
3. **"Project not found"**: Verify the project ID in the configuration matches Firebase

### Required IAM Roles:
- Firebase Admin SDK Admin Service Agent
- Cloud Datastore User
- Storage Admin (for file uploads)
- Firebase Authentication Admin (for user management)