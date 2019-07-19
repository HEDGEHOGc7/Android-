package com.bytedance.androidcamp.network.dou;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bytedance.androidcamp.network.dou.api.IMiniDouyinService;
import com.bytedance.androidcamp.network.dou.model.GetVideoResponse;
import com.bytedance.androidcamp.network.dou.model.PostVideoResponse;
import com.bytedance.androidcamp.network.dou.model.Video;
import com.bytedance.androidcamp.network.dou.util.GalleryActivity;
import com.bytedance.androidcamp.network.lib.util.ImageHelper;
import com.bytedance.androidcamp.network.dou.util.ResourceUtils;
import com.domker.study.androidstudy.ViewAdapter;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
   private Button mBtnRefresh;
    ViewPager pager = null;
    LayoutInflater layoutInflater = null;
    List<View> pages = new ArrayList<View>();
    ProgressBar progressBar;
    private static final String TAG = "MainActivity";
    private List<Video> mVideos = new ArrayList<>();
    private  String  BASE_URL = "http://test.androidcamp.bytedance.com/mini_douyin/invoke/";
    private Retrofit retrofit=new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
    private ViewAdater2 adapter;
    private Button btn_new;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = findViewById(R.id.progress_bar);
        int i;
        layoutInflater = getLayoutInflater();
        btn_new=findViewById(R.id.btn_next);
        btn_new.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, GalleryActivity.class));
            }
        });
        pager = (ViewPager) findViewById(R.id.view_pager);

        adapter = new ViewAdater2();
        adapter.setDatas(pages);
        pager.setAdapter(adapter);
        fetchFeed();

    }
    private  void init(){
        int i;

        for(i=0;i<20;i++){
            VideoView videoView = (VideoView) layoutInflater.inflate(R.layout.activity_image_item, null);
            videoView.setMediaController(new MediaController(this));
            videoView.setVideoURI(Uri.parse(mVideos.get(i).getVideoUrl() ));
            //videoView.setOnCompletionListener();
            //videoView.requestFocus();
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
             //       videoView.setBackgroundColor(0xfffffff);
                }
            });
            videoView.start();
            pages.add(videoView);

        }
        adapter.setDatas(pages);

    }
    public void fetchFeed() {

        IMiniDouyinService miniDouyinService = retrofit.create(IMiniDouyinService.class);
        Call<GetVideoResponse> call = miniDouyinService.getVideos();
        call.enqueue(new Callback<GetVideoResponse>() {
            @Override
            public void onResponse(Call<GetVideoResponse> call, Response<GetVideoResponse> response)
            {
                if (response.body() != null && response.body().getVideos() != null)
                {  mVideos = response.body().getVideos();
                    init();
                }
            }

            @Override
            public void onFailure(Call<GetVideoResponse> call, Throwable throwable) {
                Toast.makeText(MainActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT ).show();
            }

        });

    }

}
