package com.reecedunn.espeak.matxa;

import com.reecedunn.espeak.SpeechSynthesis;

import java.util.ArrayList;
import java.util.List;

/**
 * Turns raw Valencian text into the phoneme id sequence the Matxa ONNX model expects.
 *
 * Mirrors projecte-aina/tts-api's `catalan_valencia_cleaners`: lowercase, phonemize word-by-word
 * with espeak-ng's "ca-va" voice (preserving punctuation instead of letting espeak swallow it),
 * collapse whitespace, then map characters to ids and intersperse with the blank symbol.
 */
public class Phonemizer {

    private final SpeechSynthesis speech;

    public Phonemizer(SpeechSynthesis speech) {
        this.speech = speech;
        Breadcrumb.mark("Phonemizer: about to setVoiceByName(ca-va)");
        boolean ok = this.speech.setVoiceByName("ca-va");
        Breadcrumb.mark("Phonemizer: setVoiceByName(ca-va) returned " + ok);
        if (!ok) {
            StringBuilder sb = new StringBuilder();
            sb.append("espeak no ha trobat la veu \"ca-va\" (valencia).\n\nVeus disponibles que contenen \"ca\":\n");
            boolean any = false;
            for (String id : this.speech.getRawVoiceIdentifiers()) {
                if (id.toLowerCase().contains("ca")) {
                    sb.append(id).append("\n");
                    any = true;
                }
            }
            if (!any) sb.append("(cap)");
            throw new IllegalStateException(sb.toString());
        }
    }

    /** Returns the interspersed id sequence ready to feed into the "x" input of the Matcha ONNX model. */
    public int[] phonemizeToIds(String text) {
        Breadcrumb.mark("phonemizeToIds: start for text=\"" + text + "\"");
        String ipa = phonemize(text);
        Breadcrumb.mark("phonemizeToIds: got ipa=\"" + ipa + "\"");
        int[] ids = PhonemeSymbols.textToSequence(ipa);
        Breadcrumb.mark("phonemizeToIds: got ids.length=" + ids.length);
        return PhonemeSymbols.intersperse(ids);
    }

    /** Returns the raw IPA string (useful for debugging / showing the user what will be said). */
    public String phonemize(String text) {
        String lower = text.toLowerCase();
        List<Token> tokens = tokenize(lower);
        StringBuilder sb = new StringBuilder();
        for (Token token : tokens) {
            if (sb.length() > 0) sb.append(' ');
            if (token.isPunctuation) {
                sb.append(token.text);
            } else {
                Breadcrumb.mark("phonemize: about to call textToPhonemesIPA(\"" + token.text + "\")");
                String ph = speech.textToPhonemesIPA(token.text);
                Breadcrumb.mark("phonemize: textToPhonemesIPA returned \"" + ph + "\"");
                sb.append(ph.trim());
            }
        }
        return sb.toString().replaceAll("\\s+", " ").trim();
    }

    private static final class Token {
        final String text;
        final boolean isPunctuation;

        Token(String text, boolean isPunctuation) {
            this.text = text;
            this.isPunctuation = isPunctuation;
        }
    }

    private List<Token> tokenize(String text) {
        List<Token> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        Boolean currentIsPunct = null;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isWhitespace(c)) {
                if (current.length() > 0) {
                    tokens.add(new Token(current.toString(), Boolean.TRUE.equals(currentIsPunct)));
                    current.setLength(0);
                }
                currentIsPunct = null;
                continue;
            }
            boolean isPunct = PhonemeSymbols.PUNCTUATION_CHARS.contains(c);
            if (currentIsPunct != null && currentIsPunct != isPunct) {
                tokens.add(new Token(current.toString(), currentIsPunct));
                current.setLength(0);
            }
            currentIsPunct = isPunct;
            current.append(c);
        }
        if (current.length() > 0) {
            tokens.add(new Token(current.toString(), Boolean.TRUE.equals(currentIsPunct)));
        }

        return tokens;
    }
}
