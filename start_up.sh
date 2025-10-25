#!/bin/bash

# Connect Dating App - Startup Script with Environment Variable Support
# This script loads environment variables from .env file and starts the application

# Check if running in development mode (Maven) or production mode (JAR)
if [ "$1" = "dev" ] || [ "$1" = "development" ]; then
    echo "🚀 Starting Connect Application in DEVELOPMENT mode..."
    
    # Load environment variables from .env file (excluding comments)
    if [ -f .env ]; then
        echo "📄 Loading environment variables from .env file..."
        export $(cat .env | grep -v '^#' | grep -v '^$' | xargs)
        echo "✅ Environment variables loaded successfully"
    else
        echo "⚠️  Warning: .env file not found - using system environment variables"
    fi
    
    # Set default profile if not specified
    if [ -z "$SPRING_PROFILES_ACTIVE" ]; then
        export SPRING_PROFILES_ACTIVE=bld
        echo "📋 Using default profile: bld"
    else
        echo "📋 Using profile: $SPRING_PROFILES_ACTIVE"
    fi
    
    # Start with Maven (development)
    echo "🔨 Starting with Maven..."
    mvn spring-boot:run -Dspring-boot.run.profiles=$SPRING_PROFILES_ACTIVE
    
elif [ "$1" = "prod" ] || [ "$1" = "production" ]; then
    echo "🚀 Starting Connect Application in PRODUCTION mode..."
    
    # Production mode - use container/system environment variables
    app_path=${APP_PATH:-/app}
    cd $app_path
    
    jar_file=`ls $app_path/*.jar | head -1`
    if [ -z "$jar_file" ]; then
        echo "❌ Error: No JAR file found in $app_path"
        exit 1
    fi
    
    profile=${PROFILE:-prod}
    echo "📋 Environment profile: $profile"
    echo "🗂️  Starting JAR: $jar_file"
    
    # Start with Java (production)
    java -Dspring.profiles.active=$profile -jar $jar_file
    
else
    echo "🚀 Connect Dating App - Startup Script"
    echo ""
    echo "Usage:"
    echo "  ./start_up.sh dev          - Start in development mode with Maven"
    echo "  ./start_up.sh prod         - Start in production mode with JAR"
    echo ""
    echo "Environment Variables:"
    echo "  SPRING_PROFILES_ACTIVE     - Spring profile (default: bld for dev, prod for prod)"
    echo "  APP_PATH                   - Application path for production (default: /app)"
    echo "  PROFILE                    - Legacy profile variable for production"
    echo ""
    echo "Examples:"
    echo "  ./start_up.sh dev                    # Development with .env file"
    echo "  PROFILE=staging ./start_up.sh prod   # Production with staging profile"
    echo ""
    exit 1
fi