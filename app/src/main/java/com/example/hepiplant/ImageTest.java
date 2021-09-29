package com.example.hepiplant;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;

public class ImageTest extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_test);

        Button button = findViewById(R.id.getImgButt);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && null != data) {
            //System.out.println(PICK_IMAGE);
            Uri selectedImage = data.getData();
            //System.out.println("SEL" +selectedImage);


            ImageView iv1 = (ImageView) findViewById(R.id.imageView);
            //imageView.setImageBitmap(selectedImage);
            iv1.setImageURI(selectedImage);

            iv1.buildDrawingCache();
            Bitmap bitmap = iv1.getDrawingCache();

            ByteArrayOutputStream stream=new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
            byte[] image=stream.toByteArray();
            System.out.println("byte array:"+image);

            String img_str = Base64.encodeToString(image, 0);
            TextView textView = findViewById(R.id.path);
            textView.setText(img_str);

            ImageView imageView = findViewById(R.id.imageView2);

            try {
                byte[] encodeByte = Base64.decode(img_str, Base64.DEFAULT);
                Bitmap bitmapa = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
               imageView.setImageBitmap(bitmapa);
            } catch (Exception e) {
                e.getMessage();

            }
    }
    }

}