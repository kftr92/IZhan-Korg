package com.example

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.media.midi.MidiDeviceInfo
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = true, dynamicColor = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF030816)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF020712),
                                        Color(0xFF09142A),
                                        Color(0xFF0E1E3D),
                                        Color(0xFF040A1A)
                                    )
                                )
                            )
                    ) {
                        FireSparksBackgroundOverlay()
                        MidiControllerApp(viewModel = viewModel)
                        FieryKeyboardSplashScreen()
                    }
                }
            }
        }
    }
}

@Composable
fun FireSparksBackgroundOverlay() {
    val infiniteTransition = rememberInfiniteTransition(label = "fireSparksAnimation")
    val sparkAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sparkProgress"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val totalSparks = 48

        for (i in 0 until totalSparks) {
            val seedX = (i * 37 + 13) % 100 / 100f
            val speed = 0.35f + ((i % 7) * 0.15f)
            val sizeFactor = (i % 4) + 1.5f
            val progress = (sparkAnim * speed + (i * 0.021f)) % 1f

            val xSway = (kotlin.math.sin((progress * Math.PI * 2) + i) * 16f).toFloat()
            val xPos = (width * seedX) + xSway
            val yPos = height - (height * progress)
            val alpha = (1f - progress).coerceIn(0.12f, 0.92f)

            val sparkColor = when (i % 3) {
                0 -> Color(0xFFFF6D00) // Vibrant Fiery Orange
                1 -> Color(0xFFFF9E00) // Glowing Amber Orange
                else -> Color(0xFFFF3D00) // Deep Bright Flame Orange
            }

            drawCircle(
                color = sparkColor.copy(alpha = alpha),
                radius = sizeFactor.dp.toPx(),
                center = Offset(xPos, yPos)
            )

            if (i % 2 == 0) {
                drawCircle(
                    color = sparkColor.copy(alpha = alpha * 0.3f),
                    radius = (sizeFactor * 2.5f).dp.toPx(),
                    center = Offset(xPos, yPos)
                )
            }
        }
    }
}

@Composable
fun FieryKeyboardSplashScreen() {
    var isVisible by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2600)
        isVisible = false
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut(animationSpec = tween(600))
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "firePulse")
        val flameScale by infiniteTransition.animateFloat(
            initialValue = 0.96f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(900, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "flameScale"
        )
        val flameAlpha by infiniteTransition.animateFloat(
            initialValue = 0.75f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(700, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "flameAlpha"
        )
        val emberOffset by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1400, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "emberOffset"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF030816))
                .clickable { isVisible = false },
            contentAlignment = Alignment.Center
        ) {
            // Rising fiery ember particles on background canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val numEmbers = 36
                for (i in 0 until numEmbers) {
                    val startX = (canvasWidth * ((i * 31) % 100) / 100f)
                    val speed = 0.5f + ((i % 5) * 0.25f)
                    val currentY = canvasHeight - ((canvasHeight * ((emberOffset * speed + (i * 0.035f)) % 1f)))
                    val radius = (3 + (i % 4) * 2).dp.toPx()
                    val emberAlpha = (1f - (currentY / canvasHeight)).coerceIn(0.2f, 0.9f)
                    val color = if (i % 2 == 0) Color(0xFFFF6D00) else Color(0xFFFF9E00)
                    drawCircle(
                        color = color.copy(alpha = emberAlpha),
                        radius = radius,
                        center = Offset(startX, currentY)
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(160.dp)
                        .scale(flameScale)
                        .border(
                            width = 2.dp,
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFFFF6D00), Color(0xFFFF3D00), Color.Transparent)
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF08142A))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_keyboard_fire_splash_1785346525240),
                        contentDescription = "Fiery Keyboard Animation",
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { alpha = flameAlpha },
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "IZHAN KORG",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp,
                    style = TextStyle(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFFFCC80),
                                Color(0xFFFF6D00),
                                Color(0xFFFF3D00),
                                Color(0xFFFFB74D)
                            )
                        )
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "KORG KROME CONTROLLER",
                    color = Color(0xFFFF9E00),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                LinearProgressIndicator(
                    modifier = Modifier
                        .width(140.dp)
                        .height(3.dp)
                        .clip(CircleShape),
                    color = Color(0xFFFF6D00),
                    trackColor = Color(0xFF0B1936)
                )
            }
        }
    }
}

