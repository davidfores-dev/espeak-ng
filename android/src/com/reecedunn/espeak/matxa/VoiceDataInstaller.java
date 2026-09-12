package com.reecedunn.espeak.matxa;

import android.content.Context;
import android.content.res.Resources;

import com.reecedunn.espeak.CheckVoiceData;
import com.reecedunn.espeak.FileUtils;
import com.reecedunn.espeak.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Extracts the bundled espeak-ng voice data (res/raw/espeakdata.zip) the first time it's needed.
 * Normally this only happens as a side effect of running the (unrelated) DownloadVoiceData
 * activity from the original eSpeak demo app; MatxaActivity never launches that, so without this
 * the native espeak library initializes with zero voices loaded and every voice lookup fails.
 */
public final class VoiceDataInstaller {

    private VoiceDataInstaller() {}

    public static void ensureInstalled(Context context) throws Exception {
        boolean hasBase = CheckVoiceData.hasBaseResources(context);
        boolean upToDate = hasBase && !CheckVoiceData.canUpgradeResources(context);
        if (upToDate) {
            Breadcrumb.mark("VoiceDataInstaller: voice data already installed and up to date");
            return;
        }

        Breadcrumb.mark("VoiceDataInstaller: extracting espeakdata.zip");
        File dataPath = CheckVoiceData.getDataPath(context).getParentFile();
        FileUtils.rmdir(CheckVoiceData.getDataPath(context));

        Resources res = context.getResources();
        InputStream stream = res.openRawResource(R.raw.espeakdata);
        ZipInputStream zipStream = new ZipInputStream(new BufferedInputStream(stream));

        try {
            byte[] buffer = new byte[64 * 1024];
            ZipEntry entry;
            while ((entry = zipStream.getNextEntry()) != null) {
                File outFile = new File(dataPath, entry.getName());
                if (entry.isDirectory()) {
                    outFile.mkdirs();
                    continue;
                }
                outFile.getParentFile().mkdirs();
                FileOutputStream outputStream = new FileOutputStream(outFile);
                try {
                    int bytesRead;
                    while ((bytesRead = zipStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                } finally {
                    outputStream.close();
                }
                zipStream.closeEntry();
            }

            String version = FileUtils.read(res.openRawResource(R.raw.espeakdata_version));
            File versionFile = new File(dataPath, "espeak-ng-data/version");
            FileUtils.write(versionFile, version);
        } finally {
            zipStream.close();
        }

        Breadcrumb.mark("VoiceDataInstaller: extraction finished, hasBaseResources=" + CheckVoiceData.hasBaseResources(context));
    }
}

