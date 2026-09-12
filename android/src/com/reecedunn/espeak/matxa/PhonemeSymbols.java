package com.reecedunn.espeak.matxa;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Symbol table copied verbatim (same order) from projecte-aina/tts-api's text/symbols.py,
 * so that ids produced here match what the Matxa ONNX model was trained on.
 */
public final class PhonemeSymbols {

    private static final String PAD = "_";
    private static final String PUNCTUATION = ";:,.!?¡¿—…\"«»“” ";
    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final String LETTERS_IPA =
        "ɑɐɒæɓʙβɔɕçɗɖðʤəɘɚɛɜɝɞɟʄɡɠɢʛɦɧħɥʜɨɪʝɭɬɫɮʟɱɯɰŋɳɲɴøɵɸθœɶʘɹɺɾɻʀʁɽʂʃʈʧʉʊʋⱱʌɣɤʍχʎʏʑʐʒʔʡʕʢǀǁǂǃˈˌːˑʼʴʰʱʲʷˠˤ˞↓↑→↗↘'̩'ᵻ";

    public static final String SYMBOLS = PAD + PUNCTUATION + LETTERS + LETTERS_IPA;

    public static final Set<Character> PUNCTUATION_CHARS = new HashSet<>();
    private static final Map<Character, Integer> SYMBOL_TO_ID = new HashMap<>();

    static {
        for (int i = 0; i < PUNCTUATION.length(); i++) {
            PUNCTUATION_CHARS.add(PUNCTUATION.charAt(i));
        }
        for (int i = 0; i < SYMBOLS.length(); i++) {
            SYMBOL_TO_ID.put(SYMBOLS.charAt(i), i);
        }
    }

    private PhonemeSymbols() {}

    /** Maps a phoneme/punctuation string to a list of symbol ids, skipping unknown characters. */
    public static int[] textToSequence(String phonemes) {
        int[] tmp = new int[phonemes.length()];
        int count = 0;
        for (int i = 0; i < phonemes.length(); i++) {
            Integer id = SYMBOL_TO_ID.get(phonemes.charAt(i));
            if (id != null) {
                tmp[count++] = id;
            }
        }
        int[] result = new int[count];
        System.arraycopy(tmp, 0, result, 0, count);
        return result;
    }

    /** Adds the blank/pad symbol (id 0) between every id, and at the start/end. Matches Matcha-TTS's intersperse(). */
    public static int[] intersperse(int[] ids) {
        int[] result = new int[ids.length * 2 + 1];
        for (int i = 0; i < ids.length; i++) {
            result[i * 2 + 1] = ids[i];
        }
        return result;
    }
}

