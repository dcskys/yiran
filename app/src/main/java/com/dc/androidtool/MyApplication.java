package com.dc.androidtool;

import android.app.Application;
import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.baidu.mapapi.SDKInitializer;
import com.dc.androidtool.utils.ImageOptHelper;
import com.dc.androidtool.utils.cache.BaseCache;
import com.dc.greendao.DaoMaster;
import com.dc.greendao.DaoSession;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.zhy.http.okhttp.OkHttpUtils;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import okhttp3.OkHttpClient;

/**
 *
 */
public class MyApplication extends Application {

    public static RequestQueue queue; //建立队列

    //GreenDao 数据库
    private static Context mContext;
    private static DaoMaster daoMaster;
    private static DaoSession daoSession;

    @Override
    public void onCreate() {
        super.onCreate();

        mContext =getApplicationContext();

        //百度地图SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());


        //okhttp  工具类的初始化
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
//                .addInterceptor(new LoggerInterceptor("TAG"))
                .connectTimeout(10000L, TimeUnit.MILLISECONDS)
                .readTimeout(10000L, TimeUnit.MILLISECONDS)
                        //其他配置
                .build();
        OkHttpUtils.initClient(okHttpClient);




        //初始化 图片缓存框架
        initImageLoader(this);
        queue = Volley.newRequestQueue(this);
    }


    public static Context getContext() {    //全局的content对象，可在任何地方使用
        return mContext;
    }

    public static DaoMaster getDaoMaster(Context context) {
        if (daoMaster == null) {
            //// 通过 DaoMaster 的内部类 DevOpenHelper，你可以得到一个便利的 SQLiteOpenHelper 对象。
            // 可能你已经注意到了，你并不需要去编写「CREATE TABLE」这样的 SQL 语句，因为 greenDAO 已经帮你做了。
            // 注意：默认的 DaoMaster.DevOpenHelper 会在数据库升级时，删除所有的表，意味着这将导致数据的丢失。
            // 所以，在正式的项目中，你还应该做一层封装，来实现数据库的安全升级。
            DaoMaster.OpenHelper helper = new DaoMaster.DevOpenHelper(context, BaseCache.DB_NAME, null);
            daoMaster = new DaoMaster(helper.getWritableDatabase());//db 对象
        }
        return daoMaster;
    }

    public static DaoSession getDaoSession(Context context) {
        if (daoSession == null) {
            if (daoMaster == null) {
                daoMaster = getDaoMaster(context);
            }
            daoSession = daoMaster.newSession();  //主要代码
        }
        return daoSession;
    }


    //初始化图片框架
    private void initImageLoader(Context context) {

        ImageLoaderConfiguration.Builder build = new ImageLoaderConfiguration.Builder(context);
        build.tasksProcessingOrder(QueueProcessingType.LIFO);
        build.diskCacheSize(1024 * 1024 * 50);//50 Mb sd卡(本地)缓存的最大值
        build.memoryCacheSize(1024 * 1024 * 10);// 内存缓存的最大值
        build.memoryCache(new LruMemoryCache(1024 * 1024 * 10));//可以通过自己的内存缓存实现
        build.defaultDisplayImageOptions(ImageOptHelper.getImgOptions()); //自定义图片缓存路径
        ImageLoader.getInstance().init(build.build());   //正式加载运行的配置


      /*  ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .threadPriority(Thread.NORM_PRIORITY - 2)   //线程池加载的数量
                .discCacheFileNameGenerator(new Md5FileNameGenerator()) //将保存的uri用MD5加密
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .defaultDisplayImageOptions(ImageOptHelper.getImgOptions()) //自定义图片缓存路径
                .build();

        ImageLoader.getInstance().init(config);   //正式加载运行的配置*/
    }


    public static RequestQueue getRequestQueue() {
        return queue;
    }



}
