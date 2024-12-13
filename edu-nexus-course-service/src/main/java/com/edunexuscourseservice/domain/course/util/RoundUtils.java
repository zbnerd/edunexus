package com.edunexuscourseservice.domain.course.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class RoundUtils {
    public static double roundToNDecimals(double value, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("Scale must be a non-negative integer.");
        }

        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(scale, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
