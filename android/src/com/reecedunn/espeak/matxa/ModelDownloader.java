package com.reecedunn.espeak.matxa;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Downloads the two Matxa ONNX models (~227 MB total) from Hugging Face on first run.
 * They are not bundled in the APK to keep it small and to avoid re-downloading on every build.
 */
public final class ModelDownloader {

    private static final String BASE_URL =
        "https://huggingface.co/projecte-aina/matxa-tts-cat-multiaccent/resolve/main/";

    public static final String MATCHA_FILENAME = "matcha_multispeaker_cat_all_opset_15_10_steps.onnx";
    public static final String VOCODER_FILENAME = "matxa_multiaccent_wavenext_e2e.onnx";

    private ModelDownloader() {}

    public static File matchaFile(File dir) {
        return new File(dir, MATCHA_FILENAME);
    }

    public static File vocoderFile(File dir) {
        return new File(dir, VOCODER_FILENAME);
    }

    public static boolean areModelsReady(File dir) {
        return matchaFile(dir).exists() && vocoderFile(dir).exists();
    }

    public interface ProgressListener {
        void onProgress(int fileIndex, long downloaded, long total);
    }

    /** Downloads both files if missing. Call from a background thread. */
    public static void downloadModels(File dir, final ProgressListener listener) throws IOException {
        if (!dir.exists()) dir.mkdirs();
        String[] names = {MATCHA_FILENAME, VOCODER_FILENAME};
        for (int index = 0; index < names.length; index++) {
            String name = names[index];
            File dest = new File(dir, name);
            if (dest.exists()) continue;
            File tmp = new File(dir, name + ".part");
            downloadTo(BASE_URL + name, tmp, index, listener);
            if (!tmp.renameTo(dest)) {
                throw new IOException("Could not rename downloaded file for " + name);
            }
        }
    }

    private static void downloadTo(String url, File dest, final int fileIndex, final ProgressListener listener) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setInstanceFollowRedirects(true);
        connection.connect();
        long total = connection.getContentLengthLong();
        try (InputStream input = connection.getInputStream();
             OutputStream output = new FileOutputStream(dest)) {
            byte[] buffer = new byte[64 * 1024];
            long downloaded = 0;
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
                downloaded += read;
                listener.onProgress(fileIndex, downloaded, total);
            }
        } finally {
            connection.disconnect();
        }
    }
}

