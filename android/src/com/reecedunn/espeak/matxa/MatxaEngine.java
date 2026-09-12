package com.reecedunn.espeak.matxa;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OnnxValue;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

import java.nio.FloatBuffer;
import java.nio.LongBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

/**
 * Runs the two Matxa ONNX models: the acoustic model (text ids -> mel spectrogram) and the
 * WaveNeXt vocoder (mel spectrogram -> waveform). Mirrors projecte-aina/tts-api's
 * infer_wavenext_onnx.py.
 *
 * speakerId: 6 = Lluc (male, valencia), 7 = Gina (female, valencia).
 */
public class MatxaEngine {

    public final int sampleRate = 22050;

    private final OrtEnvironment env;
    private final OrtSession matchaSession;
    private final OrtSession vocoderSession;

    public MatxaEngine(String matchaModelPath, String vocoderModelPath) throws OrtException {
        Breadcrumb.mark("MatxaEngine: getting OrtEnvironment");
        env = OrtEnvironment.getEnvironment();
        Breadcrumb.mark("MatxaEngine: creating matcha session");
        matchaSession = env.createSession(matchaModelPath, new OrtSession.SessionOptions());
        Breadcrumb.mark("MatxaEngine: creating vocoder session");
        vocoderSession = env.createSession(vocoderModelPath, new OrtSession.SessionOptions());
        Breadcrumb.mark("MatxaEngine: both sessions created OK");
    }

    public float[] synthesize(int[] ids, int speakerId) throws OrtException {
        return synthesize(ids, speakerId, 0.667f, 1.0f);
    }

    public float[] synthesize(int[] ids, int speakerId, float temperature, float speakingRate) throws OrtException {
        Breadcrumb.mark("synthesize: start, ids.length=" + ids.length);
        long[] idsLong = new long[ids.length];
        for (int i = 0; i < ids.length; i++) idsLong[i] = ids[i];

        try (OnnxTensor xTensor = OnnxTensor.createTensor(
                env, LongBuffer.wrap(idsLong), new long[]{1, ids.length});
             OnnxTensor xLengthsTensor = OnnxTensor.createTensor(
                env, LongBuffer.wrap(new long[]{ids.length}), new long[]{1});
             OnnxTensor scalesTensor = OnnxTensor.createTensor(
                env, FloatBuffer.wrap(new float[]{temperature, speakingRate}), new long[]{2});
             OnnxTensor spksTensor = OnnxTensor.createTensor(
                env, LongBuffer.wrap(new long[]{speakerId}), new long[]{1})) {

            Breadcrumb.mark("synthesize: input tensors created OK");

            Map<String, OnnxTensor> matchaInputs = new HashMap<>();
            matchaInputs.put("x", xTensor);
            matchaInputs.put("x_lengths", xLengthsTensor);
            matchaInputs.put("scales", scalesTensor);
            matchaInputs.put("spks", spksTensor);

            Breadcrumb.mark("synthesize: about to run matchaSession.run()");
            try (OrtSession.Result matchaResult = matchaSession.run(matchaInputs)) {
                Breadcrumb.mark("synthesize: matchaSession.run() returned OK");
                Iterator<Map.Entry<String, OnnxValue>> matchaOutIt = matchaResult.iterator();
                Object mel = matchaOutIt.next().getValue().getValue();
                Breadcrumb.mark("synthesize: got mel output, class=" + mel.getClass().getName());

                java.util.Set<String> vocoderInputNames = vocoderSession.getInputNames();
                Breadcrumb.mark("synthesize: vocoder input names = " + vocoderInputNames);

                // Figure out the time (frame) length of the mel output, in case the vocoder
                // also wants a "*_lengths" input (mirrors what the matcha model itself needs).
                long melLength = 0;
                if (mel instanceof float[][][]) {
                    melLength = ((float[][][]) mel)[0][0].length;
                }

                Map<String, OnnxTensor> vocoderInputs = new HashMap<>();
                java.util.List<OnnxTensor> toClose = new java.util.ArrayList<>();
                try {
                    for (String name : vocoderInputNames) {
                        String lower = name.toLowerCase();
                        if (lower.contains("length")) {
                            OnnxTensor t = OnnxTensor.createTensor(env, LongBuffer.wrap(new long[]{melLength}), new long[]{1});
                            toClose.add(t);
                            vocoderInputs.put(name, t);
                        } else {
                            OnnxTensor t = OnnxTensor.createTensor(env, mel);
                            toClose.add(t);
                            vocoderInputs.put(name, t);
                        }
                    }

                    Breadcrumb.mark("synthesize: vocoder inputs built OK, about to run vocoderSession.run()");
                    try (OrtSession.Result vocoderResult = vocoderSession.run(vocoderInputs)) {
                        Breadcrumb.mark("synthesize: vocoderSession.run() returned OK");
                        Iterator<Map.Entry<String, OnnxValue>> vocoderIt = vocoderResult.iterator();
                        Object wav = vocoderIt.next().getValue().getValue();
                        Breadcrumb.mark("synthesize: got wav output, class=" + wav.getClass().getName());
                        return flattenWav(wav);
                    }
                } finally {
                    for (OnnxTensor t : toClose) t.close();
                }
            }
        }
    }

    /** The vocoder output can come back as [1, 1, N] or [1, N] depending on export; flatten either. */
    private float[] flattenWav(Object wav) {
        if (wav instanceof float[]) {
            return (float[]) wav;
        }
        if (wav instanceof float[][]) {
            return ((float[][]) wav)[0];
        }
        if (wav instanceof float[][][]) {
            return ((float[][][]) wav)[0][0];
        }
        throw new IllegalStateException("Unexpected vocoder output type: " + wav.getClass());
    }

    public void close() {
        try {
            matchaSession.close();
            vocoderSession.close();
        } catch (OrtException ignored) {
        }
    }
}
