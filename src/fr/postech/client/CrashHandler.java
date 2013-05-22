/*
    POS-Tech Android
    Copyright (C) 2012 SARL SCOP Scil (contact@scil.coop)

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package fr.postech.client;

import fr.postech.client.data.CrashData;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

/** Exception handler to send debug log by mail */
public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private static final String LOG_TAG = "POS-TECH/Crash";

    private static final CrashHandler instance = new CrashHandler();

    private Thread.UncaughtExceptionHandler defaultHandler;
    private Context appContext;

    /** Set crash handler to catch uncaught exceptions */
    public static void enableCrashHandler(Context appContext) {
        Thread.setDefaultUncaughtExceptionHandler(instance);
        instance.appContext = appContext;
    }

    private CrashHandler() {
        this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    public void uncaughtException(Thread t, Throwable e) {
        try {
            Log.e(LOG_TAG, "Uncaught exception", e);
            // Prepare mail
            String version = "unknown";
            PackageManager pm = this.appContext.getPackageManager();
            PackageInfo info = pm.getPackageInfo(this.appContext.getPackageName(), 0);
            version = info.versionName;
            String subject = "Pastèque " + version + " has crashed";
            String to = "pos-tech-debug@lists.scil.coop";
            String body = "Pastèque has crashed, here is the log\n\n";
            body += e.getClass().toString() + ":" + e.getMessage() + "\n";
            for (StackTraceElement st : e.getStackTrace()) {
                body += st.getClassName() + ":" + st.getMethodName() + " (line "
                        + st.getLineNumber() + ")\n";
            }
            body += "\nCaused by:\n";
            Throwable cause = e.getCause();
            if (cause != null) {
                body += cause.getClass().toString() + ":" + cause.getMessage() + "\n";
                for (StackTraceElement st : cause.getStackTrace()) {
                    body += st.getClassName() + ":" + st.getMethodName() + " (line "
                            + st.getLineNumber() + ")\n";
                }
            } else {
                body += "null";
            }
            // Save the body as last error
            CrashData.save(body, this.appContext);
            // Set intent and send
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{to});
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
            intent.putExtra(Intent.EXTRA_TEXT, body);
            this.appContext.startActivity(intent);
            System.out.println(intent);
        } catch (Exception ee) {
            // crash handle has crashed...
            ee.printStackTrace();
        } finally {
            // Call default handler to crash the app "gracefully"
            this.defaultHandler.uncaughtException(t, e);
        }
    }
}
