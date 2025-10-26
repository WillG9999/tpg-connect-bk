#!/bin/bash

# GCP Infrastructure Setup Script for Connect Dating App
# This script sets up all necessary GCP resources for multi-environment deployment

set -euo pipefail

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_step() {
    echo -e "${BLUE}[STEP]${NC} $1"
}

# Configuration
PROD_PROJECT_ID="connect-ea4c2"
INT_PROJECT_ID="connect-ea4c2-int"
PRE_PROJECT_ID="connect-ea4c2-pre"
REGION="us-central1"
ZONE="us-central1-a"
CLUSTER_NAME="connect-cluster"

# Check if gcloud is installed and authenticated
check_prerequisites() {
    print_step "Checking prerequisites..."
    
    if ! command -v gcloud &> /dev/null; then
        print_error "gcloud CLI is not installed. Please install it first."
        exit 1
    fi
    
    if ! gcloud auth list --filter=status:ACTIVE --format="value(account)" | grep -q .; then
        print_error "No active gcloud authentication found. Please run 'gcloud auth login'"
        exit 1
    fi
    
    print_status "Prerequisites check passed"
}

# Create GCP projects
create_projects() {
    print_step "Creating GCP projects..."
    
    # Production project should already exist
    if gcloud projects describe $PROD_PROJECT_ID &>/dev/null; then
        print_status "Production project $PROD_PROJECT_ID already exists"
    else
        print_warning "Production project $PROD_PROJECT_ID not found. Please create it manually."
    fi
    
    # Create integration project
    if gcloud projects describe $INT_PROJECT_ID &>/dev/null; then
        print_status "Integration project $INT_PROJECT_ID already exists"
    else
        print_status "Creating integration project $INT_PROJECT_ID..."
        gcloud projects create $INT_PROJECT_ID --name="Connect Integration"
    fi
    
    # Create pre-production project
    if gcloud projects describe $PRE_PROJECT_ID &>/dev/null; then
        print_status "Pre-production project $PRE_PROJECT_ID already exists"
    else
        print_status "Creating pre-production project $PRE_PROJECT_ID..."
        gcloud projects create $PRE_PROJECT_ID --name="Connect Pre-Production"
    fi
}

# Enable required APIs for each project
enable_apis() {
    local project_id=$1
    print_step "Enabling APIs for project $project_id..."
    
    gcloud config set project $project_id
    
    local apis=(
        "container.googleapis.com"
        "secretmanager.googleapis.com"
        "firebase.googleapis.com"
        "firestore.googleapis.com"
        "storage.googleapis.com"
        "iam.googleapis.com"
        "cloudresourcemanager.googleapis.com"
        "compute.googleapis.com"
    )
    
    for api in "${apis[@]}"; do
        print_status "Enabling $api..."
        gcloud services enable $api
    done
}

# Create service accounts
create_service_accounts() {
    local project_id=$1
    local sa_name=$2
    print_step "Creating service account for project $project_id..."
    
    gcloud config set project $project_id
    
    # Create service account
    if gcloud iam service-accounts describe "${sa_name}@${project_id}.iam.gserviceaccount.com" &>/dev/null; then
        print_status "Service account $sa_name already exists in $project_id"
    else
        print_status "Creating service account $sa_name in $project_id..."
        gcloud iam service-accounts create $sa_name \
            --display-name="Connect Backend Service Account" \
            --description="Service account for Connect Dating App backend"
    fi
    
    # Assign roles
    local roles=(
        "roles/secretmanager.secretAccessor"
        "roles/firebase.admin"
        "roles/storage.admin"
        "roles/logging.logWriter"
        "roles/monitoring.metricWriter"
        "roles/cloudtrace.agent"
    )
    
    for role in "${roles[@]}"; do
        print_status "Assigning role $role to $sa_name..."
        gcloud projects add-iam-policy-binding $project_id \
            --member="serviceAccount:${sa_name}@${project_id}.iam.gserviceaccount.com" \
            --role="$role" \
            --quiet
    done
}

