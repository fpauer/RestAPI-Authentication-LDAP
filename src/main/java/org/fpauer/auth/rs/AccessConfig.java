package org.fpauer.auth.rs;

public class AccessConfig {

    public static final int DEFAULT_ACCESS_EXPIRED_MINUTES = 15;
    
    public static enum Keys {
    	ACCESS_EXPIRED_MINUTES("expired.minutes");

        private final String key;

        Keys(String key) {
            this.key = key;
        }

        public String key() {
            return key;
        }
    }
}
