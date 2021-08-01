package com.jaqxues.akrolyb.sample

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
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

    @Composable
    private fun AppUi() {
        val errorFlow = remember { MutableSharedFlow<String>() }
        val scope = rememberCoroutineScope()
        val ctx = LocalContext.current
        fun showError(msg: String) = scope.launch { errorFlow.emit(msg) }

        Column {
            Button(onClick = {
                PrefManager.init(
                    File(
                        Environment.getExternalStorageDirectory(),
                        "Akrolyb/SomeFile.json"
                    ), Preferences::class
                )
            }) {
                Text("Init Prefs Module")
            }

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

        val msg by errorFlow.collectAsState(null)
        msg?.let {
            Toast.makeText(ctx, it, Toast.LENGTH_LONG).show()
        }
    }
}
