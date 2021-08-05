package com.jaqxues.akrolyb.sample.target

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jaqxues.akrolyb.sample.target.ui.theme.AkrolybTheme

class MainActivity : ComponentActivity() {
    private var errorMsg by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        states["Hook Example"] = Utils.hookTarget()
        setContent {
            AkrolybTheme {
                Scaffold {
                    AppUi(errorMsg, states)
                }
            }
        }
    }

    @Suppress("unused")
    fun testInvokeMethod() {
        states["Method Invocation"] = true
    }


    @Suppress("unused")
    fun showErrorMsg(msg: String) {
        errorMsg = msg
    }

    companion object {
        private val states = mutableStateMapOf(
            "Initial Setup" to true,
            "Method Invocation" to false,
            "Hook Example" to false,
            "Initialized Pack Hooks" to false
        )
    }
}

@Composable
fun AppUi(
    error: String?,
    states: SnapshotStateMap<String, Boolean>
) {
    LazyColumn(
        Modifier
            .padding(8.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        item {
            Text("Akrolyb Sample Hook Target", style = MaterialTheme.typography.h5)

            Spacer(Modifier.padding(32.dp))

            val ctx = LocalContext.current
            val storageAccess = remember { StorageAccessType.determineFromCtx(ctx) }

            Text("Storage Access Type: ${storageAccess?.displayName ?: "Unknown"}")

            Spacer(Modifier.padding(32.dp))
        }

        if (error != null) {
            item {
                Text("An error occurred: ")
                Text(error)
                Spacer(Modifier.padding(32.dp))
            }
        }

        items(states.toList()) {
            Row(
                Modifier
                    .let { mod ->
                        if (!it.second)
                            mod
                                .padding(8.dp)
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colors.error,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(8.dp)
                        else mod.padding(16.dp)
                    }
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(it.first)
                Text(
                    if (it.second) "Success" else "Error",
                    color = if (it.second) Color.Unspecified else MaterialTheme.colors.error
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    Scaffold {
        AppUi(
            error = "Example Error Message",
            states = remember {
                mutableStateMapOf(
                    "Preview State 1" to true,
                    "Preview State 2" to false,
                    "Preview State 3" to true,
                    "Preview State 4" to true,
                    "Preview State 5" to false
                )
            }
        )
    }
}