### QuiteDownload

> 如果你觉得这个lib对你有用,随手给个Star,让我知道它是对你有帮助的,我会继续更新和维护它。

![image](./download_simple.gif)

### 功能

  - 任何一个界面检测进度
  - 单个任务下载
  - 多个任务下载
  - 取消单个任务
  - 取消全部任务
  - 暂停所有任务
  - 队列最大同时下载任务数,超过则进入等待队列
  - 自动恢复上一次下载任务
 

### 使用方法

#### downloader 权限相关

```
    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
```


#### 添加依赖

根项目的build.gradle：

```
allprojects {
    repositories {
        jcenter()
        maven { url "https://jitpack.io" }
    }
}
```

$lastVersion = [![](https://jitpack.io/v/xwdz/QuietDownload.svg)](https://jitpack.io/#xwdz/QuietDownload)

```
implementation 'com.xwdz:QuietDownloader:$lastVersion'
```

在AndroidManifest.xml 声明如下service

```
<service android:name="com.xwdz.download.core.DownloadService"/>
```

#### 配置

    1. 在您的Application处调用初始化代码:
       XDownloaderManager.getImpl().bindService(this);
       
    2. 添加一些列配置
        QuietConfig.getImpl()
                        // 默认下载路径为[sdcard/Download/包名/xxx.apk]
                        // 若没有自定义下载文件路径,则必须调用该init代码
                        .initDownloadFile(context)
                        //debug模式
                        .setDebug(boolean isDebug)
                        //下载文件路径
                        .setDownloadDir(File file)
                        // 队列最大同时下载任务数,超过则进入等待队列 默认:3
                        .setMaxDownloadTasks(int)
                        // 最大线程下载数   默认:3
                        .setMaxDownloadThreads(int)
                        // 间隔毫秒
                        .setMinOperateInterval(long)
                        // 打开界面自动恢复下载
                        .setRecoverDownloadWhenStart(false);
                        
    QuietDownloadConfig还提供了一处全局处理网络的接口:
    
    public interface HandlerNetwork {
                  /**
                   * 处理网络状况接口
                   * @return true:  消费该事件终止运行下载任务
                   *         false: 正常执行下载任务
                   */
                  boolean onHandlerNetworkStatus();
    }
              
    QuietConfig.getImpl().setHandlerNetworkListener(new QuietDownloadConfig.HandlerNetwork() {
         @Override
         public boolean onHandlerNetworkStatus() {
             // 自己的逻辑判断，提示当前不在wifi情况dialog 等
             return false;
         }
     });
     
     
#### 关于监听

`QuiteDownload` 并没有采用传统listener方式，而是使用了观察者模式,如需要在某个界面监听下载进度

```
    private final DataUpdateWatcher mDataUpdateReceiver = new DataUpdateWatcher() {
        @Override
        public void notifyUpdate(DownloadEntry entry) {
            // calback mainUIThread 
            // do something
            // 可根据 entry status来判断一些列状态
            if(entry.status == DownloadEntry.pause || DownloadEntry.downloading ...)
        }
    };
    
    // 省略若干代码
    
    @Override
    protected void onResume() {
        super.onResume();
        mDownloader.addObserver(mDataUpdateReceiver);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        mDownloader.removeObserver(mDataUpdateReceiver);
    }
    
```
                        
                        
                        
#### 关于DownloadEntry

```
public class DownloadEntry implements Serializable {

    public String id;
    public String name;
    public String url;
    public int currentLength;
    public int totalLength;
        // ... 省略代码

    public DownloadEntry(String url) {
        this.url = url;
        this.id = url;
        this.name = url.substring(url.lastIndexOf("/") + 1);
    }

    }
    // ... 省略代码
    
    @Override
        public boolean equals(Object o) {
            return o.hashCode() == this.hashCode();
        }
    
        @Override
        public int hashCode() {
            final int PRIME = 31;
            int result = 1;
            result = PRIME * result + id.hashCode();
            return result;
    }
}

```

#### 注意
- DownloadEntry实体类重写其 equals 以及 hashCode 方法，使用其 id hashCode 来作为其标准
- QuietDownloader内部使用DownloadEntry实体类进行关联


#### DownloadEntry的几种状态

```
 public enum DownloadStatus {
        //空闲
        IDLE,
        // 等待
        WAITING,
        // 连接 获取下载信息
        CONNECTING,
        // 连接成功 即获取到下载文件大小等
        CONNECT_SUCCESSFUL,
        // 开始下载
        DOWNLOADING,
        // 暂停
        PAUSED,
        // 取消
        CANCELLED,
        // 完成
        COMPLETED,
        // 错误
        ERROR
    }
```
                        
#### 使用方法

```
private QuietDownload mDownloader = QuietDownload.getImpl();

private final DownloadEntry downloadEntry = new DownloadEntry("url");

  ... 省略代码
  
  // 常用API
  // 开始任务
  mDownloader.startDownad(downloadEntry);
  // 暂停任务
  mDownloader.pause(downloadEntry);
  //取消任务
  mDownloader.cancel(downloadEntry);
  // 恢复任务
  mDownloader.resume(downloadEntry);
  // 恢复所有
  mDownloader.recoverAll(downloadEntry);
  // 暂停所有
  mDownloader.pauseAll(downloadEntry);
  
```


### TODO
 - 重试机制
 - 拦截器实现



 
