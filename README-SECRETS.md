why c# Connect App - Secrets Management Guide

This guide explains how to manage secrets and sensitive configuration in the Connect dating application.

## 🔧 Environment Variables Setup

### 1. Local Development

Copy the template and create your local environment file:
```bash
cp .env.template .env
```

Edit `.env` with your actual values:
```bash
# Required secrets
JWT_SECRET=your-super-secure-jwt-secret-key-minimum-512-bits-long
EMAIL_PASSWORD=your-gmail-app-password
FIREBASE_CONFIG_PATH=/path/to/your/firebase-service-account.json

# Firebase configuration
FIREBASE_PROJECT_ID=your-firebase-project-id
FIREBASE_DATABASE_URL=https://your-project.firebaseio.com
FIREBASE_STORAGE_BUCKET=your-project.firebasestorage.app
```

### 2. Production with Environment Variables

Set environment variables in your deployment environment:
```bash
export JWT_SECRET="your-production-jwt-secret"
export EMAIL_PASSWORD="your-production-email-password"
export FIREBASE_PROJECT_ID="your-prod-firebase-project"
# etc...
```

## 🔐 Google Secret Manager Setup

### 1. Enable Secret Manager

Add this dependency to enable Secret Manager (already added):
```xml
<dependency>
    <groupId>com.google.cloud</groupId>
    <artifactId>spring-cloud-gcp-starter-secretmanager</artifactId>
</dependency>
```

### 2. Create Secrets in Google Cloud

```bash
# Set your project
export PROJECT_ID=your-gcp-project-id

# Create secrets
gcloud secrets create connect-app-jwt-secret --data-file=- <<EOF
your-super-secure-jwt-secret-key-minimum-512-bits-long
EOF

gcloud secrets create connect-app-email-password --data-file=- <<EOF
your-gmail-app-password
EOF

gcloud secrets create connect-app-firebase-service-account --data-file=path/to/firebase-service-account.json
```

### 3. Grant Secret Manager Access

```bash
# Get your service account email
export SERVICE_ACCOUNT="your-service-account@${PROJECT_ID}.iam.gserviceaccount.com"

# Grant Secret Manager access
gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member="serviceAccount:${SERVICE_ACCOUNT}" \
    --role="roles/secretmanager.secretAccessor"
```

### 4. Configure Application for Secret Manager

Set environment variables:
```bash
export GOOGLE_CLOUD_PROJECT=your-gcp-project-id
export GOOGLE_APPLICATION_CREDENTIALS=/path/to/service-account.json
```

Run with Secret Manager profile:
```bash
java -Dspring.profiles.active=prod,secretmanager -jar Connect-0.0.1-SNAPSHOT.jar
```

## 🚀 Deployment Options

### Option 1: Environment Variables Only
```bash
# Run with environment variables
export JWT_SECRET="..."
export EMAIL_PASSWORD="..."
java -Dspring.profiles.active=prod -jar app.jar
```

### Option 2: Google Secret Manager
```bash
# Run with Secret Manager
export GOOGLE_CLOUD_PROJECT="your-project"
export GOOGLE_APPLICATION_CREDENTIALS="/path/to/service-account.json"
java -Dspring.profiles.active=prod,secretmanager -jar app.jar
```

### Option 3: Hybrid (recommended for production)
```bash
# Use Secret Manager for sensitive secrets, env vars for configuration
export GOOGLE_CLOUD_PROJECT="your-project"
export FIREBASE_PROJECT_ID="your-firebase-project"
export FRONTEND_BASE_URL="https://your-app.com"
java -Dspring.profiles.active=prod,secretmanager -jar app.jar
```

## 🔒 Security Best Practices

### 1. Secret Rotation
- Rotate JWT secrets regularly
- Use different secrets for different environments
- Monitor secret access logs

### 2. Access Control
- Use principle of least privilege
- Grant Secret Manager access only to necessary service accounts
- Use different secrets for dev/staging/prod

### 3. Audit and Monitoring
```bash
# View secret access logs
gcloud logging read "resource.type=gce_instance AND protoPayload.serviceName=secretmanager.googleapis.com"
```

## 🛠️ Development Workflow

### 1. Local Development
```bash
# Use .env file for local development
cp .env.template .env
# Edit .env with your values
mvn spring-boot:run -Dspring.profiles.active=bld
```

### 2. Integration Testing
```bash
# Use environment variables for CI/CD
export JWT_SECRET="test-secret"
mvn test -Dspring.profiles.active=test
```

### 3. Production Deployment
```bash
# Use Secret Manager for production
java -Dspring.profiles.active=prod,secretmanager -jar app.jar
```

## 📋 Migration Checklist

- [x] Created .env.template
- [x] Updated all config files to use environment variables
- [x] Added Google Secret Manager integration
- [x] Created SecretManagerConfig class
- [x] Added application-secretmanager.yaml profile
- [ ] Test with local .env file
- [ ] Create secrets in Google Cloud Secret Manager
- [ ] Test with Secret Manager profile
- [ ] Update deployment scripts
- [ ] Document team workflow

## 🚨 Emergency Procedures

### Secret Compromise
1. **Immediate**: Disable the compromised secret in Secret Manager
2. **Generate**: Create new secret with different value
3. **Update**: Update Secret Manager with new value
4. **Restart**: Restart all application instances
5. **Audit**: Review access logs for unauthorized access

### Access Issues
1. **Check**: Service account permissions
2. **Verify**: GOOGLE_APPLICATION_CREDENTIALS path
3. **Test**: `gcloud auth application-default print-access-token`
4. **Fallback**: Use environment variables temporarily

## 📚 References

- [Google Secret Manager Documentation](https://cloud.google.com/secret-manager/docs)
- [Spring Cloud GCP Secret Manager](https://spring.io/projects/spring-cloud-gcp)
- [Security Best Practices](https://cloud.google.com/security/best-practices)

## 🔄 Automated Deployment

The Connect application uses automated CI/CD pipelines for deployment across multiple environments.