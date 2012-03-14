package org.comu.advertiseme;

import java.io.InputStream;
import java.net.URL;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageView;

public class ImageManager extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Intent i = getIntent();

		// Receiving the Data
	 String	videoUrl = i.getStringExtra("url");
        
        ImageView img=new ImageView(this);

        Drawable drawable = LoadImageFromWebOperations(videoUrl);

        img.setImageDrawable(drawable);
        setContentView(img);
    }
    private Drawable LoadImageFromWebOperations(String url){
 		try{
 			InputStream is = (InputStream) new URL(url).getContent();
 			Drawable d = Drawable.createFromStream(is, "src name");
 			return d;
 		}catch (Exception e) {
 			System.out.println("Exc="+e);
 			return null;
 		}
 	}
}