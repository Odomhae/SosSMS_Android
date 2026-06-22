package com.odom.sosSms.ui.contacts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.odom.sosSms.R
import com.odom.sosSms.data.Contact

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    viewModel: ContactsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contacts by viewModel.contacts.collectAsState()
    var editingIndex by remember { mutableStateOf<Int?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.contacts_title)) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text(stringResource(R.string.common_back))
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
        ) {
            if (contacts.isEmpty()) {
                Text(text = stringResource(R.string.contacts_empty_state))
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(contacts.size) { index ->
                    ContactRow(
                        contact = contacts[index],
                        priority = index + 1,
                        canMoveUp = index > 0,
                        canMoveDown = index < contacts.size - 1,
                        onMoveUp = { viewModel.moveUp(index) },
                        onMoveDown = { viewModel.moveDown(index) },
                        onEdit = { editingIndex = index },
                        onDelete = { viewModel.deleteContact(index) },
                    )
                }
            }

            if (contacts.size < MAX_CONTACTS) {
                Button(
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                ) {
                    Text(stringResource(R.string.contacts_add_button))
                }
            } else {
                Text(
                    text = stringResource(R.string.contacts_max_reached),
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
        }
    }

    if (showAddDialog) {
        ContactEditDialog(
            initialName = "",
            initialPhone = "",
            onConfirm = { name, phone ->
                viewModel.addContact(name, phone)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false },
        )
    }

    val editIndex = editingIndex
    if (editIndex != null) {
        val contact = contacts.getOrNull(editIndex)
        if (contact != null) {
            ContactEditDialog(
                initialName = contact.name,
                initialPhone = contact.phone,
                onConfirm = { name, phone ->
                    viewModel.updateContact(editIndex, name, phone)
                    editingIndex = null
                },
                onDismiss = { editingIndex = null },
            )
        }
    }
}

@Composable
private fun ContactRow(
    contact: Contact,
    priority: Int,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = stringResource(R.string.contacts_priority_label, priority))
            Text(text = contact.name)
            Text(text = contact.phone)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onMoveUp, enabled = canMoveUp) {
                    Text(stringResource(R.string.contacts_move_up_button))
                }
                TextButton(onClick = onMoveDown, enabled = canMoveDown) {
                    Text(stringResource(R.string.contacts_move_down_button))
                }
                TextButton(onClick = onEdit) {
                    Text(stringResource(R.string.contacts_edit_button))
                }
                TextButton(onClick = onDelete) {
                    Text(stringResource(R.string.contacts_delete_button))
                }
            }
        }
    }
}

@Composable
private fun ContactEditDialog(
    initialName: String,
    initialPhone: String,
    onConfirm: (name: String, phone: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    var phone by remember { mutableStateOf(initialPhone) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.contacts_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.contacts_name_label)) },
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(stringResource(R.string.contacts_phone_label)) },
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name.trim(), phone.trim()) },
                enabled = name.isNotBlank() && phone.isNotBlank(),
            ) {
                Text(stringResource(R.string.contacts_save_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        },
    )
}
