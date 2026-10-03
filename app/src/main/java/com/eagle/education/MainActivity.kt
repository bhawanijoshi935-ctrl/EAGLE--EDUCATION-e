package com.eagle.education

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EagleEducationApp()
        }
    }
}

data class AppUser(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "student"
)

data class Course(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val ownerId: String = ""
)

data class Video(
    val id: String = "",
    val title: String = "",
    val duration: String = "",
    val category: String = "",
    val ownerId: String = ""
)

data class Note(
    val id: String = "",
    val title: String = "",
    val subject: String = "",
    val content: String = "",
    val pages: Int = 0,
    val ownerId: String = ""
)

@Composable
fun EagleEducationApp() {
    val navController = rememberNavController()
    var currentUser by remember { mutableStateOf<AppUser?>(null) }

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onLoginSuccess = { user ->
                    currentUser = user
                    navController.navigate(
                        if (user.role == "owner") "ownerDashboard" else "studentDashboard"
                    ) {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onGoToSignup = { navController.navigate("signup") }
            )
        }

        composable("signup") {
            SignUpScreen(
                onSignUpSuccess = { user ->
                    currentUser = user
                    navController.navigate(
                        if (user.role == "owner") "ownerDashboard" else "studentDashboard"
                    ) {
                        popUpTo("signup") { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable("studentDashboard") {
            StudentDashboardScreen(
                user = currentUser ?: AppUser(),
                onLogout = {
                    FirebaseAuth.getInstance().signOut()
                    currentUser = null
                    navController.navigate("login") {
                        popUpTo(0)
                    }
                },
                navController = navController
            )
        }

        composable("ownerDashboard") {
            OwnerDashboardScreen(
                user = currentUser ?: AppUser(),
                onLogout = {
                    FirebaseAuth.getInstance().signOut()
                    currentUser = null
                    navController.navigate("login") {
                        popUpTo(0)
                    }
                },
                navController = navController
            )
        }

        composable("courses") {
            CoursesScreen(navController)
        }

        composable("videos") {
            VideosScreen(navController)
        }

        composable("notes") {
            NotesScreen(navController)
        }

        composable("uploadVideo") {
            UploadVideoScreen(
                ownerId = currentUser?.uid ?: "",
                onBack = { navController.popBackStack() }
            )
        }

        composable("uploadNotes") {
            UploadNotesScreen(
                ownerId = currentUser?.uid ?: "",
                onBack = { navController.popBackStack() }
            )
        }

        composable("manageVideos") {
            ManageVideosScreen(navController)
        }

        composable("manageNotes") {
            ManageNotesScreen(navController)
        }
    }
}

@Composable
fun LoginScreen(
    onLoginSuccess: (AppUser) -> Unit,
    onGoToSignup: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf("") }

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF5F7FF)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("EAGLE EDUCATION", fontSize = 28.sp, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )

                if (errorText.isNotEmpty()) {
                    Text(errorText, color = Color.Red)
                }

                Button(
                    onClick = {
                        loading = true
                        FirebaseAuth.getInstance()
                            .signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                loading = false
                                if (task.isSuccessful) {
                                    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                                    val db = FirebaseFirestore.getInstance()
                                    db.collection("users").document(uid).get()
                                        .addOnSuccessListener { doc ->
                                            val user = doc.toObject(AppUser::class.java)
                                            if (user != null) onLoginSuccess(user)
                                            else errorText = "User not found"
                                        }
                                        .addOnFailureListener {
                                            errorText = it.message ?: "Login failed"
                                        }
                                } else {
                                    errorText = task.exception?.message ?: "Login failed"
                                }
                            }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !loading
                ) {
                    Text("Login")
                }

                TextButton(onClick = onGoToSignup) {
                    Text("Create new account")
                }
            }
        }
    }
}

@Composable
fun SignUpScreen(
    onSignUpSuccess: (AppUser) -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("student") }
    var loading by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf("") }

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF5F7FF)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("Create Account", fontSize = 28.sp, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation()
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilterChip(
                        selected = role == "student",
                        onClick = { role = "student" },
                        label = { Text("Student") }
                    )
                    FilterChip(
                        selected = role == "owner",
                        onClick = { role = "owner" },
                        label = { Text("Owner") }
                    )
                }

                if (errorText.isNotEmpty()) {
                    Text(errorText, color = Color.Red)
                }

                Button(
                    onClick = {
                        loading = true
                        FirebaseAuth.getInstance()
                            .createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                                    val user = AppUser(
                                        uid = uid,
                                        name = name,
                                        email = email,
                                        role = role
                                    )
                                    FirebaseFirestore.getInstance()
                                        .collection("users")
                                        .document(uid)
                                        .set(user)
                                        .addOnSuccessListener {
                                            loading = false
                                            onSignUpSuccess(user)
                                        }
                                        .addOnFailureListener {
                                            loading = false
                                            errorText = it.message ?: "Sign up failed"
                                        }
                                } else {
                                    loading = false
                                    errorText = task.exception?.message ?: "Sign up failed"
                                }
                            }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !loading
                ) {
                    Text("Sign Up")
                }

                TextButton(onClick = onBack) {
                    Text("Back to Login")
                }
            }
        }
    }
}

