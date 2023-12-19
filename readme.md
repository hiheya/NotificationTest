# 使用通知

通知（notification）是Android系统中比较有特色的一个功能，当某个应用程序希望向用户发出一些提示信息，而该应用程序又不在前台运行时，就可以借助通知来实现。发出一条通知后，手机最上方的状态栏中会显示一个通知的图标，下拉状态栏后可以看到通知的详细内容。Android的通知功能自推出以来就大获成功，连iOS系统也在5.0版本之后加入了类似的功能。

### 1.1 创建通知渠道

Android 8.0系统引入了通知渠道这个概念。什么是通知渠道呢？顾名思义，就是每条通知都要属于一个对应的渠道。每个应用程序都可以自由地创建当前应用拥有哪些通知渠道，但是这些通知渠道的控制权是掌握在用户手上的。用户可以自由地选择这些通知渠道的重要程度，是否响铃、是否振动或者是否要关闭这个渠道的通知。

拥有了这些控制权之后，用户就再也不用害怕那些垃圾通知的打扰了，因为用户可以自主地选择关心哪些通知、不关心哪些通知。

而我们的应用程序如果想要发出通知，也必须创建自己的通知渠道才行，下面我们就来学习一下创建通知渠道的详细步骤。

首先需要一个`NotificationManager`对通知进行管理，可以通过调用Context的`getSystemService()`方法获取。`getSystemService()`方法接收一个字符串参数用于确定获取系统的哪个服务，这里我们传入`Context.NOTIFICATION_SERVICE`即可。因此，获取`NotificationManager`的实例就可以写成：

```kotlin
val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
```

接下来要使用`NotificationChannel`类构建一个通知渠道，并调用`NotificationManager`的`createNotificationChannel()`方法完成创建。由于`NotificationChannel`类和`createNotificationChannel()`方法都是Android 8.0系统中新增的API，因此我们在使用的时候还需要进行版本判断才可以，写法如下：

```kotlin
if (Build.VERSION.SDK_INT >= Build.VERSION_CODE.O) {
    val channel = NotificationChannel(channelId, channelName, importance)
    manager.createNotificationChannel(channel)
}
```

创建一个通知渠道至少需要**渠道ID**、**渠道名称**以及**重要等级**这**3个参数**，其中**渠道ID可以随便定义，只要保证全局唯一性就可以**。**渠道名称是给用户看的，需要可以清楚地表达这个渠道的用途**。**通知的重要等级主要有IMPORTANCE_HIGH、IMPORTANCE_DEFAULT、IMPORTANCE_LOW、IMPORTANCE_MIN这几种，对应的重要程度依次从高到低**。不同的重要等级会决定通知的不同行为，后面我们会通过具体的例子进行演示。当然这里只是初始状态下的重要等级，用户可以随时手动更改某个通知渠道的重要等级，开发者是无法干预的。

下面来看一下通知的使用方法。通知的用法还是比较灵活的，既可以在`Activity`里创建，也可以在`BroadcastReceiver`里创建，当然还可以在后面我们即将学习的`Service`里创建。相比于`BroadcastReceiver`和`Service`，在`Activity`里创建通知的场景还是比较少的，因为一般只有当程序进入后台的时候才需要使用通知。

### 1.2 通知的基本用法

不过，无论是在哪里创建通知，整体的步骤都是相同的，下面我们就来看一下创建通知的详细步骤。

首先需要使用一个`Builder`构造器来创建`Notification`对象，但问题在于，Android系统的每一个版本都会对通知功能进行或多或少的修改，API不稳定的问题在通知上凸显得尤其严重，比方说刚刚介绍的通知渠道功能在Android 8.0系统之前就是没有的。那么该如何解决这个问题呢？其实解决方案我们之前已经见过好几回了，**就是使用`AndroidX`库中提供的兼容API。`AndroidX`库中提供了一个`NotificationCompat`类，使用这个类的构造器创建`Notification`对象，就可以保证我们的程序在所有Android系统版本上都能正常工作了，代码如下所示**：

```kotlin
val notification = NotificationCompat.Builder(context, channelId).build()
```

`NotificationCompat.Builder`的构造函数中接收两个参数：**第一个参数是context**，这个没什么好说的；**第二个参数是渠道ID**，需要和我们在创建通知渠道时指定的渠道ID相匹配才行。

当然，上述代码只是创建了一个空的Notification对象，并没有什么实际作用，我们可以在最终的`build()`方法之前连缀任意多的设置方法来创建一个丰富的Notification对象，先来看一些最基本的设置：

```kotlin
val notification = NotificationCompat.Builder(context, channelId)
.setContentTitle("this is content title").setContentText("This is content text")
.setSmallIcon(R.drawable.small_icon)
.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.large_icon))
.build()
```

