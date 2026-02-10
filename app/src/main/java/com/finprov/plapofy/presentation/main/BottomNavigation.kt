package com.finprov.plapofy.presentation.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.finprov.plapofy.presentation.theme.Primary
import com.finprov.plapofy.presentation.theme.TextSecondary

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : BottomNavItem(
        route = "home",
        title = "Beranda",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    )
    object Card : BottomNavItem(
        route = "credit-card-tab",
        title = "Kartu",
        selectedIcon = Icons.Filled.CreditCard,
        unselectedIcon = Icons.Outlined.CreditCard
    )
    object History : BottomNavItem(
        route = "my-loans",
        title = "Riwayat",
        selectedIcon = Icons.Filled.History,
        unselectedIcon = Icons.Outlined.History
    )
    object Profile : BottomNavItem(
        route = "profile",
        title = "Profil",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )
}

val bottomNavItems = listOf(
    BottomNavItem.Home,
    BottomNavItem.Card,
    BottomNavItem.History,
    BottomNavItem.Profile
)

// Routes that should show bottom navigation
val bottomNavRoutes = setOf(
    "home",
    "credit-card-tab",
    "my-loans",
    "profile",
    "credit-dashboard"
)

@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 16.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomNavItems.forEach { item ->
                val selected = currentDestination?.hierarchy?.any { 
                    it.route == item.route || 
                    (item.route == "credit-card-tab" && it.route == "credit-dashboard")
                } == true
                
                BottomNavItemView(
                    item = item,
                    selected = selected,
                    onClick = {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun BottomNavItemView(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.1f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    
    val iconColor by animateColorAsState(
        targetValue = if (selected) Primary else TextSecondary,
        label = "iconColor"
    )
    
    val bgColor by animateColorAsState(
        targetValue = if (selected) Primary.copy(alpha = 0.15f) else Color.Transparent,
        label = "bgColor"
    )

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icon with background indicator
        Box(
            modifier = Modifier
                .scale(scale)
                .clip(RoundedCornerShape(12.dp))
                .background(bgColor)
                .padding(horizontal = 16.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                contentDescription = item.title,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Label
        Text(
            text = item.title,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) Primary else TextSecondary
        )
        
        // Selection indicator dot
        if (selected) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(Primary)
            )
        }
    }
}

fun shouldShowBottomBar(route: String?): Boolean {
    return route in bottomNavRoutes
}
