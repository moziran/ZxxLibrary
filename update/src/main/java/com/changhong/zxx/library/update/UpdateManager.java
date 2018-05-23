package com.changhong.zxx.library.update;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Copyright (c) 2016 北京视达科技有限责任公司 All rights reserved.
 *
 * @author baolin.yu
 * @date 2016/10/21 9:31
 * @description
 */
public final class UpdateManager {

    public enum UpdateType {
        FAILED,   //网络访问失败
        NULL,     //返回为空
        FORCE,    //强制更新
        MANUAL,   //手动更新
        NOUPDATE  //没有更新
    }

    public static final String PKG_GOOGLE = "com.android.vending";//Google Play
    public static final String PKG_QQ = "com.tencent.android.qqdownloader";//	应用宝
    public static final String PKG_360 = "om.qihoo.appstore";//	360手机助手
    public static final String PKG_BAIDU = "com.baidu.appsearch";//	百度手机助
    public static final String PKG_XIAOMI = "com.xiaomi.market";//	小米应用商店
    public static final String PKG_WANDOUJIA = "com.wandoujia.phoenix2";//	豌豆荚
    public static final String PKG_HUAWEI = "com.huawei.appmarket";//	华为应用市场
    public static final String PKG_TAOBAO = "com.taobao.appcenter";//	淘宝手机助手
    public static final String PKG_HIAPK = "com.hiapk.marketpho";//	安卓市场
    public static final String PKG_GOAPK = "cn.goapk.market";//	安智市场
    public static final String PKG_91 = "com.dragon.android.pandaspace";//	安智市场
    public static final String PKG_PP = "com.pp.assistant";//	PP手机助手
    public static final String PKG_ZHONGXIN = "zte.com.market ";//	兴应用商店
    public static final String PKG_LENOVO = "com.lenovo.leos.appstore";//	联想应用商店

    private UpdateType mUpdateType;
    private OnDealResponse responseListener;
    private static UpdateManager instance = null;

    private UpdateManager() {
    }

    public static synchronized UpdateManager getInstance() {
        if (instance == null) {
            instance = new UpdateManager();
        }
        return instance;
    }

    /**
     * 请求接口检查更新
     *
     * @param baseUrl        前缀url
     * @param extraParams    格外的参数
     * @param onUpdateResult 回调
     * @param onDealResponse 设置结果解析方法
     */
    public void checkUpdate(String baseUrl, Map<String, Object> extraParams, OnDealResponse onDealResponse, final OnUpdateResult onUpdateResult) {
        if (extraParams == null) {
            extraParams = new HashMap<>();
        }
        if (onDealResponse == null) {
            mUpdateType = UpdateType.NULL;
            UpdateInfo updateInfo = new UpdateInfo();
            updateInfo.type = mUpdateType;
            onUpdateResult.onUpdateResult(updateInfo);
            return;
        }
        responseListener = onDealResponse;
        HTTPTools.getInstance().executePost(baseUrl
                , extraParams
                , new HTTPTools.Callback() {
                    @Override
                    public void onError(Exception e) {
                        mUpdateType = UpdateType.FAILED;
                        UpdateInfo updateInfo = new UpdateInfo();
                        updateInfo.type = mUpdateType;
                        onUpdateResult.onUpdateResult(updateInfo);
                    }

                    @Override
                    public void onProgress(int progress, int currentSize, int totalSize) {

                    }

                    @Override
                    public void onResponse(final Object response) {
                        try {
                            UpdateInfo info = responseListener.onDealResponse(response.toString());
                            onUpdateResult.onUpdateResult(info);
                        } catch (Exception e) {
                            onError(new Exception());
                            Log.e("UPDATE", "error:param parse failed");
                        }
                    }

                });
    }


    /**
     * @param url              下载地址
     * @param destDir          安装包存储文件夹
     * @param fileName         文件名称
     * @param onDownloadResult
     */
    public void downloadApk(String url, String destDir, String fileName, final OnDownloadResult onDownloadResult) {
        HTTPTools.getInstance().downFile(url, destDir, fileName + ".apk", new HTTPTools.Callback() {
            @Override
            public void onError(Exception e) {
                if (onDownloadResult != null) {
                    onDownloadResult.onDownloadError(e);
                }
            }

            @Override
            public void onProgress(int progress, int currentSize, int totalSize) {
                onDownloadResult.onDownloadInfo(progress, currentSize, totalSize);
            }

            @Override
            public void onResponse(Object response) {
                if (onDownloadResult != null) {
                    onDownloadResult.onDownloadSuccess(response.toString());
                }
            }
        });
    }

    /**
     * 启动到应用商店app主页面或详情界面
     *
     * @param appPkg    目标App的包名,如果为""则由系统弹出应用商店列表供用户选择，然后跳转到该应用商店首页
     *                  如果应用商店中没有指定的目标APP，仍然会跳转到应用商店，只是页面展示为空
     * @param marketPkg 应用商店包名 ,如果为""则由系统弹出应用商店列表供用户选择,否则调转到目标市场的应用详情界面，某些应用商店可能会失败
     */
    public void launchAppDetail(Context context, String appPkg, String marketPkg, OnLaunchAppResult onLaunchAppResult) {
        try {
            Intent intent;
            if (TextUtils.isEmpty(appPkg)) {
                intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_APP_MARKET);
            } else {
                intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.parse("market://details?id=" + appPkg);
                intent.setData(uri);
                if (!TextUtils.isEmpty(marketPkg)) {
                    intent.setPackage(marketPkg);
                }
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            if (onLaunchAppResult != null) {
                if (e instanceof ActivityNotFoundException) {
                    onLaunchAppResult.onLaunchAppError(e, false);
                } else {
                    onLaunchAppResult.onLaunchAppError(e, true);
                }
            }
        }
    }

    /**
     * 打开安装包安装
     *
     * @param context
     * @param filePath
     */
    public void openApk(Context context, String filePath) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Uri uri;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            uri = UpdateProvider.getUriForFile(context, context.getPackageName() + ".updateprovider", new File(filePath));
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            uri = Uri.fromFile(new File(filePath));
        }

        intent.setDataAndType(uri, "application/vnd.android.package-archive"
        );
        context.startActivity(intent);
    }

    /**
     * @param activity
     * @param filePath
     * @param requestCode
     */
    public void openApkForResult(Activity activity, String filePath, int requestCode) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Uri uri;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            uri = UpdateProvider.getUriForFile(activity, activity.getPackageName() + ".updateprovider", new File(filePath));
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            uri = Uri.fromFile(new File(filePath));
        }

        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        activity.startActivityForResult(intent, requestCode);

        //android 7.0 startActivityForResult安装应用时，不能显示安装成功选择打开界面，结束当前进程后会显示
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            android.os.Process.killProcess(android.os.Process.myPid());
        }

    }


    public String getJsonString(JSONObject version, String item) {
        String itemStr = "";
        try {
            if (version != null) {
                itemStr = version.getString(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return itemStr;
    }

    public interface OnUpdateResult {
        void onUpdateResult(UpdateInfo updateInfo);
    }

    public interface OnLaunchAppResult {
        void onLaunchAppError(Exception e, boolean isMarketExist);
    }

    public interface OnDealResponse {
        UpdateInfo onDealResponse(String response);
    }

    public interface OnDownloadResult {
        void onDownloadSuccess(String filePath);

        void onDownloadInfo(int progress, int currentSize, int fileSize);

        void onDownloadError(Exception e);
    }
}
