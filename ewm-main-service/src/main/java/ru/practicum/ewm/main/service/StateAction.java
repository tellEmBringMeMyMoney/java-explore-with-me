package ru.practicum.ewm.main.service;

public enum StateAction {
    PUBLISH_EVENT(true),
    REJECT_EVENT(true),
    SEND_TO_REVIEW(false),
    CANCEL_REVIEW(false);

    private final boolean adminOnly;

    StateAction(boolean adminOnly) {
        this.adminOnly = adminOnly;
    }

    public boolean isAdminOnly() {
        return adminOnly;
    }
}
