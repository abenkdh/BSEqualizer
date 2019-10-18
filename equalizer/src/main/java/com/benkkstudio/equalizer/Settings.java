package com.benkkstudio.equalizer;

import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.PresetReverb;

public class Settings {
    static boolean isEqualizerEnabled = true;
    static boolean isEqualizerReloaded = true;
    static int[] seekbarpos = new int[5];
    static int presetPos;
    static short reverbPreset = -1, bassStrength = -1;
    static EqualizerModel equalizerModel;
    public static double ratio = 1.0;
    public static Equalizer equalizer;
    public static BassBoost bassBoost;
    public static PresetReverb presetReverb;

    public static void setPresetReverb(int index, int sesion){
        presetReverb = new PresetReverb(index, sesion);
    }

    public static PresetReverb getPresetReverb(){
        return presetReverb;
    }

    public static void setBassBoost(int index, int sesion){
        bassBoost = new BassBoost(index, sesion);
    }

    public static BassBoost getBassBoost(){
        return bassBoost;
    }

    public static void setEqualizer(int index, int sesion){
        equalizer = new Equalizer(index, sesion);
    }

    public static Equalizer getEqualizer(){
        return equalizer;
    }
}
