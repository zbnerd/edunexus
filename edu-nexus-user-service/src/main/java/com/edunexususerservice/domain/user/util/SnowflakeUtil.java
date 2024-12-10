package com.edunexususerservice.domain.user.util;

import cn.hutool.core.lang.Snowflake;

public class SnowflakeUtil {
    private static final long DATA_CENTER_ID = 1L; // 데이터 센터 ID
    private static final long MACHINE_ID = 1L;    // 머신 ID
    private static final Snowflake snowflake = new Snowflake(DATA_CENTER_ID, MACHINE_ID);

    private SnowflakeUtil() {
    }

    public static long generateId() {
        return snowflake.nextId();
    }
}