package com.example.aabbg.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

public class PermissionUtil {
    // 简化的权限检查工具。对于实际项目，请在 Activity 中处理运行时权限请求。
    public static boolean checkPermissions(Activity activity) {
        // 这里简单返回 true；如果要检查实际权限请实现对应逻辑
        return true;
    }

    public static boolean hasPermissions(Context context) {
        return true;
    }
}
