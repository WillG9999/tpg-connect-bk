# Connect API - Bruno Collection

This Bruno collection contains comprehensive API requests for the Connect dating application backend.

## Setup

1. **Import Collection**: Import this folder into Bruno as a collection
2. **Environment**: Use the `development` environment which points to `http://localhost:8080`
3. **Authentication**: Most endpoints require authentication via JWT token

## Authentication Flow

1. **Register** a new user or **Login** with existing credentials
2. The login response will contain a JWT token
3. This token is automatically stored in the environment variable `authToken`
4. All authenticated requests use this token in the `Authorization: Bearer {token}` header

## Collection Structure

### Health
- **Health Check**: Basic application health status

### Auth
- **Register**: Create new user account
- **Login**: Authenticate and get JWT token  
- **Logout**: Invalidate current session
- **Refresh Token**: Extend session with new token
- **Forgot Password**: Initiate password reset
- **Reset Password**: Complete password reset with token
- **Change Password**: Change password for authenticated user
- **Verify Email**: Verify email address with token
- **Resend Verification**: Resend email verification
- **Delete Account**: Permanently delete user account

### Users
- **Get Current User**: Get authenticated user's profile
- **Get User by ID**: Get another user's profile
- **Update User**: Update user profile information
- **Search Users**: Search for users with filters
- **Like User**: Like another user
- **Dislike User**: Dislike/pass on another user
- **Block User**: Block another user
- **Report User**: Report inappropriate behavior

### Profile
- **Get Current Profile**: Get detailed profile information
- **Update Profile**: Update complete profile
- **Update Basic Info**: Update basic profile fields
- **Upload Photo**: Add photo to profile
- **Remove Photo**: Remove photo from profile
- **Update Photos**: Update all photos in order
- **Update Written Prompts**: Update text prompt responses
- **Update Poll Prompts**: Update poll prompt responses
- **Update Field Visibility**: Control profile field visibility
- **Update Preferences**: Update app preferences and settings

### Discovery
- **Get Potential Matches**: Get users to potentially match with
- **Like User**: Like user in discovery flow
- **Dislike User**: Pass on user in discovery flow
- **Get Discovery Settings**: Get current discovery preferences
- **Update Discovery Settings**: Update matching preferences

### Matches
- **Get Potential Matches**: Alternative discovery endpoint
- **Get Matches**: Get current matches
- **Create Match Action**: Create like/pass action
- **Get Daily Batch Status**: Check daily match batch status
- **Get Daily Matches**: Get matches for specific date
- **Send Message**: Send message in conversation
- **Get Messages**: Get conversation messages
- **Unmatch**: End a match/conversation

### Conversations
- **Get Conversations**: Get all conversations
- **Unmatch Conversation**: End a conversation

### Premium
- **Get Subscription Plans**: Get available premium plans
- **Get Current Subscription**: Get user's current subscription
- **Create Subscription**: Purchase premium subscription
- **Cancel Subscription**: Cancel premium subscription
- **Get Subscription History**: Get subscription history
- **Get Premium Status**: Get premium status and features
- **Restore Subscription**: Restore subscription from receipt
- **Payment Webhook**: Handle payment processor webhooks

### Safety
- **Block User**: Block user for safety
- **Unblock User**: Unblock previously blocked user
- **Get Blocked Users**: Get list of blocked users
- **Report User**: Report user for inappropriate behavior
- **Get Safety Blocks**: Get safety restrictions on account
- **Create Safety Block**: Create safety restriction (admin)
- **Update Safety Block**: Update safety restriction (admin)
- **Delete Safety Block**: Remove safety restriction (admin)

### Notifications
- **Get User Notifications**: Get user's notifications with pagination
- **Get Unread Notifications**: Get only unread notifications
- **Get Unread Count**: Get count of unread notifications
- **Create Notification**: Create new notification (admin)
- **Mark Notification as Read**: Mark single notification as read
- **Mark All as Read**: Mark all notifications as read
- **Delete Notification**: Delete single notification
- **Delete All Notifications**: Delete all notifications
- **Send Test Notification**: Send test notification

## Test Data

All requests include realistic test data:
- **User profiles** with names, bios, interests, and locations
- **Authentication credentials** for testing login/registration
- **Match actions** with realistic user interactions
- **Messages** with sample conversation content
- **Subscription data** with realistic payment information
- **Safety reports** with appropriate categories and descriptions

## Usage Tips

1. **Start with Authentication**: Run Register or Login first to get a token
2. **Check Token Storage**: Verify the `authToken` environment variable is set
3. **Sequential Testing**: Some endpoints depend on data from previous requests
4. **Error Handling**: Check response status codes and error messages
5. **Data Relationships**: User IDs, match IDs, and other references are consistent across requests

## Environment Variables

- `baseUrl`: API base URL (default: http://localhost:8080)
- `authToken`: JWT authentication token (auto-populated from login)

## Notes

- Replace example user IDs with actual IDs from your test data
- Some endpoints (like admin functions) may require special permissions
- Webhook endpoints are typically called by external services, not client apps
- Test notification endpoints help verify the notification system is working

Happy testing! 🚀