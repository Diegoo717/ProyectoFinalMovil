package com.example.proyectofinalmovil.tarea

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.proyectofinalmovil.R
import com.google.accompanist.insets.navigationBarsWithImePadding
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(navController: NavHostController) {
    var taskTitle by remember { mutableStateOf("") }
    var taskContent by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    var notificationEnabled by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val selectDateText = stringResource(id = R.string.select_date)
    val selectTimeText = stringResource(id = R.string.select_time)
    var selectedDate by remember { mutableStateOf(selectDateText) }
    var selectedTime by remember { mutableStateOf(selectTimeText) }

    val calendar = Calendar.getInstance()
    val datePickerDialog = createDatePickerDialog(context, calendar) { date ->
        selectedDate = date
    }
    val timePickerDialog = createTimePickerDialog(context, calendar) { time ->
        selectedTime = time
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .navigationBarsWithImePadding()
    ) {
        HeaderRow(navController, taskTitle) { taskTitle = it }
        DropdownMenuBox(showMenu, notificationEnabled, context) { menuSelected, enabled ->
            showMenu = menuSelected
            notificationEnabled = enabled
        }
        DateAndTimePickerRow(selectedDate, selectedTime, datePickerDialog, timePickerDialog)
        TaskContentField(taskContent) { taskContent = it }
        AddMediaIcons()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeaderRow(navController: NavHostController, taskTitle: String, onTitleChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(
            imageVector = Icons.Filled.ArrowBack,
            contentDescription = stringResource(id = R.string.back_button),
            modifier = Modifier
                .size(24.dp)
                .clickable {
                    navController.popBackStack()
                }
        )

        TextField(
            value = taskTitle,
            onValueChange = onTitleChange,
            placeholder = { Text(text = stringResource(id = R.string.title_placeholder)) },
            textStyle = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        )
    }
}

@Composable
fun DropdownMenuBox(
    showMenu: Boolean,
    notificationEnabled: Boolean,
    context: Context,
    onMenuAction: (Boolean, Boolean) -> Unit
) {
    Box {
        Icon(
            imageVector = Icons.Filled.MoreVert,
            contentDescription = stringResource(id = R.string.menu),
            modifier = Modifier
                .size(24.dp)
                .clickable {
                    onMenuAction(!showMenu, notificationEnabled)
                }
        )

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { onMenuAction(false, notificationEnabled) }
        ) {
            DropdownMenuItem(
                text = { Text(if (notificationEnabled) stringResource(id = R.string.disable_notification) else stringResource(id = R.string.enable_notification)) },
                onClick = {
                    val newEnabled = !notificationEnabled
                    onMenuAction(false, newEnabled)
                    Toast.makeText(context, if (newEnabled) context.getString(R.string.notification_enabled) else context.getString(R.string.notification_disabled), Toast.LENGTH_SHORT).show()
                },
                leadingIcon = { Icon(imageVector = if (notificationEnabled) Icons.Filled.NotificationsOff else Icons.Filled.Notifications, contentDescription = null) }
            )
            // Other menu items can be added here in similar fashion...
        }
    }
}

@Composable
fun DateAndTimePickerRow(
    selectedDate: String,
    selectedTime: String,
    datePickerDialog: DatePickerDialog,
    timePickerDialog: TimePickerDialog
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Button(
            onClick = { datePickerDialog.show() },
            modifier = Modifier
                .height(36.dp)
                .weight(1f)
                .padding(end = 4.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Text(text = selectedDate, color = Color.White)
        }

        Button(
            onClick = { timePickerDialog.show() },
            modifier = Modifier
                .height(36.dp)
                .weight(1f)
                .padding(start = 4.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Text(text = selectedTime, color = Color.White)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskContentField(taskContent: String, onContentChange: (String) -> Unit) {
    TextField(
        value = taskContent,
        onValueChange = onContentChange,
        placeholder = { Text(text = stringResource(id = R.string.details_placeholder)) },
        textStyle = TextStyle(fontSize = 18.sp),
        colors = TextFieldDefaults.textFieldColors(
            containerColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 200.dp)
    )
}

@Composable
fun AddMediaIcons() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Icon(
            imageVector = Icons.Filled.Image,
            contentDescription = stringResource(id = R.string.add_image),
            modifier = Modifier.size(48.dp)
        )
        Icon(
            imageVector = Icons.Filled.Mic,
            contentDescription = stringResource(id = R.string.add_audio),
            modifier = Modifier.size(48.dp)
        )
    }
}

fun createDatePickerDialog(context: Context, calendar: Calendar, onDateSelected: (String) -> Unit): DatePickerDialog {
    return DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            onDateSelected("$dayOfMonth/${month + 1}/$year")
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
}

fun createTimePickerDialog(context: Context, calendar: Calendar, onTimeSelected: (String) -> Unit): TimePickerDialog {
    return TimePickerDialog(
        context,
        { _, hour, minute ->
            onTimeSelected(String.format("%02d:%02d", hour, minute))
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )
}
