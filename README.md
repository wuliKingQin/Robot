### 逆向分析企业微信之构建机器人

#### 前沿

有时候，我们希望能接收到消息后，实现自动回复功能，来提高我们回复一些固定消息的效率，或者是纯粹为了好玩，但又不知道如何实现，搜索了很多文章，都是东一点，西一点的。那么好了，这文章正好适合你，让我们一起来实现一个可以主动发送消息给某个人的应用吧。

#### 逆向工具

在开始逆向分析之前，我们需要掌握以下几个工具， 以方便我们更好的进行定位分析，以及验证等。

- ##### 反编译工具Jadx

  该工具用来反编译企业微信app, 使其能让我们看到企业微信的源码。当然这个源码是由字节码反编译过来，和真正的源码有很大的区别，比如企业微信打包时，对源代码进行了混淆，那么反编译出来后，很多代码的字段名以及类名，包名就不是原来的名字，全部用a~z的字母进行取代，这个给我们分析代码功能逻辑的时候带来了一些困扰。Jadx这个工具，会给我们解决一部分烦恼，它是如何解决的呢？ 它是将各个名字进行了唯一性的重命名。这样，我们在搜索某个名字的时候，极大的减少了我们分析的难度。

  当然Jadx工具也不是很完美的，可能某些方法，依然还是字节码，不能很好的反编译出来，这个时候，我们最好需要懂一些字节码的知识，否则看这个就是天书了。或者你也可以使用其他反编译工具来进行辅助，比如bytecode-viewer。

- ##### hook库xposed

  那么xposed库是什么？“ Xposed框架(Xposed Framework)是一套开源的、在Android高权限模式下运行的框架服务，可以在不修改APK文件的情况下影响程序运行(修改系统)的框架服务，基于它可以制作出许多功能强大的模块，且在功能不冲突的情况下同时运作”百度百科是这么解释的。我们可以使用xposed库，去hook住其他app的某个功能，让这个功能可以实现我们自己想要的效果，比如我们要获取到企业微信每个微信用户发过来的信息，并将这些信息转发到我们自己服务器，那么我们就可以hook住企业微信接收消息这块功能，然后去自己解析消息内容，并将消息二次封装后，转发到我们自己的服务器。

有了以上两个工具，我们实现企业微信的自动化服务机器人，对Android层面来说，已经足够了。下面，我们就来一步一步使用以上两个工具来分析定位企业微信里我们要想要自动化的功能，比如在我们自己的应用程序里调用企业微信里的发送消息的api，主动进行发送文本消息给目标用户。

#### 分析定位

##### 环境

电脑: Window 10
软件开发工具： Android Studio 4.1 
模拟器： mumu模拟Android6.0
Xposed App:  版本是3.1.5版本
企业微信版： 3.0.25（13066）

##### 分析

我们的目标是在自己应用程序里主动调用企业微信里的发送给消息的api并发送文本消息给目标用户，根据这个功能的描述，首先我们要获取到企业微信里的微信客户的用户信息，然后就是找到企业微信发送消息的api代码。

###### 获取企业微信里我的客户里的用户信息

- 我们先使用xposed给ClassLoader的loadClass方法下一个钩子，这样可以方便我们快速定位该功能所在页面的类是什么。知道了所在页面的类信息，自然就能通过Jadx快些的进行搜索该类的源码。代码如下：

  ```kotlin
  // 查看每个页面加载了那些有用的类，用来定位某个功能
  private val mLoadClassHook by lazy {
      Hook(
          // 需要hook类
          "java.lang.ClassLoader",
          // 需要hook的方法
           "loadClass",
          // hook方法的参数类型
           String::class.java,
          // 该方法执行后的接口回调，我通过这接口回调拿到loadClass方法里的参数值，然后将值打印出来
           object : XC_MethodHook() {
               // loadClass方法执行后进行回调
               override fun afterHooedMethod(param: MethodHookParams?) {
                   if (param?.hasThrowable() == false) return
                   val cls = param?.result as? Class<*>
                   logD("loadClass", "============$cls")
               }
           }
      )
  }
  ```