上述代码中一共调用了4个设置方法，下面我们来一一解析一下。`setContentTitle()`方法用于指定通知的标题内容，下拉系统状态栏就可以看到这部分内容。`setContentText()`方法用于指定通知的正文内容，同样下拉系统状态栏就可以看到这部分内容。`setSmallIcon()`方法用于设置通知的小图标，注意，只能使用纯alpha图层的图片进行设置，小图标会显示在系统状态栏上。`setLargeIcon()`方法用于设置通知的大图标，当下拉系统状态栏时，就可以看到设置的大图标了。

以上工作都完成之后，只需要调用`NotificationManager`的`notify()`方法就可以让通知显示出来了。`notify()`方法接收两个参数：**第一个参数是id**，要保证为每个通知指定的id都是不同的；**第二个参数则是`Notification`对象**，这里直接将我们刚刚创建好的Notification对象传入即可。因此，显示一个通知就可以写成：

```kotlin
manager.notify(1,notification)
```

下面就让我们通过一个具体的例子来看看通知到底是长什么样的。

```kotlin
package com.ubx.notificationtest

import android.app.NotificationChannel
import android.app.NotificationManager
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
            val channel =
                NotificationChannel("normal", "Normal", NotificationManager.IMPORTANCE_DEFAULT)
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
                // 已经有权限，可以进行下一步操作
                // AndroidX库中提供了一个NotificationCompat类，使用这个类的构造器创建
                // Notification对象，就可以保证我们的程序在所有Android系统版本上都能正常工作了
                val notification = NotificationCompat.Builder(this, "normal")
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
```

如果你使用过Android手机，此时应该会下意识地认为这条通知是可以点击的。但是当你去点击它的时候，会发现没有任何效果。不对啊，每条通知被点击之后都应该有所反应呀。其实要想实现通知的点击效果，我们还需要在代码中进行相应的设置，这就涉及了一个新的概念——`PendingIntent`。

`PendingIntent`从名字上看起来就和Intent有些类似，它们确实存在不少共同点。比如它们都可以指明某一个“意图”，都可以用于启动Activity、启动Service以及发送广播等。不同的是，`Intent`倾向于立即执行某个动作，而`PendingIntent`倾向于在某个合适的时机执行某个动作。所以，也可以把`PendingIntent`简单地理解为**延迟执行的`Intent`**。

`PendingIntent`的用法同样很简单，它主要提供了几个静态方法用于获取`PendingIntent`的实例，可以根据需求来选择是使用`getActivity()`方法、`getBroadcast()`方法，还是`getService()`方法。这几个方法所接收的参数都是相同的：第一个参数依旧是`Context`，不用多做解释；第二个参数一般用不到，传入0即可；第三个参数是一个`Intent`对象，我们可以通过这个对象构建出`PendingIntent`的“意图”；第四个参数用于确定`PendingIntent`的行为，有`FLAG_ONE_SHOT`、`FLAG_NO_CREATE`、`FLAG_CANCEL_CURRENT`和`FLAG_UPDATE_CURRENT`这4种标志可选.这些标志是用来控制 `PendingIntent` 行为的。下面是每个标志的含义：

1. `FLAG_ONE_SHOT`：这个标志表示返回的 `PendingIntent` 只能使用一次。如果后续还需要执行相同的操作，你需要再次获取一个新的 `PendingIntent`。
2. `FLAG_NO_CREATE`：如果当前的 `PendingIntent` 不存在，那么简单地返回 `null`，而不是创建一个新的 `PendingIntent`。
3. `FLAG_CANCEL_CURRENT`：这个标志表示当前的 `PendingIntent` 会被取消，然后创建一个新的 `PendingIntent`。这意味着旧的 `PendingIntent` 不再有效，所有的等待的 `Intent` 都会被取消。
4. `FLAG_UPDATE_CURRENT`：如果相同的 `PendingIntent` 已经存在，那么保持它不变，但是替换它的 `Intent` 数据。这意味着新的 `Intent` 数据会被用来更新已经存在的 `PendingIntent`。

从 Android 12（API 级别 31）开始，创建 `PendingIntent` 时必须指定 `FLAG_IMMUTABLE` 或 `FLAG_MUTABLE`。这是因为 Android 12 对 `PendingIntent` 的行为进行了更改，以提高应用的安全性。

- `FLAG_IMMUTABLE`：这个标志表示 `PendingIntent` 是不可变的，也就是说，一旦创建，就不能更改。这是推荐的选项，因为它可以防止潜在的安全问题。

- `FLAG_MUTABLE`：这个标志表示 `PendingIntent` 是可变的，也就是说，可以在创建后进行更改。只有在某些功能依赖于 `PendingIntent` 的可变性时，才应该使用这个选项，例如，需要与内联回复或气泡一起使用。

对`PendingIntent`有了一定的了解后，我们再回过头来看一下`NotificationCompat.Builder`。这个构造器还可以连缀一个`setContentIntent()`方法，接收的参数正是一个`PendingIntent`对象。因此，这里就可以通过`PendingIntent`构建一个延迟执行的“意图”，当用户点击这条通知时就会执行相应的逻辑。

