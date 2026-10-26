package com.example.datossinmvvm

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.datossinmvvm.data.AttendanceRecord
import com.example.datossinmvvm.data.User
import com.example.datossinmvvm.data.UserDatabase
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenUser() {
    val context = LocalContext.current
    val database = remember(context) { UserDatabase.getDatabase(context) }
    val userDao = remember(database) { database.userDao() }
    val attendanceDao = remember(database) { database.attendanceRecordDao() }
    val scope = rememberCoroutineScope()

    var selectedTab by rememberSaveable { mutableStateOf(0) }
    var firstName by rememberSaveable { mutableStateOf("") }
    var lastName by rememberSaveable { mutableStateOf("") }
    var users by remember { mutableStateOf(emptyList<User>()) }

    var selectedAttendanceId by rememberSaveable { mutableStateOf<Int?>(null) }
    var studentName by rememberSaveable { mutableStateOf("") }
    var classroom by rememberSaveable { mutableStateOf("5to A") }
    var attendanceDate by rememberSaveable { mutableStateOf("07/10/2026") }
    var attendanceStatus by rememberSaveable { mutableStateOf("Presente") }
    var attendanceRecords by remember { mutableStateOf(emptyList<AttendanceRecord>()) }
    var message by rememberSaveable { mutableStateOf("Listo para registrar información en Room.") }

    fun refreshUsers() {
        scope.launch {
            users = userDao.getAll()
            message = if (users.isEmpty()) "Aún no hay usuarios registrados." else "Se listaron ${users.size} usuario(s) desde Room."
        }
    }

    fun refreshAttendance() {
        scope.launch {
            attendanceRecords = attendanceDao.getAll()
            message = if (attendanceRecords.isEmpty()) "Aún no hay asistencias registradas." else "Se listaron ${attendanceRecords.size} registro(s) de asistencia."
        }
    }

    fun addUser() {
        val name = firstName.trim()
        val surname = lastName.trim()
        if (name.isBlank() || surname.isBlank()) {
            message = "Completa el nombre y el apellido antes de agregar."
            return
        }
        scope.launch {
            userDao.insert(User(firstName = name, lastName = surname))
            users = userDao.getAll()
            firstName = ""
            lastName = ""
            message = "Usuario agregado y listado desde la base de datos."
        }
    }

    fun deleteLastUser() {
        scope.launch {
            val deleted = userDao.deleteLast()
            users = userDao.getAll()
            message = if (deleted > 0) "Se eliminó el último registro añadido." else "No hay registros para eliminar."
        }
    }

    fun clearAttendanceForm() {
        selectedAttendanceId = null
        studentName = ""
        classroom = "5to A"
        attendanceDate = "07/10/2026"
        attendanceStatus = "Presente"
    }

    fun saveAttendance() {
        val student = studentName.trim()
        val section = classroom.trim()
        val date = attendanceDate.trim()
        if (student.isBlank() || section.isBlank() || date.isBlank()) {
            message = "Completa estudiante, aula y fecha para guardar la asistencia."
            return
        }
        scope.launch {
            val record = AttendanceRecord(
                id = selectedAttendanceId ?: 0,
                studentName = student,
                classroom = section,
                attendanceDate = date,
                status = attendanceStatus
            )
            if (selectedAttendanceId == null) {
                attendanceDao.insert(record)
                message = "Asistencia creada correctamente en Room."
            } else {
                attendanceDao.update(record)
                message = "Asistencia actualizada correctamente en Room."
            }
            attendanceRecords = attendanceDao.getAll()
            clearAttendanceForm()
        }
    }

    fun deleteSelectedAttendance() {
        val id = selectedAttendanceId ?: run {
            message = "Selecciona un registro de la lista para eliminarlo."
            return
        }
        scope.launch {
            attendanceDao.delete(
                AttendanceRecord(
                    id = id,
                    studentName = studentName,
                    classroom = classroom,
                    attendanceDate = attendanceDate,
                    status = attendanceStatus
                )
            )
            attendanceRecords = attendanceDao.getAll()
            clearAttendanceForm()
            message = "Registro de asistencia eliminado correctamente."
        }
    }

    LaunchedEffect(Unit) {
        users = userDao.getAll()
        attendanceRecords = attendanceDao.getAll()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (selectedTab == 0) "Usuarios con Room" else "Aula+ | Asistencia",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                actions = {
                    if (selectedTab == 0) {
                        TextButton(onClick = ::addUser) { Text("Agregar") }
                        TextButton(onClick = ::refreshUsers) { Text("Listar") }
                    } else {
                        TextButton(onClick = ::saveAttendance) { Text("Guardar") }
                        TextButton(onClick = ::refreshAttendance) { Text("Listar") }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
        ) {
            PrimaryTabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Parte guiada") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("CRUD asistencia") })
            }
            if (selectedTab == 0) {
                UserContent(firstName, lastName, users, message, { firstName = it }, { lastName = it }, ::addUser, ::refreshUsers, ::deleteLastUser)
            } else {
                AttendanceContent(
                    studentName, classroom, attendanceDate, attendanceStatus, selectedAttendanceId, attendanceRecords, message,
                    { studentName = it }, { classroom = it }, { attendanceDate = it }, { attendanceStatus = it },
                    ::saveAttendance, ::deleteSelectedAttendance, ::clearAttendanceForm
                ) { record ->
                    selectedAttendanceId = record.id
                    studentName = record.studentName
                    classroom = record.classroom
                    attendanceDate = record.attendanceDate
                    attendanceStatus = record.status
                    message = "Registro ${record.id} seleccionado: edítalo y presiona Guardar."
                }
            }
        }
    }
}