# Create static IP addresses
create_static_ips() {
    local project_id=$1
    local ip_name=$2
    print_step "Creating static IP for project $project_id..."
    
    gcloud config set project $project_id
    
    if gcloud compute addresses describe $ip_name --global &>/dev/null; then
        print_status "Static IP $ip_name already exists in $project_id"
        local ip_address=$(gcloud compute addresses describe $ip_name --global --format="value(address)")
        print_status "IP Address: $ip_address"
    else
        print_status "Creating static IP $ip_name in $project_id..."
        gcloud compute addresses create $ip_name --global
        local ip_address=$(gcloud compute addresses describe $ip_name --global --format="value(address)")
        print_status "Created IP Address: $ip_address"
    fi
}

# Create Firebase projects
create_firebase_projects() {
    local project_id=$1
    print_step "Setting up Firebase for project $project_id..."
    
    gcloud config set project $project_id
    
    print_status "Adding Firebase to project $project_id..."
    # Note: This requires manual setup in Firebase Console
    print_warning "Manual step required: Please go to https://console.firebase.google.com/"
    print_warning "1. Add Firebase to project $project_id"
    print_warning "2. Enable Firestore Database"
    print_warning "3. Enable Firebase Storage"
    print_warning "4. Create a web app configuration"
    print_warning "Press Enter when Firebase setup is complete for $project_id..."
    read -r
}

# Create secrets in Secret Manager
create_secrets() {
    local project_id=$1
    print_step "Creating secrets for project $project_id..."
    
    gcloud config set project $project_id
    
    # Define secrets based on environment
    local secrets
    if [[ $project_id == *"int"* ]]; then
        secrets=(
            "jwt-secret"
            "firebase-project-id:$project_id"
            "firebase-database-url:https://$project_id-default-rtdb.firebaseio.com"
            "firebase-storage-bucket:$project_id.firebasestorage.app"
            "firebase-config-path:/secrets/firebase/credentials.json"
            "gcp-project-id:$project_id"
            "gcp-storage-bucket:$project_id-storage"
            "gcp-credentials-path:/secrets/gcp/credentials.json"
            "email-from-email:noreply@connect-app.com"
            "email-username:connect-integration"
            "email-password:integration-password-placeholder"
        )
    elif [[ $project_id == *"pre"* ]]; then
        secrets=(
            "jwt-secret"
            "firebase-project-id:$project_id"
            "firebase-database-url:https://$project_id-default-rtdb.firebaseio.com"
            "firebase-storage-bucket:$project_id.firebasestorage.app"
            "firebase-config-path:/secrets/firebase/credentials.json"
            "gcp-project-id:$project_id"
            "gcp-storage-bucket:$project_id-storage"
            "gcp-credentials-path:/secrets/gcp/credentials.json"
            "email-from-email:noreply@connect-app.com"
            "email-username:connect-preprod"
            "email-password:preprod-password-placeholder"
        )
    else
        # Production
        secrets=(
            "jwt-secret"
            "firebase-project-id:$project_id"
            "firebase-database-url:https://$project_id-default-rtdb.firebaseio.com"
            "firebase-storage-bucket:$project_id.firebasestorage.app"
            "firebase-config-path:/secrets/firebase/credentials.json"
            "gcp-project-id:$project_id"
            "gcp-storage-bucket:$project_id-storage"
            "gcp-credentials-path:/secrets/gcp/credentials.json"
            "email-from-email:noreply@connect-app.com"
            "email-username:connect-production"
            "email-password:production-password-placeholder"
        )
    fi
    
    for secret_entry in "${secrets[@]}"; do
        local secret_name=$(echo $secret_entry | cut -d: -f1)
        local secret_value=$(echo $secret_entry | cut -d: -f2-)
        
        if gcloud secrets describe $secret_name &>/dev/null; then
            print_status "Secret $secret_name already exists in $project_id"
        else
            print_status "Creating secret $secret_name in $project_id..."
            echo -n "$secret_value" | gcloud secrets create $secret_name --data-file=-
        fi
    done
}

