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
        XposedBridge.log("UserCertificateModule: loading for " + lparam.packageName);

        Class networkSecurityConfigBuilder = findClass("android.security.net.config.NetworkSecurityConfig$Builder", lparam.classLoader);
        final Class certificatesEntryRef = findClass("android.security.net.config.CertificatesEntryRef", lparam.classLoader);
        final Class userCertificateSource = findClass("android.security.net.config.UserCertificateSource", lparam.classLoader);

        if (networkSecurityConfigBuilder == null || certificatesEntryRef == null || userCertificateSource == null) {
            XposedBridge.log("UserCertificateModule: Failed to find required classes");
            return;
        }

        XposedHelpers.findAndHookMethod(networkSecurityConfigBuilder, "getEffectiveCertificatesEntryRefs", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Object origin = param.getResult();
                if (origin == null) return;

                Object source = XposedHelpers.callStaticMethod(userCertificateSource, "getInstance");
                Object userCert = null;

                try {
                    // Try 3-arg constructor (Modern Android)
                    userCert = XposedHelpers.newInstance(certificatesEntryRef, source, true, false);
                } catch (Throwable t) {
                    try {
                        // Fallback to 2-arg constructor
                        userCert = XposedHelpers.newInstance(certificatesEntryRef, source, true);
                    } catch (Throwable t2) {
                        XposedBridge.log("UserCertificateModule: Failed to create CertificatesEntryRef instance");
                    }
                }

                if (userCert != null) {
                    XposedHelpers.callMethod(origin, "add", userCert);
                    param.setResult(origin);
                }
            }
        });
    }

    private Class findClass(String className, ClassLoader classLoader) {
        try {
            return XposedHelpers.findClass(className, classLoader);
        } catch (XposedHelpers.ClassNotFoundError e) {
            try {
                return XposedHelpers.findClass(className, ClassLoader.getSystemClassLoader());
            } catch (XposedHelpers.ClassNotFoundError e2) {
                return null;
            }
        }
    }

}
