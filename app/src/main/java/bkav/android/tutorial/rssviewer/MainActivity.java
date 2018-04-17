
package bkav.android.tutorial.rssviewer;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView mTitle;
    private TextView mDescription;
    private TextView mContentEncoded;
    private TextView mDate;
    private ImageView mImage;

    private Button mOpenInBrowserButton;
    private Button mPreviousButton;
    private Button mNextButton;

    private NewGetter mNewGetter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTitle = (TextView) findViewById(R.id.title);
        mDescription = (TextView) findViewById(R.id.image_description);
        mContentEncoded = (TextView) findViewById(R.id.content_encoded);
        mDate = (TextView) findViewById(R.id.date);
        mImage = (ImageView) findViewById(R.id.image);
        

        mOpenInBrowserButton = (Button) findViewById(R.id.open_in_browser);
        mOpenInBrowserButton.setOnClickListener(this);

        mPreviousButton = (Button) findViewById(R.id.previous);
        mPreviousButton.setOnClickListener(this);

        mNextButton = (Button) findViewById(R.id.next);
        mNextButton.setOnClickListener(this);

        mOpenInBrowserButton.setEnabled(false);
        mPreviousButton.setEnabled(false);
        mNextButton.setEnabled(false);

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                mNewGetter = new NewGetter();
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                mOpenInBrowserButton.setEnabled(true);
                mPreviousButton.setEnabled(true);
                mNextButton.setEnabled(true);
                
                NewGetter.ImageItem imageItem = mNewGetter.getCurrentItem();
                imageItem.loadData(mTitle, mDescription, mContentEncoded, mDate, mImage);
            }
        }.execute();
    }

    @Override
    public void onClick(View v) {
        NewGetter.ImageItem imageItem = mNewGetter.getCurrentItem();

        if (v == mOpenInBrowserButton) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(imageItem.link));
            startActivity(i);
        } else if (v == mPreviousButton) {
            imageItem = mNewGetter.getPreviousItem();
        } else if (v == mNextButton) {
            imageItem = mNewGetter.getNextItem();
        }

        if (imageItem != null) {
            imageItem.loadData(mTitle, mDescription, mContentEncoded, mDate, mImage);
        }
    }
}