现在我们来优化一下`NotificationTest`项目，给刚才的通知加上点击功能，让用户点击它的时候可以启动另一个Activity。

- `FLAG_IMMUTABLE`：这个标志表示 `PendingIntent` 是不可变的，也就是说，一旦创建，就不能更改。这是推荐的选项，因为它可以防止潜在的安全问题。
- `FLAG_MUTABLE`：这个标志表示 `PendingIntent` 是可变的，也就是说，可以在创建后进行更改。只有在某些功能依赖于 `PendingIntent` 的可变性时，才应该使用这个选项，例如，需要与内联回复或气泡一起使用。

```kotlin
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
            val channel =
                NotificationChannel("normal", "Normal", NotificationManager.IMPORTANCE_DEFAULT)
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
                val notification = NotificationCompat.Builder(this, "normal")
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
                    .setContentIntent(pi)
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
```

如果我们没有在代码中对该通知进行取消，它就会一直显示在系统的状态栏上。解决的方法有两种：一种是在`NotificationCompat.Builder`中再连缀一个`setAutoCancel()`方法，一种是显式地调用`NotificationManager`的`cancel()`方法将它取消。两种方法我们都学习一下。

第一种方法写法如下：

```kotlin
val notification = NotificationCompat.Builder(this, "normal")
	...
	.setAutoCancel(true)
	.build()
```

`setAutoCancel()`方法传入true，就表示当点击这个通知的时候，通知会自动取消。

第二种方法写法如下：

```kotlin
package com.ubx.notificationtest

import android.app.NotificationManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class NotificationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(1)
    }
}
```

这里我们在`cancel()`方法中传入了1，当时我们给这条通知设置的id就是1。因此，如果你想取消哪条通知，在`cancel()`方法中传入该通知的id就行了。

### 1.3 通知的进阶技巧

当我们想在通知中显示较长的富文本时会发现，Android会将通知中的长文本折叠起来。那么有没有什么办法把富文本全部显示出来呢？实际上，`NotificationCompat.Builder`中提供了非常丰富的API，以便我们创建出更加多样的通知效果。先来看看`setStyle()`方法，这个方法允许我们构建出富文本的通知内容。也就是说，通知中不光可以有文字和图标，还可以包含更多的东西。`setStyle()`方法接收一个`NotificationCompat.Style`参数，这个参数就是用来构建具体的富文本信息的，如长文字、图片等。

```kotlin
val notification = NotificationCompat.Builder(this, "normal")
	...
	.setStyle(NotificationCompat.BigTextStyle().bigText("Learn how to build notifications, send and sync data, and use voice actions. Get the official Android IDE and developer tools to build apps for Android."))
	.build()
```

这里使用了`setStyle()`方法替代`setContentText()`方法。在`setStyle()`方法中，我们创建了一个`NotificationCompat.BigTextStyle`对象，这个对象就是用于封装长文字信息的，只要调用它的`bigText()`方法并将文字内容传入就可以了。

除了显示长文字之外，通知里还可以显示一张大图片，具体用法是基本相似的：

```kotlin
val notification = NotificationCompat.Builder(this, "normal")
	...
	.setStyle(NotificationCompat.BigPictureStyle().bigPicture(BitmapFactory.decodeResource(resources, R.drawable.big_image)))
	.build()
```

可以看到，这里仍然是调用的`setStyle()`方法，这次我们在参数中创建了一个`NotificationCompat.BigPictureStyle`对象，这个对象就是用于设置大图片的，然后调用它的`bigPicture()`方法并将图片传入。这里使用事先准备好的一张图片，通过`BitmapFactory`的`decodeResource()`方法将图片解析成`Bitmap`对象，再传入`bigPicture()`方法中就可以了。

接下来来学习一下不同重要等级的通知渠道对通知的行为具体有什么影响。其实简单来讲，就是通知渠道的重要等级越高，发出的通知就越容易获得用户的注意。比如高重要等级的通知渠道发出的通知可以弹出横幅、发出声音，而低重要等级的通知渠道发出的通知不仅可能会在某些情况下被隐藏，而且可能会被改变显示的顺序，将其排在更重要的通知之后。但需要注意的是，开发者只能在创建通知渠道的时候为它指定初始的重要等级，如果用户不认可这个重要等级的话，可以随时进行修改，开发者对此无权再进行调整和变更，因为通知渠道一旦创建就不能再通过代码修改了。

虽然无法更改之前创建的通知渠道，但是我们可以创建一个新的通知渠道，如下：

```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        ...
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ...
            val channel2 = NotificationChannel("important", "Important", NotificationManager.IMPORTANCE_HIGH)
            manager.createNotificationChannel(channel2)
        }
        sendNotice.setOnClickListener {
            val intent = Intent(this, NotificationActivity::class.java)
            val pi = PendingIntent.getActivity(this, 0, intent, 0)
            val notification = NotificationCompat.Builder(this, "important")
            ...
        }
    }
}
```