package koziol.mooo.com.veedback4

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import kotlinx.coroutines.launch
import koziol.mooo.com.veedback4.models.Problem
import koziol.mooo.com.veedback4.ui.theme.Veedback4Theme
import java.io.File
import java.util.UUID

class MainActivity : ComponentActivity() {

    private val com = Communicator()

    private val takeImageLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                processPicture()
                //Do something with the image uri, go nuts!
            }
        }

    var problems: List<Problem> = ArrayList<Problem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Veedback4Theme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    //NavDrawer(this)
                    MyScaffold(this)
                }
            }
        }
    }

    suspend fun addProblem() {
        com.addProblem()
    }

    suspend fun getProblems(): List<Problem> {
        val com = Communicator()

        return com.getProblems()
    }

    fun takePicture() {

        val file = File.createTempFile(UUID.randomUUID().toString(), "",
            applicationContext.externalCacheDir)
        println(file.toString())

        val uri = FileProvider.getUriForFile(this.applicationContext, "koziol.mooo.com.veedback4.provider", file)
        takeImageLauncher.launch(uri)

    }

    private fun processPicture() {
        println("picture taken")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyScaffold(activity: MainActivity) {
    val coroutineScope = rememberCoroutineScope()

    var problemL = remember { mutableStateOf<List<Problem>?>(null) }


    Scaffold(
        topBar = { TopAppBar(title = { Text("Veedback") }) },
        bottomBar = {
            BottomAppBar {
                IconButton(onClick = {
                    coroutineScope.launch {
                        problemL.value = activity.getProblems()
                    }
                }) {
                    Icon(Icons.Filled.Menu, contentDescription = "Localized description")
                }
                IconButton(onClick = { activity.takePicture() }) {
                    Icon(Icons.Filled.Add, contentDescription = "Localized description")
                }
            }
        },
        content = { contentPadding ->
            Box(modifier = Modifier.padding(contentPadding)) {

                ProblemList(problems = problemL.value)
            }
        }
    )
}

@Composable
fun ProblemList(problems: List<Problem>?) {
    if (problems != null) {
        LazyColumn {
            items(problems) { problem ->
                ProblemCard(problem)

            }
        }
    }
}

@Composable
fun ProblemCard(problem: Problem) {
    Row {
        // image
        Column {
            Text("Geschoss: " + problem.floor)
            Text("Farbe: " + problem.color)
            // color
            // rating
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SideDrawer(activity: MainActivity) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
// icons to mimic drawer destinations
    val items = listOf(Icons.Default.Favorite, Icons.Default.Face, Icons.Default.Email)
    val selectedItem = remember { mutableStateOf(items[0]) }
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(12.dp))
                items.forEach { item ->
                    NavigationDrawerItem(
                        icon = { Icon(item, contentDescription = null) },
                        label = { Text(item.name) },
                        selected = item == selectedItem.value,
                        onClick = {
                            scope.launch { drawerState.close() }
                            selectedItem.value = item
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                /*Text(text = if (drawerState.isClosed) ">>> Swipe >>>" else "<<< Swipe <<<")
                Spacer(Modifier.height(20.dp))
                Button(onClick = { scope.launch { drawerState.open() } }) {
                    Text("Click to open")
                }*/
                if (selectedItem.value == items[0]) {
                    ColorSelector("Farbe")
                } else if (selectedItem.value == items[1]) {
                    FloorSelector("Geschoss:")
                }
                Commands(activity)
            }
        }
    )
}

@Composable
fun ColorSelector(name: String, modifier: Modifier = Modifier) {
    Text(
        text = name,
        modifier = modifier
    )
    val radioOptions = listOf("grau", "gelb", "grün")
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(radioOptions[0]) }
// Note that Modifier.selectableGroup() is essential to ensure correct accessibility behavior
    Column(Modifier.selectableGroup()) {
        radioOptions.forEach { text ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .selectable(
                        selected = (text == selectedOption),
                        onClick = { onOptionSelected(text) },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (text == selectedOption),
                    onClick = null // null recommended for accessibility with screenreaders
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}

@Composable
fun FloorSelector(name: String, modifier: Modifier = Modifier) {
    Text(
        text = name,
        modifier = modifier
    )
    val radioOptions = listOf("Erdgeschoss", "Obergeschoss")
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(radioOptions[0]) }
// Note that Modifier.selectableGroup() is essential to ensure correct accessibility behavior
    Column(Modifier.selectableGroup()) {
        radioOptions.forEach { text ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .selectable(
                        selected = (text == selectedOption),
                        onClick = { onOptionSelected(text) },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (text == selectedOption),
                    onClick = null // null recommended for accessibility with screenreaders
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}

@Composable
fun Commands(activity: MainActivity, modifier: Modifier = Modifier) {
    val coroutineScope = rememberCoroutineScope()
    Row {
        TextButton(onClick = { /* Do something! */ }) { Text("Abbrechen") }
        Button(onClick = { coroutineScope.launch { activity.addProblem() } }) { Text("Route hinzufügen") }
    }

}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Veedback4Theme {
        ColorSelector("Android")
    }
}