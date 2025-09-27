package com.example.lets_go_slavgorod.utils

import com.example.lets_go_slavgorod.data.model.BusRoute
import org.junit.Test
import org.junit.Assert.*

/**
 * Тесты для extension функций
 */
class ExtensionsTest {

    @Test
    fun `search should return all routes when query is blank`() {
        // Given
        val routes = listOf(
            BusRoute("1", "102", "Славгород-Яровое", "Описание 1", travelTime = "30 мин", paymentMethods = "Наличные"),
            BusRoute("2", "103", "Славгород-Барнаул", "Описание 2", travelTime = "2 часа", paymentMethods = "Карта")
        )
        val query = ""

        // When
        val result = routes.search(query)

        // Then
        assertEquals(routes, result)
    }

    @Test
    fun `search should filter routes by route number`() {
        // Given
        val routes = listOf(
            BusRoute("1", "102", "Славгород-Яровое", "Описание 1", travelTime = "30 мин", paymentMethods = "Наличные"),
            BusRoute("2", "103", "Славгород-Барнаул", "Описание 2", travelTime = "2 часа", paymentMethods = "Карта")
        )
        val query = "102"

        // When
        val result = routes.search(query)

        // Then
        assertEquals(1, result.size)
        assertEquals("102", result[0].routeNumber)
    }

    @Test
    fun `search should filter routes by name`() {
        // Given
        val routes = listOf(
            BusRoute("1", "102", "Славгород-Яровое", "Описание 1", travelTime = "30 мин", paymentMethods = "Наличные"),
            BusRoute("2", "103", "Славгород-Барнаул", "Описание 2", travelTime = "2 часа", paymentMethods = "Карта")
        )
        val query = "Яровое"

        // When
        val result = routes.search(query)

        // Then
        assertEquals(1, result.size)
        assertEquals("Славгород-Яровое", result[0].name)
    }

    @Test
    fun `search should filter routes by description`() {
        // Given
        val routes = listOf(
            BusRoute("1", "102", "Славгород-Яровое", "Описание 1", travelTime = "30 мин", paymentMethods = "Наличные"),
            BusRoute("2", "103", "Славгород-Барнаул", "Описание 2", travelTime = "2 часа", paymentMethods = "Карта")
        )
        val query = "Описание 2"

        // When
        val result = routes.search(query)

        // Then
        assertEquals(1, result.size)
        assertEquals("Славгород-Барнаул", result[0].name)
    }

    @Test
    fun `search should be case insensitive`() {
        // Given
        val routes = listOf(
            BusRoute("1", "102", "Славгород-Яровое", "Описание 1", travelTime = "30 мин", paymentMethods = "Наличные")
        )
        val query = "СЛАВГОРОД"

        // When
        val result = routes.search(query)

        // Then
        assertEquals(1, result.size)
        assertEquals("Славгород-Яровое", result[0].name)
    }

    @Test
    fun `searchOptimized should return same results as search`() {
        // Given
        val routes = listOf(
            BusRoute("1", "102", "Славгород-Яровое", "Описание 1", travelTime = "30 мин", paymentMethods = "Наличные"),
            BusRoute("2", "103", "Славгород-Барнаул", "Описание 2", travelTime = "2 часа", paymentMethods = "Карта")
        )
        val query = "Славгород"

        // When
        val searchResult = routes.search(query)
        val optimizedResult = routes.searchOptimized(query)

        // Then
        assertEquals(searchResult.size, optimizedResult.size)
        assertEquals(searchResult, optimizedResult)
    }
}
