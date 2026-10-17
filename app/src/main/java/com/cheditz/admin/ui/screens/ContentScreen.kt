package com.cheditz.admin.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cheditz.admin.data.model.ContentBlock
import com.cheditz.admin.ui.theme.*
import com.cheditz.admin.ui.viewmodel.ConfigViewModel

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
                Snackbar { Text("Content saved to Firestore") }
            }

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

            // Accent color
            SectionLabel("Accent Color")
            var accent by remember(config?.accent) { mutableStateOf(config?.accent ?: "#bc6bff") }
            OutlinedTextField(
                value = accent,
                onValueChange = { accent = it },
                label = { Text("Hex Accent (e.g. #bc6bff)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            Button(onClick = { vm.updateAccent(accent); saved = true }) {
                Text("Save Accent Color")
            }
            Text(
                "This updates cheditz/config.accent so the website theme changes live.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
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
