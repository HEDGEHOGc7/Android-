package com.bytedance.androidcamp.network.dou;

import android.app.Activity;
import android.content.Intent;
import android.media.Image;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

public class VideoActivity extends AppCompatActivity {
    private ImageView mail,money,like;

    public static void launch(Activity activity, String url) {
        Intent intent = new Intent(activity, VideoActivity.class);
        intent.putExtra("url", url);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        String url = getIntent().getStringExtra("url");
        VideoView videoView = findViewById(R.id.video_container);
        final ProgressBar progressBar = findViewById(R.id.progress_bar);
        videoView.setMediaController(new MediaController(this));
        videoView.setVideoURI(Uri.parse(url));
        videoView.requestFocus();
        videoView.start();
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                progressBar.setVisibility(View.GONE);
            }
        });
        progressBar.setVisibility(View.VISIBLE);
        mail = findViewById(R.id.im1);
        money = findViewById(R.id.im2);
        like = findViewById(R.id.im3);
        Animation loadAnimation = AnimationUtils.loadAnimation( this,R.anim.scale);
        like.startAnimation(loadAnimation);
//        mail.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View v) {
//           //     Toast.makeText(VideoActivity.this, "再次点击进入私信页面", Toast.LENGTH_SHORT ).show();
//            }
//        });
//        money.setOnClickListener((View.OnClickListener) this);
//        like.setOnClickListener((View.OnClickListener) this);
    }

}