- 我们打开企业微信，进入到通讯录页面，然后点击“我的客户”， 此时可以在Android Studio的Logcat界面看到打印的类信息，如下图：

  <img src="C:\Users\lenovo\Desktop\逆向分析之构建机器人\ic_loadclass.png" alt="image-20201224170802132" style="zoom:50%;" />

  从图中，我们可以找到我的客户的这个界面的类就是“com.tencent.wework.friends.controller.OutFriendListActivity”。

- 现在我们可以将准备好的企业微信app使用Jadx直接进行打开，打开后我们可以看到Jadx已经把app反编译了，具体看如下图：

  ![image-20201224163909969](C:\Users\lenovo\Desktop\逆向分析之构建机器人\ic_jadx_apk.png)

  这个时候，我们点击源码部分，便可以看企业微信app所涉及到的源码目录，如下图所示：

  <img src="C:\Users\lenovo\Desktop\逆向分析之构建机器人\ic_jadx_source.png" alt="image-20201224164435807" style="zoom:50%;" />

  根据上面我们已经知道我的客户界面的类信息，那么我们直接可以打开搜索界面，进行全局搜索，如下图：

  <img src="C:\Users\lenovo\Desktop\逆向分析之构建机器人\ic_jadx_search.png" alt="image-20201224170943442" style="zoom: 67%;" />

  可以看到，我们已经搜索到该类，然后就是点击进入，如下图：

  ![image-20201224171708221](C:\Users\lenovo\Desktop\逆向分析之构建机器人\ic_jadx_outfirend.png)

  那么问题来了， 大范围我们找到了，那么怎么找到我的客户数据加载是在哪个地方加载的呢？ 带着这个问题，我们来分析一波。

  - 做个Android开发的人都知道，数据的显示要么是通过RecyclerView，要么是ListView, 那么他们都会对应一个Adapter。

  - 所以我们只要在该类里面搜索Adapter关键词，应该就能找到数据的加载的地方。如下图所示：

    <img src="C:\Users\lenovo\Desktop\逆向分析之构建机器人\ic_jadx_adapter.png" alt="image-20201224172418111" style="zoom: 80%;" />

    我们可以看到一个setAdapter()的这个方法，发现传入的参数是一个名叫lyL的对象，那么可以肯定是，这个被混淆了的变量名就是我们要找的adpater对象，只要再一次搜索这个lyL名字，看它在哪里设置了数据，我们就找了获取我的客户数据的方法。

  - 经过一连串的跟踪搜索，我们找到获取我的客户信息的方法是guc里的一静态方法a,该方法需要我们传入两个值，一个是群组Id,一个是数据回调接口实例。如下图所示：

    ![image-20201224175015050](C:\Users\lenovo\Desktop\逆向分析之构建机器人\ic_jadx_data.png)

    那么我们是怎么确定这个群组Id是传的什么呢，有一个很简单的方法，就是对这个方法进行hook，然后打印出来它的第一个参数值，我们便能知道群组Id是多少。经过hook验证，第一个参数打印的是18， 这个时候我再看bMQ这个方法，如图所示：

    ![image-20201224174907755](C:\Users\lenovo\Desktop\逆向分析之构建机器人\ic_jadx_group_id.png)

    发现当pageType等于2的时候返回的就是固定18，那么我就确定只要调用这个方法时，我们固定传入18，然后就能从接口中获取到我们想要的客户列表信息了。

- 既然我们已经找到获取我的客户列表数据的方法，那么就对这个方法通过反射进行调用，当然这个时候，你会发现还有一个接口参数，这个我们怎么办呢，很简单就是通过动态代理来实例化这个接口的代理对象, 代码如下：

  ```kotlin
  // DES: 获取到我的客户下面的联系人
  fun getMyClientContacts(result: (contactMap: Map<*, *>?) -> Unit) {
      // 通过动态代理来实例化这个接口
      val callback = Hook.findClass("guc$c")
              .newCallbackInstance{ _, args ->
                  // 将结果返回给调用者
                  result(args?.get(1) as? Map<*, *>)
              }
      // DES: 我的客户群组Id是18
      val groupId = 18
      // 通过反射调用该静态方法
      Hook.findClass("guc")
          .callStaticMethod<Any>(
              "a",
              groupId,
              callback
      )
  }
  ```

