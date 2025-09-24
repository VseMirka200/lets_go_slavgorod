package com.example.lets_go_slavgorod.ui.navigation

/**
 * Sealed class для определения экранов навигации
 * 
 * Содержит все основные экраны приложения:
 * - Home: главный экран с маршрутами
 * - FavoriteTimes: экран избранных маршрутов
 * - Settings: экран настроек приложения
 * - About: экран информации о программе
 * - WebView: экран для отображения веб-страниц
 */
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object FavoriteTimes : Screen("favorite_times")
    object Settings : Screen("settings")
    object About : Screen("about")
    object WebView : Screen("webview/{url}/{title}") {
        fun createRoute(url: String, title: String = "Веб-страница"): String {
            return "webview/${url.encodeUrl()}/${title.encodeUrl()}"
        }
    }
}

/**
 * Кодирует URL для передачи через навигацию
 */
private fun String.encodeUrl(): String {
    return this.replace("/", "%2F")
        .replace(":", "%3A")
        .replace("?", "%3F")
        .replace("&", "%26")
        .replace("=", "%3D")
        .replace("#", "%23")
        .replace(" ", "%20")
}
