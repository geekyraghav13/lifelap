package com.life.lapse.stop.motion.video.ui.editor

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import coil.compose.AsyncImage
import com.life.lapse.stop.motion.video.ui.theme.Pink_Primary
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    onNavigateBack: () -> Unit,
    editorViewModel: EditorViewModel,
    onNavigateToCamera: () -> Unit
) {
    val uiState by editorViewModel.uiState.collectAsState()
    val context = LocalContext.current

    SystemUiController(isFullScreen = uiState.isFullScreen)

    LaunchedEffect(uiState.exportResult) {
        uiState.exportResult?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            editorViewModel.clearExportResult()
        }
    }

    if (uiState.isFullScreen) {
        /** ✅ FULLSCREEN MODE UI **/
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            AdvancedVideoPlayer(
                frames = uiState.frames,
                currentFrameIndex = uiState.currentFrameIndex,
                isPlaying = uiState.isPlaying,
                isFullScreen = true,
                showControls = uiState.showFullScreenControls,
                onPlayerTap = { editorViewModel.onFullScreenPlayerTap() },
                onTogglePlayback = { editorViewModel.onTogglePlayback() },
                onSeek = { editorViewModel.onSeekToFrame(it) },
                onToggleFullScreen = { editorViewModel.onToggleFullScreen() }
            )
        }
    } else {
        /** ✅ NORMAL MODE UI **/
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
                            onClick = { editorViewModel.onExportClicked(context) },
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
                /** ✅ Video Player in normal mode **/
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16 / 9f)
                ) {
                    AdvancedVideoPlayer(
                        frames = uiState.frames,
                        currentFrameIndex = uiState.currentFrameIndex,
                        isPlaying = uiState.isPlaying,
                        isFullScreen = false,
                        showControls = true,
                        onPlayerTap = { editorViewModel.onFullScreenPlayerTap() },
                        onTogglePlayback = { editorViewModel.onTogglePlayback() },
                        onSeek = { editorViewModel.onSeekToFrame(it) },
                        onToggleFullScreen = { editorViewModel.onToggleFullScreen() }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                /** ✅ Timeline Section **/
                Text("Timeline", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                FrameTimeline(
                    frames = uiState.frames,
                    selectedFrame = uiState.selectedFrame,
                    onFrameSelected = { frame -> editorViewModel.onFrameSelected(frame) },
                    onMove = { from, to -> editorViewModel.onMoveFrame(from, to) }
                )

                Spacer(modifier = Modifier.height(24.dp))

                /** ✅ Playback Speed Controls **/
                PlaybackSpeedControls(
                    selectedSpeed = uiState.selectedSpeed,
                    onSpeedSelected = { editorViewModel.onSpeedSelected(it) }
                )

                Spacer(modifier = Modifier.height(24.dp))

                /** ✅ Frame Actions **/
                FrameActionButtons(
                    onDelete = { editorViewModel.onDeleteFrameClicked() },
                    onDuplicate = { editorViewModel.onDuplicateFrameClicked() },
                    onAdd = onNavigateToCamera,
                    deleteEnabled = uiState.selectedFrame != null,
                    duplicateEnabled = uiState.selectedFrame != null
                )
            }
        }
    }
}

@Composable
fun AdvancedVideoPlayer(
    frames: List<Frame>,
    currentFrameIndex: Int,
    isPlaying: Boolean,
    isFullScreen: Boolean,
    showControls: Boolean,
    onPlayerTap: () -> Unit,
    onTogglePlayback: () -> Unit,
    onSeek: (Int) -> Unit,
    onToggleFullScreen: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16 / 9f)
            .clip(RoundedCornerShape(if (isFullScreen) 0.dp else 16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        onTogglePlayback() // Double tap → Play/Pause
                    },
                    onTap = {
                        if (isFullScreen) {
                            onPlayerTap() // Single tap → Show controls
                        } else {
                            onTogglePlayback() // Normal mode → Tap toggles playback
                        }
                    }
                )
            }
    ) {
        if (frames.isNotEmpty()) {
            AsyncImage(
                model = frames.getOrNull(currentFrameIndex)?.uri,
                contentDescription = "Current frame",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        AnimatedVisibility(
            visible = if (isFullScreen) showControls else true,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                            startY = 300f
                        )
                    )
            )

            // Play button center overlay
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (!isPlaying) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }

            // Bottom controls
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                if (frames.size > 1) {
                    Slider(
                        value = currentFrameIndex.toFloat(),
                        onValueChange = { onSeek(it.toInt()) },
                        valueRange = 0f..(frames.size - 1).toFloat(),
                        steps = frames.size - 2,
                        colors = SliderDefaults.colors(
                            thumbColor = Pink_Primary,
                            activeTrackColor = Pink_Primary,
                            inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                        )
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onTogglePlayback) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White
                        )
                    }
                    if (frames.isNotEmpty()) {
                        Text(
                            text = "${currentFrameIndex + 1} / ${frames.size}",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                    IconButton(onClick = onToggleFullScreen) {
                        Icon(
                            imageVector = if (isFullScreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                            contentDescription = "Fullscreen",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FrameTimeline(
    frames: List<Frame>,
    selectedFrame: Frame?,
    onFrameSelected: (Frame) -> Unit,
    onMove: (Int, Int) -> Unit
) {
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
        val state = rememberReorderableLazyListState(onMove = { from, to ->
            onMove(from.index, to.index)
        })
        LazyRow(
            state = state.listState,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .reorderable(state)
                .detectReorderAfterLongPress(state)
        ) {
            items(frames, key = { it.id }) { frame ->
                ReorderableItem(state, key = frame) {
                    Box(
                        modifier = Modifier
                            .size(width = 80.dp, height = 60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onFrameSelected(frame) }
                            .background(MaterialTheme.colorScheme.surface)
                            .border(
                                2.dp,
                                if (frame == selectedFrame) Pink_Primary else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                    ) {
                        AsyncImage(
                            model = frame.uri,
                            contentDescription = "Captured frame",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
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
fun FrameActionButtons(
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    onAdd: () -> Unit,
    deleteEnabled: Boolean,
    duplicateEnabled: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ActionButton(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Delete,
            text = "Delete Frame",
            onClick = onDelete,
            enabled = deleteEnabled
        )
        ActionButton(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.ContentCopy,
            text = "Duplicate",
            onClick = onDuplicate,
            enabled = duplicateEnabled
        )
        ActionButton(modifier = Modifier.weight(1f), icon = Icons.Default.Add, text = "Add Frame", onClick = onAdd)
    }
}

@Composable
fun ActionButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tint =
                if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.38f
                )
            Icon(imageVector = icon, contentDescription = text, tint = tint)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text,
                color = tint,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SystemUiController(isFullScreen: Boolean) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        val window = (view.context as Activity).window
        val insetsController = WindowCompat.getInsetsController(window, view)
        LaunchedEffect(isFullScreen) {
            if (isFullScreen) {
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
                insetsController.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }
}
