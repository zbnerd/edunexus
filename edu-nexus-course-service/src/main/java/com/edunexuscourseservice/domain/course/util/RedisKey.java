package com.edunexuscourseservice.domain.course.util;

public enum RedisKey {
    COURSE_RATING_TOTAL("edu-nexus-course", "course_ratings", "total"),
    COURSE_RATING_COUNT("edu-nexus-course", "course_ratings", "count");

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
     * @param id 리소스 ID
     * @return 완전한 Redis 키
     */
    public String getKey(Long id) {
        return String.format("%s:%s:%s:%d", database, table, attribute, id);
    }

    /**
     * ID 없이 Key 생성 (테이블 범위에서 사용)
     * @return 기본 Redis 키
     */
    public String getKeyWithoutId() {
        return String.format("%s:%s:%s", database, table, attribute);
    }
}