@Composable
fun StudentDashboardScreen(
    user: AppUser,
    onLogout: () -> Unit,
    navController: NavController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Student Panel") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Welcome ${user.name}", fontSize = 24.sp, fontWeight = FontWeight.Bold)

            DashboardCard("Courses", "View courses", Icons.Default.LibraryBooks) {
                navController.navigate("courses")
            }
            DashboardCard("Videos", "Watch lessons", Icons.Default.VideoLibrary) {
                navController.navigate("videos")
            }
            DashboardCard("Notes", "Read notes", Icons.Default.Notes) {
                navController.navigate("notes")
            }
        }
    }
}

@Composable
fun OwnerDashboardScreen(
    user: AppUser,
    onLogout: () -> Unit,
    navController: NavController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Owner Panel") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Welcome ${user.name}", fontSize = 24.sp, fontWeight = FontWeight.Bold)

            DashboardCard("Upload Video", "Add lesson video", Icons.Default.VideoCall) {
                navController.navigate("uploadVideo")
            }
            DashboardCard("Upload Notes", "Add study notes", Icons.Default.NoteAdd) {
                navController.navigate("uploadNotes")
            }
            DashboardCard("Manage Videos", "View all videos", Icons.Default.VideoLibrary) {
                navController.navigate("manageVideos")
            }
            DashboardCard("Manage Notes", "View all notes", Icons.Default.Notes) {
                navController.navigate("manageNotes")
            }
        }
    }
}

@Composable
fun DashboardCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Column {
                    Text(title, fontWeight = FontWeight.Bold)
                    Text(subtitle, color = Color.Gray)
                }
            }
            Icon(Icons.Default.ArrowForward, contentDescription = null)
        }
    }
}

@Composable
fun CoursesScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val courses = remember { mutableStateListOf<Course>() }

    LaunchedEffect(Unit) {
        db.collection("courses")
            .get()
            .addOnSuccessListener { snap ->
                courses.clear()
                for (doc in snap.documents) {
                    val course = doc.toObject(Course::class.java)
                    if (course != null) courses.add(course)
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Courses") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(courses) { course ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(course.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(course.description, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun VideosScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val videos = remember { mutableStateListOf<Video>() }

    LaunchedEffect(Unit) {
        db.collection("videos")
            .get()
            .addOnSuccessListener { snap ->
                videos.clear()
                for (doc in snap.documents) {
                    val video = doc.toObject(Video::class.java)
                    if (video != null) videos.add(video)
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Videos") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(videos) { video ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(video.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(video.duration, color = Color.Gray)
                        Text(video.category, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
fun NotesScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val notes = remember { mutableStateListOf<Note>() }

    LaunchedEffect(Unit) {
        db.collection("notes")
            .get()
            .addOnSuccessListener { snap ->
                notes.clear()
                for (doc in snap.documents) {
                    val note = doc.toObject(Note::class.java)
                    if (note != null) notes.add(note)
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notes") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(notes) { note ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(note.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(note.subject, color = Color.Gray)
                        Text("${note.pages} pages", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
fun UploadVideoScreen(
    ownerId: String,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Upload Video") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = duration, onValueChange = { duration = it }, label = { Text("Duration") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") }, modifier = Modifier.fillMaxWidth())

            Button(
                onClick = {
                    val video = Video(
                        id = UUID.randomUUID().toString(),
                        title = title,
                        duration = duration,
                        category = category,
                        ownerId = ownerId
                    )
                    FirebaseFirestore.getInstance()
                        .collection("videos")
                        .document(video.id)
                        .set(video)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Upload")
            }
        }
    }
}

@Composable
fun UploadNotesScreen(
    ownerId: String,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var pages by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Upload Notes") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Subject") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = pages, onValueChange = { pages = it }, label = { Text("Pages") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("Content") }, modifier = Modifier.fillMaxWidth().height(160.dp))

            Button(
                onClick = {
                    val note = Note(
                        id = UUID.randomUUID().toString(),
                        title = title,
                        subject = subject,
                        content = content,
                        pages = pages.toIntOrNull() ?: 0,
                        ownerId = ownerId
                    )
                    FirebaseFirestore.getInstance()
                        .collection("notes")
                        .document(note.id)
                        .set(note)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Upload")
            }
        }
    }
}

@Composable
fun ManageVideosScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val videos = remember { mutableStateListOf<Video>() }

    LaunchedEffect(Unit) {
        db.collection("videos")
            .get()
            .addOnSuccessListener { snap ->
                videos.clear()
                for (doc in snap.documents) {
                    val video = doc.toObject(Video::class.java)
                    if (video != null) videos.add(video)
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Videos") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(videos) { video ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(video.title, fontWeight = FontWeight.Bold)
                            Text(video.duration, color = Color.Gray)
                        }
                        IconButton(onClick = {
                            db.collection("videos").document(video.id).delete()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ManageNotesScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val notes = remember { mutableStateListOf<Note>() }

    LaunchedEffect(Unit) {
        db.collection("notes")
            .get()
            .addOnSuccessListener { snap ->
                notes.clear()
                for (doc in snap.documents) {
                    val note = doc.toObject(Note::class.java)
                    if (note != null) notes.add(note)
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Notes") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(notes) { note ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(note.title, fontWeight = FontWeight.Bold)
                            Text(note.subject, color = Color.Gray)
                        }
                        IconButton(onClick = {
                            db.collection("notes").document(note.id).delete()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                        }
                    }
                }
            }
        }
    }
}