# Set up Workload Identity
setup_workload_identity() {
    local project_id=$1
    local sa_name=$2
    print_step "Setting up Workload Identity for project $project_id..."
    
    gcloud config set project $project_id
    
    # Enable Workload Identity on the cluster (if not already enabled)
    print_status "Enabling Workload Identity on cluster..."
    gcloud container clusters update $CLUSTER_NAME \
        --zone=$ZONE \
        --workload-pool="${project_id}.svc.id.goog" \
        --quiet || true
    
    # Create Kubernetes service account binding
    local k8s_namespace
    if [[ $project_id == *"int"* ]]; then
        k8s_namespace="connect-int"
    elif [[ $project_id == *"pre"* ]]; then
        k8s_namespace="connect-pre"
    else
        k8s_namespace="connect"
    fi
    
    print_status "Creating Workload Identity binding for $k8s_namespace..."
    gcloud iam service-accounts add-iam-policy-binding \
        "${sa_name}@${project_id}.iam.gserviceaccount.com" \
        --role="roles/iam.workloadIdentityUser" \
        --member="serviceAccount:${PROD_PROJECT_ID}.svc.id.goog[$k8s_namespace/$sa_name]" \
        --quiet
}

# Main execution
main() {
    print_step "Starting GCP infrastructure setup for Connect Dating App"
    
    check_prerequisites
    
    # Create projects
    create_projects
    
    # Setup each environment (skip production since it already exists)
    # Note: Pre-production skipped due to billing account quota - set up manually later
    for project in $INT_PROJECT_ID; do
        print_step "Setting up environment: $project"
        
        # Enable APIs
        enable_apis $project
        
        # Create service accounts
        if [[ $project == *"int"* ]]; then
            create_service_accounts $project "connect-backend-int"
            create_static_ips $project "connect-backend-int-ip"
            create_secrets $project
            setup_workload_identity $project "connect-backend-int"
        elif [[ $project == *"pre"* ]]; then
            create_service_accounts $project "connect-backend-pre"
            create_static_ips $project "connect-backend-pre-ip"
            create_secrets $project
            setup_workload_identity $project "connect-backend-pre"
        fi
        
        # Setup Firebase (manual step)
        create_firebase_projects $project
    done
    
    # Handle production environment separately (already exists)
    print_step "Configuring existing production environment: $PROD_PROJECT_ID"
    gcloud config set project $PROD_PROJECT_ID
    
    # Only set up production-specific resources if they don't exist
    if ! gcloud compute addresses describe connect-backend-ip --global &>/dev/null; then
        create_static_ips $PROD_PROJECT_ID "connect-backend-ip"
    else
        print_status "Production static IP already exists"
        local ip_address=$(gcloud compute addresses describe connect-backend-ip --global --format="value(address)")
        print_status "Production IP Address: $ip_address"
    fi
    
    # Setup Workload Identity for production if not already configured
    setup_workload_identity $PROD_PROJECT_ID "connect-backend"
    
    print_step "Infrastructure setup complete!"
    print_status "Next steps:"
    print_status "1. Update DNS records to point to the static IPs"
    print_status "2. Create SSL certificates in GCP"
    print_status "3. Update your application configuration files"
    print_status "4. Deploy using Helm charts"
    
    # Display the static IPs
    print_step "Static IP addresses:"
    
    # Production IP
    gcloud config set project $PROD_PROJECT_ID
    local prod_ip=$(gcloud compute addresses describe connect-backend-ip --global --format="value(address)" 2>/dev/null || echo "Not found")
    print_status "$PROD_PROJECT_ID (api.connect-app.com): $prod_ip"
    
    # Integration IP
    gcloud config set project $INT_PROJECT_ID
    local int_ip=$(gcloud compute addresses describe connect-backend-int-ip --global --format="value(address)" 2>/dev/null || echo "Not found")
    print_status "$INT_PROJECT_ID (int-api.connect-app.com): $int_ip"
    
    # Pre-production IP
    gcloud config set project $PRE_PROJECT_ID
    local pre_ip=$(gcloud compute addresses describe connect-backend-pre-ip --global --format="value(address)" 2>/dev/null || echo "Not found")
    print_status "$PRE_PROJECT_ID (pre-api.connect-app.com): $pre_ip"
}

# Run main function
main "$@"