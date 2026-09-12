package com.reecedunn.espeak.matxa;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OnnxValue;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

import java.nio.FloatBuffer;
import java.nio.LongBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Runs the Matxa ONNX model(s). The "vocoder" file (matxa_multiaccent_wavenext_e2e.onnx) is
 * actually an end-to-end model: it takes the same (x, x_lengths, scales, spks) inputs as the
 * acoustic-only model and outputs the waveform directly ("wav"/"wav_lengths"), matching
 * projecte-aina/tts-api's infer_wavenext_onnx.py has_vocoder_embedded branch. So we just run
 * this single session directly on the phoneme ids; the separate "matcha" acoustic model is not
 * needed for this export and is not used here.
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
        Breadcrumb.mark("MatxaEngine: both sessions created OK, vocoder inputs=" + vocoderSession.getInputNames()
            + " vocoder outputs=" + vocoderSession.getOutputNames());
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

            Map<String, OnnxTensor> allCandidates = new HashMap<>();
            allCandidates.put("x", xTensor);
            allCandidates.put("x_lengths", xLengthsTensor);
            allCandidates.put("scales", scalesTensor);
            allCandidates.put("spks", spksTensor);

            Set<String> vocoderInputNames = vocoderSession.getInputNames();
            Breadcrumb.mark("synthesize: vocoder input names = " + vocoderInputNames);

            Map<String, OnnxTensor> vocoderInputs = new HashMap<>();
            for (String name : vocoderInputNames) {
                OnnxTensor t = allCandidates.get(name);
                if (t != null) {
                    vocoderInputs.put(name, t);
                }
            }

            Breadcrumb.mark("synthesize: about to run vocoderSession.run() directly with " + vocoderInputs.keySet());
            try (OrtSession.Result result = vocoderSession.run(vocoderInputs)) {
                Breadcrumb.mark("synthesize: vocoderSession.run() returned OK");
                Iterator<Map.Entry<String, OnnxValue>> it = result.iterator();
                Object wav = it.next().getValue().getValue();
                Breadcrumb.mark("synthesize: got wav output, class=" + wav.getClass().getName());
                return flattenWav(wav);
            }
        }
    }

    /** The wav output can come back as [1, 1, N] or [1, N] or [N] depending on export; flatten either. */
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
