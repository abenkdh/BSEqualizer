package com.benkkstudio.equalizer;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.PresetReverb;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.ChartView;
import com.db.chart.view.LineChartView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;


public class DialogEqualizerFragment extends DialogFragment {
    SharedPreferences sharedpreference;
    static Context contextx;
    private static int           accentAlpha     = Color.BLUE;
    private static int           darkBackground  = Color.GRAY;
    private static int           textColor       = Color.WHITE;
    private static int           themeColor      = Color.parseColor("#B24242");
    private static int           backgroundColor = Color.WHITE;
    private        Equalizer     mEqualizer;
    private        BassBoost     bassBoost;
    private        PresetReverb  presetReverb;
    private        LineSet       dataset;
    private        LineChartView chart;
    private        float[]       points;
    private        int           y               = 0;
    private        SeekBar[]     seekBarFinal    = new SeekBar[5];
    private        Spinner       presetSpinner;
    private        Context       ctx;
    private        int           audioSesionId;

    public DialogEqualizerFragment() {
        // Required empty public constructor
    }

    private static DialogEqualizerFragment newInstance(int audioSessionId) {

        Bundle args = new Bundle();

        DialogEqualizerFragment fragment = new DialogEqualizerFragment();
        fragment.audioSesionId = audioSessionId;
        fragment.setArguments(args);
        return fragment;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedpreference = PreferenceManager.getDefaultSharedPreferences(contextx);
        mEqualizer = Settings.getEqualizer();
        bassBoost = Settings.getBassBoost();
        BassBoost.Settings bassBoostSettingTemp = bassBoost.getProperties();
        BassBoost.Settings bassBoostSetting     = new BassBoost.Settings(bassBoostSettingTemp.toString());
        bassBoostSetting.strength = (1000 / 19);
        bassBoost.setProperties(bassBoostSetting);

        presetReverb = Settings.getPresetReverb();
        presetReverb.setPreset(PresetReverb.PRESET_NONE);
        Settings.equalizerModel = new EqualizerModel();
        if(sharedpreference.contains("ENABLE_EQUALIZER") && sharedpreference.getBoolean("ENABLE_EQUALIZER", false)){
            mEqualizer.setEnabled(true);
            bassBoost.setEnabled(true);
            presetReverb.setEnabled(true);
        } else {
            mEqualizer.setEnabled(false);
            bassBoost.setEnabled(false);
            presetReverb.setEnabled(false);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ctx = context;
    }

    public static void loadEqualizer(Activity activity){
        for (short i = 0; i < 5; i++) {
            Settings.getEqualizer().setBandLevel(i, (short) (PreferenceManager.getDefaultSharedPreferences(activity).getInt(String.valueOf(i),0) - 1500));
            Settings.getEqualizer().setEnabled(true);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return inflater.inflate(R.layout.dialog_fragment_equalizer, container, false);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView backBtn = view.findViewById(R.id.equalizer_back_btn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        backBtn.setColorFilter(textColor);
        view.findViewById(R.id.equalizerLayout).setBackgroundColor(backgroundColor);

        TextView fragTitle = view.findViewById(R.id.equalizer_fragment_title);
        fragTitle.setTextColor(textColor);
        SwitchCompat equalizerSwitch = view.findViewById(R.id.equalizer_switch);
        if(sharedpreference.contains("ENABLE_EQUALIZER") && sharedpreference.getBoolean("ENABLE_EQUALIZER", false)){
            equalizerSwitch.setChecked(true);
            mEqualizer.setEnabled(true);
            bassBoost.setEnabled(true);
            presetReverb.setEnabled(true);
        } else {
            equalizerSwitch.setChecked(false);
            mEqualizer.setEnabled(false);
            bassBoost.setEnabled(false);
            presetReverb.setEnabled(false);
        }
        equalizerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sharedpreference.edit().putBoolean("ENABLE_EQUALIZER", isChecked).apply();
                mEqualizer.setEnabled(isChecked);
                bassBoost.setEnabled(isChecked);
                presetReverb.setEnabled(isChecked);
            }
        });


        presetSpinner = view.findViewById(R.id.equalizer_preset_spinner);
        presetSpinner.getBackground().setColorFilter(textColor, PorterDuff.Mode.SRC_ATOP);

        chart = view.findViewById(R.id.lineChart);
        Paint paint = new Paint();
        dataset = new LineSet();

        AnalogController bassController   = view.findViewById(R.id.controllerBass);
        AnalogController reverbController = view.findViewById(R.id.controller3D);

        bassController.setLabel("BASS");
        reverbController.setLabel("3D");


        bassController.circlePaint2.setColor(themeColor);
        bassController.linePaint.setColor(themeColor);
        bassController.invalidate();
        reverbController.circlePaint2.setColor(themeColor);
        reverbController.linePaint.setColor(themeColor);
        reverbController.invalidate();

        if (!Settings.isEqualizerReloaded) {
            int x = 0;
            if (bassBoost != null) {
                try {
                    x = ((bassBoost.getRoundedStrength() * 19) / 1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (presetReverb != null) {
                try {
                    y = (presetReverb.getPreset() * 19) / 6;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (x == 0) {
                bassController.setProgress(1);
            } else {
                bassController.setProgress(x);
            }

            if (y == 0) {
                reverbController.setProgress(1);
            } else {
                reverbController.setProgress(y);
            }
        } else {
            int x = ((Settings.bassStrength * 19) / 1000);
            y = (Settings.reverbPreset * 19) / 6;
            if (x == 0) {
                bassController.setProgress(1);
            } else {
                bassController.setProgress(x);
            }

            if (y == 0) {
                reverbController.setProgress(1);
            } else {
                reverbController.setProgress(y);
            }
        }

        bassController.setOnProgressChangedListener(new AnalogController.onProgressChangedListener() {
            @Override
            public void onProgressChanged(int progress) {
                Settings.bassStrength = (short) (((float) 1000 / 19) * (progress));
                try {
                    bassBoost.setStrength(Settings.bassStrength);
                    Settings.equalizerModel.setBassStrength(Settings.bassStrength);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        reverbController.setOnProgressChangedListener(new AnalogController.onProgressChangedListener() {
            @Override
            public void onProgressChanged(int progress) {
                Settings.reverbPreset = (short) ((progress * 6) / 19);
                Settings.equalizerModel.setReverbPreset(Settings.reverbPreset);
                try {
                    presetReverb.setPreset(Settings.reverbPreset);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                y = progress;
            }
        });

        TextView equalizerHeading = new TextView(ctx);
        equalizerHeading.setText(R.string.eq);
        equalizerHeading.setTextSize(20);
        equalizerHeading.setGravity(Gravity.CENTER_HORIZONTAL);

        short numberOfFrequencyBands = 5;

        points = new float[numberOfFrequencyBands];

        final short lowerEqualizerBandLevel = mEqualizer.getBandLevelRange()[0];
        final short upperEqualizerBandLevel = mEqualizer.getBandLevelRange()[1];

        for (short i = 0; i < numberOfFrequencyBands; i++) {
            final short    equalizerBandIndex      = i;
            final TextView frequencyHeaderTextView = new TextView(ctx);
            frequencyHeaderTextView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            frequencyHeaderTextView.setGravity(Gravity.CENTER_HORIZONTAL);
            frequencyHeaderTextView.setTextColor(textColor);
            frequencyHeaderTextView.setText((mEqualizer.getCenterFreq(equalizerBandIndex) / 1000) + "Hz");

            LinearLayout seekBarRowLayout = new LinearLayout(ctx);
            seekBarRowLayout.setOrientation(LinearLayout.VERTICAL);

            TextView lowerEqualizerBandLevelTextView = new TextView(ctx);
            lowerEqualizerBandLevelTextView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));
            lowerEqualizerBandLevelTextView.setTextColor(textColor);
            lowerEqualizerBandLevelTextView.setText((lowerEqualizerBandLevel / 100) + "dB");

            TextView upperEqualizerBandLevelTextView = new TextView(ctx);
            lowerEqualizerBandLevelTextView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            upperEqualizerBandLevelTextView.setTextColor(textColor);
            upperEqualizerBandLevelTextView.setText((upperEqualizerBandLevel / 100) + "dB");

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            layoutParams.weight = 1;

            SeekBar  seekBar  = new SeekBar(ctx);
            TextView textView = new TextView(ctx);
            switch (i) {
                case 0:
                    seekBar = view.findViewById(R.id.seekBar1);
                    textView = view.findViewById(R.id.textView1);
                    break;
                case 1:
                    seekBar = view.findViewById(R.id.seekBar2);
                    textView = view.findViewById(R.id.textView2);
                    break;
                case 2:
                    seekBar = view.findViewById(R.id.seekBar3);
                    textView = view.findViewById(R.id.textView3);
                    break;
                case 3:
                    seekBar = view.findViewById(R.id.seekBar4);
                    textView = view.findViewById(R.id.textView4);
                    break;
                case 4:
                    seekBar = view.findViewById(R.id.seekBar5);
                    textView = view.findViewById(R.id.textView5);
                    break;
            }
            seekBarFinal[i] = seekBar;
            seekBar.getProgressDrawable().setColorFilter(new PorterDuffColorFilter(Color.DKGRAY, PorterDuff.Mode.SRC_IN));
            seekBar.getThumb().setColorFilter(new PorterDuffColorFilter(themeColor, PorterDuff.Mode.SRC_IN));
            seekBar.setId(i);
//            seekBar.setLayoutParams(layoutParams);
            seekBar.setMax(upperEqualizerBandLevel - lowerEqualizerBandLevel);

            textView.setText(frequencyHeaderTextView.getText());
            textView.setTextColor(textColor);
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

            if (!sharedpreference.contains("1")) {
                points[i] = Settings.seekbarpos[i] - lowerEqualizerBandLevel;
                dataset.addPoint(frequencyHeaderTextView.getText().toString(), points[i]);
                seekBar.setProgress(Settings.seekbarpos[i] - lowerEqualizerBandLevel);
            } else {
                mEqualizer.setBandLevel(equalizerBandIndex, (short) (sharedpreference.getInt(String.valueOf(seekBar.getId()),0) + lowerEqualizerBandLevel));
                Log.d("ABENK 2", String.valueOf(sharedpreference.getInt(String.valueOf(seekBar.getId()),0) + lowerEqualizerBandLevel));
                points[i] = mEqualizer.getBandLevel(equalizerBandIndex) - lowerEqualizerBandLevel;
                dataset.addPoint(frequencyHeaderTextView.getText().toString(), points[i]);
                seekBar.setProgress(sharedpreference.getInt(String.valueOf(seekBar.getId()),0));
                Settings.seekbarpos[i] = mEqualizer.getBandLevel(equalizerBandIndex);
            }

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    mEqualizer.setBandLevel(equalizerBandIndex, (short) (progress + lowerEqualizerBandLevel));
                    points[seekBar.getId()] = mEqualizer.getBandLevel(equalizerBandIndex) - lowerEqualizerBandLevel;
                    Settings.seekbarpos[seekBar.getId()] = (progress + lowerEqualizerBandLevel);
                    Settings.equalizerModel.getSeekbarpos()[seekBar.getId()] = (progress + lowerEqualizerBandLevel);
                    dataset.updateValues(points);
                    chart.notifyDataUpdate();
                    sharedpreference.edit().putInt(String.valueOf(seekBar.getId()), progress).apply();
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    presetSpinner.setSelection(0);
                    Settings.presetPos = 0;
                    Settings.equalizerModel.setPresetPos(0);
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

        }

        equalizeSound();

        paint.setColor(textColor);
        paint.setStrokeWidth((float) (1.10 * Settings.ratio));

        dataset.setColor(themeColor);
        dataset.setSmooth(true);
        dataset.setThickness(5);

        chart.setXAxis(false);
        chart.setYAxis(false);

        chart.setYLabels(AxisController.LabelPosition.NONE);
        chart.setXLabels(AxisController.LabelPosition.NONE);
        chart.setGrid(ChartView.GridType.NONE, 7, 10, paint);

        chart.setAxisBorderValues(-300, 3300);

        chart.addData(dataset);
        chart.show();

        Button mEndButton = new Button(ctx);
        mEndButton.setBackgroundColor(themeColor);
        mEndButton.setTextColor(textColor);


    }

    public void equalizeSound() {
        ArrayList<String> equalizerPresetNames = new ArrayList<>();
        ArrayAdapter<String> equalizerPresetSpinnerAdapter = new ArrayAdapter<>(ctx,
                R.layout.spinner_item,
                equalizerPresetNames);
        equalizerPresetSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        equalizerPresetNames.add("Custom");

        for (short i = 0; i < mEqualizer.getNumberOfPresets(); i++) {
            equalizerPresetNames.add(mEqualizer.getPresetName(i));
        }

        presetSpinner.setAdapter(equalizerPresetSpinnerAdapter);

        if (sharedpreference.contains("SPINNER_POSITION")) {
            Settings.presetPos = sharedpreference.getInt("SPINNER_POSITION",0);
            presetSpinner.setSelection(Settings.presetPos);
        }

        presetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sharedpreference.edit().putInt("SPINNER_POSITION", position).apply();
                try {
                    if (position != 0) {
                        mEqualizer.usePreset((short) (position - 1));
                        Settings.presetPos = position;
                        short numberOfFreqBands = 5;

                        final short lowerEqualizerBandLevel = mEqualizer.getBandLevelRange()[0];

                        for (short i = 0; i < numberOfFreqBands; i++) {
                            seekBarFinal[i].setProgress(mEqualizer.getBandLevel(i) - lowerEqualizerBandLevel);
                            points[i] = mEqualizer.getBandLevel(i) - lowerEqualizerBandLevel;
                            Settings.seekbarpos[i] = mEqualizer.getBandLevel(i);
                            Settings.equalizerModel.getSeekbarpos()[i] = mEqualizer.getBandLevel(i);
                        }
                        dataset.updateValues(points);
                        chart.notifyDataUpdate();
                    }
                } catch (Exception e) {
                    Toast.makeText(ctx, "Error while updating Equalizer", Toast.LENGTH_SHORT).show();
                }
                Settings.equalizerModel.setPresetPos(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    public static class Builder {
        private int id = -1;

        public Builder setAudioSessionId(int id) {
            this.id = id;
            return this;
        }

        public Builder setAccentColor(int color) {
            themeColor = color;
            return this;
        }

        public Builder themeColor(int color) {
            backgroundColor = color;
            return this;
        }

        public Builder textColor(int color) {
            textColor = color;
            return this;
        }

        public Builder darkColor(int color) {
            darkBackground = color;
            return this;
        }
        public Builder setActivity(Context context) {
            contextx = context;
            return this;
        }

        public Builder accentAlpha(int color) {
            accentAlpha = color;
            return this;
        }

        public DialogEqualizerFragment build() {
            return DialogEqualizerFragment.newInstance(id);
        }
    }


}