- 下面我们用同样的方法来找到发送消息的方法，首先发送消息，必然会在点击发送按钮的时候触发，那么我们就可以对View的点击事件的方法进行hook。然后在打印出这个视图的唯一Id, 这样我们就能通过搜索这个Id就能在代码中找到发送按钮。找到发送按钮，我们自然就能顺藤摸瓜，找到发送消息的具体调用了那个api。 废话不多说，经过搜索和追踪，发现发送消息的地方是封装在被混淆的类hqq下面，该类下面提供了几个静态的发送消息的方法，比如发送文本消息、发送文件消息、发送视频消息、发送图片消息等待，我在里只贴出一个发送所有的消息的最底层方法，方便自己构造任何自定义的消息，代码如下所示：

  ```kotlin
      // DES: 用于包装发送消息方法，检查当前上下文是否正常
      private inline fun sendMessageWrapper(actionBlock: (Context) -> Unit): Boolean {
          // 异常捕获
          return tryCatch {
              val context = ActivityUtils.getTopActivity()?.let {
                  // 执行发送消息的地方
                  actionBlock(it)
                  it
              }
              if (context == null) {
                  logD("发送消息失败")
                  false
              } else {
                  logD("发送消息成功")
                  true
              }
          } ?: false
      }
      // DES: 封装发送任何消息的方法，并带有回调方法
      private fun sendMessage(
          // 发送消息建立的会话对象
          conversation: Any,
          // 发送消息内容
          message: Any?,
          // 其他携带信息，比如分享的时候的描述语
          sendExtraInfo: Any?,
          // 发送成功还是失败的将走该回调接口
          sendCallback: ISendMessageCallback?
      ) {
          val isSuccess = sendMessageWrapper { context ->
              // 通过动态代理构建一个发送消息的接口实例
              val sendMessageCallback = Hook.findClass("com.tencent.wework.foundation.callback.ISendMessageCallback")
                  .newCallbackInstance { methodName, args ->
                      sendCallback?.let { callback ->
                          args?.let { it ->
                              if (it.size >= 3) {
                                  if (methodName == "onProgress") {
                                      // 发送消息的进度
                                      val progress = it[1] as? Long ?: 0L
                                      val allSize = it[2] as? Long ?: 0L
                                      callback.onProgress(it[0], progress, allSize)
                                  } else if (methodName == "onResult") {
                                      // 发送消息的结果
                                      val sendState = it[0] as? Int ?: -1
                                      callback.onResult(sendState, it[1], it[2])
                                  }
                              }
                          }
                      }
                  }
              // DES: 调用hqq类里面发送消息的静态方法sendMessage
              Hook.findClass("hqq")
                  .callStaticMethod<Any>(
                      "sendMessage",
                      context,
                      conversation,
                      message,
                      sendExtraInfo,
                      sendMessageCallback
                  )
          }
          if (!isSuccess) {
              // DES: 发送失败的回调
              sendCallback?.onResult(SEND_STATE_FAIL, conversation, message)
          }
      }
  ```

通过分析与定位，我们以及把两个功能（拿到我的客户用户信息、发送消息）都已经找到，并进行了代码封装。下面，我们来看如何构造自己的机器人。

#### 构建机器人

##### 建立新工程

首先我们使用Android Stuido 新建一个工程名字就叫Robot， 如下图所示：

![image-20201225100132666](C:\Users\lenovo\Desktop\逆向分析之构建机器人\ic_android_studio_new.png)

##### 配置工程环境

- 新建工程完毕后，我们先进行配置Xposed库，如下图所示：

