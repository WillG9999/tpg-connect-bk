# Connect Backend Helm Chart

This Helm chart deploys the Connect Dating App backend to Kubernetes with support for multiple environments (int/pre/prod).

## 🏗️ Architecture

### Multi-Environment Strategy
- **Integration (int)**: Testing environment with minimal resources
- **Pre-Production (pre)**: Production-like environment for final testing  
- **Production (prod)**: Full production deployment with high availability

### Separate Infrastructure Per Environment
- **GCP Projects**: `connect-ea4c2-int`, `connect-ea4c2-pre`, `connect-ea4c2` (prod)
- **Firebase Projects**: Separate Firebase instances per environment
- **Service Accounts**: Environment-specific service accounts with minimal permissions
- **Secrets**: Isolated Secret Manager instances per project

## 📋 Prerequisites

### 1. GCP Infrastructure Setup
Run the infrastructure setup script to create all necessary GCP resources:

```bash
./scripts/setup-gcp-infrastructure.sh
```

This script will:
- Create GCP projects for each environment
- Set up service accounts with proper IAM roles
- Enable required APIs
- Create static IP addresses
- Set up Secret Manager with environment-specific secrets
- Configure Workload Identity

### 2. Firebase Setup
After running the GCP script, manually set up Firebase for each project:

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Add Firebase to each GCP project
3. Enable Firestore Database
4. Enable Firebase Storage
5. Create web app configurations

### 3. DNS Configuration
Point your domains to the static IP addresses:

```
int-api.connect-app.com  → Integration static IP
pre-api.connect-app.com  → Pre-production static IP
api.connect-app.com      → Production static IP
```

### 4. SSL Certificates
Create managed SSL certificates in GCP:

```bash
# Integration
gcloud compute ssl-certificates create connect-backend-int-ssl \
    --domains=int-api.connect-app.com \
    --global \
    --project=connect-ea4c2-int

# Pre-production  
gcloud compute ssl-certificates create connect-backend-pre-ssl \
    --domains=pre-api.connect-app.com \
    --global \
    --project=connect-ea4c2-pre

# Production
gcloud compute ssl-certificates create connect-backend-ssl \
    --domains=api.connect-app.com \
    --global \
    --project=connect-ea4c2
```

## 🚀 Deployment

### Using Helm Directly

#### Integration Environment
```bash
# Deploy to integration
helm upgrade --install connect-int ./helm \
  --namespace connect-int \
  --values helm/values-int.yaml \
  --set image.tag="v1.0.0" \
  --create-namespace
```

#### Pre-Production Environment
```bash
# Deploy to pre-production
helm upgrade --install connect-pre ./helm \
  --namespace connect-pre \
  --values helm/values-pre.yaml \
  --set image.tag="v1.0.0" \
  --create-namespace
```

#### Production Environment
```bash
# Deploy to production
helm upgrade --install connect ./helm \
  --namespace connect \
  --values helm/values-prod.yaml \
  --set image.tag="v1.0.0" \
  --create-namespace
```

### Using GitHub Actions (Recommended)

The CI/CD pipeline automatically handles deployments:

1. **Create a release**: Commit with `release:` prefix
2. **Auto-deploy to Integration**: Happens automatically on release tag
3. **Promote to Pre-Production**: Manual GitHub Actions workflow
4. **Promote to Production**: Manual GitHub Actions workflow with approval

## ⚙️ Configuration

### Environment-Specific Values

#### Integration (`values-int.yaml`)
- 1 replica
- 512Mi memory, 250m CPU
- Debug logging enabled
- Reduced timeouts for testing
- Test features enabled

#### Pre-Production (`values-pre.yaml`)
- 2 replicas  
- 768Mi memory, 375m CPU
- Production-like settings
- Full feature set enabled
- Autoscaling enabled

#### Production (`values-prod.yaml`)
- 3+ replicas
- 1Gi memory, 500m CPU
- Optimized for performance
- High availability settings
- Full autoscaling and monitoring

