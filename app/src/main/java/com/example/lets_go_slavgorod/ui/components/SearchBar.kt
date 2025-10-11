package com.example.lets_go_slavgorod.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

/**
 * Переиспользуемый компонент поисковой строки
 * 
 * Создает современную поисковую строку в стиле Material Design 3
 * с поддержкой ввода текста, очистки запроса и выполнения поиска.
 * 
 * Особенности:
 * - Автоматическое управление фокусом
 * - Кнопка очистки при наличии текста
 * - Поддержка клавиатурных действий
 * - Настраиваемый placeholder и иконка
 * 
 * @param query текущий поисковый запрос
 * @param onQueryChange callback-функция при изменении запроса
 * @param onSearch callback-функция при выполнении поиска
 * @param modifier модификатор для настройки внешнего вида
 * @param placeholder текст-подсказка в поле поиска
 * @param leadingIcon иконка слева от поля ввода
 * 
 * @author VseMirka200
 * @version 2.0
 * @since 1.0
 */
@SuppressLint("RememberInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Поиск маршрутов...",
    leadingIcon: ImageVector = Icons.Default.Search
) {
    val focusRequester = FocusRequester()

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp)
            .focusRequester(focusRequester),
        placeholder = { Text(placeholder) },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedLabelColor = MaterialTheme.colorScheme.primary
        ),
        shape = MaterialTheme.shapes.medium
    )
}