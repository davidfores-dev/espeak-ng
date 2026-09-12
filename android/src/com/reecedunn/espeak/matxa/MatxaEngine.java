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
        env = OrtEnvironment.getEnvironment();
        matchaSession = env.createSession(matchaModelPath, new OrtSession.SessionOptions());
        vocoderSession = env.createSession(vocoderModelPath, new OrtSession.SessionOptions());
    }

    public float[] synthesize(int[] ids, int speakerId) throws OrtException {
        return synthesize(ids, speakerId, 0.667f, 1.0f);
    }

    public float[] synthesize(int[] ids, int speakerId, float temperature, float speakingRate) throws OrtException {
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

            Map<String, OnnxTensor> matchaInputs = new HashMap<>();
            matchaInputs.put("x", xTensor);
            matchaInputs.put("x_lengths", xLengthsTensor);
            matchaInputs.put("scales", scalesTensor);
            matchaInputs.put("spks", spksTensor);

            try (OrtSession.Result matchaResult = matchaSession.run(matchaInputs)) {
                Iterator<Map.Entry<String, OnnxValue>> matchaIt = matchaResult.iterator();
                Object mel = matchaIt.next().getValue().getValue();

                String vocoderInputName = vocoderSession.getInputNames().iterator().next();
                try (OnnxTensor melTensor = OnnxTensor.createTensor(env, mel)) {
                    Map<String, OnnxTensor> vocoderInputs = new HashMap<>();
                    vocoderInputs.put(vocoderInputName, melTensor);

                    try (OrtSession.Result vocoderResult = vocoderSession.run(vocoderInputs)) {
                        Iterator<Map.Entry<String, OnnxValue>> vocoderIt = vocoderResult.iterator();
                        Object wav = vocoderIt.next().getValue().getValue();
                        return flattenWav(wav);
                    }
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

