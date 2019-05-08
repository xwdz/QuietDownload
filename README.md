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
  - 支持队列
  - 队列最大同时下载任务数,超过则进入等待队列
  - 自动恢复上一次下载任务
 

### 使用方法

#### downloader 权限相关

```
    <uses-permission android:name="android.permission.INTERNET"/>
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
    
        DownloadConfig downloadConfig = new DownloadConfig(this);
        
        // 自定义配置 均有默认值
        
        // downloadConfig.setMaxDownloadTasks(); 队列最大同时下载任务数,超过则进入等待队列 默认:3
        // downloadConfig.setMaxDownloadThreads() 最大线程下载数   默认:3
        // downloadConfig.setDownloadDir() 下载文件路径
            ...省略若干
        
        QuietDownloader.get().setDownloadConfig(downloadConfig);
        QuietDownloader.get().setContext(this);
        QuietDownloader.get().startService(); 
   
     
#### DownloadEntry的几种状态

|状态|说明|
|:---|:---|
|`IDLE`|空闲|
|`WAITING`|等待|
|`CONNECTING`|连接|
|`CONNECT_SUCCESSFUL`|连接成功|
|`DOWNLOADING`|开始下载|
|`PAUSED`|暂停|
|`CANCELLED`|取消|
|`COMPLETED`|完成|
|`ERROR`|发生错误|

                        
#### 使用方法

```
private QuietDownload mDownloader = QuietDownload.getImpl();

private final DownloadEntry downloadEntry = new DownloadEntry("url","name");

  ... 省略代码
  
```


|`mDownloader` 常用Api|参数|说明|
|:---|:---|:---|
|`startDownload`|downloadEntry|下载一个任务|
|`pause`|downloadEntry|在听一个任务|
|`cancel`|downloadEntry|取消一个任务|
|`resume`|downloadEntry|恢复一个下载任务|
|`recoverAll`|无|恢复所有下载任务|
|`pauseAll`|无|暂停所有任务|
|`queryAll`|无|查询所有下载任务返回一个list|
|`queryById`|id|查询一个`downloadEntry`从数据库中
|`deleteById`|id|从数据库中删除一个downloadEntry|
|`getDBDao`|无|返回`Dao<DownloadEntry, String>`自定义进行数据查询|


......
  
  
     
#### 监听

`QuiteDownload` 并没有采用传统listener方式，而是使用了观察者模式,如需要在某个界面监听下载进度

```
    private final DataUpdatedWatcher mDataUpdateReceiver = new DataUpdatedWatcher() {
        @Override
        public void notifyUpdate(DownloadEntry entry) {
            // calback mainUIThread 
            // do something
            // 可根据 entry status来判断一些列状态
            if(entry.status == DownloadEntry.pause || DownloadEntry.downloading ...)
        }
    };
    
    // 省略若干代码
    
    //监听下载状态
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

    /**
     * @param url  下载地址
     * @param name 文件名称（带后缀）
     */
    public DownloadEntry(String url, String name) {
        this.url = url;
        this.id = url;
        this.name = name;
        this.filePath = QuietDownloader.getImpl().getConfigs().getDownloadFile(name).getAbsolutePath();
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
- `DownloadEntry` 实体类重写其 equals 以及 hashCode 方法，使用其 id hashCode 来作为其标准
- `QuietDownloader` 内部使用DownloadEntry实体类进行关联
- `QuietDownEntry`的`name`属性最终作为下载文件名称  
- 如果自定义了`downloadConfig.setDownloadDir()`下载位置注意申请读写权限



### TODO
 - 重试机制
 - 拦截器实现
 

 
---

## 版本历史

### v1.0.3
  - 新增配置`QuietDownloader.setReadTimeoutMillis()`,`QuietDownloader.setConnTimeMillis`

### v1.0.2
  - [fix Issues5](https://github.com/xwdz/QuietDownload/issues/5)

### v1.0.1
  - `DownloadConfig`提供自定义下载目录
  - [Fix Issues3](https://github.com/xwdz/QuietDownload/issues/3)

### v0.0.6
  - `QuietDownloader` 可通过`getDBDao()`拿到`Dao<DownloadEntry, String>`对象操作数据库
  - `QuietDownloader` 提供查询所有方法数据库DownloadEntry `queryAll()`

### v0.0.5
  - 修复默认使用url作为文件名称url长度过长问题    

### v0.0.4
  - 新增DownloadStatus连接成功`CONNECT_SUCCESSFUL`枚举


 
