package com.example.blog.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.blog.R;
import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class RegisterActivity extends AppCompatActivity {

    ImageView ImgUserPhoto;
    static  int PReqCode = 1 ;
    static  int REQUESCODE = 1 ;
    Uri pickedImgUri;




    private EditText userEmail,userPassword,userPassword2,userName;
    private ProgressBar loadingProgress;
    private Button regBtn;

    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        userEmail = findViewById(R.id.regMail);
        userPassword = findViewById(R.id.regPassword);
        userPassword2 = findViewById(R.id.regPassword2);
        userName= findViewById(R.id.regName);
        loadingProgress = findViewById(R.id.regprogressBar);
        regBtn = findViewById(R.id.regBtn);
        loadingProgress.setVisibility(View.INVISIBLE);

        mAuth = FirebaseAuth.getInstance();

        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                regBtn.setVisibility(View.INVISIBLE);
                loadingProgress.setVisibility(View.VISIBLE);
                final  String email = userEmail.getText().toString();
                final  String password = userPassword.getText().toString();
                final String  password2 = userPassword2.getText().toString();
                final String name = userName.getText().toString();

                if (email.isEmpty() || name.isEmpty() || password.isEmpty()  || !password.equals(password2)){

                    showMessage("請輸入個人資料") ;
                    regBtn.setVisibility(View.VISIBLE);
                    loadingProgress.setVisibility(View.INVISIBLE);
                    

                }

                else {
                    //如果之廖正確就創建帳號

                    CreateUserAccount (email,name,password);


                }








            }
        });


        ImgUserPhoto = findViewById(R.id.regUserPhoto);
        ImgUserPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (Build.VERSION.SDK_INT >= 22) {
                    checkAndRequestForPermission();
                } else {
                    openGallery();
                }


            }
        });
    }

    private void CreateUserAccount(String email, String name, String password) {

        //這邊方法創造帳號
        mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            //使用者創建成功
                            showMessage("Account created");
                            //創建帳號之後我們要更新資料它的圖片和姓名
                            updateUserInfo(name ,pickedImgUri,mAuth.getCurrentUser());

                        }

                        else {

                            //帳號創建失敗
                            showMessage("account creation failed"+ task.getException().getMessage());
                            regBtn.setVisibility(View.VISIBLE);
                            loadingProgress.setVisibility(View.INVISIBLE);

                        }
                    }
                });


    }
    //更新使用者圖片和名字
    private void updateUserInfo(String name, Uri pickedImgUri, FirebaseUser currentUser) {
        //第一次我們需要上船使用者圖片到firebase儲存和取得這個uri
        StorageReference mStorage = FirebaseStorage.getInstance().getReference().child("users_photos");
        StorageReference imageFilePath = mStorage.child(pickedImgUri.getLastPathSegment());

        imageFilePath.putFile(pickedImgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                //圖片載入成功
                //現在我們可以取得我們的圖片
                imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        //uri 包含使用者圖片

                        UserProfileChangeRequest profleUpdate = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .setPhotoUri(uri)
                                .build();

                        currentUser.updateProfile(profleUpdate)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if (task.isSuccessful()){
                                            //使用者資料更新過成功
                                            showMessage("Register Complete");
                                            updateUI();
                                        }

                                    }
                                });

                    }
                });

            }
        });



    }

    private void updateUI() {


        Intent homeActivity = new Intent(getApplicationContext(),HomeActivity.class);
        startActivity(homeActivity);
        finish();


    }

    private void showMessage(String message) {

        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();





    }

    private void openGallery() {

        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,REQUESCODE);


    }

    private void checkAndRequestForPermission() {

        if (ContextCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)

                != PackageManager.PERMISSION_GRANTED) {
        if(ActivityCompat.shouldShowRequestPermissionRationale(RegisterActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE)){
            Toast.makeText(RegisterActivity.this,"Please accept for reguired permission",Toast.LENGTH_SHORT).show();


        }

        else{
            ActivityCompat.requestPermissions(RegisterActivity.this,

                                                                     new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                                                      PReqCode);
        }

        }
        else
            openGallery();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REQUESCODE && data != null){

            pickedImgUri = data.getData();
            ImgUserPhoto.setImageURI(pickedImgUri);

        }
    }
}
