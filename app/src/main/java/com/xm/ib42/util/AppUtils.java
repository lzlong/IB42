package com.xm.ib42.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.webkit.MimeTypeMap;

import java.io.File;

/**
 * Created by long on 17-2-10.
 * desc  : App相关工具类
 */

public class AppUtils {
    private AppUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    /**
     * 安装App(支持6.0)
     * @param context  上下文
     * @param filePath 文件路径
     */
    public static void installApp(Context context, String filePath) {
        installApp(context, new File(filePath));
    }

    /**
     * 安装App（支持6.0）
     *
     * @param context 上下文
     * @param file    文件
     */
    public static void installApp(Context context, File file) {
        if (!(file != null && file.exists())) return;
        context.startActivity(getInstallAppIntent(context, file));
    }
    public static Intent getInstallAppIntent(Context context, File file) {
        if (file == null) return null;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        String type;

        if (Build.VERSION.SDK_INT < 23) {
            type = "application/vnd.android.package-archive";
        } else {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(getFileExtension(file));
        }
        if (Build.VERSION.SDK_INT >= 23) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            Uri contentUri = FileProvider.getUriForFile(context, "com.your.package.fileProvider", file);
            Uri contentUri = Uri.fromFile(file);
            intent.setDataAndType(contentUri, type);
        }
        intent.setDataAndType(Uri.fromFile(file), type);
        return intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }
    /**
     * 获取全路径中的文件拓展名
     */
    public static String getFileExtension(File file) {
        if (file == null) return null;
        return getFileExtension(file.getPath());
    }
    public static String getFileExtension(String filePath) {
        if (Utils.isBlank(filePath)) return filePath;
        int lastPoi = filePath.lastIndexOf('.');
        int lastSep = filePath.lastIndexOf(File.separator);
        if (lastPoi == -1 || lastSep >= lastPoi) return "";
        return filePath.substring(lastPoi + 1);
    }
    /**
     * 获取App版本码
     *
     * @param context 上下文
     * @return App版本码
     */
    public static int getAppVersionCode(Context context) {
        return getAppVersionCode(context, context.getPackageName());
    }

    /**
     * 获取App版本码
     *
     * @param context     上下文
     * @param packageName 包名
     * @return App版本码
     */
    public static int getAppVersionCode(Context context, String packageName) {
        if (Utils.isBlank(packageName)) return -1;
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(packageName, 0);
            return pi == null ? -1 : pi.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
