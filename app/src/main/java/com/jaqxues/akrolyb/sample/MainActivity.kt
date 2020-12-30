package com.jaqxues.akrolyb.sample

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.jaqxues.akrolyb.pack.ModPackBase
import com.jaqxues.akrolyb.pack.PackException
import com.jaqxues.akrolyb.prefs.PrefManager
import com.jaqxues.akrolyb.sample.ipack.ModPack
import com.jaqxues.akrolyb.sample.ipack.PackFactory
import com.jaqxues.akrolyb.sample.prefs.Preferences
import com.jaqxues.akrolyb.utils.Security
import java.io.File

private const val PERM_REQ_CODE = 0xcafe

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val listener = View.OnClickListener {
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERM_REQ_CODE
                )
            }
            findViewById<View>(R.id.init_pref_btn).setOnClickListener(listener)
            findViewById<View>(R.id.init_pack_btn).setOnClickListener(listener)
        } else init()
    }

    private fun init() {
        // Make sure App has permissions to read/write to external Storage
        findViewById<View>(R.id.init_pref_btn).setOnClickListener {
            PrefManager.init(
                File(Environment.getExternalStorageDirectory(), "Akrolyb/SomeFile.json"), Preferences::class
            )
        }

        findViewById<View>(R.id.init_pack_btn).setOnClickListener {
            val packFile = File(Environment.getExternalStorageDirectory(), "Akrolyb/Pack.jar")
            if (packFile.exists()) {
                try {
                    val value = if (BuildConfig.DEBUG) null else Security.certificateFromApk(this, BuildConfig.APPLICATION_ID)
                    val pack: ModPack = ModPackBase.buildPack(
                        this, packFile, value, PackFactory
                    )
                    pack.showSuccessToast(this)
                } catch (t: PackException) {
                    Toast.makeText(this, "${t::class.java.simpleName}: ${t.message}", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "Pack does not exist", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == PERM_REQ_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                init()
            else
                Toast.makeText(this, "Permission not granted", Toast.LENGTH_LONG).show()
        }
    }
}