@Composable
private fun UserContent(
    firstName: String, lastName: String, users: List<User>, message: String,
    onFirstNameChange: (String) -> Unit, onLastNameChange: (String) -> Unit,
    onAdd: () -> Unit, onList: () -> Unit, onDeleteLast: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Registra y consulta usuarios en la tabla User.", style = MaterialTheme.typography.bodyMedium)
        OutlinedTextField(
            value = users.lastOrNull()?.uid?.toString().orEmpty(),
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Último ID (solo lectura)") },
            readOnly = true,
            singleLine = true
        )
        OutlinedTextField(firstName, onFirstNameChange, Modifier.fillMaxWidth(), label = { Text("Nombre") }, singleLine = true)
        OutlinedTextField(lastName, onLastNameChange, Modifier.fillMaxWidth(), label = { Text("Apellido") }, singleLine = true)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onAdd, Modifier.weight(1f)) { Text("Agregar usuario") }
            OutlinedButton(onList, Modifier.weight(1f)) { Text("Listar usuarios") }
        }
        OutlinedButton(onDeleteLast, Modifier.fillMaxWidth(), enabled = users.isNotEmpty()) { Text("Eliminar último registro") }
        StatusMessage(message)
        HorizontalDivider()
        Text("Usuarios almacenados", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        if (users.isEmpty()) {
            Text("No hay registros todavía.", style = MaterialTheme.typography.bodyMedium)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(users, key = { it.uid }) { user ->
                    Card(Modifier.fillMaxWidth()) {
                        Text("${user.uid}. ${user.firstName.orEmpty()} ${user.lastName.orEmpty()}", Modifier.padding(14.dp), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun AttendanceContent(
    studentName: String, classroom: String, attendanceDate: String, attendanceStatus: String,
    selectedAttendanceId: Int?, records: List<AttendanceRecord>, message: String,
    onStudentNameChange: (String) -> Unit, onClassroomChange: (String) -> Unit,
    onDateChange: (String) -> Unit, onStatusChange: (String) -> Unit,
    onSave: () -> Unit, onDelete: () -> Unit, onClear: () -> Unit, onSelect: (AttendanceRecord) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { OutlinedTextField(studentName, onStudentNameChange, Modifier.fillMaxWidth(), label = { Text("Estudiante") }, singleLine = true) }
        item { OutlinedTextField(classroom, onClassroomChange, Modifier.fillMaxWidth(), label = { Text("Aula") }, singleLine = true) }
        item { OutlinedTextField(attendanceDate, onDateChange, Modifier.fillMaxWidth(), label = { Text("Fecha") }, placeholder = { Text("dd/mm/aaaa") }, singleLine = true) }
        item {
            Text("Estado de asistencia", fontWeight = FontWeight.SemiBold)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("Presente", "Tardanza", "Ausente", "Justificado").forEach { status ->
                    FilterChip(attendanceStatus == status, { onStatusChange(status) }, label = { Text(status) })
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onSave, Modifier.weight(1f)) { Text(if (selectedAttendanceId == null) "Crear registro" else "Actualizar") }
                OutlinedButton(onClear, Modifier.weight(1f)) { Text("Limpiar") }
            }
        }
        item { OutlinedButton(onDelete, Modifier.fillMaxWidth(), enabled = selectedAttendanceId != null) { Text("Eliminar registro seleccionado") } }
        item { StatusMessage(message) }
        item { HorizontalDivider(); Spacer(Modifier.height(8.dp)); Text("Asistencia registrada", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
        if (records.isEmpty()) {
            item { Text("Crea un registro para iniciar el CRUD.", style = MaterialTheme.typography.bodyMedium) }
        } else {
            items(records, key = { it.id }) { record ->
                Card(Modifier.fillMaxWidth().clickable { onSelect(record) }) {
                    Column(Modifier.padding(14.dp)) {
                        Text(record.studentName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text("${record.classroom} · ${record.attendanceDate}")
                        Text(record.status, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusMessage(message: String) {
    Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
}