### Key Configuration Options

```yaml
# Resource allocation
resources:
  requests:
    memory: "1Gi"
    cpu: "500m"
  limits:
    memory: "2Gi" 
    cpu: "1000m"

# Autoscaling
autoscaling:
  enabled: true
  minReplicas: 3
  maxReplicas: 10
  targetCPUUtilizationPercentage: 70

# Feature flags
features:
  emailVerification: true
  premiumFeatures: true
  # ... more features
```

## 🔐 Security

### Service Account Configuration
Each environment uses a dedicated service account:
- `connect-backend-int@connect-ea4c2-int.iam.gserviceaccount.com`
- `connect-backend-pre@connect-ea4c2-pre.iam.gserviceaccount.com`
- `connect-backend@connect-ea4c2.iam.gserviceaccount.com`

### Workload Identity
Kubernetes service accounts are bound to GCP service accounts using Workload Identity for secure, keyless authentication.

### Security Context
- Runs as non-root user (65532)
- Read-only root filesystem
- Drops all capabilities
- No privilege escalation

### Secret Management
All sensitive configuration is stored in Google Secret Manager:
- JWT secrets
- Firebase configuration
- Database credentials
- Email service credentials

## 📊 Monitoring & Health Checks

### Health Endpoints
- **Liveness**: `/api/health/live`
- **Readiness**: `/api/health/ready`  
- **Startup**: `/api/health`

### Monitoring Integration
- Prometheus metrics collection
- Structured JSON logging
- OpenTelemetry tracing support
- GCP monitoring integration

## 🛠️ Troubleshooting

### Common Issues

#### 1. ImagePullBackOff
```bash
# Check if image exists
gcloud container images list --repository=gcr.io/connect-ea4c2

# Verify service account permissions
kubectl describe pod -n connect-int
```

#### 2. Secret Access Errors
```bash
# Check Secret Manager access
gcloud secrets versions access latest --secret="jwt-secret" --project=connect-ea4c2-int

# Verify Workload Identity binding
gcloud iam service-accounts get-iam-policy connect-backend-int@connect-ea4c2-int.iam.gserviceaccount.com
```

#### 3. Health Check Failures
```bash
# Check pod logs
kubectl logs -n connect-int deployment/connect-backend-int

# Test health endpoints
kubectl port-forward -n connect-int svc/connect-backend-service-int 8080:80
curl http://localhost:8080/api/health
```

### Useful Commands

```bash
# View Helm releases
helm list -A

# Get release values
helm get values connect-int -n connect-int

# Rollback to previous version
helm rollback connect-int 1 -n connect-int

# View pod status
kubectl get pods -n connect-int

# Check ingress status
kubectl get ingress -n connect-int

# View logs
kubectl logs -f deployment/connect-backend-int -n connect-int
```

## 🔄 Maintenance

### Updating the Chart
```bash
# Update dependencies (if any)
helm dependency update

# Validate templates
helm template connect-int ./helm --values helm/values-int.yaml

# Dry run deployment
helm upgrade --install connect-int ./helm \
  --namespace connect-int \
  --values helm/values-int.yaml \
  --dry-run
```

### Backup and Recovery
```bash
# Backup Helm values
helm get values connect -n connect > backup-values.yaml

# Backup Kubernetes resources
kubectl get all -n connect -o yaml > backup-resources.yaml

# Restore from backup
helm upgrade connect ./helm -n connect -f backup-values.yaml
```

## 📞 Support

### Environment URLs
- **Integration**: https://int-api.connect-app.com
- **Pre-Production**: https://pre-api.connect-app.com
- **Production**: https://api.connect-app.com

### Health Check URLs
- **Integration**: https://int-api.connect-app.com/api/health
- **Pre-Production**: https://pre-api.connect-app.com/api/health
- **Production**: https://api.connect-app.com/api/health

For deployment issues, check:
1. GitHub Actions workflow logs
2. Kubernetes pod logs
3. GCP Cloud Logging
4. Helm release status