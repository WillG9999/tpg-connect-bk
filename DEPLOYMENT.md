# Connect Backend - Production Deployment Guide

## 🚀 Overview

This guide covers deploying the Connect dating app backend using Google Jib for containerization and Google Cloud Platform for production hosting.

## 📋 Prerequisites

### Development Environment
- **Java 21** (OpenJDK or Oracle JDK)
- **Maven 3.9+**
- **Docker** (for local testing)
- **Google Cloud SDK** (`gcloud` CLI)

### Google Cloud Platform Setup
- **GCP Project** with billing enabled
- **Service Account** with appropriate permissions
- **Container Registry** or **Artifact Registry** enabled
- **Secret Manager** enabled
- **Cloud Run** or **GKE** for hosting

## 🔧 Build & Container Configuration

### Jib Plugin Configuration
The project uses Google Jib for efficient container builds without requiring Docker:

```xml
<plugin>
    <groupId>com.google.cloud.tools</groupId>
    <artifactId>jib-maven-plugin</artifactId>
    <version>3.4.0</version>
</plugin>
```

### Key Features
- **Distroless base image** for security and minimal size
- **Production-optimized JVM settings** with G1GC
- **Security-first approach** with non-root user
- **Optimized layering** for faster builds and deployments

## 🔐 Secret Management

### Google Secret Manager Setup
```bash
# Create secrets in Google Secret Manager
gcloud secrets create jwt-secret --data-file=- <<< "your-jwt-secret"
gcloud secrets create firebase-project-id --data-file=- <<< "connect-ea4c2"
gcloud secrets create firebase-config --data-file=firebase-service-account.json
gcloud secrets create email-password --data-file=- <<< "your-email-password"

# Grant access to service account
gcloud secrets add-iam-policy-binding jwt-secret \
    --member="serviceAccount:connect-backend@connect-ea4c2.iam.gserviceaccount.com" \
    --role="roles/secretmanager.secretAccessor"
```

### Environment Variables
The application uses environment variables for all configuration:
- `JWT_SECRET` - JWT signing secret
- `FIREBASE_CONFIG_PATH` - Path to Firebase service account
- `EMAIL_PASSWORD` - Email service password
- `GCP_PROJECT_ID` - Google Cloud project ID

## 🏗️ Build Commands

### Local Development Build
```bash
# Build container locally
mvn clean compile jib:buildTar

# Load and run locally
docker load < target/jib-image.tar
docker run -p 8080:8080 --env-file .env connect-backend
```

### Production Build
```bash
# Authenticate with GCP
gcloud auth configure-docker gcr.io

# Build and push to Container Registry
export GCP_PROJECT_ID=connect-ea4c2
mvn clean compile jib:build
```

## ☁️ Cloud Run Deployment

### Deploy to Cloud Run
```bash
# Deploy production service
gcloud run deploy connect-backend \
  --image gcr.io/connect-ea4c2/connect-backend:latest \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated \
  --memory 4Gi \
  --cpu 4 \
  --max-instances 100 \
  --set-env-vars="SPRING_PROFILES_ACTIVE=prod" \
  --set-secrets="JWT_SECRET=jwt-secret:latest,FIREBASE_CONFIG_PATH=firebase-config:latest"
```

### Cloud Run Configuration
- **Memory**: 4GB for production workloads
- **CPU**: 4 vCPUs for optimal performance
- **Scaling**: Up to 100 instances for high availability
- **Health Checks**: Automatic with `/api/health` endpoints

## 🎯 Kubernetes Deployment

### Prerequisites
```bash
# Create namespace and service account
kubectl apply -f k8s/namespace.yaml

# Create secrets (update with real values first)
kubectl apply -f k8s/secrets.yaml

# Deploy application
kubectl apply -f k8s/deployment.yaml

# Set up autoscaling
kubectl apply -f k8s/hpa.yaml
```

### Kubernetes Features
- **High Availability**: 3 replica minimum with rolling updates
- **Auto-scaling**: CPU and memory-based HPA
- **Security**: Non-root containers, read-only filesystem
- **Health Checks**: Liveness, readiness, and startup probes
- **Resource Management**: Requests and limits configured

## 🔍 Health Check Endpoints

### Available Endpoints
- `GET /api/health` - Basic health check for load balancers
- `GET /api/health/live` - Kubernetes liveness probe
- `GET /api/health/ready` - Kubernetes readiness probe with dependency checks
- `GET /api/health/info` - Detailed system information

### Monitoring Integration
```bash
# Check service health
curl https://your-service-url/api/health/ready

# Get detailed system info
curl https://your-service-url/api/health/info
```

## 🚀 CI/CD Pipeline

### GitHub Actions Workflow
The included `.github/workflows/ci-cd.yaml` provides:
- **Automated Testing**: Unit and integration tests
- **Security Scanning**: OWASP dependency check
- **Container Building**: Jib-based builds
- **Multi-Environment Deployment**: Staging and production
- **Health Verification**: Post-deployment checks

### Required Secrets
Configure these secrets in GitHub repository settings:
- `GCP_SA_KEY` - Service account JSON for GCP authentication

## 📊 Production Monitoring

### Structured Logging
Production configuration includes JSON structured logging:
```json
{
  "timestamp": "2024-01-15 10:30:45.123",
  "level": "INFO",
  "logger": "com.tpg.connect.services.AuthService",
  "message": "User authentication successful",
  "mdc": {"userId": "123456789012"}
}
```

### Key Metrics to Monitor
- **Response Times**: API endpoint performance
- **Error Rates**: 4xx/5xx HTTP responses
- **Resource Usage**: CPU, memory, and disk utilization
- **Database Health**: Firebase connection status
- **Email Service**: Email delivery success rates

## 🔒 Security Best Practices

### Container Security
- **Distroless base image** reduces attack surface
- **Non-root user** (UID 65532) for runtime
- **Read-only root filesystem** prevents tampering
- **No shell or package managers** in production image

### Application Security
- **JWT tokens** with configurable expiration
- **Environment-based secrets** (no hardcoded values)
- **CORS configuration** for frontend integration
- **Input validation** on all API endpoints

## 🚨 Troubleshooting

### Common Issues

#### Container Won't Start
```bash
# Check logs
docker logs <container-id>
gcloud run logs read --service=connect-backend

# Verify environment variables
kubectl get pods -n connect
kubectl describe pod <pod-name> -n connect
```

#### Health Checks Failing
```bash
# Test health endpoints locally
curl http://localhost:8080/api/health/ready

# Check dependency status
curl http://localhost:8080/api/health/info
```

#### Firebase Connection Issues
```bash
# Verify service account permissions
gcloud projects get-iam-policy connect-ea4c2

# Test Firebase connectivity
gcloud firestore databases list --project=connect-ea4c2
```

## 📝 Development vs Production

### Development (.env file)
```bash
# Start with development profile
./start_up.sh dev
```

### Production (Container)
```bash
# Environment variables from Secret Manager
# SPRING_PROFILES_ACTIVE=prod
# All secrets injected at runtime
```

## 🎯 Next Steps

1. **Set up monitoring** with Google Cloud Monitoring
2. **Configure alerting** for critical metrics
3. **Implement backup strategy** for Firebase data
4. **Set up CDN** for static assets
5. **Configure custom domain** with SSL certificates

## 📞 Support

For deployment issues:
1. Check the health endpoints for service status
2. Review application logs for error details
3. Verify all environment variables are set correctly
4. Ensure GCP service account has required permissions

This deployment guide provides a production-ready setup for the Connect dating app backend with enterprise-grade security, monitoring, and scalability features.