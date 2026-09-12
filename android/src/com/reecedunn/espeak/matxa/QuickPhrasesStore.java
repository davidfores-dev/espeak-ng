package com.reecedunn.espeak.matxa;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Persists the user's quick-phrase buttons (label + text to speak) in SharedPreferences as a
 * JSON array, seeded with a default set of useful phrases the first time the app runs.
 */
public final class QuickPhrasesStore {

    private static final String PREFS_NAME = "matxa_quick_phrases";
    private static final String KEY_PHRASES = "phrases";

    private QuickPhrasesStore() {}

    public static List<QuickPhrase> load(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_PHRASES, null);
        List<QuickPhrase> result = new ArrayList<>();

        if (json == null) {
            result.addAll(defaults());
            save(context, result);
            return result;
        }

        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                result.add(new QuickPhrase(obj.getString("label"), obj.getString("text")));
            }
        } catch (Exception e) {
            result.addAll(defaults());
        }

        return result;
    }

    public static void save(Context context, List<QuickPhrase> phrases) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        JSONArray array = new JSONArray();
        try {
            for (QuickPhrase p : phrases) {
                JSONObject obj = new JSONObject();
                obj.put("label", p.label);
                obj.put("text", p.text);
                array.put(obj);
            }
        } catch (Exception ignored) {
        }
        prefs.edit().putString(KEY_PHRASES, array.toString()).apply();
    }

    private static List<QuickPhrase> defaults() {
        List<QuickPhrase> list = new ArrayList<>();
        list.add(new QuickPhrase("Sí", "Sí."));
        list.add(new QuickPhrase("No", "No."));
        list.add(new QuickPhrase("Gràcies", "Gràcies."));
        list.add(new QuickPhrase("Tinc dolor", "Tinc dolor."));
        list.add(new QuickPhrase("Tinc set", "Tinc set."));
        list.add(new QuickPhrase("Tinc gana", "Tinc gana."));
        list.add(new QuickPhrase("Necessite ajuda", "Necessite ajuda, per favor."));
        list.add(new QuickPhrase("Vaig al bany", "Vull anar al bany."));
        list.add(new QuickPhrase("Estic cansat", "Estic cansat."));
        list.add(new QuickPhrase("No em trobe bé", "No em trobe bé."));
        list.add(new QuickPhrase("Espera un moment", "Espera un moment, per favor."));
        list.add(new QuickPhrase("Vull descansar", "Vull descansar un poc."));
        return list;
    }
}

