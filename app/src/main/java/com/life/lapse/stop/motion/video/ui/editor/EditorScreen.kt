package com.life.lapse.stop.motion.video.ui.editor

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.life.lapse.stop.motion.video.ui.theme.LifeLapseTheme
import com.life.lapse.stop.motion.video.ui.theme.Pink_Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    onNavigateBack: () -> Unit,
    editorViewModel: EditorViewModel // Receives the shared ViewModel
) {
    val uiState by editorViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("My Project", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = { /* TODO: Handle Export */ },
                        colors = ButtonDefaults.buttonColors(containerColor = Pink_Primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Export")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            VideoPlayerPlaceholder()
            Spacer(modifier = Modifier.height(24.dp))

            Text("Timeline", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            FrameTimeline(
                frames = uiState.frames, // Use real frames from the ViewModel
                selectedFrameUri = uiState.selectedFrameUri
            )
            Spacer(modifier = Modifier.height(24.dp))

            PlaybackSpeedControls(
                selectedSpeed = uiState.selectedSpeed,
                onSpeedSelected = { editorViewModel.onSpeedSelected(it) }
            )
            Spacer(modifier = Modifier.height(24.dp))

            FrameActionButtons(
                onDelete = { editorViewModel.onDeleteFrameClicked() },
                onDuplicate = { editorViewModel.onDuplicateFrameClicked() },
                onAdd = { /* TODO: Navigate back to camera */ }
            )
        }
    }
}

@Composable
fun VideoPlayerPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16 / 9f)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Play Video",
            tint = Color.White,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.4f))
                .padding(8.dp)
        )
    }
}

@Composable
fun FrameTimeline(frames: List<Uri>, selectedFrameUri: Uri?) {
    if (frames.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No frames captured yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(frames) { frameUri ->
                Box(
                    modifier = Modifier
                        .size(width = 80.dp, height = 60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(
                            2.dp,
                            // Highlight the first frame by default, or the selected one
                            if (frameUri == selectedFrameUri || (selectedFrameUri == null && frames.indexOf(frameUri) == 0)) Pink_Primary else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                ) {
                    // Use AsyncImage to load the real photo
                    AsyncImage(
                        model = frameUri,
                        contentDescription = "Captured frame",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

@Composable
fun PlaybackSpeedControls(selectedSpeed: Float, onSpeedSelected: (Float) -> Unit) {
    Column {
        Text("Playback Speed", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val speeds = listOf(0.5f, 1f, 2f)
            speeds.forEach { speed ->
                Button(
                    onClick = { onSpeedSelected(speed) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedSpeed == speed) Pink_Primary else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Text("${speed}x")
                }
            }
        }
    }
}

@Composable
fun FrameActionButtons(onDelete: () -> Unit, onDuplicate: () -> Unit, onAdd: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ActionButton(modifier = Modifier.weight(1f), icon = Icons.Default.Delete, text = "Delete Frame", onClick = onDelete)
        ActionButton(modifier = Modifier.weight(1f), icon = Icons.Default.ContentCopy, text = "Duplicate", onClick = onDuplicate)
        ActionButton(modifier = Modifier.weight(1f), icon = Icons.Default.Add, text = "Add Frame", onClick = onAdd)
    }
}

@Composable
fun ActionButton(modifier: Modifier = Modifier, icon: ImageVector, text: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = text, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun EditorScreenPreview() {
    LifeLapseTheme {
        // Previewing this screen is complex as it relies on a shared ViewModel.
        // For a static preview, you could pass a mock ViewModel.
    }
}
