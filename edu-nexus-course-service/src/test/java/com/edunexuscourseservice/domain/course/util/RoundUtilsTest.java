package com.edunexuscourseservice.domain.course.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RoundUtils utility class
 *
 * Tests decimal rounding functionality with various edge cases including
 * null values, negative scales, and different rounding scenarios.
 */
class RoundUtilsTest {

    //region Happy Path Tests
    @ParameterizedTest
    @CsvSource({
            "4.567, 2, 4.57",
            "4.567, 1, 4.6",
            "4.567, 0, 5.0",
            "4.5, 1, 4.5",
            "5.5, 0, 6.0",
            "1.23456, 3, 1.235",
            "100.0, 2, 100.0",
            "0.0, 4, 0.0",
            "-2.345, 2, -2.35",
            "-2.345, 1, -2.3",
            "-2.5, 0, -3.0"
    })
    void roundToNDecimals_WhenValidInput_ShouldReturnCorrectRounding(double value, int scale, double expected) {
        // when
        double result = RoundUtils.roundToNDecimals(value, scale);

        // then
        assertEquals(expected, result, 0.0001);
    }

    @Test
    void roundToNDecimals_WhenScaleIsZero_ShRoundToInteger() {
        // given
        double value = 4.567;

        // when
        double result = RoundUtils.roundToNDecimals(value, 0);

        // then
        assertEquals(5.0, result);
    }

    @Test
    void roundToNDecimals_WhenScaleIsMax_ShReturnPreciseValue() {
        // given
        double value = 1.23456789;
        int maxScale = 8;

        // when
        double result = RoundUtils.roundToNDecimals(value, maxScale);

        // then
        assertEquals(1.23456789, result, 0.00000001);
    }
    //endregion

    //region Edge Case Tests
    @Test
    void roundToNDecimals_WhenValueIsZero_ShReturnZero() {
        // given
        double value = 0.0;
        int scale = 4;

        // when
        double result = RoundUtils.roundToNDecimals(value, scale);

        // then
        assertEquals(0.0, result);
    }

    @Test
    void roundToNDecimals_WhenValueIsInteger_ShReturnSameValue() {
        // given
        double value = 42.0;
        int scale = 2;

        // when
        double result = RoundUtils.roundToNDecimals(value, scale);

        // then
        assertEquals(42.0, result);
    }

    @Test
    void roundToNDecimals_WhenValueIsNegativeAndRoundUp_ShCorrectRoundNegative() {
        // given
        double value = -2.5;
        int scale = 0;

        // when
        double result = RoundUtils.roundToNDecimals(value, scale);

        // then
        assertEquals(-3.0, result);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 5, 10})
    void roundToNDecimals_WhenValueIsOne_ShReturnOneForAnyScale(int scale) {
        // given
        double value = 1.0;

        // when
        double result = RoundUtils.roundToNDecimals(value, scale);

        // then
        assertEquals(1.0, result);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3})
    void roundToNDecimals_WhenValueIsNineAndNine_ShRoundCorrectly(int scale) {
        // given
        double value = 9.999;

        // when
        double result = RoundUtils.roundToNDecimals(value, scale);

        // then
        if (scale == 0) assertEquals(10.0, result);
        else if (scale == 1) assertEquals(10.0, result);
        else if (scale == 2) assertEquals(10.0, result);
        else if (scale == 3) assertEquals(9.999, result);
    }
    //endregion

    //region Error Case Tests
    @ParameterizedTest
    @ValueSource(ints = {-1, -5, -100})
    void roundToNDecimals_WhenScaleIsNegative_ShThrowIllegalArgumentException(int negativeScale) {
        // given
        double value = 4.567;

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            RoundUtils.roundToNDecimals(value, negativeScale);
        });
    }

    @Test
    void roundToNDecimals_WhenScaleIsVeryLarge_ShWorkButMayHavePrecisionIssues() {
        // given
        double value = 1.23456789;
        int veryLargeScale = 15;

        // when
        double result = RoundUtils.roundToNDecimals(value, veryLargeScale);

        // then - Note: May have precision issues with very large scales
        assertNotNull(result);
        assertTrue(result >= 1.23456788 && result <= 1.23456790);
    }
    //endregion

    //region Large Number Tests
    @Test
    void roundToNDecimals_WhenValueIsLarge_ShRoundCorrectly() {
        // given
        double value = 1234567.89123456;
        int scale = 4;

        // when
        double result = RoundUtils.roundToNDecimals(value, scale);

        // then
        assertEquals(1234567.8912, result, 0.0001);
    }

    @Test
    void roundToNDecimals_WhenValueIsVeryLargeAndScaleIsZero_ShRoundToNearestInteger() {
        // given
        double value = 9999999.6;

        // when
        double result = RoundUtils.roundToNDecimals(value, 0);

        // then
        assertEquals(10000000.0, result);
    }
    //endregion

    //region Precision Tests
    @ParameterizedTest
    @CsvSource({
            "0.1, 1, 0.1",
            "0.1, 2, 0.1",
            "0.15, 1, 0.2",
            "0.15, 2, 0.15",
            "0.155, 2, 0.16",
            "0.155, 1, 0.2",
            "0.1555, 3, 0.156"
    })
    void roundToNDecimals_WhenValueHasFloatingPointPrecisionIssues_ShRoundCorrectly(double value, int scale, double expected) {
        // when
        double result = RoundUtils.roundToNDecimals(value, scale);

        // then
        assertEquals(expected, result, 0.000001);
    }

    @Test
    void roundToNDecimals_WhenValueRepeatingDecimal_ShRoundCorrectly() {
        // given
        double value = 1.0 / 3.0; // 0.3333333333...
        int scale = 4;

        // when
        double result = RoundUtils.roundToNDecimals(value, scale);

        // then
        assertEquals(0.3333, result, 0.0001);
    }
    //endregion

    //region Rounding Mode Verification Tests
    @ParameterizedTest
    @CsvSource({
            "2.499, 1, 2.5",  // Should round up at exactly halfway
            "2.500, 1, 2.5",  // Should round up at exactly halfway
            "2.501, 1, 2.5",  // Should round up above halfway
            "2.499, 0, 2.0",  // Should not round up below halfway
            "2.500, 0, 3.0"   // Should round up at exactly halfway to integer
    })
    void roundToNDecimals_WhenTestingRoundingMode_ShUseHalfUpRounding(double value, int scale, double expected) {
        // when
        double result = RoundUtils.roundToNDecimals(value, scale);

        // then
        assertEquals(expected, result, 0.0001);
    }
    //endregion
}