@Composable
fun SoundSlotCard(
    preset: SoundPreset,
    index: Int,
    isSelected: Boolean,
    isEditMode: Boolean,
    isMidiInputTriggered: Boolean = false,
    transpose: Int = 0,
    onPress: () -> Unit = {},
    onRelease: () -> Unit = {},
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    var hasBeenPressed by remember { mutableStateOf(false) }

    LaunchedEffect(isPressed, isEditMode) {
        if (isEditMode) return@LaunchedEffect
        if (isPressed) {
            hasBeenPressed = true
            onPress()
        } else if (hasBeenPressed) {
            onRelease()
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else if (isMidiInputTriggered) 1.06f else if (isSelected) 1.03f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "slotScale"
    )

    val gradient = if (isMidiInputTriggered) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF00E5FF), // Bright Neon Cyan RX Burst
                Color(0xFFFF6D00), // Vibrant Korg Orange
                Color(0xFF040A18)  // Dark Deep Blue
            )
        )
    } else if (isPressed) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFF6D00), // Vibrant Fiery Orange
                Color(0xFFFF3D00), // Deep Orange Flame
                Color(0xFF0B1C3E)  // Dark Deep Blue
            )
        )
    } else if (isSelected) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFF7700), // Glowing Orange
                Color(0xFFFF3D00), // Intense Orange
                Color(0xFF0A1B38), // Dark Deep Navy
                Color(0xFF040B1A)  // Dark Deep Blue Surface
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF091428), // Dark Deep Navy
                Color(0xFF0E1E3A), // Deep Midnight Blue
                Color(0xFF040A17)  // Deep Dark Blue
            )
        )
    }

    val borderColor = if (isMidiInputTriggered) Color(0xFF00E5FF) else if (isPressed) Color(0xFFFF9E00) else if (isSelected) Color(0xFFFF6D00) else Color(0xFF6B7280)

    Surface(
        modifier = modifier
            .padding(1.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = onClick
            )
            .border(1.5.dp, if (isEditMode) Color(0xFFFF6D00) else borderColor, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        color = Color.Transparent,
        contentColor = if (isSelected || isPressed || isMidiInputTriggered) Color.White else Color.LightGray
    ) {
        Box(
            modifier = Modifier
                .background(gradient)
                .fillMaxWidth()
                .height(92.dp)
                .padding(horizontal = 3.dp, vertical = 4.dp)
        ) {
            // Slot index badge
            Text(
                text = "${index + 1}",
                color = if (isMidiInputTriggered) Color.White else if (isSelected || isPressed) Color(0xFFFFCC80) else Color(0xFF64748B),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.TopStart)
            )

            // MIDI RX Active Tag / Edit Indicator
            if (isMidiInputTriggered) {
                Surface(
                    color = Color(0xFF00E5FF),
                    shape = RoundedCornerShape(3.dp),
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Text(
                        text = "RX",
                        color = Color.Black,
                        fontSize = 7.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 3.dp, vertical = 0.5.dp)
                    )
                }
            } else if (isEditMode) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Mode Active",
                    tint = Color(0xFFFF6D00),
                    modifier = Modifier
                        .size(13.dp)
                        .align(Alignment.TopEnd)
                )
            }

            // Sound Name
            Text(
                text = preset.name,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                fontSize = 11.sp,
                lineHeight = 13.sp,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 1.dp)
            )

            // Bottom trigger & output note indicator
            val triggerLabel = if (preset.triggerNote >= 0) "IN:${preset.triggerNote}" else "IN:${36 + index}"
            val baseOutNote = if (preset.outputNote in 0..127) {
                preset.outputNote
            } else if (preset.buttonType == "NOTE") {
                (60 + index).coerceIn(0, 127)
            } else {
                -1
            }
            val effectiveOutNote = if (baseOutNote in 0..127) {
                (baseOutNote - transpose).coerceIn(0, 127)
            } else {
                -1
            }
            val notes = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
            val outLabel = if (effectiveOutNote in 0..127) {
                "${notes[effectiveOutNote % 12]}${(effectiveOutNote / 12) - 1}"
            } else null

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = triggerLabel,
                    fontSize = 7.5.sp,
                    maxLines = 1,
                    color = if (isMidiInputTriggered) Color(0xFF00E5FF) else Color(0xFF8090A8),
                    fontWeight = FontWeight.Bold
                )
                if (outLabel != null) {
                    Text(
                        text = outLabel,
                        fontSize = 7.5.sp,
                        maxLines = 1,
                        color = Color(0xFFFF9E00),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun SmallGlossyButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false,
    fontSize: TextUnit = 10.sp,
    horizontalPadding: Dp = 6.dp,
    verticalPadding: Dp = 3.dp,
    icon: @Composable (() -> Unit)? = null
) {
    val gradient = if (isSelected || isPrimary) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFF6D00), // Fiery Orange
                Color(0xFFFF3D00), // Intense Orange
                Color(0xFF0A1938)  // Dark Deep Blue
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0A162D), // Dark Deep Blue
                Color(0xFF050B17)  // Deep Dark Surface
            )
        )
    }

    val borderColor = if (isSelected || isPrimary) Color(0xFFFF6D00) else Color(0xFF152A50)

    Surface(
        modifier = modifier
            .padding(1.dp)
            .clickable { onClick() }
            .border(1.dp, borderColor, RoundedCornerShape(6.dp)),
        shape = RoundedCornerShape(6.dp),
        color = Color.Transparent,
        contentColor = Color.White
    ) {
        Box(
            modifier = Modifier
                .background(gradient)
                .padding(
                    horizontal = if (text.isEmpty()) (horizontalPadding - 2.dp).coerceAtLeast(3.dp) else horizontalPadding,
                    vertical = verticalPadding
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    icon()
                }
                if (text.isNotEmpty()) {
                    if (icon != null) Spacer(Modifier.width(3.dp))
                    Text(text, fontWeight = FontWeight.Bold, fontSize = fontSize, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MidiControllerApp(viewModel: MainViewModel) {
    val devices by viewModel.midiController.devices.collectAsStateWithLifecycle()
    val selectedDevice by viewModel.midiController.selectedDevice.collectAsStateWithLifecycle()
    val selectedInputDevice by viewModel.selectedInputDevice.collectAsStateWithLifecycle()
    val selectedOutputDevice by viewModel.selectedOutputDevice.collectAsStateWithLifecycle()
    val connectionStatus by viewModel.midiController.connectionStatus.collectAsStateWithLifecycle()
    val statusMessage by viewModel.midiController.statusMessage.collectAsStateWithLifecycle()
    val trafficLogs by viewModel.midiController.trafficLogs.collectAsStateWithLifecycle()
    val transpose by viewModel.transpose.collectAsStateWithLifecycle()
    val soundPresets by viewModel.soundPresets.collectAsStateWithLifecycle()
    val selectedPresetIndex by viewModel.selectedPresetIndex.collectAsStateWithLifecycle()
    val savedConfigs by viewModel.savedConfigurations.collectAsStateWithLifecycle()
    val currentConfigName by viewModel.currentConfigName.collectAsStateWithLifecycle()
    val currentDevicePatch by viewModel.midiController.currentDevicePatch.collectAsStateWithLifecycle()
    val activeInputTriggerPadIndex by viewModel.activeInputTriggerPadIndex.collectAsStateWithLifecycle()
    val lastMidiInputInfo by viewModel.lastMidiInputInfo.collectAsStateWithLifecycle()
    val midiLearningPadIndex by viewModel.midiLearningPadIndex.collectAsStateWithLifecycle()
    val scannedBleDevices by viewModel.scannedBleDevices.collectAsStateWithLifecycle()
    val isScanningBle by viewModel.isScanningBle.collectAsStateWithLifecycle()

    var editPresetIndex by remember { mutableStateOf<Int?>(null) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var showBleMidiDialog by remember { mutableStateOf(false) }
    var newConfigName by remember { mutableStateOf("") }
    var showConfigMenu by remember { mutableStateOf(false) }
    var isEditMode by remember { mutableStateOf(false) }
    var isFullscreen by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val activity = context as? Activity

    val blePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions[Manifest.permission.BLUETOOTH_SCAN] == true &&
            permissions[Manifest.permission.BLUETOOTH_CONNECT] == true
        } else {
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        }
        if (granted) {
            viewModel.startBleScan()
        }
    }

    fun requestBleScanWithPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val hasScan = ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
            val hasConnect = ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
            if (hasScan && hasConnect) {
                viewModel.startBleScan()
            } else {
                blePermissionLauncher.launch(arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
                ))
            }
        } else {
            val hasLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            if (hasLocation) {
                viewModel.startBleScan()
            } else {
                blePermissionLauncher.launch(arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ))
            }
        }
    }

    fun toggleFullscreen() {
        activity?.window?.let { window ->
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            if (!isFullscreen) {
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
                insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                isFullscreen = true
            } else {
                insetsController.show(WindowInsetsCompat.Type.systemBars())
                isFullscreen = false
            }
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    val json = viewModel.exportConfigJson()
                    outputStream.write(json.toByteArray(Charsets.UTF_8))
                }
                Toast.makeText(context, "Setup exported successfully!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            try {
                val json = context.contentResolver.openInputStream(it)?.use { inputStream ->
                    inputStream.bufferedReader().readText()
                }
                if (!json.isNullOrBlank()) {
                    val importedName = viewModel.importConfigFromJson(json)
                    if (importedName != null) {
                        Toast.makeText(context, "Imported '$importedName' successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Invalid setup JSON format", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Import failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Sound Slot Edit Dialog
    if (editPresetIndex != null) {
        val preset = soundPresets[editPresetIndex!!]
        var name by remember { mutableStateOf(preset.name) }
        var msb by remember { mutableStateOf(preset.msb.toString()) }
        var lsb by remember { mutableStateOf(preset.lsb.toString()) }
        var program by remember { mutableStateOf(preset.program.toString()) }
        var sysexHex by remember { mutableStateOf(preset.sysexHex) }
        var selectedMode by remember { mutableStateOf(preset.mode) } // Prog, Combi, Favourites
        var selectedButtonType by remember(preset.buttonType) { mutableStateOf(if (preset.buttonType.isBlank()) "PGM" else preset.buttonType) } // PGM, NOTE, CC, SX, CUST
        var triggerNoteState by remember(preset.triggerNote) { mutableIntStateOf(preset.triggerNote) }
        var outputNoteState by remember(preset.outputNote) { mutableStateOf(if (preset.outputNote >= 0) preset.outputNote.toString() else "") }
        var outputVelocityState by remember(preset.outputVelocity) { mutableStateOf(preset.outputVelocity.toString()) }
        var midiChannelState by remember(preset.midiChannel) { mutableIntStateOf(preset.midiChannel) }
        val context = androidx.compose.ui.platform.LocalContext.current

        AlertDialog(
            onDismissRequest = { editPresetIndex = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Edit Sound Slot ${editPresetIndex!! + 1}", color = Color.White, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { editPresetIndex = null }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
            },
            containerColor = Color(0xFF08142A),
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Button Name & Button Type row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Button Name", color = Color(0xFFB0BEC5)) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Button Type Chips
                    Column {
                        Text("Button Type", color = Color.Gray, fontSize = 12.sp)
                        Spacer(Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("PGM", "NOTE", "CC", "SX", "CUST").forEach { type ->
                                val isSelected = selectedButtonType == type
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            selectedButtonType = type
                                            if (type == "NOTE" && outputNoteState.isBlank()) {
                                                outputNoteState = (60 + editPresetIndex!!).toString()
                                            }
                                        }
                                        .border(
                                            1.dp,
                                            if (isSelected) Color(0xFFFF6D00) else Color(0xFF13264A),
                                            RoundedCornerShape(6.dp)
                                        ),
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color.Transparent
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .background(
                                                if (isSelected)
                                                    Brush.verticalGradient(listOf(Color(0xFFFF6D00), Color(0xFF091C3E)))
                                                else
                                                    Brush.verticalGradient(listOf(Color(0xFF081226), Color(0xFF040A18)))
                                            )
                                            .padding(vertical = 8.dp)
                                    ) {
                                        Text(
                                            type,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Divider(color = Color(0xFF13264A))

                    // Mode Selection Options (Stacked Top to Bottom)
                    Column {
                        Text("Mode", color = Color.Gray, fontSize = 11.sp)
                        Spacer(Modifier.height(2.dp))
                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("Prog", "Combi", "Favourites").forEach { modeOption ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(30.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (selectedMode == modeOption) Color(0xFF182D54) else Color(0xFF081226))
                                        .border(
                                            1.dp,
                                            if (selectedMode == modeOption) Color(0xFFFF6D00) else Color.Transparent,
                                            RoundedCornerShape(6.dp)
                                        )
                                        .clickable { selectedMode = modeOption }
                                        .padding(horizontal = 6.dp)
                                ) {
                                    RadioButton(
                                        selected = selectedMode == modeOption,
                                        onClick = { selectedMode = modeOption },
                                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFFF6D00)),
                                        modifier = Modifier.scale(0.75f)
                                    )
                                    Spacer(Modifier.width(2.dp))
                                    Text(
                                        text = modeOption,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = if (selectedMode == modeOption) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }

                    // Bank MSB, LSB, Program fields
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = msb,
                            onValueChange = { msb = it.filter { c -> c.isDigit() } },
                            label = { Text("Bank MSB", color = Color(0xFFB0BEC5)) },
                            modifier = Modifier.weight(1f),
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )
                        OutlinedTextField(
                            value = lsb,
                            onValueChange = { lsb = it.filter { c -> c.isDigit() } },
                            label = { Text("Bank LSB", color = Color(0xFFB0BEC5)) },
                            modifier = Modifier.weight(1f),
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )
                        OutlinedTextField(
                            value = program,
                            onValueChange = { program = it.filter { c -> c.isDigit() } },
                            label = { Text("Program", color = Color(0xFFB0BEC5)) },
                            modifier = Modifier.weight(1f),
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )
                    }

                    if (selectedButtonType == "SX" || selectedButtonType == "CUST") {
                        OutlinedTextField(
                            value = sysexHex,
                            onValueChange = { sysexHex = it },
                            label = { Text("Custom SysEx (Hex e.g. F0 42 30 00 F7)", color = Color(0xFFB0BEC5)) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )
                    }

                    // MIDI Output Note Mapping Card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF040A18), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFF13264A), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text("MIDI Output Note Configuration", color = Color(0xFFFF9E00), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        val parsedOutNote = outputNoteState.toIntOrNull() ?: -1
                        val baseOutNote = if (parsedOutNote in 0..127) parsedOutNote else if (selectedButtonType == "NOTE") (60 + editPresetIndex!!) else -1
                        val effectiveOutNote = if (baseOutNote in 0..127) (baseOutNote - transpose).coerceIn(0, 127) else -1
                        val noteLabel = if (effectiveOutNote in 0..127) {
                            val notesArr = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
                            val oct = (effectiveOutNote / 12) - 1
                            if (transpose != 0) {
                                "Note $effectiveOutNote (${notesArr[effectiveOutNote % 12]}$oct) [Base: $baseOutNote, Trans: ${if (transpose > 0) "+$transpose" else transpose}]"
                            } else {
                                "Note $effectiveOutNote (${notesArr[effectiveOutNote % 12]}$oct)"
                            }
                        } else {
                            "Disabled (Sends PC/SysEx Program Change)"
                        }
                        val channelLabel = if (midiChannelState < 0) "Global Ch" else "Ch ${midiChannelState + 1}"
                        Text(
                            text = "Output Signal: $noteLabel [$channelLabel]",
                            color = Color.White,
                            fontSize = 11.sp
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = outputNoteState,
                                onValueChange = { outputNoteState = it.filter { c -> c.isDigit() } },
                                label = { Text("Out Note (0-127)", color = Color(0xFFB0BEC5), fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                            )
                            OutlinedTextField(
                                value = outputVelocityState,
                                onValueChange = { outputVelocityState = it.filter { c -> c.isDigit() } },
                                label = { Text("Velocity (1-127)", color = Color(0xFFB0BEC5), fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(36 to "C2", 48 to "C3", 60 to "C4", 72 to "C5").forEach { (noteVal, noteName) ->
                                Button(
                                    onClick = { outputNoteState = noteVal.toString() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0E2248)),
                                    shape = RoundedCornerShape(4.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(noteName, fontSize = 10.sp, color = Color.White)
                                }
                            }
                            if (outputNoteState.isNotBlank()) {
                                TextButton(
                                    onClick = { outputNoteState = "" },
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text("Clear Note", fontSize = 10.sp, color = Color.Gray)
                                }
                            }
                        }

                        Spacer(Modifier.height(6.dp))

                        // MIDI Channel Dropdown Selector
                        var channelDropdownExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = if (midiChannelState < 0) "Global / Main Channel" else "Channel ${midiChannelState + 1}",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("MIDI Output Channel", color = Color(0xFFB0BEC5), fontSize = 11.sp) },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = channelDropdownExpanded)
                                },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { channelDropdownExpanded = true }
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable { channelDropdownExpanded = true }
                            )

                            DropdownMenu(
                                expanded = channelDropdownExpanded,
                                onDismissRequest = { channelDropdownExpanded = false },
                                modifier = Modifier.background(Color(0xFF0A1B3B))
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "Global / Main Channel",
                                            color = if (midiChannelState == -1) Color(0xFFFF9E00) else Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = if (midiChannelState == -1) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        midiChannelState = -1
                                        channelDropdownExpanded = false
                                    }
                                )
                                Divider(color = Color(0xFF13264A))
                                for (ch in 0..15) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                "Channel ${ch + 1}",
                                                color = if (midiChannelState == ch) Color(0xFFFF9E00) else Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = if (midiChannelState == ch) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        onClick = {
                                            midiChannelState = ch
                                            channelDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // MIDI Input Control Mapping Card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF040A18), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFF13264A), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text("MIDI Input Control Mapping", color = Color(0xFFFF9E00), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val currentNoteLabel = if (triggerNoteState >= 0) {
                                "Note/CC $triggerNoteState"
                            } else {
                                "Default (Note ${36 + editPresetIndex!!} or PC $program)"
                            }
                            Text(
                                text = "Trigger: $currentNoteLabel",
                                color = Color.White,
                                fontSize = 12.sp
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                val isLearning = (midiLearningPadIndex == editPresetIndex)
                                Button(
                                    onClick = {
                                        if (isLearning) {
                                            viewModel.cancelMidiLearn()
                                        } else {
                                            viewModel.startMidiLearn(editPresetIndex!!)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isLearning) Color(0xFFFF3D00) else Color(0xFF0E2248)
                                    ),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (isLearning) "Press Key / Pad..." else "MIDI LEARN",
                                        fontSize = 10.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                if (triggerNoteState >= 0) {
                                    TextButton(
                                        onClick = { triggerNoteState = -1 },
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text("Reset", fontSize = 10.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }

                    // Action Buttons Row: GET CURRENT PATCH, DELETE, & SAVE
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                val currentPatch = viewModel.midiController.currentDevicePatch.value
                                if (currentPatch != null) {
                                    msb = currentPatch.msb.toString()
                                    lsb = currentPatch.lsb.toString()
                                    program = currentPatch.program.toString()
                                    selectedMode = currentPatch.mode
                                    if (name.isBlank() || name.startsWith("Sound ")) {
                                        name = "${currentPatch.mode} ${currentPatch.program}"
                                    }
                                    android.widget.Toast.makeText(
                                        context,
                                        "Captured [${currentPatch.mode}]: MSB ${currentPatch.msb}, LSB ${currentPatch.lsb}, PC ${currentPatch.program}",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    android.widget.Toast.makeText(
                                        context,
                                        "No MIDI patch signal received yet. Change a program on your synth first.",
                                        android.widget.Toast.LENGTH_LONG
                                    ).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0E2248)),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("GET CURRENT PATCH", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1)
                        }

                        if (soundPresets.size > 1) {
                            Button(
                                onClick = {
                                    viewModel.deleteSoundPreset(editPresetIndex!!)
                                    editPresetIndex = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }

                        Button(
                            onClick = {
                                val parsedOutNote = outputNoteState.toIntOrNull() ?: -1
                                val parsedOutVel = outputVelocityState.toIntOrNull() ?: 100
                                val updatedPreset = SoundPreset(
                                    name = name.ifBlank { "Sound ${editPresetIndex!! + 1}" },
                                    msb = msb.toIntOrNull() ?: 0,
                                    lsb = lsb.toIntOrNull() ?: 0,
                                    program = program.toIntOrNull() ?: 0,
                                    sysexHex = sysexHex.trim(),
                                    mode = selectedMode,
                                    triggerNote = triggerNoteState,
                                    outputNote = parsedOutNote,
                                    outputVelocity = parsedOutVel.coerceIn(1, 127),
                                    buttonType = selectedButtonType,
                                    midiChannel = midiChannelState
                                )
                                viewModel.updatePreset(editPresetIndex!!, updatedPreset)
                                editPresetIndex = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6D00)),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text("SAVE", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {}
        )
    }

    // Save Configuration Dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save Setlist Setup", color = Color.White) },
            containerColor = Color(0xFF08142A),
            text = {
                Column {
                    Text("Enter a name for this setlist / sound setup:", color = Color.LightGray, fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newConfigName,
                        onValueChange = { newConfigName = it },
                        label = { Text("Setup Name", color = Color(0xFFB0BEC5)) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveConfiguration(newConfigName)
                        showSaveDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6D00))
                ) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    // BLE MIDI Connection Dialog
    if (showBleMidiDialog) {
        BleMidiDialog(
            scannedDevices = scannedBleDevices,
            isScanning = isScanningBle,
            onStartScan = { requestBleScanWithPermission() },
            onStopScan = { viewModel.stopBleScan() },
            onConnectDevice = { device, asInput, asOutput ->
                viewModel.connectBleDevice(device, asInput, asOutput)
            },
            onDisconnectDevice = { device ->
                viewModel.disconnectBleDevice(device)
            },
            onDismiss = {
                viewModel.stopBleScan()
                showBleMidiDialog = false
            }
        )
    }

    val topPadding = if (isFullscreen) 4.dp else 24.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = topPadding, bottom = 4.dp, start = 3.dp, end = 3.dp)
    ) {
        // Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Configuration Selector & Sound Slot Add
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Config Dropdown Button (Hamburger Menu)
                Box {
                    SmallGlossyButton(
                        text = "",
                        isSelected = true,
                        onClick = { showConfigMenu = true },
                        horizontalPadding = 5.dp,
                        verticalPadding = 3.dp,
                        icon = { Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White, modifier = Modifier.size(13.dp)) }
                    )

                    DropdownMenu(
                        expanded = showConfigMenu,
                        onDismissRequest = { showConfigMenu = false },
                        modifier = Modifier.background(Color(0xFF08142A)).border(1.dp, Color(0xFF13264A))
                    ) {
                        savedConfigs.forEach { configName ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            configName,
                                            color = if (configName == currentConfigName) Color(0xFFFF6D00) else Color.White,
                                            fontWeight = if (configName == currentConfigName) FontWeight.Bold else FontWeight.Normal
                                        )
                                        if (configName != "Default Setup") {
                                            IconButton(
                                                onClick = { viewModel.deleteConfiguration(configName) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFF6D00), modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                },
                                onClick = {
                                    viewModel.loadConfiguration(configName)
                                    showConfigMenu = false
                                }
                            )
                        }
                        HorizontalDivider(color = Color(0xFF13264A))
                        DropdownMenuItem(
                            text = { Text("Export Setup (.json)", color = Color(0xFF00E5FF), fontSize = 12.sp) },
                            onClick = {
                                showConfigMenu = false
                                exportLauncher.launch("${currentConfigName}.json")
                            },
                            leadingIcon = { Icon(Icons.Default.FileUpload, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(16.dp)) }
                        )
                        DropdownMenuItem(
                            text = { Text("Import Setup (.json)", color = Color(0xFFFF6D00), fontSize = 12.sp) },
                            onClick = {
                                showConfigMenu = false
                                importLauncher.launch(arrayOf("application/json", "*/*"))
                            },
                            leadingIcon = { Icon(Icons.Default.FileDownload, contentDescription = null, tint = Color(0xFFFF6D00), modifier = Modifier.size(16.dp)) }
                        )
                    }
                }

                // Save Config Button
                SmallGlossyButton(
                    text = "Save",
                    isSelected = false,
                    onClick = {
                        newConfigName = currentConfigName
                        showSaveDialog = true
                    },
                    fontSize = 10.sp,
                    horizontalPadding = 5.dp,
                    verticalPadding = 3.dp,
                    icon = { Icon(Icons.Default.Save, contentDescription = "Save", tint = Color.White, modifier = Modifier.size(12.dp)) }
                )

                // Add Sound Slot Button
                SmallGlossyButton(
                    text = "+ Slot",
                    isSelected = false,
                    onClick = { viewModel.addSoundPreset() },
                    fontSize = 10.sp,
                    horizontalPadding = 5.dp,
                    verticalPadding = 3.dp,
                    icon = { Icon(Icons.Default.Add, contentDescription = "Add Sound Slot", tint = Color.White, modifier = Modifier.size(12.dp)) }
                )
            }

            // Transpose, MIDI & Fullscreen Control
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Edit Toggle Button
                SmallGlossyButton(
                    text = "Edit",
                    isSelected = isEditMode,
                    onClick = { isEditMode = !isEditMode },
                    fontSize = 10.sp,
                    horizontalPadding = 5.dp,
                    verticalPadding = 3.dp,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Toggle Edit Mode",
                            tint = if (isEditMode) Color(0xFFFF6D00) else Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                )

                val noteNames = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
                val currentNote = noteNames[(transpose % 12 + 12) % 12]
                val transposeLabel = if (transpose == 0) "$currentNote (0)" else if (transpose > 0) "$currentNote (+$transpose)" else "$currentNote ($transpose)"

                SmallGlossyButton(
                    text = "-",
                    isSelected = false,
                    onClick = { viewModel.setTranspose(transpose - 1) },
                    modifier = Modifier.width(56.dp).height(36.dp),
                    fontSize = 20.sp,
                    horizontalPadding = 6.dp,
                    verticalPadding = 2.dp
                )
                SmallGlossyButton(
                    text = transposeLabel,
                    isSelected = true,
                    onClick = { viewModel.setTranspose(0) },
                    modifier = Modifier.width(68.dp).height(36.dp),
                    fontSize = 11.5.sp,
                    horizontalPadding = 4.dp,
                    verticalPadding = 2.dp
                )
                SmallGlossyButton(
                    text = "+",
                    isSelected = false,
                    onClick = { viewModel.setTranspose(transpose + 1) },
                    modifier = Modifier.width(56.dp).height(36.dp),
                    fontSize = 20.sp,
                    horizontalPadding = 6.dp,
                    verticalPadding = 2.dp
                )

                // BLE MIDI Quick Action Button
                val hasBleConnected = (selectedInputDevice?.type == MidiDeviceInfo.TYPE_BLUETOOTH) ||
                        (selectedOutputDevice?.type == MidiDeviceInfo.TYPE_BLUETOOTH) ||
                        scannedBleDevices.any { it.isConnected }

                SmallGlossyButton(
                    text = "BLE",
                    isSelected = hasBleConnected,
                    onClick = {
                        showBleMidiDialog = true
                        requestBleScanWithPermission()
                    },
                    fontSize = 10.sp,
                    horizontalPadding = 5.dp,
                    verticalPadding = 3.dp,
                    icon = {
                        Icon(
                            imageVector = if (hasBleConnected) Icons.Default.BluetoothConnected else if (isScanningBle) Icons.Default.BluetoothSearching else Icons.Default.Bluetooth,
                            contentDescription = "BLE MIDI",
                            tint = if (hasBleConnected) Color(0xFF00E676) else if (isScanningBle) Color(0xFF00E5FF) else Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                )

                // Dual MIDI IN / MIDI OUT Device selection
                var expandedInput by remember { mutableStateOf(false) }
                val isInputConnected = selectedInputDevice != null
                val inputStatusColor = if (isInputConnected) Color(0xFF00E676) else Color(0xFFFF1744)

                ExposedDropdownMenuBox(
                    expanded = expandedInput,
                    onExpandedChange = { expandedInput = !expandedInput },
                    modifier = Modifier.width(112.dp)
                ) {
                    val inputName = selectedInputDevice?.properties?.getString(MidiDeviceInfo.PROPERTY_NAME)
                        ?: selectedInputDevice?.properties?.getString(MidiDeviceInfo.PROPERTY_PRODUCT)
                        ?: "None"
                    OutlinedTextField(
                        value = "IN: $inputName",
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 9.5.sp, color = inputStatusColor, fontWeight = FontWeight.Bold),
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .padding(start = 4.dp)
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(inputStatusColor)
                            )
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedInput) },
                        modifier = Modifier
                            .menuAnchor()
                            .height(36.dp),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                            focusedBorderColor = inputStatusColor,
                            unfocusedBorderColor = inputStatusColor,
                            focusedContainerColor = inputStatusColor.copy(alpha = 0.12f),
                            unfocusedContainerColor = inputStatusColor.copy(alpha = 0.08f),
                            focusedTrailingIconColor = inputStatusColor,
                            unfocusedTrailingIconColor = inputStatusColor
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expandedInput,
                        onDismissRequest = { expandedInput = false },
                        modifier = Modifier
                            .background(Color(0xFF08142A))
                            .border(1.dp, inputStatusColor)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.BluetoothSearching, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Scan / Connect BLE...", color = Color(0xFF00E5FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            },
                            onClick = {
                                expandedInput = false
                                showBleMidiDialog = true
                                requestBleScanWithPermission()
                            }
                        )
                        HorizontalDivider(color = Color(0xFF13264A))
                        if (devices.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No MIDI devices connected", color = Color.Gray, fontSize = 11.sp) },
                                onClick = { expandedInput = false }
                            )
                        } else {
                            DropdownMenuItem(
                                text = { Text("Disconnect Input", color = Color.Gray, fontSize = 11.sp) },
                                onClick = {
                                    viewModel.midiController.selectInputDevice(null)
                                    expandedInput = false
                                }
                            )
                            devices.forEach { device ->
                                val isBle = (device.type == MidiDeviceInfo.TYPE_BLUETOOTH)
                                val name = device.properties.getString(MidiDeviceInfo.PROPERTY_NAME)
                                    ?: device.properties.getString(MidiDeviceInfo.PROPERTY_PRODUCT)
                                    ?: "Device ${device.id}"
                                val displayName = if (isBle) "⚡ [BLE] $name" else name
                                val isThisSelected = device.id == selectedInputDevice?.id
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (isBle) {
                                                Icon(Icons.Default.Bluetooth, contentDescription = null, tint = if (isThisSelected) Color(0xFF00E676) else Color(0xFF00E5FF), modifier = Modifier.size(13.dp))
                                                Spacer(Modifier.width(4.dp))
                                            }
                                            Text(
                                                displayName,
                                                color = if (isThisSelected) Color(0xFF00E676) else if (isBle) Color(0xFF80D8FF) else Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = if (isThisSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    },
                                    onClick = {
                                        viewModel.midiController.selectInputDevice(device)
                                        expandedInput = false
                                    }
                                )
                            }
                        }
                    }
                }

                var expandedOutput by remember { mutableStateOf(false) }
                val isOutputConnected = selectedOutputDevice != null
                val outputStatusColor = if (isOutputConnected) Color(0xFF00E676) else Color(0xFFFF1744)

                ExposedDropdownMenuBox(
                    expanded = expandedOutput,
                    onExpandedChange = { expandedOutput = !expandedOutput },
                    modifier = Modifier.width(112.dp)
                ) {
                    val outputName = selectedOutputDevice?.properties?.getString(MidiDeviceInfo.PROPERTY_NAME)
                        ?: selectedOutputDevice?.properties?.getString(MidiDeviceInfo.PROPERTY_PRODUCT)
                        ?: "None"
                    OutlinedTextField(
                        value = "OUT: $outputName",
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 9.5.sp, color = outputStatusColor, fontWeight = FontWeight.Bold),
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .padding(start = 4.dp)
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(outputStatusColor)
                            )
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedOutput) },
                        modifier = Modifier
                            .menuAnchor()
                            .height(36.dp),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                            focusedBorderColor = outputStatusColor,
                            unfocusedBorderColor = outputStatusColor,
                            focusedContainerColor = outputStatusColor.copy(alpha = 0.12f),
                            unfocusedContainerColor = outputStatusColor.copy(alpha = 0.08f),
                            focusedTrailingIconColor = outputStatusColor,
                            unfocusedTrailingIconColor = outputStatusColor
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expandedOutput,
                        onDismissRequest = { expandedOutput = false },
                        modifier = Modifier
                            .background(Color(0xFF08142A))
                            .border(1.dp, outputStatusColor)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.BluetoothSearching, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Scan / Connect BLE...", color = Color(0xFF00E5FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            },
                            onClick = {
                                expandedOutput = false
                                showBleMidiDialog = true
                                requestBleScanWithPermission()
                            }
                        )
                        HorizontalDivider(color = Color(0xFF13264A))
                        if (devices.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No MIDI devices connected", color = Color.Gray, fontSize = 11.sp) },
                                onClick = { expandedOutput = false }
                            )
                        } else {
                            DropdownMenuItem(
                                text = { Text("Disconnect Output", color = Color.Gray, fontSize = 11.sp) },
                                onClick = {
                                    viewModel.midiController.selectOutputDevice(null)
                                    expandedOutput = false
                                }
                            )
                            devices.forEach { device ->
                                val isBle = (device.type == MidiDeviceInfo.TYPE_BLUETOOTH)
                                val name = device.properties.getString(MidiDeviceInfo.PROPERTY_NAME)
                                    ?: device.properties.getString(MidiDeviceInfo.PROPERTY_PRODUCT)
                                    ?: "Device ${device.id}"
                                val displayName = if (isBle) "⚡ [BLE] $name" else name
                                val isThisSelected = device.id == selectedOutputDevice?.id
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (isBle) {
                                                Icon(Icons.Default.Bluetooth, contentDescription = null, tint = if (isThisSelected) Color(0xFF00E676) else Color(0xFF00E5FF), modifier = Modifier.size(13.dp))
                                                Spacer(Modifier.width(4.dp))
                                            }
                                            Text(
                                                displayName,
                                                color = if (isThisSelected) Color(0xFF00E676) else if (isBle) Color(0xFF80D8FF) else Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = if (isThisSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    },
                                    onClick = {
                                        viewModel.midiController.selectOutputDevice(device)
                                        expandedOutput = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Fullscreen Toggle Button (far right next to MIDI selector)
                SmallGlossyButton(
                    text = "",
                    isSelected = isFullscreen,
                    onClick = { toggleFullscreen() },
                    modifier = Modifier.size(36.dp),
                    horizontalPadding = 0.dp,
                    verticalPadding = 0.dp,
                    icon = {
                        Icon(
                            imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                            contentDescription = if (isFullscreen) "Exit Fullscreen" else "Enter Fullscreen",
                            tint = if (isFullscreen) Color(0xFFFF6D00) else Color.White,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                )
            }
        }

        // Active Sound & Main Screen Pager Setup
        val activePreset = soundPresets.getOrNull(selectedPresetIndex)
        val mainScreenPagerState = rememberPagerState(initialPage = 1, pageCount = { 2 })
        val coroutineScope = rememberCoroutineScope()

        // Page Navigation Switcher Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Page Navigation Button (Ribbon Controller)
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { coroutineScope.launch { mainScreenPagerState.animateScrollToPage(0) } }
                    .border(
                        1.dp,
                        if (mainScreenPagerState.currentPage == 0) Color(0xFFFF6D00) else Color(0xFF13264A),
                        RoundedCornerShape(6.dp)
                    ),
                shape = RoundedCornerShape(6.dp),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            if (mainScreenPagerState.currentPage == 0)
                                Brush.horizontalGradient(listOf(Color(0xFFFF6D00), Color(0xFF091C3E)))
                            else
                                Brush.horizontalGradient(listOf(Color(0xFF071022), Color(0xFF030712)))
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "◄ RIBBON PITCH BEND",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (mainScreenPagerState.currentPage == 0) Color.White else Color.Gray
                    )
                }
            }

            // Indicator Dots
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(if (mainScreenPagerState.currentPage == 0) 8.dp else 6.dp)
                        .clip(CircleShape)
                        .background(if (mainScreenPagerState.currentPage == 0) Color(0xFFFF6D00) else Color(0xFF13264A))
                        .clickable { coroutineScope.launch { mainScreenPagerState.animateScrollToPage(0) } }
                )
                Spacer(Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(if (mainScreenPagerState.currentPage == 1) 8.dp else 6.dp)
                        .clip(CircleShape)
                        .background(if (mainScreenPagerState.currentPage == 1) Color(0xFFFF6D00) else Color(0xFF13264A))
                        .clickable { coroutineScope.launch { mainScreenPagerState.animateScrollToPage(1) } }
                )
            }

            // Right Page Navigation Button (Sound Slots)
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { coroutineScope.launch { mainScreenPagerState.animateScrollToPage(1) } }
                    .border(
                        1.dp,
                        if (mainScreenPagerState.currentPage == 1) Color(0xFFFF6D00) else Color(0xFF13264A),
                        RoundedCornerShape(6.dp)
                    ),
                shape = RoundedCornerShape(6.dp),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            if (mainScreenPagerState.currentPage == 1)
                                Brush.horizontalGradient(listOf(Color(0xFFFF6D00), Color(0xFF091C3E)))
                            else
                                Brush.horizontalGradient(listOf(Color(0xFF071022), Color(0xFF030712)))
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "SOUND SLOTS ►",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (mainScreenPagerState.currentPage == 1) Color.White else Color.Gray
                    )
                }
            }
        }

        // Horizontal Pager for Main Screen Pages (0: Ribbon Controller, 1: Sound Performance Grid)
        HorizontalPager(
            state = mainScreenPagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            if (page == 0) {
                // PAGE 0: Ribbon Pitch Bend Controller Screen
                KorgRibbonControllerScreen(
                    viewModel = viewModel,
                    activePreset = activePreset,
                    currentDevicePatch = currentDevicePatch
                )
            } else {
                // PAGE 1: Active Sound Display Box & Sound Slot Buttons Grid
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Active Sound Header Bar
                    ActiveSoundHeaderBar(
                        activePreset = activePreset,
                        currentDevicePatch = currentDevicePatch,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    // Sound Slot Buttons Grid (2 rows x 6 columns = 12 slots per page)
                    val pageCount = maxOf(1, (soundPresets.size + 11) / 12)
                    val pagerState = rememberPagerState(pageCount = { pageCount })

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF08142A),
                                        Color(0xFF040A18)
                                    )
                                ),
                                RoundedCornerShape(10.dp)
                            )
                            .border(1.dp, Color(0xFF13264A), RoundedCornerShape(10.dp))
                            .padding(vertical = 4.dp, horizontal = 2.dp)
                    ) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(210.dp)
                        ) { soundPage ->
                            val startIndex = soundPage * 12
                            val endIndex = minOf(startIndex + 12, soundPresets.size)

                            LazyVerticalGrid(
                                columns = GridCells.Fixed(6),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                                userScrollEnabled = false
                            ) {
                                items(endIndex - startIndex) { pageIndex ->
                                    val globalIndex = startIndex + pageIndex
                                    val preset = soundPresets[globalIndex]
                                    SoundSlotCard(
                                        preset = preset,
                                        index = globalIndex,
                                        isSelected = selectedPresetIndex == globalIndex && !isEditMode,
                                        isEditMode = isEditMode,
                                        isMidiInputTriggered = (activeInputTriggerPadIndex == globalIndex),
                                        transpose = transpose,
                                        onPress = {
                                            if (!isEditMode) {
                                                viewModel.onPadPress(globalIndex)
                                            }
                                        },
                                        onRelease = {
                                            if (!isEditMode) {
                                                viewModel.onPadRelease(globalIndex)
                                            }
                                        },
                                        onClick = {
                                            if (isEditMode) {
                                                editPresetIndex = globalIndex
                                                isEditMode = false
                                            } else {
                                                viewModel.onPadPress(globalIndex)
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        if (pageCount > 1) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp, bottom = 2.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                repeat(pageCount) { iteration ->
                                    val isSelected = pagerState.currentPage == iteration
                                    Box(
                                        modifier = Modifier
                                            .padding(horizontal = 3.dp)
                                            .size(if (isSelected) 8.dp else 6.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) Color(0xFFFF6D00) else Color(0xFF13264A))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KorgRibbonControllerScreen(
    viewModel: MainViewModel,
    activePreset: SoundPreset?,
    currentDevicePatch: KorgPatchInfo? = null
) {
    var pitchBendPosition by remember { mutableFloatStateOf(0f) } // -1.0f to +1.0f
    var autoReturn by remember { mutableStateOf(true) }
    var selectedRangeSemitones by remember { mutableIntStateOf(2) } // 2, 7, 12 semitones

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF08142A),
                        Color(0xFF030816)
                    )
                ),
                RoundedCornerShape(12.dp)
            )
            .border(1.dp, Color(0xFF13264A), RoundedCornerShape(12.dp))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Active Sound Header Bar
        ActiveSoundHeaderBar(
            activePreset = activePreset,
            currentDevicePatch = currentDevicePatch
        )

        Spacer(Modifier.height(8.dp))

        // Title and Live Readout Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "KORG Pa1000 RIBBON CONTROLLER",
                color = Color(0xFFFF6D00),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )

            val semitones = pitchBendPosition * selectedRangeSemitones
            val rawMidi = (8192 + (pitchBendPosition * 8191f)).toInt().coerceIn(0, 16383)
            val formatSemitones = String.format("%.2f ST", semitones)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (semitones > 0) "+$formatSemitones" else formatSemitones,
                    color = when {
                        pitchBendPosition > 0.02f -> Color(0xFFFF9E00)
                        pitchBendPosition < -0.02f -> Color(0xFFFF3D00)
                        else -> Color.White
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "($rawMidi)",
                    color = Color.Gray,
                    fontSize = 10.sp
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        // Horizontal Ribbon Surface
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF020712), Color(0xFF0A1833), Color(0xFF030816))
                    )
                )
                .border(1.5.dp, Color(0xFF152D58), RoundedCornerShape(10.dp))
                .pointerInput(autoReturn) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull()
                            if (change != null && change.pressed) {
                                val width = size.width.toFloat()
                                if (width > 0f) {
                                    val touchX = change.position.x
                                    val norm = ((touchX / width) * 2f - 1f).coerceIn(-1.0f, 1.0f)
                                    pitchBendPosition = norm
                                    viewModel.sendPitchBendNormalized(norm)
                                }
                                change.consume()
                            } else if (change != null && !change.pressed) {
                                if (autoReturn) {
                                    pitchBendPosition = 0f
                                    viewModel.sendPitchBendNormalized(0f)
                                }
                            }
                        }
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val centerX = w / 2f

                val trackYTop = h * 0.25f
                val trackYBottom = h * 0.75f
                val trackHeight = trackYBottom - trackYTop

                drawRect(
                    color = Color(0xFF08142A),
                    topLeft = Offset(0f, trackYTop),
                    size = Size(w, trackHeight)
                )

                val ticks = listOf(-1.0f, -0.75f, -0.5f, -0.25f, 0f, 0.25f, 0.5f, 0.75f, 1.0f)
                ticks.forEach { tick ->
                    val x = centerX + (tick * (w / 2f))
                    val isCenter = tick == 0f
                    val tickHeight = if (isCenter) trackHeight + 12f else trackHeight / 2f
                    val y1 = if (isCenter) trackYTop - 6f else trackYTop

                    drawLine(
                        color = if (isCenter) Color(0xFFFF6D00) else Color(0xFF18305B),
                        start = Offset(x, y1),
                        end = Offset(x, y1 + tickHeight),
                        strokeWidth = if (isCenter) 2.5f else 1f
                    )
                }

                drawLine(
                    color = Color(0xFFFF6D00),
                    start = Offset(centerX, 0f),
                    end = Offset(centerX, h),
                    strokeWidth = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                )

                if (pitchBendPosition != 0f) {
                    val touchX = centerX + (pitchBendPosition * (w / 2f))
                    val isUp = pitchBendPosition > 0f
                    val glowColor = if (isUp) Color(0xFFFF9E00) else Color(0xFFFF3D00)

                    val left = if (isUp) centerX else touchX
                    val barWidth = kotlin.math.abs(touchX - centerX)

                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = if (isUp) listOf(Color(0x22FF9E00), Color(0xAAFF9E00))
                                     else listOf(Color(0xAAFF3D00), Color(0x22FF3D00)),
                            startX = left,
                            endX = left + barWidth
                        ),
                        topLeft = Offset(left, trackYTop),
                        size = Size(barWidth, trackHeight)
                    )

                    drawLine(
                        color = glowColor,
                        start = Offset(touchX, 0f),
                        end = Offset(touchX, h),
                        strokeWidth = 4f
                    )

                    drawCircle(
                        color = Color.White,
                        radius = 7f,
                        center = Offset(touchX, h / 2f)
                    )
                    drawCircle(
                        color = glowColor,
                        radius = 12f,
                        center = Offset(touchX, h / 2f),
                        alpha = 0.6f
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(start = 12.dp, end = 12.dp, bottom = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("-$selectedRangeSemitones ST", color = Color(0xFFFFB74D), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text("CENTER (0)", color = Color(0xFFFF6D00), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text("+$selectedRangeSemitones ST", color = Color(0xFFFFB74D), fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(8.dp))

        // Ribbon Controls Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Range Selector (Compact on bottom left)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text("RANGE:", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                listOf(2, 7, 12).forEach { range ->
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { selectedRangeSemitones = range }
                            .border(
                                1.dp,
                                if (selectedRangeSemitones == range) Color(0xFFFF6D00) else Color(0xFF13264A),
                                RoundedCornerShape(4.dp)
                            ),
                        color = Color.Transparent
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    if (selectedRangeSemitones == range)
                                        Brush.verticalGradient(listOf(Color(0xFFFF6D00), Color(0xFF091C3E)))
                                    else
                                        Brush.verticalGradient(listOf(Color(0xFF071022), Color(0xFF030712)))
                                )
                                .padding(horizontal = 5.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "±$range",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Right: Auto Return & Reset 0
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { autoReturn = !autoReturn }
                        .border(
                            1.dp,
                            if (autoReturn) Color(0xFFFF6D00) else Color(0xFF13264A),
                            RoundedCornerShape(6.dp)
                        ),
                    color = Color.Transparent
                ) {
                    Row(
                        modifier = Modifier
                            .background(
                                if (autoReturn)
                                    Brush.horizontalGradient(listOf(Color(0xFFFF6D00), Color(0xFF091C3E)))
                                else
                                    Brush.horizontalGradient(listOf(Color(0xFF071022), Color(0xFF030712)))
                            )
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (autoReturn) Color(0xFFFF9E00) else Color.Gray)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "AUTO RETURN",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Button(
                    onClick = {
                        pitchBendPosition = 0f
                        viewModel.sendPitchBendNormalized(0f)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF08142A)),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier
                        .height(26.dp)
                        .border(1.dp, Color(0xFFFF6D00), RoundedCornerShape(6.dp))
                ) {
                    Text("RESET 0", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun ActiveSoundHeaderBar(
    activePreset: SoundPreset?,
    currentDevicePatch: KorgPatchInfo? = null,
    modifier: Modifier = Modifier
) {
    val patchMsb = currentDevicePatch?.msb ?: activePreset?.msb ?: 0
    val patchLsb = currentDevicePatch?.lsb ?: activePreset?.lsb ?: 0
    val patchProgram = currentDevicePatch?.program ?: activePreset?.program ?: 0
    val patchMode = currentDevicePatch?.mode ?: activePreset?.mode ?: "Prog"
    val customName = currentDevicePatch?.customName

    val soundNameOnKorg = KorgKromeSoundBank.getSoundName(patchMsb, patchLsb, patchProgram, patchMode, customName)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF08142A),
                        Color(0xFF0E2044),
                        Color(0xFF08142A)
                    )
                ),
                RoundedCornerShape(8.dp)
            )
            .border(1.dp, Color(0xFFFF6D00), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = Color(0xFFFF6D00),
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "ACTIVE SOUND:",
                fontSize = 11.sp,
                color = Color(0xFFB0BEC5),
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = soundNameOnKorg,
                fontSize = 14.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.width(8.dp))

        Surface(
            color = Color(0xFF0A1B3B),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.border(1.dp, Color(0xFFFF6D00), RoundedCornerShape(4.dp))
        ) {
            Text(
                text = patchMode.uppercase(),
                color = Color(0xFFFF9E00),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
fun MidiTrafficConsoleDialog(
    connectionStatus: KorgConnectionStatus,
    statusMessage: String,
    trafficLogs: List<MidiTrafficLog>,
    selectedDevice: MidiDeviceInfo?,
    onDismiss: () -> Unit,
    onRequestSoundInfo: () -> Unit,
    onTestNote: () -> Unit,
    onClearLogs: () -> Unit,
    onRefreshDevices: () -> Unit
) {
    var filterType by remember { mutableStateOf("ALL") }

    val filteredLogs = remember(trafficLogs, filterType) {
        when (filterType) {
            "SYSEX" -> trafficLogs.filter { it.summary.contains("SysEx", ignoreCase = true) }
            "IN" -> trafficLogs.filter { it.direction == MidiTrafficLog.Direction.IN }
            "OUT" -> trafficLogs.filter { it.direction == MidiTrafficLog.Direction.OUT }
            else -> trafficLogs
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .widthIn(max = 860.dp)
            .padding(vertical = 12.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val statusColor = when (connectionStatus) {
                        KorgConnectionStatus.CONNECTED -> Color(0xFF00E676)
                        KorgConnectionStatus.CONNECTING, KorgConnectionStatus.SCANNING -> Color(0xFFFF9E00)
                        else -> Color(0xFFFF1744)
                    }
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "KORG MIDI SERVICE CONSOLE",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        },
        containerColor = Color(0xFF08142A),
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF040A18)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF13264A), RoundedCornerShape(8.dp))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Status: ${connectionStatus.name}",
                                color = when (connectionStatus) {
                                    KorgConnectionStatus.CONNECTED -> Color(0xFF00E676)
                                    KorgConnectionStatus.CONNECTING, KorgConnectionStatus.SCANNING -> Color(0xFFFF9E00)
                                    else -> Color(0xFFFF1744)
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            if (selectedDevice != null) {
                                Text(
                                    selectedDevice.properties.getString(MidiDeviceInfo.PROPERTY_NAME)
                                        ?: selectedDevice.properties.getString(MidiDeviceInfo.PROPERTY_PRODUCT)
                                        ?: "Korg MIDI Hardware",
                                    color = Color(0xFFFFCC80),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            statusMessage,
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("ALL", "SYSEX", "IN", "OUT").forEach { type ->
                            val isSel = filterType == type
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable { filterType = type }
                                    .border(1.dp, if (isSel) Color(0xFFFF6D00) else Color(0xFF13264A), RoundedCornerShape(4.dp)),
                                color = if (isSel) Color(0xFFFF6D00).copy(alpha = 0.2f) else Color.Transparent
                            ) {
                                Text(
                                    text = type,
                                    color = if (isSel) Color(0xFFFF9E00) else Color.Gray,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Text(
                        "${filteredLogs.size} logs",
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color(0xFF020612), RoundedCornerShape(6.dp))
                        .border(1.dp, Color(0xFF0C1935), RoundedCornerShape(6.dp))
                        .padding(6.dp)
                ) {
                    if (filteredLogs.isEmpty()) {
                        Text(
                            "No MIDI traffic recorded yet.\nConnect Korg hardware or trigger controls to monitor activity.",
                            color = Color.DarkGray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        val scrollState = rememberScrollState()
                        LaunchedEffect(filteredLogs.size) {
                            scrollState.animateScrollTo(scrollState.maxValue)
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            filteredLogs.forEach { log ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF061024), RoundedCornerShape(4.dp))
                                        .padding(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        val (dirText, dirColor) = when (log.direction) {
                                            MidiTrafficLog.Direction.IN -> "◄ RX" to Color(0xFF00E5FF)
                                            MidiTrafficLog.Direction.OUT -> "► TX" to Color(0xFFFF9100)
                                            MidiTrafficLog.Direction.SYSTEM -> "● SYS" to Color(0xFFE040FB)
                                        }
                                        Text(dirText, color = dirColor, fontWeight = FontWeight.ExtraBold, fontSize = 10.sp)
                                        Text(log.timestamp, color = Color.Gray, fontSize = 9.sp)
                                    }
                                    Text(
                                        log.summary,
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp
                                    )
                                    if (log.hexDump.isNotBlank()) {
                                        Text(
                                            log.hexDump,
                                            color = Color(0xFF80D8FF),
                                            fontSize = 9.sp,
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = onRequestSoundInfo,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0E2248)),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("SysEx Request", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onTestNote,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0E2248)),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Test Note", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onRefreshDevices,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0E2248)),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Scan Hardware", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onClearLogs,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF801313)),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear", tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}

@Composable
fun BleMidiDialog(
    scannedDevices: List<BleMidiDevice>,
    isScanning: Boolean,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onConnectDevice: (android.bluetooth.BluetoothDevice, asInput: Boolean, asOutput: Boolean) -> Unit,
    onDisconnectDevice: (android.bluetooth.BluetoothDevice) -> Unit,
    onDismiss: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ble_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .widthIn(max = 840.dp)
            .padding(vertical = 8.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Icon(
                        imageVector = if (isScanning) Icons.Default.BluetoothSearching else Icons.Default.Bluetooth,
                        contentDescription = null,
                        tint = if (isScanning) Color(0xFF00E5FF).copy(alpha = pulseAlpha) else Color(0xFF00E5FF),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("Bluetooth LE MIDI Setup", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(
                            if (isScanning) "Scanning for MIDI controllers & synths..." else "Connect BLE MIDI Input & Output",
                            color = Color(0xFFB0BEC5),
                            fontSize = 10.5.sp
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                SmallGlossyButton(
                    text = if (isScanning) "Stop Scan" else "Scan BLE",
                    isSelected = isScanning,
                    onClick = {
                        if (isScanning) onStopScan() else onStartScan()
                    },
                    fontSize = 11.sp,
                    horizontalPadding = 12.dp,
                    verticalPadding = 6.dp,
                    icon = {
                        Icon(
                            imageVector = if (isScanning) Icons.Default.Close else Icons.Default.Refresh,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    "Connect your Bluetooth MIDI keyboard, foot controller, or sound module (e.g. CME WIDI, Korg microKEY Air, Roland, Yamaha MD-BT01, etc.):",
                    color = Color(0xFF90CAF9),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (scannedDevices.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .background(Color(0xFF071224), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFF193256), RoundedCornerShape(8.dp))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.BluetoothSearching,
                                contentDescription = null,
                                tint = Color(0xFF4FC3F7),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                if (isScanning) "Scanning for nearby Bluetooth MIDI devices..." else "No BLE MIDI devices detected.",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                if (isScanning) "Please ensure your BLE controller or synth is in pairing mode." else "Tap 'Scan BLE' above to start searching for controllers, keyboards, or pedals.",
                                color = Color(0xFF90A4AE),
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    scannedDevices.forEach { bleDevice ->
                        val isConnected = bleDevice.isConnected
                        val borderColor = if (isConnected) Color(0xFF00E676) else Color(0xFF1D3B6A)
                        val bgColor = if (isConnected) Color(0xFF062B1D) else Color(0xFF08152B)

                        Card(
                            colors = CardDefaults.cardColors(containerColor = bgColor),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = if (isConnected) Icons.Default.BluetoothConnected else Icons.Default.Bluetooth,
                                        contentDescription = null,
                                        tint = if (isConnected) Color(0xFF00E676) else Color(0xFF00E5FF),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                bleDevice.name,
                                                color = Color.White,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (bleDevice.isBonded) {
                                                Spacer(Modifier.width(6.dp))
                                                Text(
                                                    "[Paired]",
                                                    color = Color(0xFFFFB74D),
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                            if (isConnected) {
                                                Spacer(Modifier.width(6.dp))
                                                Text(
                                                    "[CONNECTED]",
                                                    color = Color(0xFF00E676),
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            "${bleDevice.address}   |   RSSI: ${bleDevice.rssi} dBm",
                                            color = Color(0xFF90A4AE),
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                Spacer(Modifier.width(10.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (isConnected) {
                                        OutlinedButton(
                                            onClick = { onDisconnectDevice(bleDevice.device) },
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = Color(0xFFFF5252)
                                            ),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5252)),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text("Disconnect", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                        }
                                    } else {
                                        Button(
                                            onClick = { onConnectDevice(bleDevice.device, true, true) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF00B0FF)
                                            ),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text("Connect IN+OUT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                        }
                                        OutlinedButton(
                                            onClick = { onConnectDevice(bleDevice.device, true, false) },
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = Color(0xFF00E5FF)
                                            ),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF)),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text("IN", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                        OutlinedButton(
                                            onClick = { onConnectDevice(bleDevice.device, false, true) },
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = Color(0xFFFF6D00)
                                            ),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF6D00)),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text("OUT", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color(0xFF061022),
        tonalElevation = 8.dp
    )
}
