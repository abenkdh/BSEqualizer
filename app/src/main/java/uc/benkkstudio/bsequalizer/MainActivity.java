package uc.benkkstudio.bsequalizer;

import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.benkkstudio.equalizer.DialogEqualizerFragment;
import com.benkkstudio.equalizer.EqualizerFragment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button = findViewById(R.id.buttonEQ);
        final MediaPlayer mediaPlayer = new MediaPlayer();
        if(!mediaPlayer.isPlaying()){
            try {
                AssetFileDescriptor assetFileDescriptor = getAssets().openFd("a.mp3");
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.setDataSource(assetFileDescriptor.getFileDescriptor(), assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength());
                assetFileDescriptor.close();
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        EqualizerFragment equalizerFragment = EqualizerFragment.newBuilder()
                .setAccentColor(Color.parseColor("#ffffff"))
                .setAudioSessionId(mediaPlayer.getAudioSessionId())
                .setActivity(this)
                .build();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.eqFrame, equalizerFragment)
                .commit();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogEqualizerFragment fragment = DialogEqualizerFragment.newBuilder()
                        .setActivity(MainActivity.this)
                        .setAudioSessionId(mediaPlayer.getAudioSessionId())
                        .themeColor(ContextCompat.getColor(MainActivity.this, R.color.colorPrimary))
                        .textColor(ContextCompat.getColor(MainActivity.this, R.color.colorAccent))
                        .accentAlpha(ContextCompat.getColor(MainActivity.this, R.color.colorPrimaryDark))
                        .darkColor(ContextCompat.getColor(MainActivity.this, R.color.colorPrimaryDark))
                        .setAccentColor(ContextCompat.getColor(MainActivity.this, R.color.colorAccent))
                        .build();
                fragment.show(getSupportFragmentManager(), "eq");
            }
        });
    }
}