<img src="C:\Users\lenovo\Desktop\逆向分析之构建机器人\ic_android_studio_build.png" alt="image-20201225100910222" style="zoom:50%;" />

​	   我们进入app目录，找到build.gradle，然后在build.gradle的dependencies里面添加一个xposed库的依赖，这里依赖的时候必须使用”compileOnly“进行，	    	   否	则hook无法正常工作。添加后我点击Android Studio右边出现的”Sync Now“, 表示现在同步，将配置的库进行下载到本地。

- 依赖完Xposed库后，我还需要在build.gradle文件最的组后添加几个自定义任务，目的是方便在运行程序后，让Xposed app里面我们的模块即时生效。需要添加配置如下代码:

  ```groovy
  // 每次修改运行后自动让 VXP 中的模块`即时生效` ,需要将 (Debug Configurations) - Before Launch - Gradle aware Make - 修改为 :app:installDebug
  afterEvaluate {
      installDebug.doLast {
          updateVirtualXposedAPP.execute()
          rebootVirtualXposedAPP.execute()
          launchVirtualXposedAPP.execute()
      }
  }
  
  // 更新 VXP 中的 app
  task updateVirtualXposedAPP(type: Exec) {
      def pkg = android.defaultConfig.applicationId
      commandLine android.adbExecutable, 'shell', 'am', 'broadcast', '-a', 'io.va.exposed.CMD', '-e', 'cmd', 'update', '-e', 'pkg', pkg
  }
  // 重启 VXP
  task rebootVirtualXposedAPP(type: Exec) {
      commandLine android.adbExecutable, 'shell', 'am', 'broadcast', '-a', 'io.va.exposed.CMD', '-e', 'cmd', 'reboot'
  }
  // 重启 VXP 企业微信
  task launchVirtualXposedAPP(type: Exec) {
      def pkg = 'com.tencent.wework'
      commandLine android.adbExecutable, 'shell', 'am', 'broadcast', '-a', 'io.va.exposed.CMD', '-e', 'cmd', 'launch', '-e', 'pkg', pkg
  }
  ```

- 最后在AndroidMainfest.xml中的Application标签里还需要配置我们在Xposed app的里模块信息，如下代码所示：

  ```xml
  <?xml version="1.0" encoding="utf-8"?>
  <manifest xmlns:android="http://schemas.android.com/apk/res/android"
      package="com.wuliqinwang.android.robot">
      <application
          android:allowBackup="true"
          android:icon="@mipmap/ic_launcher"
          android:label="@string/app_name"
          android:roundIcon="@mipmap/ic_launcher_round"
          android:supportsRtl="true"
          android:theme="@style/Theme.Robot">
          <activity android:name=".MainActivity">
              <intent-filter>
                  <action android:name="android.intent.action.MAIN" />
  
                  <category android:name="android.intent.category.LAUNCHER" />
              </intent-filter>
          </activity>
          <!--配置我们机器人是否要作为Xposed中的一个模块-->
          <meta-data
              android:name="xposedmodule"
              android:value="true"/>
          <!--配置我们的机器热在Xposed的里面模块的描述-->
          <meta-data
              android:name="xposeddescription"
              android:value="@string/app_name"/>
          <!--配置Xposed的最小版本号-->
          <meta-data
              android:name="xposedminversion"
              android:value="53"/>
      </application>
  </manifest>
  ```

  

- 配置以上后，我们就可以在主目录里面新建一个机器人hook的入口类叫mainHookLoadPackage，该实现IXposedHookLoadPackage接口，如下图所示：

  ![image-20201225102819336](C:\Users\lenovo\Desktop\逆向分析之构建机器人\ic_main_hook_load_package.png)

  创建该类以后，还需要在main目录下创建一个assets文件夹，然后在该文件下再创建一个xposed_init文件，在里面写入mainHookLoadPackage类的全路径名，如下图所示：

  ![image-20201225161809495](C:\Users\lenovo\Desktop\逆向分析之构建机器人\ic_xposed_init.png)

经过以上几步，我们的配置基本就结束，现在你就可以运行以下项目，项目跑起来后，进入到xposed app里面就能见到我们的模块了，如下所示:

