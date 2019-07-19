package com.bytedance.androidcamp.network.dou.util;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bytedance.androidcamp.network.dou.R;
import com.bytedance.androidcamp.network.dou.RecordVideoActivity;
import com.bytedance.androidcamp.network.dou.VideoActivity;
import com.bytedance.androidcamp.network.dou.api.IMiniDouyinService;
import com.bytedance.androidcamp.network.dou.chooseuploadway;
import com.bytedance.androidcamp.network.dou.model.GetVideoResponse;
import com.bytedance.androidcamp.network.dou.model.PostVideoResponse;
import com.bytedance.androidcamp.network.dou.model.Video;
import com.bytedance.androidcamp.network.lib.util.ImageHelper;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.io.File;
import java.security.acl.Group;
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

public class GalleryActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;
    private static final int PICK_VIDEO = 2;
    private static final String TAG = "Gallery";
    private RecyclerView mRv;
    private List<Video> mVideos = new ArrayList<>();
    public Uri mSelectedImage;
    private Uri mSelectedVideo;
    public Button mBtn;
    private Button mBtnRefresh;

    RefreshLayout activity_joke_refreshLayout;

    // TODO 8: initialize retrofit & miniDouyinService
    private Retrofit retrofit=new Retrofit.Builder()
            .baseUrl(IMiniDouyinService.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    private IMiniDouyinService miniDouyinService=retrofit.create(IMiniDouyinService.class);

    private void initBtns() {
        activity_joke_refreshLayout = findViewById(R.id.activity_joke_refreshLayout);
           activity_joke_refreshLayout.setDisableContentWhenRefresh(true);
            activity_joke_refreshLayout.setDisableContentWhenLoading(true);
        activity_joke_refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                Log.i(TAG, "onRefresh: 刷新成功");
                fetchFeed();
                refreshLayout.finishRefresh();
            }
        });
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView img;
        public TextView username;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.img);
            username=itemView.findViewById(R.id.tv_username);
        }

        public void bind(final Activity activity, final Video video) {
            ImageHelper.displayWebImage(video.getImageUrl(), img);
            username.setText(video.getUserName());
            img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    VideoActivity.launch(activity, video.getVideoUrl());
                }
            });
        }
    }

    private void initRecyclerView() {
        mRv = findViewById(R.id.rv);
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        mRv.setLayoutManager(staggeredGridLayoutManager);
        mRv.setAdapter(new RecyclerView.Adapter<GalleryActivity.MyViewHolder>() {
            @NonNull
            @Override
            public GalleryActivity.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                return new MyViewHolder(
                        LayoutInflater.from(GalleryActivity.this)
                                .inflate(R.layout.video_item_view, viewGroup, false));
            }

            @Override
            public void onBindViewHolder(@NonNull GalleryActivity.MyViewHolder viewHolder, int i) {
                final Video video = mVideos.get(i);
                viewHolder.bind(GalleryActivity.this, video);
            }

            @Override
            public int getItemCount() {
                return mVideos.size();
            }
        });
    }

    public void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"),
                PICK_IMAGE);
    }

    public void chooseVideo() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Video"), PICK_VIDEO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult() called with: requestCode = ["
                + requestCode
                + "], resultCode = ["
                + resultCode
                + "], data = ["
                + data
                + "]");

        if (resultCode == RESULT_OK && null != data) {
            if (requestCode == PICK_IMAGE) {
                mSelectedImage = data.getData();
                Log.d(TAG, "selectedImage = " + mSelectedImage);
                mBtn.setText(R.string.select_a_video);
            } else if (requestCode == PICK_VIDEO) {
                mSelectedVideo = data.getData();
                Log.d(TAG, "mSelectedVideo = " + mSelectedVideo);
                mBtn.setText(R.string.post_it);
            }
        }
    }

    private MultipartBody.Part getMultipartFromUri(String name, Uri uri) {
        File f = new File(ResourceUtils.getRealPath(GalleryActivity.this, uri));
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), f);
        return MultipartBody.Part.createFormData(name, f.getName(), requestFile);
    }

    private void postVideo() {
        mBtn.setText("POSTING...");
        mBtn.setEnabled(false);
        MultipartBody.Part coverImagePart = getMultipartFromUri("cover_image", mSelectedImage);
        MultipartBody.Part videoPart = getMultipartFromUri("video", mSelectedVideo);
        // TODO 9: post video & update buttons
        Call<PostVideoResponse> call = miniDouyinService.postVideo("123456","james",coverImagePart,videoPart);
        call.enqueue(new Callback<PostVideoResponse>() {
            @Override
            public void onResponse(Call<PostVideoResponse> call, Response<PostVideoResponse> response) {
                if(response.body()!=null && response.isSuccessful()){
                    mBtn.setText(R.string.select_an_image);
                    mBtn.setEnabled(true);
                    Toast.makeText(GalleryActivity.this, "上传成功", Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(GalleryActivity.this, "上传失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<PostVideoResponse> call, Throwable throwable) {
                Toast.makeText(GalleryActivity.this, "上传失败", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void fetchFeed() {
        mBtnRefresh.setText("requesting...");
        mBtnRefresh.setEnabled(false);
        // TODO 10: get videos & update recycler list
        miniDouyinService.getVideos().enqueue(new Callback<GetVideoResponse>() {
            @Override
            public void onResponse(Call<GetVideoResponse> call, Response<GetVideoResponse> response) {
                if(response.body()!=null&& response.isSuccessful()){
                    mVideos=response.body().getVideos();
                    mRv.getAdapter().notifyDataSetChanged();
                    mBtnRefresh.setText("refresh feed");
                    mBtnRefresh.setEnabled(true);
                    Toast.makeText(GalleryActivity.this, "获取成功", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GetVideoResponse> call, Throwable throwable) {
                Toast.makeText(GalleryActivity.this, "获取失败", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                startActivity(new Intent(GalleryActivity.this, chooseuploadway.class));
            }
        });
        initRecyclerView();
        mBtnRefresh = findViewById(R.id.btn_refresh);
        initBtns();
        fetchFeed();
    }
}
