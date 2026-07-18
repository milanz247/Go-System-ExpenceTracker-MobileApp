package com.example.ui.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.network.CATEGORY_COLORS
import com.example.network.CATEGORY_ICONS
import com.example.network.CATEGORY_TYPE_EXPENSE
import com.example.network.CATEGORY_TYPE_INCOME
import com.example.network.CategoryResponse
import com.example.network.categoryColorHex
import com.example.ui.common.EmptyState
import com.example.ui.common.ErrorBanner
import com.example.ui.common.FullScreenLoader
import com.example.ui.common.parseHexColor
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.Zinc400
import com.example.ui.theme.Zinc500
import com.example.ui.theme.Zinc800
import com.example.ui.theme.Zinc900
import com.example.ui.theme.Zinc950

private val fieldColors: @Composable () -> TextFieldColors = {
    OutlinedTextFieldDefaults.colors(
        focusedTextColor = PureWhite, unfocusedTextColor = PureWhite,
        focusedBorderColor = PureWhite, unfocusedBorderColor = Zinc800,
        focusedLabelColor = PureWhite, unfocusedLabelColor = Zinc400,
        cursorColor = PureWhite
    )
}

@Composable
fun CategoriesScreen(
    viewModel: CategoriesViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedType by viewModel.selectedType.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val showForm by viewModel.showForm.collectAsState()
    val pendingDelete by viewModel.categoryPendingDelete.collectAsState()

    val visibleCategories = remember(categories, selectedType) { categories.filter { it.type == selectedType } }

    Box(modifier = modifier.fillMaxSize().background(PitchBlack).windowInsetsPadding(WindowInsets.safeDrawing)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = PureWhite)
                    }
                    Text(text = "Categories", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = PureWhite, modifier = Modifier.weight(1f))
                    IconButton(onClick = { viewModel.openAddForm() }) {
                        Icon(Icons.Default.Add, contentDescription = "Add category", tint = PureWhite)
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TypeChip("Expense", selectedType == CATEGORY_TYPE_EXPENSE) { viewModel.selectType(CATEGORY_TYPE_EXPENSE) }
                    TypeChip("Income", selectedType == CATEGORY_TYPE_INCOME) { viewModel.selectType(CATEGORY_TYPE_INCOME) }
                }
            }

            if (errorMessage != null) {
                item { ErrorBanner(errorMessage!!) }
            }

            if (isLoading && categories.isEmpty()) {
                item { FullScreenLoader(modifier = Modifier.fillMaxWidth().height(200.dp)) }
            } else if (visibleCategories.isEmpty()) {
                item { EmptyState("No categories yet.") }
            } else {
                items(visibleCategories, key = { it.id }) { category ->
                    CategoryRow(
                        category = category,
                        onEdit = { viewModel.openEditForm(category) },
                        onDelete = { viewModel.requestDelete(category) }
                    )
                }
            }
        }
    }

    if (showForm) {
        CategoryFormDialog(viewModel)
    }

    pendingDelete?.let { category ->
        AlertDialog(
            onDismissRequest = { viewModel.cancelDelete() },
            containerColor = Zinc950,
            titleContentColor = PureWhite,
            textContentColor = Zinc400,
            title = { Text("Delete \"${category.name}\"?") },
            text = { Text("This can't be undone. Categories still used by transactions can't be deleted.") },
            confirmButton = { TextButton(onClick = { viewModel.confirmDelete() }) { Text("Delete", color = Color.Red) } },
            dismissButton = { TextButton(onClick = { viewModel.cancelDelete() }) { Text("Cancel", color = Zinc500) } }
        )
    }
}

@Composable
private fun TypeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) PureWhite else Zinc900)
            .border(1.dp, if (selected) Color.Transparent else Zinc800, RoundedCornerShape(50))
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 8.dp)
    ) {
        Text(text = label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = if (selected) PitchBlack else Zinc400)
    }
}

@Composable
private fun CategoryRow(category: CategoryResponse, onEdit: () -> Unit, onDelete: () -> Unit) {
    var menuExpanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Zinc950)
            .border(1.dp, Zinc800, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(parseHexColor(categoryColorHex(category.color)).copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = com.example.ui.common.iconForCategory(category.icon),
                contentDescription = null,
                tint = parseHexColor(categoryColorHex(category.color)),
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = category.name, fontSize = 14.sp, color = PureWhite, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (category.isSystem) {
                Text(text = "System", fontSize = 10.sp, color = Zinc500)
            }
        }
        if (!category.isSystem) {
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Category options", tint = Zinc500)
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }, containerColor = Zinc900) {
                    DropdownMenuItem(text = { Text("Edit", color = PureWhite) }, onClick = { menuExpanded = false; onEdit() })
                    DropdownMenuItem(text = { Text("Delete", color = Color.Red) }, onClick = { menuExpanded = false; onDelete() })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryFormDialog(viewModel: CategoriesViewModel) {
    val name by viewModel.formName.collectAsState()
    val type by viewModel.formType.collectAsState()
    val color by viewModel.formColor.collectAsState()
    val icon by viewModel.formIcon.collectAsState()
    val formError by viewModel.formError.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val editingId by viewModel.editingCategoryId.collectAsState()
    val isCreate = editingId == null

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) viewModel.dismissForm() },
        containerColor = Zinc950,
        titleContentColor = PureWhite,
        textContentColor = Zinc400,
        title = { Text(if (isCreate) "Add Category ($type)" else "Edit Category") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = viewModel::onNameChanged,
                    label = { Text("Name") },
                    singleLine = true,
                    colors = fieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(text = "Color", fontSize = 12.sp, color = Zinc500)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(CATEGORY_COLORS) { colorOption ->
                        val hex = categoryColorHex(colorOption)
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(parseHexColor(hex))
                                .border(2.dp, if (color == colorOption) PureWhite else Color.Transparent, CircleShape)
                                .clickable { viewModel.onColorSelected(colorOption) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (color == colorOption) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = PureWhite, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                Text(text = "Icon", fontSize = 12.sp, color = Zinc500)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(CATEGORY_ICONS) { iconOption ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (icon == iconOption) PureWhite else Zinc900)
                                .clickable { viewModel.onIconSelected(iconOption) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = com.example.ui.common.iconForCategory(iconOption),
                                contentDescription = iconOption,
                                tint = if (icon == iconOption) PitchBlack else Zinc400,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                if (formError != null) {
                    Text(text = formError!!, color = Color.Red, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { viewModel.submitForm() }, enabled = !isSubmitting) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = PureWhite)
                } else {
                    Text(if (isCreate) "Create" else "Save", color = PureWhite)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.dismissForm() }, enabled = !isSubmitting) {
                Text("Cancel", color = Zinc500)
            }
        }
    )
}
