package com.reecedunn.espeak.matxa;

import android.app.Activity;
import android.app.AlertDialog;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.reecedunn.espeak.R;
import com.reecedunn.espeak.SpeechSynthesis;

import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MatxaActivity extends Activity {

    private EditText textInput;
    private RadioGroup speakerGroup;
    private Button playButton;
    private ProgressBar progressBar;
    private TextView statusText;
    private GridLayout quickPhrasesGrid;
    private Button addPhraseButton;
    private Button saveCurrentButton;
    private final List<QuickPhrase> quickPhrases = new ArrayList<>();

    private MatxaEngine engine;
    private Phonemizer phonemizer;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private String crashPrefix = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matxa);

        Breadcrumb.init(getApplicationContext());

        textInput = findViewById(R.id.matxa_text_input);
        speakerGroup = findViewById(R.id.matxa_speaker_group);
        playButton = findViewById(R.id.matxa_play_button);
        progressBar = findViewById(R.id.matxa_progress);
        statusText = findViewById(R.id.matxa_status);
        quickPhrasesGrid = findViewById(R.id.matxa_quick_phrases_grid);
        addPhraseButton = findViewById(R.id.matxa_add_phrase_button);
        saveCurrentButton = findViewById(R.id.matxa_save_button);

        String lastStep = Breadcrumb.readLast();
        if (lastStep != null) {
            crashPrefix = "LA VEGADA ANTERIOR ES VA TANCAR JUST DESPRES DE:\n" + lastStep + "\n\n---\n\n";
            statusText.setText(crashPrefix);
        }

        quickPhrases.addAll(QuickPhrasesStore.load(this));
        renderQuickPhrases();

        addPhraseButton.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                showAddPhraseDialog();
            }
        });

        saveCurrentButton.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                String text = textInput.getText().toString().trim();
                if (text.isEmpty()) return;
                quickPhrases.add(new QuickPhrase(text, text));
                QuickPhrasesStore.save(MatxaActivity.this, quickPhrases);
                renderQuickPhrases();
                Toast.makeText(MatxaActivity.this, "Guardat com a frase rapida", Toast.LENGTH_SHORT).show();
            }
        });

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

    private int currentSpeakerId() {
        return speakerGroup.getCheckedRadioButtonId() == R.id.matxa_speaker_gina ? 7 : 6;
    }

    private void renderQuickPhrases() {
        quickPhrasesGrid.removeAllViews();
        for (final QuickPhrase phrase : quickPhrases) {
            Button b = new Button(this);
            b.setText(phrase.label);
            b.setAllCaps(false);
            b.setSingleLine(false);
            b.setMaxLines(2);

            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = 0;
            lp.height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
            lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            lp.setMargins(6, 6, 6, 6);
            b.setLayoutParams(lp);

            b.setOnClickListener(new android.view.View.OnClickListener() {
                @Override
                public void onClick(android.view.View v) {
                    speak(phrase.text, currentSpeakerId());
                }
            });
            b.setOnLongClickListener(new android.view.View.OnLongClickListener() {
                @Override
                public boolean onLongClick(android.view.View v) {
                    showEditPhraseDialog(phrase);
                    return true;
                }
            });

            quickPhrasesGrid.addView(b);
        }
    }

    private void showAddPhraseDialog() {
        final EditText input = new EditText(this);
        input.setHint("Ex: Vull aigua");

        new AlertDialog.Builder(this)
            .setTitle("Nova frase rapida")
            .setView(input)
            .setPositiveButton("Afegir", new android.content.DialogInterface.OnClickListener() {
                @Override
                public void onClick(android.content.DialogInterface dialog, int which) {
                    String text = input.getText().toString().trim();
                    if (text.isEmpty()) return;
                    quickPhrases.add(new QuickPhrase(text, text));
                    QuickPhrasesStore.save(MatxaActivity.this, quickPhrases);
                    renderQuickPhrases();
                }
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }

    private void showEditPhraseDialog(final QuickPhrase phrase) {
        final EditText input = new EditText(this);
        input.setText(phrase.text);
        input.setSelection(input.getText().length());

        new AlertDialog.Builder(this)
            .setTitle("Editar frase")
            .setView(input)
            .setPositiveButton("Guardar", new android.content.DialogInterface.OnClickListener() {
                @Override
                public void onClick(android.content.DialogInterface dialog, int which) {
                    String text = input.getText().toString().trim();
                    if (text.isEmpty()) return;
                    phrase.label = text;
                    phrase.text = text;
                    QuickPhrasesStore.save(MatxaActivity.this, quickPhrases);
                    renderQuickPhrases();
                }
            })
            .setNeutralButton("Eliminar", new android.content.DialogInterface.OnClickListener() {
                @Override
                public void onClick(android.content.DialogInterface dialog, int which) {
                    quickPhrases.remove(phrase);
                    QuickPhrasesStore.save(MatxaActivity.this, quickPhrases);
                    renderQuickPhrases();
                }
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }

    private void prepareEverything() {
        setStatus(getString(R.string.matxa_status_preparing));
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Breadcrumb.mark("prepareEverything: ensuring voice data is extracted");
                    VoiceDataInstaller.ensureInstalled(MatxaActivity.this);
                    Breadcrumb.mark("prepareEverything: creating SpeechSynthesis (espeak init)");
                    SpeechSynthesis speech = new SpeechSynthesis(MatxaActivity.this, new SpeechSynthesis.SynthReadyCallback() {
                        @Override
                        public void onSynthDataReady(byte[] audioData) {}

                        @Override
                        public void onSynthDataComplete() {}
                    });
                    Breadcrumb.mark("prepareEverything: SpeechSynthesis created OK, creating Phonemizer");
                    phonemizer = new Phonemizer(speech);
                    Breadcrumb.mark("prepareEverything: Phonemizer created OK");

                    File modelDir = getExternalFilesDir(null);
                    if (modelDir == null) modelDir = getFilesDir();
                    final File dir = modelDir;

                    if (!ModelDownloader.areModelsReady(dir)) {
                        Breadcrumb.mark("prepareEverything: models not ready, downloading");
                        setStatus(getString(R.string.matxa_status_downloading));
                        ModelDownloader.downloadModels(dir, new ModelDownloader.ProgressListener() {
                            @Override
                            public void onProgress(int fileIndex, long downloaded, long total) {
                                int pct = total > 0 ? (int) (downloaded * 100 / total) : 0;
                                String label = fileIndex == 0 ? "model" : "vocoder";
                                setStatus(getString(R.string.matxa_status_downloading) + " (" + label + " " + pct + "%)");
                            }
                        });
                        Breadcrumb.mark("prepareEverything: download finished OK");
                    } else {
                        Breadcrumb.mark("prepareEverything: models already on disk");
                    }

                    Breadcrumb.mark("prepareEverything: creating MatxaEngine (loading ONNX sessions)");
                    engine = new MatxaEngine(
                        ModelDownloader.matchaFile(dir).getAbsolutePath(),
                        ModelDownloader.vocoderFile(dir).getAbsolutePath()
                    );
                    Breadcrumb.mark("prepareEverything: MatxaEngine created OK, everything ready");

                    setStatus(getString(R.string.matxa_status_ready));
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(ProgressBar.GONE);
                            playButton.setEnabled(true);
                        }
                    });
                } catch (final Throwable t) {
                    Log.e("MatxaActivity", "prepareEverything failed", t);
                    setStatus("ERROR PREPARANT:\n" + Log.getStackTraceString(t));
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
                    Breadcrumb.mark("speak: calling phonemizeToIds");
                    int[] ids = phonemizer.phonemizeToIds(text);
                    Breadcrumb.mark("speak: calling engine.synthesize, ids.length=" + ids.length);
                    final float[] audio = engine.synthesize(ids, speakerId);
                    Breadcrumb.mark("speak: synthesize returned, audio.length=" + audio.length + ", calling playAudio");
                    playAudio(audio, engine.sampleRate);
                    Breadcrumb.mark("speak: playAudio returned OK");
                } catch (final Throwable t) {
                    Log.e("MatxaActivity", "speak failed", t);
                    final String trace = Log.getStackTraceString(t);
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            statusText.setText("ERROR REPRODUINT:\n" + trace);
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
                statusText.setText(crashPrefix + text);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (engine != null) engine.close();
    }
}
