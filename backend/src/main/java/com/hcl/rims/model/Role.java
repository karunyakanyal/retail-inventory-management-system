package com.hcl.rims.model;

public enum Role {
    ROLE_ADMIN,
    ROLE_MANAGER,
    ROLE_STAFF,
    ROLE_VIEWER;

    public static Role fromString(String roleName) {
        if (roleName == null) return ROLE_VIEWER;
        String clean = roleName.trim().toUpperCase();
        if (!clean.startsWith("ROLE_")) {
            clean = "ROLE_" + clean;
        }
        return Role.valueOf(clean);
    }
}
