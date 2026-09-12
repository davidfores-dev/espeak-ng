package com.reecedunn.espeak.matxa;

import android.app.Activity;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.reecedunn.espeak.R;
import com.reecedunn.espeak.SpeechSynthesis;

import java.io.File;

public class MatxaActivity extends Activity {

    private EditText textInput;
    private RadioGroup speakerGroup;
    private Button playButton;
    private ProgressBar progressBar;
    private TextView statusText;

    private MatxaEngine engine;
    private Phonemizer phonemizer;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matxa);

        textInput = findViewById(R.id.matxa_text_input);
        speakerGroup = findViewById(R.id.matxa_speaker_group);
        playButton = findViewById(R.id.matxa_play_button);
        progressBar = findViewById(R.id.matxa_progress);
        statusText = findViewById(R.id.matxa_status);

        playButton.setEnabled(false);
        prepareEverything();

        playButton.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                String text = textInput.getText().toString().trim();
                if (text.isEmpty()) return;
                int speakerId = speakerGroup.getCheckedRadioButtonId() == R.id.matxa_speaker_gina ? 7 : 6;
                speak(text, speakerId);
            }
        });
    }

    private void prepareEverything() {
        setStatus(getString(R.string.matxa_status_preparing));
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SpeechSynthesis speech = new SpeechSynthesis(MatxaActivity.this, new SpeechSynthesis.SynthReadyCallback() {
                        @Override
                        public void onSynthDataReady(byte[] audioData) {}

                        @Override
                        public void onSynthDataComplete() {}
                    });
                    phonemizer = new Phonemizer(speech);

                    File modelDir = getExternalFilesDir(null);
                    if (modelDir == null) modelDir = getFilesDir();
                    final File dir = modelDir;

                    if (!ModelDownloader.areModelsReady(dir)) {
                        setStatus(getString(R.string.matxa_status_downloading));
                        ModelDownloader.downloadModels(dir, new ModelDownloader.ProgressListener() {
                            @Override
                            public void onProgress(int fileIndex, long downloaded, long total) {
                                int pct = total > 0 ? (int) (downloaded * 100 / total) : 0;
                                String label = fileIndex == 0 ? "model" : "vocoder";
                                setStatus(getString(R.string.matxa_status_downloading) + " (" + label + " " + pct + "%)");
                            }
                        });
                    }

                    engine = new MatxaEngine(
                        ModelDownloader.matchaFile(dir).getAbsolutePath(),
                        ModelDownloader.vocoderFile(dir).getAbsolutePath()
                    );

                    setStatus(getString(R.string.matxa_status_ready));
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(ProgressBar.GONE);
                            playButton.setEnabled(true);
                        }
                    });
                } catch (final Exception e) {
                    setStatus("Error: " + e.getMessage());
                }
            }
        }).start();
    }

    private void speak(final String text, final int speakerId) {
        playButton.setEnabled(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (phonemizer == null || engine == null) return;
                    int[] ids = phonemizer.phonemizeToIds(text);
                    final float[] audio = engine.synthesize(ids, speakerId);
                    playAudio(audio, engine.sampleRate);
                } catch (final Exception e) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MatxaActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                } finally {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            playButton.setEnabled(true);
                        }
                    });
                }
            }
        }).start();
    }

    private void playAudio(float[] samples, int sampleRate) {
        int minBufferSize = AudioTrack.getMinBufferSize(
            sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_FLOAT);

        AudioTrack track = new AudioTrack.Builder()
            .setAudioAttributes(new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build())
            .setAudioFormat(new AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                .setSampleRate(sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build())
            .setBufferSizeInBytes(Math.max(minBufferSize, samples.length * 4))
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build();

        track.write(samples, 0, samples.length, AudioTrack.WRITE_BLOCKING);
        track.play();
    }

    private void setStatus(final String text) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                statusText.setText(text);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (engine != null) engine.close();
    }
}

