/*
    Pasteque Android client
    Copyright (C) Pasteque contributors, see the COPYRIGHT file

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
package fr.pasteque.client;

import android.widget.Toast;
import fr.pasteque.client.data.Data;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.io.IOError;

/**
 * Exception handler to send debug log by sendMail
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private static final String LOG_TAG = "Pasteque/Crash";

    private static final CrashHandler instance = new CrashHandler();
    private static final String MAIL = "support@pasteque.coop";

    private Thread.UncaughtExceptionHandler defaultHandler;
    private Context appContext;

    /**
     * Set crash handler to catch uncaught exceptions
     */
    public static void enableCrashHandler(Context appContext) {
        Thread.setDefaultUncaughtExceptionHandler(instance);
        instance.appContext = appContext;
    }

    private CrashHandler() {
        this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        try {
            Log.e(LOG_TAG, "Uncaught exception", e);
            String body = getBody(e);
            if (Pasteque.getConf().isMailEnabled()) {
                sendMail(body);
            }
            Data.Crash.save(body, this.appContext);
        } catch (IOError | Exception ee) {
            ee.printStackTrace();
        } finally {
            this.defaultHandler.uncaughtException(t, e);
        }
    }

    private void sendMail(String body) throws PackageManager.NameNotFoundException {
        PackageManager pm = this.appContext.getPackageManager();
        PackageInfo info = pm.getPackageInfo(this.appContext.getPackageName(), 0);
        String version = info.versionName;
        String subject = "Pastèque " + version + " has crashed";
        String to = CrashHandler.MAIL;
        mailIntent(to, subject, body);
    }

    private String getBody(Throwable e) {
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
        return body;
    }

    private void mailIntent(String to, String subject, String body) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{to});
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, body);
        try {
            this.appContext.startActivity(intent);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this.appContext, "Could not send email", Toast.LENGTH_SHORT).show();
        }
    }
}
