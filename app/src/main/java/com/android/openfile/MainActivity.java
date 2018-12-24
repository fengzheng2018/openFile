package com.android.openfile;

import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Context mContext;
    private Button openBut;
    private TextView showTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = MainActivity.this;

        openBut = findViewById(R.id.open_button);
        showTxt = findViewById(R.id.show_text);
    }

    @Override
    protected void onResume(){
        super.onResume();

        openBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForChoose();
            }
        });
    }

    /**
     * 请求选择文件
     */
    private void startActivityForChoose(){
        final int FILE_SELECT_CODE = 15;
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        /**
         * 此处过滤打开文件类型
         */
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try{
            startActivityForResult(intent,FILE_SELECT_CODE);
        }catch (ActivityNotFoundException ane){
            Toast.makeText(mContext,"请安装一个文件管理器",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent intent){
        super.onActivityResult(requestCode,resultCode,intent);

        switch (requestCode){
            case 15:{
                if(resultCode==RESULT_OK){
                    Uri uri = intent.getData();
                    /**
                     * 使用第三方应用打开
                     */
                    if("file".equalsIgnoreCase(uri.getScheme())){
                        showTxt.setText(uri.getPath());
                    }
                    /**
                     * android 4.4以后
                     */
                    else if(Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT){
                        showTxt.setText(getPath(mContext,uri));
                    }
                }
                break;
            }
        }
    }


    /**
     * Android4.4以后获取文件绝对路径
     */
    private String getPath(Context context,Uri uri){
        boolean isKitkat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        if(isKitkat && DocumentsContract.isDocumentUri(context,uri)){
            if(isExternalStorageDocument(uri)){
                String fileId = DocumentsContract.getDocumentId(uri);
                String[] split = fileId.split(":");
                String type = split[0];

                if("primary".equalsIgnoreCase(type)){
                    return Environment.getExternalStorageState() + "/" + split[1];
                }
            }
            else if(isDownloadsDocument(uri)){
                String id = DocumentsContract.getDocumentId(uri);
                Uri fileUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(context,fileUri,null,null);
            }
            else if (isMediaDocument(uri)) {
                String docId = DocumentsContract.getDocumentId(uri);
                String[] split = docId.split(":");
                String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                }
                else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                }
                else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                String selection = "_id=?";
                String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    /**
     * 判断是否是外部存储
     */
    private boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private String getDataColumn(Context context,Uri uri,String selection,String[] selectionArgs){
        Cursor cursor = null;
        String column = "_data";
        String[] projection = {column};

        try{
            cursor = context.getContentResolver().query(uri,projection,selection,selectionArgs,null);
            if(cursor != null && cursor.moveToFirst()){
                int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        }finally {
            if(cursor != null){
                cursor.close();
            }
        }
        return null;
    }
}
