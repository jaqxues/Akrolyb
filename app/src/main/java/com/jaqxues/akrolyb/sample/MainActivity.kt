package com.jaqxues.akrolyb.sample

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.jaqxues.akrolyb.pack.ModPackBase
import com.jaqxues.akrolyb.pack.PackException
import com.jaqxues.akrolyb.prefs.PrefManager
import com.jaqxues.akrolyb.sample.ipack.ModPack
import com.jaqxues.akrolyb.sample.ipack.PackFactory
import com.jaqxues.akrolyb.sample.prefs.Preferences
import com.jaqxues.akrolyb.utils.Security
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

class MainActivity : AppCompatActivity() {
    private var hasPermission by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        hasPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        setContent {
            Scaffold {
                if (hasPermission) {
                    AppUi()
                } else {
                    val launcher =
                        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
                            hasPermission = it
                        }
                    Button(onClick = {
                        launcher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }) {
                        Text("Request Storage Permission")
                    }
                }
            }
        }
    }

}

@Composable
fun AppUi() {
    val errorFlow = remember { MutableSharedFlow<String>() }
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current
    fun showError(msg: String) = scope.launch { errorFlow.emit(msg) }

    LazyColumn(
        Modifier
            .padding(32.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        item {
            Text("Akrolyb Sample", style = MaterialTheme.typography.h5)

            Spacer(Modifier.padding(32.dp))

            val ctx = LocalContext.current
            val storageAccess = remember { StorageAccessType.determineFromCtx(ctx) }

            Text("Storage Access Type: ${storageAccess?.displayName ?: "Unknown"}")

            Spacer(Modifier.padding(32.dp))

            Button(onClick = {
                try {
                    PrefManager.init(
                        File(
                            Environment.getExternalStorageDirectory(),
                            "Akrolyb/SomeFile.json"
                        ), Preferences::class
                    )
                    showError("Loaded Preferences successfully")
                } catch (t: Throwable) {
                    showError("Preferences failed to load")
                }
            }) {
                Text("Init Prefs Module")
            }

            Spacer(Modifier.padding(16.dp))

            Button(onClick = {
                val packFile = File(Environment.getExternalStorageDirectory(), "Akrolyb/Pack.jar")
                if (packFile.exists()) {
                    try {
                        val value = if (BuildConfig.DEBUG) null else Security.certificateFromApk(
                            ctx,
                            BuildConfig.APPLICATION_ID
                        )
                        val pack: ModPack = ModPackBase.buildPack(
                            ctx, packFile, value, packBuilder = PackFactory
                        )
                        pack.showSuccessToast(ctx)
                    } catch (t: PackException) {
                        showError("${t::class.java.simpleName}: ${t.message}")
                    }
                } else {
                    showError("Pack does not exist")
                }
            }) {
                Text("Init Module Pack")
            }
        }
    }

    val msg by errorFlow.collectAsState(null)
    msg?.let {
        Toast.makeText(ctx, it, Toast.LENGTH_LONG).show()
    }
}

@Preview
@Composable
fun AppUiPreview() {
    Scaffold { AppUi() }
}

enum class StorageAccessType(val displayName: String) {
    LEGACY("Legacy"), DENIED("Denied"), SCOPED("Scoped");

    companion object {
        fun determineFromCtx(ctx: Context) = try {
            val hasWritePermission = ContextCompat.checkSelfPermission(
                ctx, Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED

            when {
                !hasWritePermission -> DENIED

                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                        && !Environment.isExternalStorageLegacy() -> SCOPED

                else -> LEGACY
            }
        } catch (t: Throwable) {
            Timber.e(t, "Could not determine Storage Access Type")
            null
        }
    }
}