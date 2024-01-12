 package com.vitproject.imagetotextconvertor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Locale;

 public class MainActivity extends AppCompatActivity {
    EditText mEditText;
    ImageView mImageView;
    Button scanButton;
    Button mSaveBtn;
    String mText;
    private static final int WRITE_EXTERNAL_STORAGE_CODE = 1;

    String cameraPermission[];
    String storagePermission[];

    private static final int REQUEST_CODE_FOR_CAMERA = 200;
    private static final int STORAGE_REQUEST_CODE = 400;
    private static final int GALLERY_CODE = 700;
    private static final int PICK_CAMERA_CODE = 1000;


    Uri imageUril;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEditText = findViewById(R.id.scanResult);
        mImageView = findViewById(R.id.imageDisplay);
        scanButton = findViewById(R.id.scanBtn);
        mSaveBtn = findViewById(R.id.saveBtn);

        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mText = mEditText.getText().toString();
                if (mText.isEmpty()){
                    Toast.makeText(MainActivity.this,"Please Enter Something",Toast.LENGTH_SHORT).show();
                }
                else {
                    if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
                        if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED){
                            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                            requestPermissions(permissions,WRITE_EXTERNAL_STORAGE_CODE);
                        }
                        else {
                            saveToTxtFile(mText);
                        }
                    }
                    else{
                        saveToTxtFile(mText);
                    }
                }
            }
        });

        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageImportDialog();
            }
        });
    }



    private void saveToTxtFile(String mText) {
        String timeStamp= new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(System.currentTimeMillis());
        try{

            File dir = new File(getExternalFilesDir(null) + "/My Files/");
            dir.mkdirs();
            String fileName = "MyFile_"+timeStamp + ".txt";

            File file = new File(dir,fileName);

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(mText);
            bw.close();

            Toast.makeText(this,fileName+" is saved to \n"+dir,Toast.LENGTH_SHORT).show();
        }
        catch (Exception e){
            Toast.makeText(this,e.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }

    private void showImageImportDialog(){
        String[] items = {"Camera","Gallery"};
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Select Image To Scan");
        dialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which==0){
                    if (!checkCameraPermission()){
                        requestPermissions();
                    }else{
                        pickCamera();
                    }
                }
                if (which==1){
                    if (!checkStoragePermission()){
                        requestStoragePermissions();
                    }else{
                        pickGallery();
                    }
                }
            }
        });
        dialog.create().show();
    }
    private void pickCamera(){
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"NewPhoto");
        values.put(MediaStore.Images.Media.DESCRIPTION,"Image To Text");
        imageUril = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI , values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUril);
        startActivityForResult(cameraIntent,PICK_CAMERA_CODE);
    }
    private void  pickGallery(){
        Intent myIntent = new Intent(Intent.ACTION_PICK);
        myIntent.setType("image/*");
        startActivityForResult(myIntent,GALLERY_CODE);
    }
    private void requestStoragePermissions(){
        ActivityCompat.requestPermissions(this,storagePermission,STORAGE_REQUEST_CODE);
    }
    private void requestPermissions(){
        ActivityCompat.requestPermissions(this , cameraPermission,REQUEST_CODE_FOR_CAMERA);
    }
    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }
    private boolean checkCameraPermission(){
        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this , Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case REQUEST_CODE_FOR_CAMERA:
                if(grantResults.length>0){
                    boolean cameraAccepted = grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted && writeStorageAccepted){
                        pickCamera();
                    }else {
                        Toast.makeText(this,"permission Denied",Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case STORAGE_REQUEST_CODE:
                if(grantResults.length>0){
                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted){
                        pickGallery();
                    }else {
                        Toast.makeText(this , "Permission Denied",Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case WRITE_EXTERNAL_STORAGE_CODE:{
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    saveToTxtFile(mText);
                }
                else{
                    Toast.makeText(this,"Storage Permission is required to store data",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode == RESULT_OK){
            if(requestCode == GALLERY_CODE){
                CropImage.activity(data.getData()).setGuidelines(CropImageView.Guidelines.ON).start(this);
            }
        }
        if(resultCode == RESULT_OK){
            if(requestCode == PICK_CAMERA_CODE){
                CropImage.activity(imageUril).setGuidelines(CropImageView.Guidelines.ON).start(this);
            }
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
          if (resultCode == RESULT_OK){
              Uri resultUri = result.getUri();
              mImageView.setImageURI(resultUri);
              try {
                  Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),resultUri);
                  TextRecognizer recognizer = new TextRecognizer.Builder(getApplicationContext()).build();
                  if (!recognizer.isOperational()){
                      Toast.makeText(this,"Error",Toast.LENGTH_LONG).show();
                  }else {
                      Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                      SparseArray<TextBlock> items = recognizer.detect(frame);
                      StringBuilder sb = new StringBuilder();
                      for(int j = 0; j<items.size();j++){
                          TextBlock myItems = items.valueAt(j);
                          sb.append(myItems.getValue());
                          sb.append("\n");
                      }
                      mEditText.append("\n"+ sb.toString());
                  }
              }catch (Exception e){
                  Toast.makeText(this,"Error",Toast.LENGTH_LONG).show();
              }

          }
        }
    }
}
