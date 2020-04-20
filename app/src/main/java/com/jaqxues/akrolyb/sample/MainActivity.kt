package com.jaqxues.akrolyb.sample

import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import com.jaqxues.akrolyb.pack.ModPack
import com.jaqxues.akrolyb.prefs.PrefManager
import com.jaqxues.akrolyb.sample.ipack.AMetadata
import com.jaqxues.akrolyb.sample.ipack.AModPack
import com.jaqxues.akrolyb.sample.ipack.toPackMetadata
import com.jaqxues.akrolyb.sample.prefs.Preferences
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.jar.Attributes

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Make sure App has permissions to read/write to external Storage
        init_pref_btn.setOnClickListener {
            PrefManager.init(
                File(Environment.getExternalStorageDirectory(), "SomeFile.json"), Preferences::class
            )
        }

        init_pack_btn.setOnClickListener {
            val pack = ModPack.buildPack<AMetadata, AModPack>(
                this,
                File(Environment.getExternalStorageDirectory(), "CompiledModPack.jar_unsigned.jar"), null, Attributes::toPackMetadata)
            pack.loadFeatureManager()
        }
    }
}
