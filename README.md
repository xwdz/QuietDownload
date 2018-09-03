### QuiteDownload

### 功能

  - 任何一个界面检测进度
  - 单个任务下载
  - 多个任务下载
  - 取消单个任务
  - 取消全部任务
  - 暂停所有任务
  - 最大同时下载任务数
  - 自动恢复上一次下载任务
 

### 使用方法

#### downloader 权限相关

```
    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
```


#### 添加依赖

```
compile 'com.xwdz:downloader:0.0.1'
```

在AndroidManifest.xml 声明如下service

```
<service android:name="com.xwdz.download.core.DownloadService"/>
```

#### 配置

    1. 在您的Application处调用初始化代码:
       XDownloaderManager.getImpl().bindService(this);
       
    2. 添加一些列配置
        DownloadConfig.getConfig()
                        //debug模式
                        .setDebug(boolean isDebug)
                        //下载文件路径
                        .setDownloadDir(File file)
                        // 最大同时下载任务数
                        .setMaxDownloadTasks(int)
                        // 最大线程下载数
                        .setMaxDownloadThreads(int)
                        // 间隔毫秒
                        .setMinOperateInterval(long)
                        // 自动恢复下载
                        .setRecoverDownloadWhenStart(false);
                        
                        
#### 关于DownloadEntry

```
public class DownloadEntry implements Serializable, Cloneable {

    public String id;
    public String name;
    public String url;
    public int currentLength;
    public int totalLength;
    public DownloadStatus status = DownloadStatus.idle;
    public boolean isSupportRange = false;
    public HashMap<Integer, Integer> ranges;

    public DownloadEntry() {

    }

    public DownloadEntry(String url) {
        this.url = url;
        this.id = url;
        this.name = url.substring(url.lastIndexOf("/") + 1);
    }

    public void reset() {
        currentLength = 0;
        ranges = null;
        File file = DownloadConfig.getConfig().getDownloadFile(url);
        if (file.exists()) {
            file.delete();
        }
    }
    // ... 省略代码
}

Quited内部使用DownloadEntry实体类进行关联
```

#### DownloadEntry的几种状态

```
public enum DownloadStatus {
        idle, waiting, connecting, downloading, paused, resumed, cancelled, completed, error
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

#### 一些接口

```
 public interface HandlerNetwork {
        /**
         * 处理网络状况接口
         * @return true:  消费该事件终止运行下载任务
         *         false: 正常执行下载任务
         */
        boolean onHandlerNetworkStatus();
    }
    
    
mDownloader.setHandlerNetworkListener(new QuiteDownload.DispatcherNetwork() {
            @Override
            public boolean onHandlerNetworkStatus() {
                return false;
            }
        });
```

#### 关于监听

`QuiteDownload` 并没有采用传统listener方式，而是使用了观察者模式,如需要在某个界面监听下载进度

```
    private final DataUpdateReceiver mDataUpdateReceiver = new DataUpdateReceiver() {
        @Override
        public void notifyUpdate(DownloadEntry data) {
            // calback mainUIThread 
            // do something
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



 
