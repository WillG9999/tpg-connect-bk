package com.tpg.connect.constants;

public final class EndpointConstants {

    private EndpointConstants() {
        // Utility class - prevent instantiation
    }

    // Base API paths
    public static final String API_BASE = "/api";
    public static final String API_V1 = API_BASE + "/v1";

    // Authentication & JWT endpoints
    public static final class Auth {
        public static final String BASE = API_BASE + "/auth";
        public static final String LOGIN = BASE + "/login";
        public static final String LOGOUT = BASE + "/logout";
        public static final String REFRESH = BASE + "/refresh";
        public static final String GENERATE = BASE + "/generate";
        public static final String GENERATE_WITH_ROLE = BASE + "/generate-with-role";
        
        private Auth() {}
    }

    // User profile endpoints
    public static final class Users {
        public static final String BASE = API_BASE + "/users";
        public static final String ME = BASE + "/me";
        public static final String PROFILE = BASE + "/profile";
        public static final String PHOTOS = BASE + "/photos";
        public static final String PREFERENCES = BASE + "/preferences";
        public static final String VISIBILITY = BASE + "/visibility";
        
        private Users() {}
    }

    // Health and system endpoints
    public static final class Health {
        public static final String BASE = API_BASE + "/health";
        public static final String STATUS = BASE + "/status";
        public static final String READY = BASE + "/ready";
        public static final String LIVE = BASE + "/live";
        
        private Health() {}
    }

    // Discovery and matching endpoints (future use)
    public static final class Discovery {
        public static final String BASE = API_BASE + "/discovery";
        public static final String MATCHES = BASE + "/matches";
        public static final String LIKE = BASE + "/like";
        public static final String PASS = BASE + "/pass";
        public static final String SUPER_LIKE = BASE + "/super-like";
        
        private Discovery() {}
    }

    // Chat and messaging endpoints (future use)
    public static final class Chat {
        public static final String BASE = API_BASE + "/chat";
        public static final String CONVERSATIONS = BASE + "/conversations";
        public static final String MESSAGES = BASE + "/messages";
        public static final String SEND = BASE + "/send";
        
        private Chat() {}
    }

    // Admin endpoints (future use)
    public static final class Admin {
        public static final String BASE = API_BASE + "/admin";
        public static final String USERS = BASE + "/users";
        public static final String REPORTS = BASE + "/reports";
        public static final String ANALYTICS = BASE + "/analytics";
        
        private Admin() {}
    }

    // File upload endpoints (future use)
    public static final class Upload {
        public static final String BASE = API_BASE + "/upload";
        public static final String PHOTO = BASE + "/photo";
        public static final String PROFILE_PHOTO = BASE + "/profile-photo";
        public static final String AVATAR = BASE + "/avatar";
        
        private Upload() {}
    }

    // Notification endpoints (future use)
    public static final class Notifications {
        public static final String BASE = API_BASE + "/notifications";
        public static final String PREFERENCES = BASE + "/preferences";
        public static final String HISTORY = BASE + "/history";
        public static final String MARK_READ = BASE + "/mark-read";
        
        private Notifications() {}
    }

    // Common path parameters
    public static final class PathParams {
        public static final String USER_ID = "/{userId}";
        public static final String CONVERSATION_ID = "/{conversationId}";
        public static final String MESSAGE_ID = "/{messageId}";
        public static final String PHOTO_ID = "/{photoId}";
        public static final String MATCH_ID = "/{matchId}";
        
        private PathParams() {}
    }

    // Query parameters
    public static final class QueryParams {
        public static final String INCLUDE_PREFERENCES = "includePreferences";
        public static final String PAGE = "page";
        public static final String SIZE = "size";
        public static final String SORT = "sort";
        public static final String FILTER = "filter";
        public static final String SEARCH = "search";
        
        private QueryParams() {}
    }

    // HTTP Headers
    public static final class Headers {
        public static final String AUTHORIZATION = "Authorization";
        public static final String BEARER_PREFIX = "Bearer ";
        public static final String CONTENT_TYPE = "Content-Type";
        public static final String APPLICATION_JSON = "application/json";
        public static final String MULTIPART_FORM_DATA = "multipart/form-data";
        
        private Headers() {}
    }
}
