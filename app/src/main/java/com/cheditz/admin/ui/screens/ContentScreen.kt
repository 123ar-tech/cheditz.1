package com.cheditz.admin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cheditz.admin.data.model.ContentBlock
import com.cheditz.admin.ui.theme.*
import com.cheditz.admin.ui.viewmodel.ConfigViewModel

private val ACCENT_SWATCHES = listOf(
    "#dc2626", "#9333ea", "#1d4ed8", "#c026d3", "#ef4444", "#3b82f6",
    "#f97316", "#0d9488", "#16a34a", "#0891b2", "#000000", "#111111"
)

private fun parseHex(hex: String): Color = try {
    Color(android.graphics.Color.parseColor(hex))
} catch (_: Exception) {
    VioletPrimary
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentScreen(vm: ConfigViewModel = viewModel()) {
    val config by vm.config.collectAsStateWithLifecycle()
    val content = config?.content ?: ContentBlock()

    var heroBadge by remember(content.heroBadge) { mutableStateOf(content.heroBadge) }
    var heroHead by remember(content.heroHead) { mutableStateOf(content.heroHead) }
    var heroSub by remember(content.heroSub) { mutableStateOf(content.heroSub) }
    var worksTitle by remember(content.worksTitle) { mutableStateOf(content.worksTitle) }
    var svcTitle by remember(content.svcTitle) { mutableStateOf(content.svcTitle) }
    var orderTitle by remember(content.orderTitle) { mutableStateOf(content.orderTitle) }
    var orderDesc by remember(content.orderDesc) { mutableStateOf(content.orderDesc) }
    var contactHead by remember(content.contactHead) { mutableStateOf(content.contactHead) }
    var contactDesc by remember(content.contactDesc) { mutableStateOf(content.contactDesc) }
    var accent by remember(config?.accent) { mutableStateOf(config?.accent ?: "#9333ea") }
    var saved by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Site Content", fontWeight = FontWeight.Bold) },
                actions = {
                    TextButton(onClick = {
                        vm.updateContent(
                            ContentBlock(heroBadge, heroHead, heroSub, worksTitle, svcTitle, orderTitle, orderDesc, contactHead, contactDesc)
                        )
                        saved = true
                    }) { Text("Save", color = VioletPrimary) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (saved) {
                Snackbar { Text("Saved to Firestore — website updates live") }
            }

            SectionLabel("Accent Color")
            Text(
                "Tap a swatch — applies to the website theme instantly after Save.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            ACCENT_SWATCHES.chunked(6).forEach { rowColors ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowColors.forEach { c ->
                        val swatchColor = remember(c) { parseHex(c) }
                        val selected = accent.equals(c, ignoreCase = true)
                        Box(
                            Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(swatchColor)
                                .border(
                                    width = if (selected) 3.dp else 1.dp,
                                    color = if (selected) VioletPrimary else TextSecondary.copy(alpha = 0.3f),
                                    shape = CircleShape
                                )
                                .clickable { accent = c }
                        )
                    }
                }
            }
            OutlinedTextField(
                value = accent,
                onValueChange = { accent = it },
                label = { Text("Hex Accent (e.g. #9333ea)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Button(onClick = { vm.updateAccent(accent); saved = true }) {
                Text("Save Accent Color")
            }

            Spacer(Modifier.height(4.dp))
            SectionLabel("Hero Section")
            ContentField("Hero Badge", heroBadge) { heroBadge = it }
            ContentField("Hero Headline", heroHead) { heroHead = it }
            ContentField("Hero Subtitle", heroSub) { heroSub = it }

            SectionLabel("Section Titles")
            ContentField("Works Title", worksTitle) { worksTitle = it }
            ContentField("Services Title", svcTitle) { svcTitle = it }

            SectionLabel("Order Section")
            ContentField("Order Title", orderTitle) { orderTitle = it }
            ContentField("Order Description", orderDesc, multiline = true) { orderDesc = it }

            SectionLabel("Contact Section")
            ContentField("Contact Headline", contactHead) { contactHead = it }
            ContentField("Contact Description", contactDesc, multiline = true) { contactDesc = it }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = VioletPrimary)
}

@Composable
fun ContentField(label: String, value: String, multiline: Boolean = false, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        minLines = if (multiline) 3 else 1,
        maxLines = if (multiline) 6 else 2,
        shape = MaterialTheme.shapes.small
    )
}
