package com.changhong.zxx.library.update;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by zxx on 2018/5/22.
 */

public class HTTPTools {

    private static HTTPTools mInstance = null;
    private final Handler dispatchHandler = new Handler(Looper.getMainLooper());
    private ExecutorService executor;

    private HTTPTools() {
        this.executor = Executors.newFixedThreadPool(1);
    }

    public static HTTPTools getInstance() {
        if (mInstance == null) {
            synchronized (HTTPTools.class) {
                mInstance = new HTTPTools();
            }
        }
        return mInstance;
    }

    public void executeGet(final String urlStr, final Map<String, String> paramsMap, final Callback callback) {
        String content = "";
        if (paramsMap != null) {
            for (Map.Entry<String, String> extra : paramsMap.entrySet()) {
                try {
                    content += extra.getKey()
                            + "="
                            + URLEncoder.encode(extra.getValue(), "UTF-8")
                            + "&";
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            content = content.substring(0, content.length() - 1);
        }

        final String finalContent = content;

        this.executor.execute(new Runnable() {
            @Override
            public void run() {
                InputStream inputStream = null;
                ByteArrayOutputStream baos = null;
                HttpURLConnection urlConnection = null;

                try {
                    URL url;
                    if (paramsMap != null) {
                        url = new URL(urlStr + "?" + finalContent);
                    } else {
                        url = new URL(urlStr);
                    }
                    urlConnection = (HttpURLConnection) url.openConnection();
                    Log.d("UPDATE", "url:" + urlConnection.getURL().toString());
                    Log.d("UPDATE", urlConnection.getURL().toString());
                    //设置超时时间
                    urlConnection.setConnectTimeout(10 * 1000);
                    urlConnection.setReadTimeout(10 * 1000);

                    int statusCode = urlConnection.getResponseCode();
                    if (statusCode == 200) {
                        inputStream = new BufferedInputStream(urlConnection.getInputStream());
                        baos = new ByteArrayOutputStream();
                        int len = 0;
                        byte buffer[] = new byte[1024];
                        while ((len = inputStream.read(buffer)) != -1) {
                            // 根据读取的长度写入到os对象中
                            baos.write(buffer, 0, len);
                        }
                        sendSuccessResultCallback(new String(baos.toByteArray(), "UTF-8"), callback);
                    } else {
                        if (callback != null) {
                            sendFailResultCallback(new IOException("Canceled Or Failed"), callback);
                        }
                    }
                } catch (Exception e) {
                    if (callback != null) {
                        sendFailResultCallback(e, callback);
                    }
                } finally {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (Exception e) {
                            if (callback != null) {
                                sendFailResultCallback(e, callback);
                            }
                        }
                    }
                    if (baos != null) {
                        try {
                            baos.close();
                        } catch (IOException e) {
                            if (callback != null) {
                                sendFailResultCallback(e, callback);
                            }
                        }
                    }
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
            }
        });
    }

    public void executePost(final String urlStr, final Map<String, Object> paramsMap, final Callback callback) {

        this.executor.execute(new Runnable() {
            @Override
            public void run() {
                InputStream inputStream = null;
                ByteArrayOutputStream baos = null;
                HttpURLConnection urlConnection = null;
                String json = null;
                try {
                    URL url = new URL(urlStr);
                    if (paramsMap != null) {
                        json = mapToJson(paramsMap).toString();
                    }
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                    if (!TextUtils.isEmpty(mapToJson(paramsMap).toString())) {

                        byte[] writebytes = json.getBytes();
                        // 设置文件长度
                        urlConnection.setRequestProperty("Content-Length", String.valueOf(writebytes.length));
                        OutputStream outwritestream = urlConnection.getOutputStream();
                        outwritestream.write(json.getBytes());
                        outwritestream.flush();
                        outwritestream.close();
                        Log.d("UPDATE", "post param: " + json);
                    }
                    Log.d("UPDATE", "url:" + urlConnection.getURL().toString());
                    Log.d("UPDATE", urlConnection.getURL().toString());
                    //设置超时时间
                    urlConnection.setConnectTimeout(10 * 1000);
                    urlConnection.setReadTimeout(10 * 1000);

                    int statusCode = urlConnection.getResponseCode();
                    if (statusCode == 200) {
                        inputStream = new BufferedInputStream(urlConnection.getInputStream());
                        baos = new ByteArrayOutputStream();
                        int len = 0;
                        byte buffer[] = new byte[1024];
                        while ((len = inputStream.read(buffer)) != -1) {
                            // 根据读取的长度写入到os对象中
                            baos.write(buffer, 0, len);
                        }
                        sendSuccessResultCallback(new String(baos.toByteArray(), "UTF-8"), callback);
                    } else {
                        if (callback != null) {
                            sendFailResultCallback(new IOException("Canceled Or Failed"), callback);
                        }
                    }
                } catch (Exception e) {
                    if (callback != null) {
                        sendFailResultCallback(e, callback);
                    }
                } finally {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (Exception e) {
                            if (callback != null) {
                                sendFailResultCallback(e, callback);
                            }
                        }
                    }
                    if (baos != null) {
                        try {
                            baos.close();
                        } catch (IOException e) {
                            if (callback != null) {
                                sendFailResultCallback(e, callback);
                            }
                        }
                    }
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
            }
        });
    }


    public static JSONObject mapToJson(Map<String, Object> map) {
        JSONObject json = new JSONObject();
        Set<String> set = map.keySet();
        for (Iterator<String> it = set.iterator(); it.hasNext(); ) {
            String key = it.next();
            try {
                json.put(key, map.get(key));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return json;
    }

    public void downFile(final String urlStr, final String destDir, final String apkName, final Callback callback) {
        this.executor.execute(new Runnable() {
            @Override
            public void run() {
                InputStream inputStream = null;
                FileOutputStream fos = null;
                HttpURLConnection urlConnection = null;

                try {
                    URL url = new URL(urlStr);

                    urlConnection = (HttpURLConnection) url.openConnection();
                    //设置超时时间

                    urlConnection.setConnectTimeout(20 * 1000);
                    urlConnection.setReadTimeout(20 * 1000);

                    int statusCode = urlConnection.getResponseCode();
                    if (statusCode == 200) {
                        //获得网络字节输入流对象
                        InputStream is = urlConnection.getInputStream();// 不是操作文件的吗
                        int totalSize = urlConnection.getContentLength();
                        int currentSize = 0;
                        int progress = 0;
                        //建立内存到硬盘的连接
                        File file = new File(getSavePath(destDir), apkName);
                        if (file.exists()) {
                            if (file.length() == urlConnection.getContentLength()) {
                                //如果以前已经下载完毕，直接回调下载完成
                                sendSuccessResultCallback(file.getAbsolutePath(), callback);
                                return;
                            } else {
                                file.delete();
                            }
                        }
                        fos = new FileOutputStream(file);
                        byte[] buf = new byte[1024];
                        int len = 0;

                        while ((len = is.read(buf)) != -1) {
                            fos.write(buf, 0, len);
                            currentSize += len;
                            int tmpProgress = currentSize * 100 / totalSize;
                            if (tmpProgress > progress) {
                                progress = tmpProgress;
                                sendProgress(progress, currentSize, totalSize, callback);
                            }
                        }
                        fos.flush();
                        sendSuccessResultCallback(file.getAbsolutePath(), callback);
                    } else {
                        if (callback != null) {
                            sendFailResultCallback(new IOException("Canceled Or Failed"), callback);
                        }
                    }
                } catch (Exception e) {
                    if (callback != null) {
                        sendFailResultCallback(e, callback);
                    }
                } finally {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (Exception e) {
                            if (callback != null) {
                                sendFailResultCallback(e, callback);
                            }
                        }
                    }
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            if (callback != null) {
                                sendFailResultCallback(e, callback);
                            }
                        }
                    }
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
            }
        });
    }

    public interface Callback {
        void onError(Exception e);

        void onProgress(int progress, int currentSize, int totalSize);

        void onResponse(Object response);
    }

    public void sendFailResultCallback(final Exception e, final Callback callback) {
        if (callback == null)
            return;
        dispatchHandler.post(new Runnable() {
            @Override
            public void run() {
                callback.onError(e);
            }
        });
    }

    public void sendSuccessResultCallback(final Object object, final Callback callback) {
        if (callback == null)
            return;
        dispatchHandler.post(new Runnable() {
            @Override
            public void run() {
                callback.onResponse(object);
            }
        });
    }

    public void sendProgress(final int progress, final int currentSize, final int totalSize, final Callback callback) {
        if (callback == null)
            return;
        dispatchHandler.post(new Runnable() {
            @Override
            public void run() {
                callback.onProgress(progress, currentSize, totalSize);
            }
        });
    }


    /**
     * 获取apk应该保存的位置，如果有SD卡，则安装在SD卡目录
     *
     * @return
     */
    public String getSavePath(String destDir) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory().getAbsolutePath();
        } else {
            if ("".equals(destDir)) {
                return Environment.getDataDirectory().getAbsolutePath();
            }
            return destDir + "/file";
        }
    }

}
