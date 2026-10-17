package com.cheditz.admin.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cheditz.admin.data.model.Contact
import com.cheditz.admin.ui.theme.*
import com.cheditz.admin.ui.viewmodel.ConfigViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactScreen(onBack: () -> Unit, vm: ConfigViewModel = viewModel()) {
    val config by vm.config.collectAsStateWithLifecycle()
    val contact = config?.contact ?: Contact()
    val context = LocalContext.current

    var email by remember(contact.email) { mutableStateOf(contact.email) }
    var insta by remember(contact.insta) { mutableStateOf(contact.insta) }
    var instaUrl by remember(contact.instaUrl) { mutableStateOf(contact.instaUrl) }
    var whatsapp by remember(contact.whatsapp) { mutableStateOf(contact.whatsapp) }
    var whatsappLabel by remember(contact.whatsappLabel) { mutableStateOf(contact.whatsappLabel) }
    var whatsappText by remember(contact.whatsappText) { mutableStateOf(contact.whatsappText) }
    var hours by remember(contact.hours) { mutableStateOf(contact.hours) }
    var reply by remember(contact.reply) { mutableStateOf(contact.reply) }
    var saved by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contact Info", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back") } },
                actions = {
                    TextButton(onClick = {
                        vm.updateContact(
                            Contact(
                                email = email,
                                insta = insta,
                                instaUrl = instaUrl,
                                whatsapp = whatsapp,
                                whatsappLabel = whatsappLabel,
                                whatsappText = whatsappText,
                                hours = hours,
                                reply = reply
                            )
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
            if (saved) Snackbar { Text("Contact info saved") }

            ContactField("Email", email) { email = it }
            ContactField("Instagram Handle", insta) { insta = it }
            ContactField("Instagram URL", instaUrl) { instaUrl = it }
            ContactField("WhatsApp Number", whatsapp) { whatsapp = it }
            ContactField("WhatsApp Button Label", whatsappLabel) { whatsappLabel = it }
            ContactField("WhatsApp Greeting Text", whatsappText) { whatsappText = it }
            ContactField("Working Hours", hours) { hours = it }
            ContactField("Reply Time", reply) { reply = it }

            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = {
                    context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email")))
                }) { Icon(Icons.Filled.Email, "Email", modifier = Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text("Email") }

                OutlinedButton(onClick = {
                    val digits = whatsapp.filter { it.isDigit() }
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/91$digits")))
                }) { Icon(Icons.Filled.Chat, "WhatsApp", modifier = Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text("WhatsApp") }

                OutlinedButton(onClick = {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(instaUrl)))
                }, enabled = instaUrl.isNotBlank()) { Icon(Icons.Filled.CameraAlt, "Instagram", modifier = Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text("Instagram") }
            }
        }
    }
}

@Composable
fun ContactField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = MaterialTheme.shapes.small
    )
}
