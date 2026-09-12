package com.reecedunn.espeak.matxa;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Writes a one-line "we got this far" marker to disk before every risky native call, so that if
 * the process is killed outright (native crash / OOM-kill) with no Java exception to catch, the
 * NEXT app launch can show exactly which step it died on, without needing adb/logcat.
 */
public final class Breadcrumb {

    private static File file;

    private Breadcrumb() {}

    public static void init(Context context) {
        file = new File(context.getFilesDir(), "last_step.txt");
    }

    public static void mark(String step) {
        if (file == null) return;
        try (FileOutputStream out = new FileOutputStream(file, false)) {
            out.write((System.currentTimeMillis() + " :: " + step).getBytes("UTF-8"));
        } catch (IOException ignored) {
        }
    }

    /** Returns the last recorded step from a previous run, or null if none exists. */
    public static String readLast() {
        if (file == null || !file.exists()) return null;
        StringBuilder sb = new StringBuilder();
        try (FileInputStream in = new FileInputStream(file)) {
            Reader reader = new InputStreamReader(in, "UTF-8");
            char[] buffer = new char[256];
            int read;
            while ((read = reader.read(buffer)) != -1) {
                sb.append(buffer, 0, read);
            }
        } catch (IOException e) {
            return null;
        }
        return sb.toString();
    }
}

