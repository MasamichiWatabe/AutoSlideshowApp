package jp.techacademy.masamichi.watabe.autoslideshowapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.view.View;
import android.widget.ImageView;
import android.os.Handler;

import java.util.Timer;
import java.util.TimerTask;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CODE = 100;


    Button mForwardButton;
    Button mBackButton;
    Button mPlayButton;

    //int fieldIndex = 0;
    Cursor mCursor;
    //Uri imageUri;
    ImageView mImageView;
    int CntNum = 0;
    ArrayList<Uri> imageUriArray = new ArrayList<>();

    Timer mTimer;
    Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mForwardButton = (Button) findViewById(R.id.forward_button);
        mBackButton = (Button) findViewById(R.id.back_button);
        mPlayButton = (Button) findViewById(R.id.play_button);

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo();
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo();
        }

        mForwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {   // どうもmoveToNextとかうまく使えない・・・
/*
                ContentResolver resolver = getContentResolver();
                mCursor = resolver.query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        null, // 項目(null = 全項目)
                        null, // フィルタ条件(null = フィルタなし)
                        null, // フィルタ用パラメータ
                        null // ソート (null ソートなし)
                );

                if (mCursor.moveToNext()) {
                    CntNum += 1;
                } else if (mCursor.moveToFirst()) {
                    CntNum = 0;
                } else {
                    showAlertDialog();
                    return;
                }

                mImageView = (ImageView) findViewById(R.id.imageView);
                mImageView.setImageURI(imageUriArray.get(CntNum));

                mCursor.close();
*/
                if(imageUriArray.size() != 0){
                    CntNum += 1;
                    if (CntNum == imageUriArray.size()) {
                        CntNum = 0;
                    }
                    int num = CntNum % imageUriArray.size();
                    mImageView.setImageURI(imageUriArray.get(num));
                } else {
                    showAlertDialog();
                }

            }
        });


        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {   // こちらもmoveToPreviousとかうまく使えない・・・
/*
                ContentResolver resolver = getContentResolver();
                mCursor = resolver.query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        null, // 項目(null = 全項目)
                        null, // フィルタ条件(null = フィルタなし)
                        null, // フィルタ用パラメータ
                        null // ソート (null ソートなし)
                );

                if (mCursor.moveToPrevious()) {
                    CntNum -= 1;
                } else if (mCursor.moveToLast()) {
                    CntNum = imageUriArray.size();
                } else {
                    showAlertDialog();
                    return;
                }

                mImageView = (ImageView) findViewById(R.id.imageView);
                mImageView.setImageURI(imageUriArray.get(CntNum));

                mCursor.close();
*/
                if(imageUriArray.size() != 0){
                    CntNum -= 1;
                    if (CntNum < 0) {
                        CntNum = imageUriArray.size() - 1;
                    }
                    int num = CntNum % imageUriArray.size();
                    mImageView.setImageURI(imageUriArray.get(num));
                } else {
                    showAlertDialog();
                }

            }
        });



        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageUriArray.size() != 0) {
                    if (mTimer == null) {
                        mTimer = new Timer();

                        mPlayButton.setText("停止");

                        mTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                CntNum += 1;
                                if (CntNum == imageUriArray.size()) {
                                    CntNum = 0;
                                }
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        int num = CntNum % imageUriArray.size();
                                        mImageView.setImageURI(imageUriArray.get(num));
                                    }
                                });
                            }
                        }, 2000, 2000);
                    } else {
                        //カウンターを止める
                        mTimer.cancel();
                        mTimer = null;
                        mPlayButton.setText("再生");
                    }
                } else {
                    showAlertDialog();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo();
                }
                break;
            default:
                break;
        }
    }

    private void getContentsInfo() {

        ContentResolver resolver = getContentResolver();
        mCursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, // 項目(null = 全項目)
                null, // フィルタ条件(null = フィルタなし)
                null, // フィルタ用パラメータ
                null // ソート (null ソートなし)
        );

        if (mCursor.moveToFirst()) {
            do {
                int fieldIndex = mCursor.getColumnIndex(MediaStore.Images.Media._ID);
                Long id = mCursor.getLong(fieldIndex);
                Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

                Log.d("ANDROID", "URI : " + imageUri.toString());
                imageUriArray.add(imageUri);

            } while(mCursor.moveToNext());

            mImageView = (ImageView) findViewById(R.id.imageView);
            mImageView.setImageURI(imageUriArray.get(0));

        } else {
            showAlertDialog();
        }

        mCursor.close();
    }

    private void showAlertDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("画像がありません");
        alertDialogBuilder.setMessage("画像をフォルダに保存してください。");

        alertDialogBuilder.setPositiveButton("了解",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("UI_PARTS", "肯定ボタン");
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }




}