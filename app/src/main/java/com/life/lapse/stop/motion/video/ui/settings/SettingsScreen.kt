package com.life.lapse.stop.motion.video.ui.settings

// ✅ ADDED: New imports for sharing
import android.content.Intent
import androidx.compose.ui.platform.LocalContext

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share // ✅ ADDED: Share icon
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.life.lapse.stop.motion.video.data.repository.AppTheme
import com.life.lapse.stop.motion.video.data.repository.ExportQuality
import com.life.lapse.stop.motion.video.ui.theme.LifeLapseTheme
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val uiState by settingsViewModel.uiState.collectAsState()
    // ✅ ADDED: Get the context to use for the share intent
    val context = LocalContext.current

    if (uiState.isThemeDialogOpen) {
        ThemeSelectionDialog(
            currentTheme = uiState.appTheme,
            onThemeSelected = { settingsViewModel.onThemeChanged(it) },
            onDismiss = { settingsViewModel.showThemeDialog(false) }
        )
    }

    if (uiState.isQualityDialogOpen) {
        QualitySelectionDialog(
            currentQuality = uiState.exportQuality,
            onQualitySelected = { settingsViewModel.onQualityChanged(it) },
            onDismiss = { settingsViewModel.showQualityDialog(false) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "GENERAL",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            SettingsItem(
                icon = Icons.Default.Brightness4,
                title = "Theme",
                value = uiState.appTheme.name.replace("_", " ").lowercase()
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            ) {
                settingsViewModel.showThemeDialog(true)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "EXPORT DEFAULTS",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            SettingsItem(
                icon = Icons.Default.HighQuality,
                title = "Default Quality",
                value = when (uiState.exportQuality) {
                    ExportQuality.P720 -> "720p"
                    ExportQuality.P1080 -> "1080p"
                    ExportQuality.P4K -> "4K"
                }
            ) {
                settingsViewModel.showQualityDialog(true)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "ABOUT & SUPPORT",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // ✅ ADDED: Share App item and its logic
            SettingsItem(
                icon = Icons.Default.Share,
                title = "Share App"
            ) {
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "Check out this cool Stop Motion app!\n\n" +
                                "https://play.google.com/store/apps/details?id=${context.packageName}"
                    )
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, null)
                context.startActivity(shareIntent)
            }

            SettingsItem(
                icon = Icons.Default.Feedback,
                title = "Send Feedback"
            ) {
                // TODO: Handle send feedback
            }
            SettingsItem(
                icon = Icons.Default.Security,
                title = "Privacy Policy"
            ) {
                // TODO: Handle privacy policy
            }
        }
    }
}

// ... rest of the file (SettingsItem, Dialogs, Preview) is the same ...

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    value: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (value != null) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ThemeSelectionDialog(
    currentTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Theme") },
        text = {
            Column {
                AppTheme.entries.forEach { theme ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (theme == currentTheme),
                                onClick = { onThemeSelected(theme) }
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (theme == currentTheme),
                            onClick = { onThemeSelected(theme) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = theme.name.replace("_", " ").lowercase()
                            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() })
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun QualitySelectionDialog(
    currentQuality: ExportQuality,
    onQualitySelected: (ExportQuality) -> Unit,
    onDismiss: () -> Unit
) {
    val qualityMap = mapOf(
        ExportQuality.P720 to "720p (HD)",
        ExportQuality.P1080 to "1080p (Full HD)",
        ExportQuality.P4K to "4K (Ultra HD)"
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Export Quality") },
        text = {
            Column {
                ExportQuality.entries.forEach { quality ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (quality == currentQuality),
                                onClick = { onQualitySelected(quality) }
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (quality == currentQuality),
                            onClick = { onQualitySelected(quality) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = qualityMap[quality] ?: "")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun SettingsScreenPreview() {
    LifeLapseTheme {
        SettingsScreen(onNavigateBack = {})
    }
}