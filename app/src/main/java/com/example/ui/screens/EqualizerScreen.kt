package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.EqualizerPresetEntity
import com.example.ui.MediaViewModel
import com.example.ui.theme.CrimsonBorder
import com.example.ui.theme.DarkGreyBase
import com.example.ui.theme.ScarletPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerScreen(
    viewModel: MediaViewModel,
    onBack: () -> Unit
) {
    val presets by viewModel.equalizerPresets.collectAsState()
    val activePresetName by viewModel.activePresetName.collectAsState()
    val activeBands by viewModel.playbackManager.equalizerBands.collectAsState()

    var showSaveDialog by remember { mutableStateOf(false) }
    var newPresetName by remember { mutableStateOf("") }

    val bandLabels = listOf("60 Hz", "230 Hz", "910 Hz", "4 kHz", "14 kHz")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090909))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Equalizer Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ECUALIZADOR AVANZADO",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )

            IconButton(
                onClick = { showSaveDialog = true },
                modifier = Modifier
                    .border(1.dp, CrimsonBorder, RoundedCornerShape(8.dp))
                    .size(40.dp)
                    .testTag("save_preset_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Guardar Ajuste",
                    tint = ScarletPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Visual Frequency Curve Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .border(1.dp, CrimsonBorder, RoundedCornerShape(12.dp))
                .background(Color(0xFF131313), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val midY = height / 2f
                val pointsCount = 5

                // Draw background grid lines (white translucent outlines as requested)
                val gridLines = 4
                for (i in 1..gridLines) {
                    val y = (height / (gridLines + 1)) * i
                    drawLine(
                        color = Color(0x1AFFFFFF),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1f
                    )
                }

                // Bezier path for the curve
                val path = Path()
                val stepX = width / (pointsCount - 1)

                for (i in 0 until pointsCount) {
                    val sliderVal = activeBands[i] // assumed range from -12f to +12f
                    // Map -12..12 to heights: -12 is bottom (height), 12 is top (0)
                    val percent = (sliderVal + 12f) / 24f
                    val y = height - (percent * height)

                    val x = i * stepX
                    if (i == 0) {
                        path.moveTo(x, y)
                    } else {
                        val prevX = (i - 1) * stepX
                        val prevSlider = activeBands[i - 1]
                        val prevPercent = (prevSlider + 12f) / 24f
                        val prevY = height - (prevPercent * height)

                        // control points for smooth bezier curve
                        val controlX1 = prevX + stepX / 2f
                        val controlY1 = prevY
                        val controlX2 = prevX + stepX / 2f
                        val controlY2 = y

                        path.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
                    }
                }

                // Draw curve outline (White linear outline as requested)
                drawPath(
                    path = path,
                    color = Color.White,
                    style = Stroke(width = 4f)
                )

                // Fill under the curve with scarlet-to-black glowing gradient brush
                val fillPath = Path().apply {
                    addPath(path)
                    lineTo(width, height)
                    lineTo(0f, height)
                    close()
                }
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(ScarletPrimary.copy(alpha = 0.4f), Color.Transparent),
                        startY = 0f,
                        endY = height
                    )
                )

                // Draw points on the curve
                for (i in 0 until pointsCount) {
                    val sliderVal = activeBands[i]
                    val percent = (sliderVal + 12f) / 24f
                    val y = height - (percent * height)
                    val x = i * stepX

                    drawCircle(
                        color = ScarletPrimary,
                        radius = 8f,
                        center = Offset(x, y)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 4f,
                        center = Offset(x, y)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Preset Selector Row (horizontal)
        Text(
            text = "AJUSTES PREESTABLECIDOS",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(presets) { preset ->
                val isSelected = preset.name == activePresetName
                Button(
                    onClick = { viewModel.selectPreset(preset) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) ScarletPrimary else Color(0xFF1F1F1F),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (isSelected) Color.White else CrimsonBorder),
                    modifier = Modifier.testTag("preset_${preset.name.lowercase().replace(" ", "_")}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(preset.name, style = MaterialTheme.typography.labelMedium)

                        if (preset.isCustom) {
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = { viewModel.deletePreset(preset) },
                                modifier = Modifier.size(18.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Borrar",
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sliders Table
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF141414), RoundedCornerShape(16.dp))
                .border(1.dp, CrimsonBorder.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .padding(vertical = 20.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            for (i in 0 until 5) {
                val label = bandLabels[i]
                val value = activeBands[i]

                Column(
                    modifier = Modifier.fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${if (value >= 0) "+" else ""}${value.toInt()} dB",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall
                    )

                    Slider(
                        value = value,
                        onValueChange = { newValue ->
                            val updatedBands = activeBands.copyOf()
                            updatedBands[i] = newValue
                            viewModel.playbackManager.setEqualizerBands(updatedBands)
                        },
                        valueRange = -12f..12f,
                        steps = 24,
                        colors = SliderDefaults.colors(
                            activeTrackColor = ScarletPrimary,
                            inactiveTrackColor = Color(0xFF333333),
                            thumbColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .width(36.dp)
                            .graphicsLayer {
                                rotationZ = -90f // Rotate slider vertical!
                            }
                    )

                    Text(
                        text = label,
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Close/Confirm button
        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .border(1.dp, Color.White, RoundedCornerShape(25.dp))
                .testTag("apply_equalizer_button"),
            colors = ButtonDefaults.buttonColors(containerColor = ScarletPrimary),
            shape = RoundedCornerShape(25.dp)
        ) {
            Text("GUARDAR Y APLICAR", style = MaterialTheme.typography.bodyLarge, color = Color.White)
        }

        // Save Custom Preset Dialog
        if (showSaveDialog) {
            Dialog(onDismissRequest = { showSaveDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF222222)),
                    border = BorderStroke(1.dp, CrimsonBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "NUEVO AJUSTE PERSONAL",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = newPresetName,
                            onValueChange = { newPresetName = it },
                            label = { Text("Nombre del ajuste...", color = Color.Gray) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedLabelColor = ScarletPrimary,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.5f)
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showSaveDialog = false }) {
                                Text("Cancelar", color = Color.Gray)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (newPresetName.isNotBlank()) {
                                        viewModel.saveCustomPreset(newPresetName, activeBands)
                                        showSaveDialog = false
                                        newPresetName = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ScarletPrimary)
                            ) {
                                Text("Guardar", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}
