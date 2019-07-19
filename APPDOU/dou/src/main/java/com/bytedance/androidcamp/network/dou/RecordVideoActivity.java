package com.bytedance.androidcamp.network.dou;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.VideoView;

import com.bytedance.androidcamp.network.dou.api.IMiniDouyinService;
import com.bytedance.androidcamp.network.dou.model.PostVideoResponse;
import com.bytedance.androidcamp.network.dou.util.ResourceUtils;
import com.bytedance.androidcamp.network.dou.utils.Utils;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.bytedance.androidcamp.network.dou.utils.Utils.MEDIA_TYPE_VIDEO;
import static com.bytedance.androidcamp.network.dou.utils.Utils.getOutputMediaFile;
import static java.lang.Boolean.TRUE;

public class RecordVideoActivity extends AppCompatActivity {
    public Uri mSelectedImage;
    private Uri mSelectedVideo;
    private VideoView videoView;
    private static final int REQUEST_VIDEO_CAPTURE = 1;
    private Button btnPost;
    private  File F;
    private static final int REQUEST_EXTERNAL_CAMERA = 101;
    String[] permissions = new String[] {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
    };
    private  String  BASE_URL = "http://test.androidcamp.bytedance.com/mini_douyin/invoke/";
    private Retrofit retrofit=new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
    public IMiniDouyinService miniDouyinService=retrofit.create(IMiniDouyinService.class);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_video);
        videoView = findViewById(R.id.img);
        findViewById(R.id.btn_picture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Utils.isPermissionsReady(RecordVideoActivity.this, permissions)) {
                    //todo 打开摄像机
                    Log.d("test", "open");
                    RecordVideoActivity.this.openVideoRecordApp();

                } else {
                    //todo 权限检查
                    Utils.reuqestPermissions(RecordVideoActivity.this, permissions, REQUEST_EXTERNAL_CAMERA);
                }
            }
        });
        btnPost=findViewById(R.id.post);
        findViewById(R.id.post).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecordVideoActivity.this.postVideo();
            }
        });

    }

    private MultipartBody.Part getMultipartFromUri(String name, Uri uri) {
        File f = new File(ResourceUtils.getRealPath(RecordVideoActivity.this, uri));
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), f);
        return MultipartBody.Part.createFormData(name, f.getName(), requestFile);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            Uri videoUri = intent.getData();
            videoView.setVideoURI(videoUri);
            videoView.start();
            mSelectedVideo=FileProvider.getUriForFile(this, "com.bytedance.androidcamp.network.demo", F);
            String path =videoUri.getPath();
            //   f.setReadable(true);
            Log.d("test", String.valueOf(F.getAbsolutePath()));
            MediaMetadataRetriever media = new MediaMetadataRetriever();
            //   Log.d("test",path);
            Log.d("test", String.valueOf(videoUri));


            media.setDataSource(path);
            Bitmap bitmap = media.getFrameAtTime();
            mSelectedImage = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, null,null));
        }
    }
    private void postVideo() {
        btnPost.setText("POSTING...");
        btnPost.setEnabled(false); RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"),F);


        MultipartBody.Part coverImagePart = getMultipartFromUri("cover_image", mSelectedImage);
        MultipartBody.Part videoPart =MultipartBody.Part.createFormData("video", F.getName(), requestFile);
        Log.d("test", "0");
        Log.d("test", "1");
        miniDouyinService.postVideo("5759","ljy",coverImagePart,videoPart).enqueue(new Callback<PostVideoResponse>() {
            @Override
            public void onResponse(Call<PostVideoResponse> call, Response<PostVideoResponse> response) {
                //mBtnRefresh.setText(R.string.refresh_feed);
                if (response.body() != null && response.isSuccessful()== TRUE) {
                    //  mRv.getAdapter().notifyDataSetChanged();
                    btnPost.setText("success");
                    btnPost.setEnabled(true);
                    Log.d("test", "2");
                    Toast.makeText(RecordVideoActivity.this, "post success", Toast.LENGTH_SHORT ).show();
                }
            }

            @Override
            public void onFailure(Call<PostVideoResponse> call, Throwable throwable) {

                Log.d("test", "3");
                btnPost.setText("select an image");
                btnPost.setEnabled(true);
                Toast.makeText(RecordVideoActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT ).show();
            }
        });
        // TODO 9: post video & update buttons

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_CAMERA: {
                //todo 判断权限是否已经授予
                if (Utils.isPermissionsReady(this, permissions)) {
                    //todo 打开摄像机
                    openVideoRecordApp();
                }
                break;
            }
        }
    }

    private void openVideoRecordApp() {
        Log.d("test","open2");
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        F=getOutputMediaFile(MEDIA_TYPE_VIDEO);
        if(takeVideoIntent.resolveActivity(getPackageManager())!=null){
            Uri fileUri =
                    FileProvider.getUriForFile(this, "com.bytedance.androidcamp.network.demo", F);
            takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT,fileUri);
            takeVideoIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(takeVideoIntent,REQUEST_VIDEO_CAPTURE);
        }
    }
}
