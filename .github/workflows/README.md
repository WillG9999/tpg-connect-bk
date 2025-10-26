# GitHub Actions CI/CD Pipeline

This directory contains the GitHub Actions workflows for the Connect Dating App backend, implementing a comprehensive CI/CD pipeline aligned with the bld/int/pre/prod environment strategy.

## 🏗️ Workflow Overview

### Environment Strategy
- **bld (local)** - Local development with .env files
- **int** - Integration testing environment
- **pre** - Pre-production environment  
- **prod** - Production environment

### Deployment Flow
```
1. Developer creates release tag (v1.0.0)
   ↓
2. Auto-deploy to Integration (int)
   ↓
3. Manual testing in Integration
   ↓
4. Manual promotion to Pre-Production (pre)
   ↓
5. Manual testing in Pre-Production
   ↓
6. Manual promotion to Production (prod)
```

## 📋 Workflows

### 1. `ci.yaml` - Continuous Integration
**Triggers**: Push to main/develop/feature/bugfix branches, Pull Requests
**Purpose**: Runs tests, security scans, and builds container images

**Jobs**:
- **test** - Unit and integration tests
- **security** - OWASP dependency scanning
- **build** - Container image build and push (main branch only)
- **notify** - Build status notifications

### 2. `deploy-int.yaml` - Integration Deployment
**Triggers**: Release tags (v*)
**Purpose**: Automatically deploys release versions to Integration environment

**Features**:
- Automatic deployment on version tags
- Health checks and smoke tests
- Deployment verification
- Ready for promotion to pre-production

### 3. `deploy-pre.yaml` - Pre-Production Promotion
**Triggers**: Manual workflow dispatch
**Purpose**: Promotes tested versions from Integration to Pre-Production

**Required Inputs**:
- **version** - Version to promote (e.g., v1.0.0)
- **force_deploy** - Force deployment even if health checks fail
- **run_full_tests** - Run comprehensive test suite before deployment

**Features**:
- Version validation from Integration
- Optional comprehensive testing
- Health verification
- Rollback on failure

### 4. `deploy-prod.yaml` - Production Promotion
**Triggers**: Manual workflow dispatch  
**Purpose**: Promotes validated versions from Pre-Production to Production

**Required Inputs**:
- **version** - Version to promote (e.g., v1.0.0)
- **maintenance_mode** - Enable maintenance mode during deployment
- **skip_health_checks** - Skip health checks (emergency only)
- **approval_required** - Require manual approval before deployment

**Features**:
- Manual approval process
- Pre-production verification
- Maintenance mode support
- Comprehensive health checks
- Emergency rollback capability

## 🚀 Usage Instructions

## 🤖 Automated Release Process

### Commit Message Triggered Releases (Recommended)

**Start your commit message with `release:` to trigger automatic releases!**

1. **Create a release with your commit**:
   ```bash
   git commit -m "release: add user authentication feature"
   git push origin main
   ```

2. **Different release types**:
   ```bash
   # Patch release (v1.0.0 → v1.0.1) - default
   git commit -m "release: fix login issue"
   
   # Minor release (v1.0.0 → v1.1.0) - new features
   git commit -m "release: minor add messaging system"
   
   # Major release (v1.0.0 → v2.0.0) - breaking changes  
   git commit -m "release: major new API structure"
   ```

3. **Automatic process when message starts with `release:`**:
   - ✅ Auto-increments version based on type specified
   - ✅ Creates release tag and GitHub release
   - ✅ Automatically deploys to Integration environment

4. **No release trigger = no release**:
   - Regular commits that don't start with `release:` won't create releases
   - Perfect for work-in-progress commits

### Manual Release (Alternative)

If you need a specific version bump type:

1. **Go to GitHub Actions** → **Auto Release & Deploy**
2. **Click "Run workflow"**
3. **Choose bump type**:
   - `patch` - Bug fixes (1.0.0 → 1.0.1)
   - `minor` - New features (1.0.0 → 1.1.0)  
   - `major` - Breaking changes (1.0.0 → 2.0.0)
4. **Click "Run workflow"**

### Promoting to Pre-Production

