package com.example.usercertificate;

import de.robv.android.xposed.*;
import de.robv.android.xposed.callbacks.*;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

/**
* https://api.xposed.info/reference/de/robv/android/xposed/XposedHelpers.html
* https://codeshare.frida.re/@tiiime/android-network-security-config-bypass/
**/

public class MainModule implements IXposedHookLoadPackage {

    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lparam) throws Throwable {
        XposedBridge.log(lparam.packageName);
        Class NetworkSecurityConfig_Builder = XposedHelpers.findClass("android.security.net.config.NetworkSecurityConfig$Builder", lparam.classLoader);
        Class CertificatesEntryRef = XposedHelpers.findClass("android.security.net.config.CertificatesEntryRef", lparam.classLoader);
        Class CertificateSource = XposedHelpers.findClass("android.security.net.config.CertificateSource", lparam.classLoader);
        Class UserCertificateSource = XposedHelpers.findClass("android.security.net.config.UserCertificateSource", lparam.classLoader);

        XposedHelpers.findAndHookMethod("android.security.net.config.NetworkSecurityConfig$Builder", lparam.classLoader, "getEffectiveCertificatesEntryRefs", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
            }
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Object origin =  param.getResult();
                Object source = XposedHelpers.callStaticMethod(UserCertificateSource, "getInstance");
                Object userCert = XposedHelpers.newInstance(CertificatesEntryRef, source, true);
                XposedHelpers.callMethod(origin, "add", userCert);
                param.setResult(origin);
            }
        });
    }

}
