package com.example.sajankumarv.girdDetailActivity;

/**
 * Created by sajankumarv on 22-02-2016.
 */

        import android.app.AlertDialog;
        import android.app.WallpaperManager;
        import android.graphics.Bitmap;
        import android.graphics.BitmapFactory;
        import android.graphics.drawable.BitmapDrawable;
        import android.graphics.drawable.Drawable;
        import android.os.AsyncTask;
        import android.os.Bundle;
        import android.support.v7.app.ActionBar;
        import android.support.v7.app.AppCompatActivity;
        import android.text.Html;
        import android.util.Log;
        import android.view.View;
        import android.widget.ImageView;
        import android.widget.ProgressBar;
        import android.widget.TextView;

        import com.example.sajankumarv.flickrapp.R;
        import com.squareup.picasso.Picasso;

        import java.io.IOException;
        import java.io.InputStream;

public class DetailActivity extends AppCompatActivity {
    private TextView titleTextView;
    private ImageView imageView;
    private ProgressBar progressBarDetailView;
    private TextView infoText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_view);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        String title = getIntent().getStringExtra("title");
        String image = getIntent().getStringExtra("image");
        titleTextView = (TextView) findViewById(R.id.title);
        infoText = (TextView) findViewById(R.id.infoView);
        imageView = (ImageView) findViewById(R.id.grid_item_image);
        progressBarDetailView = (ProgressBar) findViewById(R.id.progressBarDetailView);
        titleTextView.setText(Html.fromHtml(title));

        progressBarDetailView.setVisibility(View.VISIBLE);
        new DownloadImageTask(imageView).execute(image);


    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            urldisplay = urldisplay.substring(8, urldisplay.length());
            Log.d("BITMAP :", urldisplay);
            Bitmap image = null;
            try {
                InputStream in = new java.net.URL("http://".concat(urldisplay)).openStream();
                image = BitmapFactory.decodeStream(in);
                in.close();
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return image;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
            progressBarDetailView.setVisibility(View.GONE);
            imageView.setLongClickable(true);
            imageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    new AlertDialog.Builder(v.getContext())
                            .setTitle("Info")
                            .setMessage("Are you sure you want to set this photo as your device wallpaper?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    WallpaperManager wm = WallpaperManager.getInstance(getApplicationContext());
                                    Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();

                                    if (bitmap != null) {
                                        try {
                                            wm.setBitmap(bitmap);
                                            infoText.setText("Wallpaper updated!");
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                }
                            })
                            .show();
                    return true;
                }
            });
        }
    }
}