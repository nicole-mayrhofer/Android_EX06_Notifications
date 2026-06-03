package at.fhooe.sail.mc.notifications

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.ProgressDialog.show
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.VibrationEffect
import android.os.VibratorManager
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import at.fhooe.sail.mc.notifications.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar

const val TAG: String = "Notification Test"

class MainActivity : AppCompatActivity(), View.OnClickListener {

    val launcher: ActivityResultLauncher<String> = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { res: Boolean ->
        if (res) {
            showNotification()
        } else {
            Log.e(TAG, "no permission to post notifications!!!")
        }
    }


    lateinit var binding: ActivityMainBinding
    lateinit var nChannel: NotificationChannel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        with(binding) {                                            // with ist wie scope function
            activityMainButtonToast.setOnClickListener(this@MainActivity)      // this der MainActivity
            activityMainButtonSnackbar.setOnClickListener(this@MainActivity)
            activityMainButtonNotification.setOnClickListener(this@MainActivity)
            activityMainButtonVibrate.setOnClickListener(this@MainActivity)
            activityMainButtonDialog.setOnClickListener(this@MainActivity)
        }

        // SET UP NOTIFICATION CHANNEL
        val nMgr: NotificationManager = getSystemService(NotificationManager::class.java)
        nChannel = NotificationChannel(
            "dreizehn",
            "NotificationTestChannel",
            NotificationManager.IMPORTANCE_DEFAULT)
        nMgr.createNotificationChannel(nChannel)

    }

    override fun onClick(v: View?) {
        when (v?.id) {  // habe ich eine view? ist eigentlich immer TRUE
            R.id.activity_main_button_toast -> {
                Log.e(TAG, "MainActivity::onClick()...show Toast")
                showToast("Cheers")
            }

            R.id.activity_main_button_snackbar -> {
                Log.e(TAG, "MainActivity::onClick()...show Snackbar")
                showSnackbar("Want some snacks?")
            }

            R.id.activity_main_button_notification -> {
                Log.e(TAG, "MainActivity::onClick()...show Notifications")
                showNotification()
            }

            R.id.activity_main_button_vibrate -> {
                Log.e(TAG, "MainActivity::onClick()...vibrate")
                vibrate()
            }

            R.id.activity_main_button_dialog -> {
                Log.e(TAG, "MainActivity::onClick()...show Dialog")
                presentDialog()
            }
            // nicht vergessen das ELSE (default) hinzuzufügen
            else -> {
                Log.e(TAG, "MainActivity::onClick()...unexpected ID encountered")
            }
        }
    }

    private fun showSnackbar(m: String) {
        Snackbar.make(binding.root, m, Snackbar.LENGTH_INDEFINITE).apply {
            setAction(android.R.string.cancel) {
                Log.i(TAG, "MainActivity::Snackbar::CancelAction...")
                dismiss()       // snackbar existiert bis wir sie wegklicken
            }
            show()
        }
    }

    //function
    fun showToast(m: String): Unit {     // Unit == Void (kein Rückgabetyp
        Toast.makeText(this, m, Toast.LENGTH_SHORT).show()  // CONTEXT = this = MainActivity
        // zeile 36: show toast muss ich da definieren
    }

    // permission für die Vibration
    fun vibrate() {
        val vMgr: VibratorManager = getSystemService(VibratorManager::class.java)
        val seq: LongArray = longArrayOf(0, 500, 100, 500)
        val vEffect: VibrationEffect = VibrationEffect.createWaveform(seq, -1)
        vMgr.defaultVibrator.vibrate(vEffect)
    }

    fun presentDialog() {
        val bob: AlertDialog.Builder = AlertDialog.Builder(this)

        val view: View = layoutInflater.inflate(R.layout.activity_main_dialog_test, null)

        bob.setTitle("Ein Test-Titel")
            //.setMessage("Lies mich!") --- wird ersetzt durch die view
            .setView(view)
            .setNeutralButton(android.R.string.ok, object : DialogInterface.OnClickListener {
                override fun onClick(p0: DialogInterface?, which: Int) {
                    val etName: EditText = view.findViewById(R.id.dialog_data_name)
                    val etCallSign: EditText = view.findViewById(R.id.dialog_data_callsign)

                    val name: String = etName.text.toString()
                    val callsign: String = etCallSign.text.toString()
                    Log.i(TAG, "on click detected ($name/$callsign) ... ")
                }
            })
            .create()
            .show()
    }
    fun showNotification() {
        val i: Intent = Intent(this, MainActivity::class.java)
        val pi: PendingIntent = PendingIntent.getActivity(
            this,
            42,
            i,
            PendingIntent.FLAG_IMMUTABLE
            )

        val bob: Notification.Builder = Notification.Builder(this, nChannel.id)
        bob.setSmallIcon(R.drawable.heart_24dp)
        bob.setContentTitle("Notification Title")
        bob.setContentText("Eine kleine Message ...")
        bob.setAutoCancel(true)
        bob.setContentIntent(pi)
        val noti: Notification = bob.build()

        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            val nMgr: NotificationManager = getSystemService(NotificationManager::class.java)
            nMgr.notify(1337, noti)
        } else {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

