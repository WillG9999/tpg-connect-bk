# Admin Scripts

This directory contains administrative scripts for the Connect dating application.

## Scripts

### cleanup_userprofiles.py

**Purpose**: Deletes all documents from the 'userProfiles' Firestore collection except the admin@connect.com profile.

**Requirements**:
- Python 3.6+
- firebase-admin Python package (`pip install firebase-admin`)
- Firebase service account key file at `/Users/willgraham/Desktop/keys/firebase-service-account.json`

**Usage**:
```bash
cd AdminScripts
python3 cleanup_userprofiles.py
```

**Safety Features**:
- Lists all profiles before deletion
- Shows which profiles will be preserved (admin@connect.com)
- Requires user confirmation before proceeding
- Provides detailed summary of operations

**Warning**: This script permanently deletes data. Use with caution and ensure you have backups if needed.

## Installation

1. Install required Python package:
```bash
pip install firebase-admin
```

2. Ensure Firebase service account key is available at the specified path
3. Run scripts from the AdminScripts directory

## Security Notes

- These scripts require Firebase Admin SDK privileges
- Keep service account keys secure and never commit them to version control
- Test scripts in development environment before running in production
- Always review the profiles to be deleted before confirming operations