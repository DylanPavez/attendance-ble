package cl.duocuc.docente

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.*
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.ParcelUuid
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.*

class MainActivity : ComponentActivity() {

    /** UUID que anunciaremos como baliza BLE */
    private val SERVICE_UUID: UUID =
        UUID.fromString("0000fee7-0000-1000-8000-00805f9b34fb")

    /** Permisos requeridos a partir de Android 12 (API 31) */
    private val requiredPerms = arrayOf(
        Manifest.permission.BLUETOOTH_ADVERTISE,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    private lateinit var advertiser: BluetoothLeAdvertiser
    private var isAdvertising by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ── Solicitar permisos si alguno falta ──
        if (
            requiredPerms.any {
                ContextCompat.checkSelfPermission(this, it) !=
                        PackageManager.PERMISSION_GRANTED
            }
        ) {
            ActivityCompat.requestPermissions(this, requiredPerms, 100)
        }

        advertiser = BluetoothAdapter.getDefaultAdapter().bluetoothLeAdvertiser

        setContent {
            Button(onClick = { toggleAdvertise() }) {
                Text(if (isAdvertising) "Finalizar clase" else "Activar lista")
            }
        }
    }

    /** Alterna entre iniciar y detener la baliza */
    private fun toggleAdvertise() {
        if (isAdvertising) stopAdvertising() else startAdvertising()
    }

    /** Inicia la baliza BLE */
    @SuppressLint("MissingPermission")
    private fun startAdvertising() {
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
            .setConnectable(false)
            .build()

        val data = AdvertiseData.Builder()
            .addServiceUuid(ParcelUuid(SERVICE_UUID))
            .setIncludeDeviceName(false)
            .build()

        advertiser.startAdvertising(settings, data, advertiseCallback)
        isAdvertising = true
    }

    /** Detiene la baliza BLE */
    @SuppressLint("MissingPermission")
    private fun stopAdvertising() {
        advertiser.stopAdvertising(advertiseCallback)
        isAdvertising = false
    }

    /** Notificaciones de éxito / error del anunciante */
    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            Log.i("BLE-DOCENTE", "Baliza iniciada correctamente")
        }

        override fun onStartFailure(errorCode: Int) {
            Log.e("BLE-DOCENTE", "Error $errorCode al iniciar baliza")
        }
    }
}