![image-20201225110341522](C:\Users\lenovo\Desktop\逆向分析之构建机器人\ic_robot_show.png)

##### 界面和中转服务

下面我们把发送消息的界面写好, 代码比较简单， 就是一个输入框和一个发送按钮，代码如下：

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <androidx.appcompat.widget.AppCompatEditText
        android:id="@+id/message_content_edt"
        android:layout_width="match_parent"
        android:layout_height="50dp"
        android:text="我是一个文本消息"
        android:textColor="@android:color/black"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintRight_toRightOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

    <androidx.appcompat.widget.AppCompatButton
        android:id="@+id/send_message_btn"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="发送"
        android:layout_marginTop="10dp"
        app:layout_constraintTop_toBottomOf="@+id/message_content_edt"/>

</androidx.constraintlayout.widget.ConstraintLayout>
```

效果如下：

![image-20201225161642175](C:\Users\lenovo\Desktop\逆向分析之构建机器人\ic_send_message_activity.png)

我再点击发送文本之前，我们还有一问题需要解决，什么问题呢？就是我们如何将文本消息发送到企业微信，企业微信接收到我们发送的消息后，然后再拿到目标执行真正的发送任务。这里大家可能会有疑问，这些代码都在一个工程里， 为何这里不能直接拿我们封装好的代码直接运行呢？ 

原因很简单，虽然我们把hook以及反射的代码都都封装再了机器人这个工程里，但这部分代码都是运行在企业微信这个进程的，而不在我们机器人这个进程里面。那这个这个怎么解决呢？答案很简单，我们可以使用Binder机制实现多进程的通信。下面是机器人进程、中转服务进程和企业微信进程的通信架构图：

![image-20201225163214334](C:\Users\lenovo\Desktop\逆向分析之构建机器人\ic_jiagou.png)

下面我们就来实现这么一个消息中转处理服务类：

- 首先我们新建两个，分别是IMessageHandler和IMessageSeceiver的aidl接口文件。IMessageHandler接口用来处理转发消息和注册接收器，而IMessageSeceiver用来接收消息。代码如下：

  ```java
  // IMessageHandler.aidl
  package com.wuliqinwang.android.robot.message;
  
  // Declare any non-default types here with import statements
  import com.wuliqinwang.android.robot.message.IMessageReceiver;
  
  interface IMessageHandler {
     // 用于转发送消息的方法
     void transitMessage(String sender, String targetUser, in byte[] message);
     // 用于注册的接收者
     void registerMessageReceiver(in IMessageReceiver receiver);
     // 用于注销的接收者
     void unregisterMessageReceiver(in IMessageReceiver receiver);
  }
  ```

  ```java
  // IMessageReceiver.aidl
  package com.wuliqinwang.android.robot.message;
  
  // Declare any non-default types here with import statements
  
  interface IMessageReceiver {
       // 接收者名字，该名字必须是唯一的，转发时需要用到
       String getReceiverName();
       // 接收消息的方法
       void onReceiving(String senderName, in byte[] message);
  }
  ```

- 下面我们再新建一个消息的服务类MessageTransitService，它继承于Service， 并使其单独一个进程，然后再将该类注册到机器人工程的AndroidMainfest.xml文件里。到时候只需要机器人和企业微信在启动的时候绑定该服务，那么我们就能得到该服务的Stub子类的代理类，来操作IMessageHandler接口提供的方法，就能实现消息的接收以及发送等。下面是MessageTransitService类实现的代码：

  ```kotlin
  package com.wuliqinwang.android.robot.message
  
  import android.app.Service
  import android.content.Intent
  import android.os.IBinder
  import android.util.Log
  import java.lang.NullPointerException
  import java.util.concurrent.ConcurrentHashMap
  
  /**
   * @Description: 用于消息的中转的服务类
   * @Version: 1.0.0
   */
  class MessageTransitService: Service(){
  
      companion object {
          // DES: 用户存储大小，默认是4
          private const val USER_INITIAL_CAPACITY = 4
      }
  
      // DES: 用于存储消息接收者
      private val mUsers by lazy {
          ConcurrentHashMap<String, IMessageReceiver>(USER_INITIAL_CAPACITY)
      }
  
      override fun onBind(intent: Intent?): IBinder = MessageHandler()
  
  
      // DES: 用于转发消息的处理器
      inner class MessageHandler: IMessageHandler.Stub() {
  
          override fun transitMessage(sender: String?, targetUser: String?, message: ByteArray?) {
              val receiver = mUsers[targetUser]
              receiver?.onReceiving(sender, message)
          }
  
          override fun registerMessageReceiver(receiver: IMessageReceiver?) {
              receiver ?: return
              if (receiver.receiverName.isNullOrEmpty()) {
                  throw NullPointerException("The getReceiverName() method cannot return null")
              }
              Log.d("test===", "注册${receiver.receiverName}成功")
              mUsers[receiver.receiverName] = receiver
          }
  
          override fun unregisterMessageReceiver(receiver: IMessageReceiver?) {
              receiver ?: return
              mUsers.remove(receiver.receiverName)
          }
      }
  }
  ```

- 然后我们再建一个用于消息发送和注册消息接收者的静态管理类MessageManager，在进行消息接收者注册的时候，优先进行服务的绑定，该类分别可以运行在企业微信和机器人进程，所以只需要建立一个则可以。这个方法registerReceiverToTransitService需要在Application的onCreate方法中运行比较佳， 如果没有则可以在主界面的onCreate方式中调用。之后才可以调用sendMessage方法来发送消息，具体代码如下：

  ```kotlin
  package com.wuliqinwang.android.robot.message
  
  import android.content.ComponentName
  import android.content.Context
  import android.content.Intent
  import android.content.ServiceConnection
  import android.os.IBinder
  import android.util.Log
  
  /**
   * @Description: 用于其他进程绑定消息中转服务以及注册消息监听等，该类运行在其他进程
   * @Version: 1.0.0
   */
  object MessageManger {
  
      // DES: 中转服务的所处的包名
      private const val TRANSIT_SERVICE_PACKAGE_NAME = "com.wuliqinwang.android.robot"
  
      // DES: 中转服务的类名
      private const val TRANSIT_SERVICE_NAME =
          "com.wuliqinwang.android.robot.message.MessageTransitService"
  
      // DES: 保存中转服务链接
      private val mTransitServiceConnection by lazy {
          TransitServiceConnection()
      }
  
      // DES: 发送字节消息
      fun sendMessage(target: String, message: ByteArray) {
          mTransitServiceConnection.sendMessage(target, message)
      }
  
      // DES: 发送字符串消息
      fun sendMessage(target: String, message: String) {
          sendMessage(target, message.toByteArray())
      }
  
      // DES: 注册接收器到中转服务
      fun registerReceiverToTransitService(context: Context, receiver: IMessageReceiver) {
          Log.d("test===", "开始注册${receiver.receiverName}接收器")
          mTransitServiceConnection.receiver = receiver
          bindTransitService(context)
      }
  
      // DES: 绑定中转服务
      private fun bindTransitService(context: Context) {
          context.bindService(Intent().apply {
              setClassName(
                  TRANSIT_SERVICE_PACKAGE_NAME,
                  TRANSIT_SERVICE_NAME
              )
              type = context.packageName
          }, mTransitServiceConnection, Context.BIND_AUTO_CREATE)
      }
  
      // DES: 中转服务绑定链接监听
      class TransitServiceConnection(var receiver: IMessageReceiver? = null) : ServiceConnection {
          // DES: 拿到消息处理器的代理类
          private var mMessageHandlerProxy: IMessageHandler? = null
          override fun onServiceDisconnected(name: ComponentName?) {
              // DES: 中断服务的时候将接收者注销掉
              mMessageHandlerProxy?.unregisterMessageReceiver(receiver)
              mMessageHandlerProxy = null
          }
  
          override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
              mMessageHandlerProxy = IMessageHandler.Stub.asInterface(service)
              // DES: 注册我们的消息接收器到中转服务
              mMessageHandlerProxy?.registerMessageReceiver(receiver)
          }
  
          // DES: 发送消息到目标用户
          fun sendMessage(target: String, message: ByteArray) {
              mMessageHandlerProxy?.transitMessage(receiver?.receiverName, target, message)
          }
      }
  }
  ```

- 接下来，我们在对企业微信的Application的onCreate方法进行hook, 新建一个hook提供器类WeworkHookProvider，这样可方便我们注册消息接收器，以及执行其他需要初始化的代码，比如注册Activity的生命周期接口，可以获取到企业微信每个界面的Activity等。下面是具体的代码：

  ```kotlin
  package com.wuliqinwang.android.robot.wework
  
  import android.app.Application
  import android.os.IBinder
  import android.util.Log
  import com.wuliqinwang.android.robot.hook.Hook
  import com.wuliqinwang.android.robot.hook.IHook
  import com.wuliqinwang.android.robot.hook.IHookProvider
  import com.wuliqinwang.android.robot.message.IMessageReceiver
  import com.wuliqinwang.android.robot.message.MessageManger
  import com.wuliqinwang.android.robot.wework.utils.*
  import de.robv.android.xposed.XC_MethodHook
  
  /**
   * @Description: 用于集中处理企业微信需要配hook的方法
   * @Version: 1.0.0
   */
  class WeworkHookProvider: IHookProvider{
  
      override fun onHooks(): ArrayList<IHook> = arrayListOf(
          mApplicationOnCreateHook
      )
  
      // DES: 对企业微信的application的onCreate方法进行Hook，
      // 方便拿到企业微信的全局环境，以及Activity的生命周期
      private val mApplicationOnCreateHook by lazy {
          Hook(
              "com.tencent.wework.launch.WwApplicationLike",
              "onCreate",
              object : XC_MethodHook() {
                  override fun afterHookedMethod(param: MethodHookParam?) {
                      param ?: return
                      if (ActivityUtils.globalContent == null) {
                          (param.thisObject as? Application)?.apply {
                              // DES: 保存全局上下文
                              ActivityUtils.globalContent = this
                              // DES: 注册Activity的生命周期回调，用来获取顶部界面上下文
                              registerActivityLifecycleCallbacks(ActivityLifecycleCallbackIml())
                              // DES: 用于后面绑定机器人提供的服务所用
                              applicationOnCreate(this)
                          }
                      }
                  }
              })
      }
  
      // DES: 执行企业微信的Application的 onCreate方法的扩展
      private fun applicationOnCreate(context: Application) {
          MessageManger.registerReceiverToTransitService(context, MessageReceiver())
      }
  
      // DES: 消息接收器
      class MessageReceiver: IMessageReceiver.Stub() {
          override fun onReceiving(senderName: String?, message: ByteArray?) {
              MessageManger.sendMessage("Robot002", "收到消息")
              val messageContent = message?.toString(Charsets.UTF_8) ?: ""
              Log.d("test====", "$senderName send content: $messageContent")
              UserUtils.getFirstUser {
                  Log.d("test====", "${it?.name}")
                  ConversationUtils.createConversationByUser(it?.realUser) { id, conversation ->
                      Log.d("test====", "conversation id=${id}")
                      MessageUtils.sendTextMessage(conversation, messageContent, null)
                  }
              }
          }
  
          override fun getReceiverName(): String = "WeWork001"
      }
  }
  ```

  该类将在MainHookLoadPackage类的handleLoadPackage方法中执行，具体代码如下：

  ```kotlin
  package com.wuliqinwang.android.robot
  
  import android.util.Log
  import com.wuliqinwang.android.robot.hook.ClassLoaderUtils
  import com.wuliqinwang.android.robot.hook.IHookProvider
  import com.wuliqinwang.android.robot.wework.WeworkHookProvider
  import de.robv.android.xposed.IXposedHookLoadPackage
  import de.robv.android.xposed.callbacks.XC_LoadPackage
  
  /**
   * @Version: 1.0.0
   */
  class MainHookLoadPackage: IXposedHookLoadPackage {
  
      companion object {
          private const val WEWORK_PROCESS_NAME = "com.tencent.wework"
      }
  
      override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam?) {
          lpparam ?: return
          tryCatch {
              // DES: 判断是否在企业微信开始hook
              val isHookProcess = lpparam.processName == WEWORK_PROCESS_NAME
              val isHookPackage = lpparam.packageName == WEWORK_PROCESS_NAME
              if (isHookProcess && isHookPackage) {
                  // DES: 是的话则进入的企业微信的进程
                  ClassLoaderUtils.targetClassLoader = lpparam.classLoader
                  Log.d("test===", "hook 企业微信成功")
                  // DES: 开始进行hook处理
                  runHook(WeworkHookProvider::class.java)
              }
          }
      }
  
      // DES: 运行hook开始地方
      private fun runHook(vararg hookCls: Class<*>) {
          hookCls.forEach {
              val provider = it.newInstance()
              if(provider is IHookProvider) {
                  provider.onHooks()?.forEach { hook ->
                      hook.run()
                  }
              }
          }
      }
  }
  ```

- 现在我们在机器人的主界面的添加如下代码，我们就可以看到最终效果了。

  ```kotlin
  package com.wuliqinwang.android.robot
  
  import androidx.appcompat.app.AppCompatActivity
  import android.os.Bundle
  import android.util.Log
  import android.view.View
  import android.widget.EditText
  import com.wuliqinwang.android.robot.message.IMessageReceiver
  import com.wuliqinwang.android.robot.message.MessageManger
  
  class MainActivity : AppCompatActivity() {
      override fun onCreate(savedInstanceState: Bundle?) {
          super.onCreate(savedInstanceState)
          setContentView(R.layout.activity_main)
          // DES: 注册消息接收器到中转服务
          MessageManger.registerReceiverToTransitService(this, MessageReceiver())
  
          findViewById<View>(R.id.send_message_btn)?.setOnClickListener {
              val messageContent = findViewById<EditText>(
                  R.id.message_content_edt)?.text?.toString()?.trim() ?: ""
              // DES: 发送消息到企业微信，然后企业微信再随机选取一个用户发送消息
              MessageManger.sendMessage("WeWork001", messageContent)
          }
      }
  
      // DES: 接收企业微信发送来的消息
      class MessageReceiver: IMessageReceiver.Stub() {
          override fun onReceiving(senderName: String?, message: ByteArray?) {
              Log.d("test===", message.toString())
          }
  
          override fun getReceiverName(): String = "Robot002"
      }
  }
  ```

通过以上的步骤，我们已经建立一个和企业微信进行互动的机器人，机器人可以随时操控企业微信进行做任何事情，下面我们就来测试一番，看看运行效果。

##### 效果展示

首先我们优先启动机器人，然后再把企业微信启动起来。这个时候我们再回到机器人界面，随意输入一个文本信息，点击发送。我们再切换到企业微信，可以看到企业微信已经把消息发送给我们一个随机用户，具体看效果：

<img src="C:\Users\lenovo\Desktop\逆向分析之构建机器人\gif_xiaoguo.gif" alt="xiaoguo" style="zoom:50%;" />

项目地址：https://github.com/CuteyBoy/Robot.git

#### 总结

经过以上的步骤，我们就能建立自己的机器人了，虽然以上功能实现的比较简单，但它是所有其他功能的基础，有了这个基础，就能让我们建立起高楼大厦。所以无论是企业微信也好，还是其他应用，都可以用这样方法去实现一个为我们自己服务的机器人。亦或者实现更多有价值的自动化应用，让效率变得高效。当然，在实现其他应用的时候，可能我们遇到其他问题，比如混淆的更严重，亦或是app被加固，但不管有什么困难，我们都能有方法去解决，只要我们有一颗钻研的心。





