1. **Go to GitHub Actions** → **Promote to Pre-Production**
2. **Click "Run workflow"**
3. **Fill in parameters**:
   - Version: `v1.0.0` (the version you want to promote)
   - Force Deploy: Leave unchecked (unless emergency)
   - Run Full Tests: Checked (recommended)
4. **Click "Run workflow"**
5. **Monitor the deployment progress**

### Promoting to Production

1. **Go to GitHub Actions** → **Promote to Production**
2. **Click "Run workflow"**
3. **Fill in parameters**:
   - Version: `v1.0.0` (must be tested in pre-production)
   - Maintenance Mode: Checked (recommended)
   - Skip Health Checks: Leave unchecked (unless emergency)
   - Approval Required: Checked (recommended)
4. **Click "Run workflow"**
5. **Approve the deployment** when prompted
6. **Monitor production deployment**

## 🔧 Configuration Requirements

### GitHub Secrets
The following secrets must be configured in your GitHub repository:

- `GCP_SA_KEY` - Google Cloud Service Account JSON key with the following permissions:
  - Container Registry access
  - Google Kubernetes Engine access
  - Secret Manager access

### Google Cloud Setup

1. **Service Account Permissions**:
   ```bash
   # Required IAM roles
   roles/container.developer
   roles/container.clusterViewer
   roles/secretmanager.secretAccessor
   roles/kubernetes.engine.developer
   ```

2. **Kubernetes Namespaces**:
   ```bash
   # Create namespaces if they don't exist
   kubectl create namespace connect-int
   kubectl create namespace connect-pre
   kubectl create namespace connect
   ```

3. **Static IP Addresses** (if using Ingress):
   ```bash
   # Reserve static IPs for each environment
   gcloud compute addresses create connect-backend-int-ip --global
   gcloud compute addresses create connect-backend-pre-ip --global
   gcloud compute addresses create connect-backend-ip --global
   ```

### Environment URLs
Configure the following DNS entries to point to your load balancers:
- Integration: `int-api.connect-app.com`
- Pre-Production: `pre-api.connect-app.com`  
- Production: `api.connect-app.com`

## 🛡️ Security Features

### Image Security
- Container images are scanned for vulnerabilities
- OWASP dependency checking
- Security-hardened base images
- Non-root container execution

### Deployment Security
- Workload Identity for secure GCP access
- Secret Manager integration for sensitive data
- Role-based access control (RBAC)
- Network policies and security contexts

### Access Control
- Manual approval required for production deployments
- Environment-specific service accounts
- Principle of least privilege

## 📊 Monitoring & Health Checks

### Health Endpoints
All deployments verify the following endpoints:
- `/api/health` - Basic application health
- `/api/health/ready` - Readiness probe
- `/api/health/live` - Liveness probe

### Deployment Verification
- Progressive health checks with retries
- Smoke test execution
- Rollback on failure
- Comprehensive logging

## 🚨 Troubleshooting

### Common Issues

1. **Health Check Failures**:
   - Check application logs: `kubectl logs -n <namespace> deployment/<deployment-name>`
   - Verify Secret Manager access
   - Check database connectivity

2. **Image Pull Errors**:
   - Verify GCR permissions
   - Check service account configuration
   - Confirm image exists in registry

3. **Deployment Stuck**:
   - Check resource quotas
   - Verify node capacity
   - Review pod events: `kubectl describe pod -n <namespace>`

### Emergency Procedures

1. **Rollback Production**:
   ```bash
   kubectl rollout undo deployment/connect-backend -n connect
   ```

2. **Force Deployment** (use with caution):
   - Set `force_deploy: true` in workflow parameters
   - Monitor closely and be ready to rollback

3. **Emergency Access**:
   ```bash
   # Get shell access to running pod
   kubectl exec -it -n connect deployment/connect-backend -- /bin/sh
   ```

## 📞 Support

For issues with the CI/CD pipeline:
1. Check the GitHub Actions workflow logs
2. Review the deployment status in Kubernetes
3. Verify all prerequisites are met
4. Contact the DevOps team for assistance

## 🔄 Maintenance

### Regular Tasks
- Review and update dependency versions
- Rotate service account keys
- Update base container images
- Review and test rollback procedures

### Security Updates
- Monitor security scan results
- Apply critical patches promptly
- Update GitHub Actions versions
- Review access permissions regularly