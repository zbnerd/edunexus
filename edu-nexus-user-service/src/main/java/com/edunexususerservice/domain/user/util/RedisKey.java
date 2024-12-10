package com.edunexususerservice.domain.user.util;

public enum RedisKey {
    USER_LOGIN_TOKEN("edu-nexus-user", "users", "login_token");

    private final String database;
    private final String table;
    private final String attribute;

    RedisKey(String database, String table, String attribute) {
        this.database = database;
        this.table = table;
        this.attribute = attribute;
    }

    /**
     * Key 생성 메서드
     * @param userId 유저 ID
     * @return 완전한 Redis 키
     */
    public String getKey(String userId) {
        return String.format("%s:%s:%s:%s", database, table, attribute, userId);
    }

    /**
     * ID 없이 Key 생성 (테이블 범위에서 사용)
     * @return 기본 Redis 키
     */
    public String getKeyWithoutId() {
        return String.format("%s:%s:%s", database, table, attribute);
    }
}
