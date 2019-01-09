package burp.faraday.models;

import burp.IBurpExtenderCallbacks;

public class ExtensionSettings {

    private final IBurpExtenderCallbacks callbacks;

    private static final String DEFAULT_FARADAY_URL = "http://localhost:5985";

    private static final String KEY_FARADAY_URL = "faraday_url";
    private static final String KEY_USERNAME = "faraday_username";
    private static final String KEY_COOKIE = "faraday_cookie";
    private static final String KEY_CURRENT_WORKSPACE = "faraday_current_workspace";


    public ExtensionSettings(IBurpExtenderCallbacks callbacks) {
        this.callbacks = callbacks;
    }

    public String getFaradayURL() {
        return getSetting(KEY_FARADAY_URL, DEFAULT_FARADAY_URL);
    }

    public void setFaradayURL(String faradayURL) {
        if (faradayURL != null) {
            faradayURL = faradayURL.trim();
        }
        callbacks.saveExtensionSetting(KEY_FARADAY_URL, faradayURL);
    }

    public String getUsername() {
        return getSetting(KEY_USERNAME);
    }

    public void setUsername(String username) {
        if (username != null) {
            username = username.trim();
        }
        callbacks.saveExtensionSetting(KEY_USERNAME, username);
    }

    public String getCookie() {
        return getSetting(KEY_COOKIE);
    }

    public void setCookie(String cookie) {
        if (cookie != null) {
            cookie = cookie.trim();
        }
        callbacks.saveExtensionSetting(KEY_COOKIE, cookie);
    }

    public String getCurrentWorkspace() {
        return getSetting(KEY_CURRENT_WORKSPACE);
    }

    public void setCurrentWorkspace(String currentWorkspace) {
        if (currentWorkspace != null) {
            currentWorkspace = currentWorkspace.trim();
        }

        callbacks.saveExtensionSetting(KEY_CURRENT_WORKSPACE, currentWorkspace);
    }

    public void restore() {
        callbacks.saveExtensionSetting(KEY_FARADAY_URL, DEFAULT_FARADAY_URL);
        callbacks.saveExtensionSetting(KEY_USERNAME, "");
        callbacks.saveExtensionSetting(KEY_COOKIE, "");
        callbacks.saveExtensionSetting(KEY_CURRENT_WORKSPACE, "");
    }

    private String getSetting(final String key) {
        return getSetting(key, "");
    }

    private String getSetting(final String key, final String defaultValue) {
        String value = callbacks.loadExtensionSetting(key);

        if (value == null) {
            return defaultValue;
        }

        value = value.trim();

        if (value.isEmpty()) {
            return defaultValue;
        }

        return value;
    }

    public void resetCookie() {
        setCookie("");
    }

}
