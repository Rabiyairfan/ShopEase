package com.marketplace.presentation.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.marketplace.domain.model.User
import com.marketplace.domain.model.UserRole
import com.marketplace.presentation.viewmodel.admin.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserScreen(
    onViewUserDetails: (String) -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showFilterDialog by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf<UserRole?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showDeactivateDialog by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadDashboardStats()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Users") },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(Icons.Default.FilterList, "Filter")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search users...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                singleLine = true
            )

            // Users List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    state.users.filter { user ->
                        (selectedRole == null || user.role == selectedRole) &&
                        (searchQuery.isEmpty() || user.name.contains(searchQuery, ignoreCase = true) ||
                         user.email.contains(searchQuery, ignoreCase = true))
                    }
                ) { user ->
                    UserCard(
                        user = user,
                        onViewDetails = { onViewUserDetails(user.id) },
                        onUpdateRole = { newRole ->
                            viewModel.updateUserRole(user.id, newRole)
                        },
                        onDeactivate = { showDeactivateDialog = user }
                    )
                }
            }
        }
    }

    // Filter Dialog
    if (showFilterDialog) {
        AlertDialog(
            onDismissRequest = { showFilterDialog = false },
            title = { Text("Filter Users") },
            text = {
                Column {
                    Text(
                        text = "User Role",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    UserRole.values().forEach { role ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedRole == role,
                                onClick = {
                                    selectedRole = if (selectedRole == role) null else role
                                }
                            )
                            Text(
                                text = role.name.replace("_", " ").lowercase()
                                    .replaceFirstChar { it.uppercase() },
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFilterDialog = false }) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        selectedRole = null
                        showFilterDialog = false
                    }
                ) {
                    Text("Clear")
                }
            }
        )
    }

    // Deactivate User Dialog
    showDeactivateDialog?.let { user ->
        AlertDialog(
            onDismissRequest = { showDeactivateDialog = null },
            title = { Text("Deactivate User") },
            text = { Text("Are you sure you want to deactivate ${user.name}'s account?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deactivateUser(user.id)
                        showDeactivateDialog = null
                    }
                ) {
                    Text("Deactivate")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeactivateDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserCard(
    user: User,
    onViewDetails: () -> Unit,
    onUpdateRole: (UserRole) -> Unit,
    onDeactivate: () -> Unit
) {
    var showRoleMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box {
                    TextButton(
                        onClick = { showRoleMenu = true }
                    ) {
                        Text(
                            text = user.role.name.replace("_", " ").lowercase()
                                .replaceFirstChar { it.uppercase() },
                            color = when (user.role) {
                                UserRole.ADMIN -> MaterialTheme.colorScheme.error
                                UserRole.USER -> MaterialTheme.colorScheme.primary
                            }
                        )
                    }
                    DropdownMenu(
                        expanded = showRoleMenu,
                        onDismissRequest = { showRoleMenu = false }
                    ) {
                        UserRole.values().forEach { role ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = role.name.replace("_", " ").lowercase()
                                            .replaceFirstChar { it.uppercase() }
                                    )
                                },
                                onClick = {
                                    onUpdateRole(role)
                                    showRoleMenu = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (user.isActive) "Active" else "Inactive",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (user.isActive) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.error
                )
                Row {
                    TextButton(onClick = onViewDetails) {
                        Text("View Details")
                    }
                    if (user.isActive) {
                        TextButton(onClick = onDeactivate) {
                            Text("Deactivate")
                        }
                    }
                }
            }
        }
    }
} 