package com.project.tickr.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.tickr.presentation.additem.AddItemEvent
import com.project.tickr.presentation.additem.AddItemViewModel
import com.project.tickr.presentation.expiry.ExpiryViewModel
import com.project.tickr.presentation.home.HomeEvent
import com.project.tickr.presentation.home.HomeViewModel
import com.project.tickr.presentation.navigation.Navigator
import com.project.tickr.ui.screen.expiry.ExpiryRoute
import com.project.tickr.ui.screen.home.HomeRoute
import com.project.tickr.ui.screen.home.additem.AddItemSheet
import com.project.tickr.ui.theme.TickrTheme
import org.koin.compose.viewmodel.koinViewModel

// Icons (using Material Icons as placeholder — TODO(user): replace with Lucide/Phosphor outline icons)
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.TrendingUp

private enum class BottomTab(
    val icon: ImageVector,
    val label: String,
    val contentDesc: String,
) {
    HOME(Icons.Outlined.Home, "Beranda", "Tab Beranda"), // TODO(user): sesuaikan
    EXPIRING(Icons.Outlined.Refresh, "Kadaluwarsa", "Tab Kadaluwarsa"),
    INSIGHT(Icons.Outlined.TrendingUp, "Wawasan", "Tab Wawasan"),
    PROFILE(Icons.Outlined.Person, "Profil", "Tab Profil"),
}

@Composable
fun MainShell(navigator: Navigator) {
    val homeVm: HomeViewModel = koinViewModel()
    val addItemVm: AddItemViewModel = koinViewModel()
    val expiryVm: ExpiryViewModel = koinViewModel()

    var selectedTab by rememberSaveable { mutableStateOf(BottomTab.HOME) }
    var showAddItem by remember { mutableStateOf(false) }

    // HomeRoute's "Tambah Barang" CTA button routes through HomeEvent — handle it here.
    LaunchedEffect(Unit) {
        homeVm.events.collect { event ->
            when (event) {
                HomeEvent.ShowAddItemSheet -> {
                    addItemVm.onAction(com.project.tickr.presentation.additem.AddItemAction.Reset)
                    showAddItem = true
                }
                else -> Unit
            }
        }
    }
    LaunchedEffect(Unit) {
        addItemVm.events.collect { event ->
            when (event) {
                AddItemEvent.Saved -> {
                    showAddItem = false
                    homeVm.onAction(com.project.tickr.presentation.home.HomeAction.Refresh)
                    expiryVm.onAction(com.project.tickr.presentation.expiry.ExpiryAction.Refresh)
                }
                else -> Unit
            }
        }
    }

    Scaffold(
        containerColor = TickrTheme.colors.background,
        bottomBar = {
            TickrBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                // FAB sets state directly — no event channel hop that causes double-click bug.
                onFabClick = {
                    addItemVm.onAction(com.project.tickr.presentation.additem.AddItemAction.Reset)
                    showAddItem = true
                },
            )
        },
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            when (selectedTab) {
                BottomTab.HOME -> HomeRoute(navigator = navigator, viewModel = homeVm)
                BottomTab.EXPIRING -> ExpiryRoute(
                    navigator = navigator,
                    viewModel = expiryVm,
                    onAddItemRequest = {
                        addItemVm.onAction(com.project.tickr.presentation.additem.AddItemAction.Reset)
                        showAddItem = true
                    },
                )
                BottomTab.INSIGHT -> ComingSoonPlaceholder("Wawasan")     // TODO(user): implementasi layar
                BottomTab.PROFILE -> ComingSoonPlaceholder("Profil")      // TODO(user): implementasi layar
            }
        }
    }

    if (showAddItem) {
        AddItemSheet(
            viewModel = addItemVm,
            onDismiss = { showAddItem = false },
        )
    }
}

@Composable
private fun TickrBottomBar(
    selectedTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
    onFabClick: () -> Unit,
) {
    val colors = TickrTheme.colors

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface),
    ) {
        NavigationBar(
            modifier = Modifier.fillMaxWidth(),
            containerColor = colors.surface,
            tonalElevation = 0.dp,
        ) {
            // Kiri: Beranda, Kadaluwarsa
            NavigationBarItem(
                selected = selectedTab == BottomTab.HOME,
                onClick = { onTabSelected(BottomTab.HOME) },
                icon = {
                    Icon(
                        BottomTab.HOME.icon, contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                },
                label = { Text(BottomTab.HOME.label, fontSize = 11.sp) },
                colors = navBarItemColors(colors.primaryBrand, colors.textSecondary),
            )
            NavigationBarItem(
                selected = selectedTab == BottomTab.EXPIRING,
                onClick = { onTabSelected(BottomTab.EXPIRING) },
                icon = {
                    Icon(
                        BottomTab.EXPIRING.icon, contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                },
                label = { Text(BottomTab.EXPIRING.label, fontSize = 11.sp) },
                colors = navBarItemColors(colors.primaryBrand, colors.textSecondary),
            )
            // Slot kosong tengah untuk FAB notch
            NavigationBarItem(
                selected = false,
                onClick = {},
                icon = { Spacer(Modifier.size(56.dp)) },
                label = {},
                enabled = false,
                colors = navBarItemColors(colors.primaryBrand, colors.textSecondary),
            )
            NavigationBarItem(
                selected = selectedTab == BottomTab.INSIGHT,
                onClick = { onTabSelected(BottomTab.INSIGHT) },
                icon = {
                    Icon(
                        BottomTab.INSIGHT.icon, contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                },
                label = { Text(BottomTab.INSIGHT.label, fontSize = 11.sp) },
                colors = navBarItemColors(colors.primaryBrand, colors.textSecondary),
            )
            NavigationBarItem(
                selected = selectedTab == BottomTab.PROFILE,
                onClick = { onTabSelected(BottomTab.PROFILE) },
                icon = {
                    Icon(
                        BottomTab.PROFILE.icon, contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                },
                label = { Text(BottomTab.PROFILE.label, fontSize = 11.sp) },
                colors = navBarItemColors(colors.primaryBrand, colors.textSecondary),
            )
        }

        // FAB notch — menonjol di tengah atas NavigationBar
        FloatingActionButton(
            onClick = onFabClick,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-24).dp)
                .size(56.dp)
                .semantics { contentDescription = "Tambah barang baru" }, // TODO(user): sesuaikan
            shape = CircleShape,
            containerColor = colors.primaryBrand,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 6.dp,
                pressedElevation = 4.dp,
            ),
        ) {
            Icon(
                Icons.Outlined.Add,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Composable
private fun navBarItemColors(selected: Color, unselected: Color) =
    NavigationBarItemDefaults.colors(
        selectedIconColor = selected,
        selectedTextColor = selected,
        unselectedIconColor = unselected,
        unselectedTextColor = unselected,
        indicatorColor = Color.Transparent,
    )

@Composable
private fun ComingSoonPlaceholder(label: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label, // TODO(user): sesuaikan
                style = TickrTheme.typography.sectionTitle,
                color = TickrTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Coming soon", // TODO(user): sesuaikan
                style = TickrTheme.typography.body,
                color = TickrTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}
