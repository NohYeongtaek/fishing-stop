package com.example.fishingstop.core.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.fishingstop.ui.theme.AppTheme

/** 하단 탭 항목. */
data class BottomBarItem(
    val label: String,
    val icon: ImageVector,
    val selected: Boolean,
    val onClick: () -> Unit
)

/**
 * 앱 하단 탭바(디자인 스펙 1-7 TabBar). 5탭, 아이콘 21/26dp, 라벨 12/14sp.
 * 활성=green.primary, 배경=navbar.bg, 인디케이터는 투명(브랜드 톤 유지).
 */
@Composable
fun AppBottomBar(items: List<BottomBarItem>) {
    val colors = AppTheme.colors
    NavigationBar(containerColor = colors.navbarBg) {
        items.forEach { item ->
            NavigationBarItem(
                selected = item.selected,
                onClick = item.onClick,
                icon = {
                    Icon(
                        item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(AppTheme.sizes.tabIcon)
                    )
                },
                label = { Text(item.label, style = AppTheme.type.tabLabel) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colors.greenPrimary,
                    selectedTextColor = colors.greenPrimary,
                    unselectedIconColor = colors.textTertiary,
                    unselectedTextColor = colors.textTertiary,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}
