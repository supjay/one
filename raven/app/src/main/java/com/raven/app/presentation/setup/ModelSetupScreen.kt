package com.raven.app.presentation.setup

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.raven.app.presentation.theme.ColorAssistant
import com.raven.app.presentation.theme.RavenPrimary

@Composable
fun ModelSetupScreen(
    onSetupComplete: () -> Unit,
    onSkip: () -> Unit,
    viewModel: ModelSetupViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state) {
        when (state) {
            is SetupUiState.Ready, is SetupUiState.Skipped -> {
                if (state is SetupUiState.Ready) onSetupComplete() else onSkip()
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(RavenPrimary, Color(0xFF0D1B2A)))
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            // Animated Raven logo
            val infiniteTransition = rememberInfiniteTransition(label = "logo")
            val rotation by infiniteTransition.animateFloat(
                initialValue = -5f, targetValue = 5f,
                animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse),
                label = "rotate"
            )

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(ColorAssistant.copy(0.15f))
                    .rotate(rotation),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = ColorAssistant,
                    modifier = Modifier.size(52.dp)
                )
            }

            AnimatedContent(targetState = state, label = "content") { currentState ->
                when (currentState) {
                    is SetupUiState.Checking -> CheckingContent()
                    is SetupUiState.NeedsDownload -> NeedsDownloadContent(
                        modelSizeMb = currentState.modelSizeMb,
                        onDownload = { viewModel.startDownload() },
                        onSkip = { viewModel.skip() }
                    )
                    is SetupUiState.Downloading -> DownloadingContent(
                        progressPercent = currentState.progressPercent,
                        downloadedMb = currentState.downloadedMb,
                        totalMb = currentState.totalMb
                    )
                    is SetupUiState.Initializing -> InitializingContent()
                    is SetupUiState.Error -> ErrorContent(
                        message = currentState.message,
                        onRetry = { viewModel.retry() },
                        onSkip = { viewModel.skip() }
                    )
                    is SetupUiState.Ready, is SetupUiState.Skipped -> {
                        // Handled by LaunchedEffect above
                        Box(modifier = Modifier.size(1.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CheckingContent() {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Raven", style = MaterialTheme.typography.displaySmall, color = Color.White, fontWeight = FontWeight.Bold)
        CircularProgressIndicator(color = ColorAssistant)
        Text("Checking setup...", color = Color.White.copy(0.7f))
    }
}

@Composable
private fun NeedsDownloadContent(modelSizeMb: Int, onDownload: () -> Unit, onSkip: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Meet Raven AI", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Text(
            "Upgrade to a conversational AI assistant that understands your reminders, budget, trips, and family — all on your device, no internet required after setup.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(0.8f),
            textAlign = TextAlign.Center
        )

        // Feature bullets
        FeatureBullet(Icons.Filled.Psychology, "Understands your personal data")
        FeatureBullet(Icons.Filled.WifiOff, "100% on-device after download")
        FeatureBullet(Icons.Filled.Lock, "Private — nothing leaves your phone")
        FeatureBullet(Icons.Filled.MoneyOff, "Completely free, no API key")

        Spacer(Modifier.height(8.dp))

        // Size warning
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(0.1f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Wifi, null, tint = ColorAssistant, modifier = Modifier.size(20.dp))
                Text(
                    "Requires ~${modelSizeMb / 1000}GB download. Connect to WiFi recommended.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(0.8f)
                )
            }
        }

        Button(
            onClick = onDownload,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = ColorAssistant)
        ) {
            Icon(Icons.Filled.Download, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Download Raven AI (~${modelSizeMb / 1000}GB)", fontWeight = FontWeight.SemiBold)
        }

        TextButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) {
            Text("Use basic mode (no download)", color = Color.White.copy(0.6f))
        }
    }
}

@Composable
private fun DownloadingContent(progressPercent: Int, downloadedMb: Int, totalMb: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("Downloading Raven AI", style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)

        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.dp)) {
            CircularProgressIndicator(
                progress = { progressPercent / 100f },
                modifier = Modifier.fillMaxSize(),
                color = ColorAssistant,
                trackColor = ColorAssistant.copy(0.2f),
                strokeWidth = 8.dp
            )
            Text(
                "$progressPercent%",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            "$downloadedMb MB / $totalMb MB",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(0.7f)
        )

        Text(
            "Keep the app open. This only happens once.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(0.5f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun InitializingContent() {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Almost there!", style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
        CircularProgressIndicator(color = ColorAssistant)
        Text("Loading model into memory...", color = Color.White.copy(0.7f), textAlign = TextAlign.Center)
        Text("(30-60 seconds on first launch)", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.5f))
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit, onSkip: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Icon(Icons.Filled.ErrorOutline, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
        Text("Setup failed", style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
        Text(message, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.7f), textAlign = TextAlign.Center)
        Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = ColorAssistant)) {
            Text("Try again")
        }
        TextButton(onClick = onSkip) {
            Text("Use basic mode instead", color = Color.White.copy(0.6f))
        }
    }
}

@Composable
private fun FeatureBullet(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, null, tint = ColorAssistant, modifier = Modifier.size(20.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(0.9f))
    }
}
