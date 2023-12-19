package com.ubx.notificationtest

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ubx.notificationtest.databinding.ActivityMainBinding
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private lateinit var mainBinding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)
        // 获取NotificationManager实例
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // 判断安卓版本是否为Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 安卓版本为 8.0+ 则使用NotificationChannel类构建一个通知渠道，
            // 并调用NotificationManager的createNotificationChannel()方法完成创建
            /*val channel =
                NotificationChannel("normal", "Normal", NotificationManager.IMPORTANCE_DEFAULT)*/
            val channel =
                NotificationChannel("important", "Important", NotificationManager.IMPORTANCE_HIGH)
            manager.createNotificationChannel(channel)
        }

        mainBinding.sendNotice.setOnClickListener {
            // 检查是否已经有了通知权限
            if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
                // 如果没有权限，向用户显示一个对话框，引导他们去设置中开启
                AlertDialog.Builder(this)
                    .setTitle("需要通知权限")
                    .setMessage("此应用需要您允许通知权限才能正常工作。请点击“设置”按钮，然后在设置中开启通知权限。")
                    .setPositiveButton("设置") { _, _ ->
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package", packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    }
                    .setNegativeButton("取消") { _, _ ->
                        /*这个方法需要两个参数（一个 DialogInterface 对象和一个表示哪个按钮被点击的 int 值），
                        但是我们不关心这两个参数的具体值，所以我们用下划线 _ 来代替它们*/
                        // 用户点击取消，结束应用
                        finish()
                    }
                    .show()
            } else {
                val intent = Intent(this, NotificationActivity::class.java)
                // 从 Android 12（API 级别 31）开始，创建 PendingIntent 时必须指定 FLAG_IMMUTABLE 或 FLAG_MUTABLE。
                // 这是因为 Android 12 对 PendingIntent 的行为进行了更改，以提高应用的安全性。
                /*
                *
                * FLAG_IMMUTABLE：这个标志表示 PendingIntent 是不可变的，也就是说，一旦创建，就不能更改。这是推荐的选项，因为它可以防止潜在的安全问题。
                * FLAG_MUTABLE：这个标志表示 PendingIntent 是可变的，也就是说，可以在创建后进行更改。只有在某些功能依赖于 PendingIntent 的可变性时，才应该使用这个选项，例如，需要与内联回复或气泡一起使用。
                * */
                val pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                // 已经有权限，可以进行下一步操作
                // AndroidX库中提供了一个NotificationCompat类，使用这个类的构造器创建
                // Notification对象，就可以保证我们的程序在所有Android系统版本上都能正常工作了
//                val notification = NotificationCompat.Builder(this, "normal")
                val notification = NotificationCompat.Builder(this, "important")
                    // setContentTitle()方法用于指定通知的标题内容，下拉系统状态栏就可以看到这部分内容。
                    .setContentTitle("this is content title")
                    // setContentText()方法用于指定通知的正文内容，同样下拉系统状态栏就可以看到这部分内容。
                    .setContentText("this is content text".repeat(Random.nextInt(5, 10)))
                    // setSmallIcon()方法
                    //用于设置通知的小图标，注意，只能使用纯alpha图层的图片进行设置，小图标会显示在系统状态栏上
                    .setSmallIcon(R.drawable.small_icon)
                    // setLargeIcon()方法用于设置通知的大图标，当下拉系统状态栏时，就可以看到设置的大图标了。
                    .setLargeIcon(
                        BitmapFactory.decodeResource(
                            resources,
                            R.drawable.large_icon
                        )
                    )
                    // 通过`PendingIntent`构建一个延迟执行的“意图”，当用户点击这条通知时就会执行相应的逻辑。
                    .setContentIntent(pi)
                    // 设置自动取消，当点击通知时，通知会自动取消
                    .setAutoCancel(true)
                    // 设置Style
                    .setStyle(NotificationCompat.BigPictureStyle().bigPicture(BitmapFactory.decodeResource(resources, R.drawable.big_image)))
                    .build()
                // 调用NotificationManager的notify()方法就可以让通知显示出来了。notify()方法接收两个参数：
                // 第一个参数是id，要保证为每个通知指定的id都是不同的；第二个参数则是Notification对象，
                // 这里直接将我们刚刚创建好的Notification对象传入即可。
                manager.notify(1, notification)
            }
        }
    }

    companion object {
        const val TAG = "MainActivity"
    }
}