package com.example.lets_go_slavgorod.utils

import org.junit.Test
import org.junit.Assert.*

/**
 * Тесты для констант приложения
 */
class ConstantsTest {

    @Test
    fun `animation durations should be positive`() {
        assertTrue(Constants.ANIMATION_DURATION_SHORT > 0)
        assertTrue(Constants.ANIMATION_DURATION_MEDIUM > 0)
        assertTrue(Constants.ANIMATION_DURATION_LONG > 0)
    }

    @Test
    fun `animation durations should be in ascending order`() {
        assertTrue(Constants.ANIMATION_DURATION_SHORT < Constants.ANIMATION_DURATION_MEDIUM)
        assertTrue(Constants.ANIMATION_DURATION_MEDIUM < Constants.ANIMATION_DURATION_LONG)
    }

    @Test
    fun `UI dimensions should be positive`() {
        assertTrue(Constants.CARD_ELEVATION >= 0)
        assertTrue(Constants.CARD_CORNER_RADIUS > 0)
        assertTrue(Constants.ROUTE_NUMBER_BOX_SIZE > 0)
        assertTrue(Constants.ROUTE_NUMBER_BOX_CORNER_RADIUS > 0)
    }

    @Test
    fun `padding values should be positive`() {
        assertTrue(Constants.PADDING_SMALL > 0)
        assertTrue(Constants.PADDING_MEDIUM > 0)
        assertTrue(Constants.PADDING_LARGE > 0)
    }

    @Test
    fun `padding values should be in ascending order`() {
        assertTrue(Constants.PADDING_SMALL < Constants.PADDING_MEDIUM)
        assertTrue(Constants.PADDING_MEDIUM < Constants.PADDING_LARGE)
    }

    @Test
    fun `default colors should be valid hex colors`() {
        assertTrue(Constants.DEFAULT_ROUTE_COLOR.startsWith("#"))
        assertTrue(Constants.DEFAULT_ROUTE_COLOR_ALT.startsWith("#"))
        assertEquals(9, Constants.DEFAULT_ROUTE_COLOR.length) // #FF6200EE
        assertEquals(9, Constants.DEFAULT_ROUTE_COLOR_ALT.length) // #FF1976D2
    }

    @Test
    fun `color alpha should be between 0 and 1`() {
        assertTrue(Constants.COLOR_ALPHA > 0f)
        assertTrue(Constants.COLOR_ALPHA <= 1f)
    }

    @Test
    fun `notification settings should be reasonable`() {
        assertTrue(Constants.NOTIFICATION_LEAD_TIME_MINUTES > 0)
        assertTrue(Constants.NOTIFICATION_LEAD_TIME_MINUTES <= 60) // Not more than 1 hour
    }

    @Test
    fun `database settings should be valid`() {
        assertTrue(Constants.DATABASE_NAME.isNotEmpty())
        assertTrue(Constants.DATABASE_VERSION > 0)
    }

    @Test
    fun `search settings should be reasonable`() {
        assertTrue(Constants.SEARCH_DEBOUNCE_DELAY > 0)
        assertTrue(Constants.SEARCH_DEBOUNCE_DELAY <= 1000) // Not more than 1 second
    }

    @Test
    fun `cache settings should be reasonable`() {
        assertTrue(Constants.CACHE_SIZE > 0)
        assertTrue(Constants.CACHE_EXPIRE_TIME_HOURS > 0)
    }

    @Test
    fun `performance settings should be reasonable`() {
        assertTrue(Constants.MAX_SEARCH_RESULTS > 0)
        assertTrue(Constants.DEBOUNCE_DELAY_MS > 0)
        assertTrue(Constants.SWIPE_THRESHOLD_PERCENT > 0f)
        assertTrue(Constants.SWIPE_THRESHOLD_PERCENT <= 1f)
        assertTrue(Constants.MIN_SWIPE_DISTANCE > 0)
    }

    @Test
    fun `log levels should be in ascending order`() {
        assertTrue(Constants.LOG_LEVEL_DEBUG < Constants.LOG_LEVEL_INFO)
        assertTrue(Constants.LOG_LEVEL_INFO < Constants.LOG_LEVEL_WARN)
        assertTrue(Constants.LOG_LEVEL_WARN < Constants.LOG_LEVEL_ERROR)
    }
}
