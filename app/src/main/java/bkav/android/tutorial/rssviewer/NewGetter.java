
package bkav.android.tutorial.rssviewer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by quanglh on 29/08/2016.
 */
public class NewGetter {
    private static final String TAG = NewGetter.class.getSimpleName();
    private static final String MAXIM_URL = "http://www.maxim.com/.rss/full/";
    private static final String RSS_URL = MAXIM_URL;

    private static final String ITEM_TAG = "item";
    private static final String TITLE_TAG = "title";
    private static final String DESCRIPTION_TAG = "description";
    private static final String CONTENT_ENCODED_TAG = "content:encoded";
    private static final String LINK_TAG = "link";
    private static final String ENCLOSURE_TAG = "enclosure";
    private static final String PUB_DATE_TAG = "pubDate";
    

    private static final String URL_ATT = "url";

    private static final int SOCKET_TIMEOUT = 15000;

    private ArrayList<ImageItem> mItems;
    private int mCurrentItem;

    public NewGetter() {
        mItems = new ArrayList<ImageItem>();
        mCurrentItem = 0;

        try {
            URL url = new URL(RSS_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // set Timeout and method
            conn.setReadTimeout(SOCKET_TIMEOUT);
            conn.setConnectTimeout(SOCKET_TIMEOUT);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();

            InputStream is = conn.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(is));
            String inputLine;
            String response = "";
            while ((inputLine = in.readLine()) != null) {
                response += inputLine;
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(response)));

            doc.getDocumentElement().normalize();

            NodeList allItems = doc.getElementsByTagName(ITEM_TAG);
            for (int i = 0; i < allItems.getLength(); i++) {
                Element xmlItem = (Element) allItems.item(i);
                ImageItem imageItem = new ImageItem();

                // Lấy ra thông tin của thẻ <title> đầu tiên trong thẻ <item>. Tương tự với
                // description, date.
                Node node = xmlItem.getElementsByTagName(TITLE_TAG).item(0);
                if (node != null) {
                    imageItem.title = node.getTextContent();
                }

                node = xmlItem.getElementsByTagName(DESCRIPTION_TAG).item(0);
                if (node != null) {
                    imageItem.description = node.getTextContent();
                }
                
                node = xmlItem.getElementsByTagName(CONTENT_ENCODED_TAG).item(0);
                if (node != null) {
                    imageItem.contentEncoded = node.getTextContent();
                }

                node = xmlItem.getElementsByTagName(LINK_TAG).item(0);
                if (node != null) {
                    imageItem.link = node.getTextContent();
                }

                node = xmlItem.getElementsByTagName(PUB_DATE_TAG).item(0);
                if (node != null) {
                    imageItem.date = node.getTextContent();
                }

                // Lấy ra thông tin của thẻ <enclosure> rồi đọc tiếp thuộc tính "url" của nó.
                Element enclosureItem = (Element) xmlItem.getElementsByTagName(ENCLOSURE_TAG).item(
                        0);
                if (enclosureItem != null) {
                    imageItem.imageUrl = enclosureItem.getAttributeNode(URL_ATT).getTextContent();
                }

                mItems.add(imageItem);
            }

        } catch (MalformedURLException e) {
            Log.e(TAG, e.toString());
        } catch (SAXException e) {
            Log.e(TAG, e.toString());
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        } catch (ParserConfigurationException e) {
            Log.e(TAG, e.toString());
        }
    }

    public ImageItem getNextItem() {
        mCurrentItem++;
        if (mCurrentItem >= mItems.size()) {
            mCurrentItem = mItems.size() - 1;
        }
        return mItems.get(mCurrentItem);
    }

    public ImageItem getPreviousItem() {
        mCurrentItem--;
        if (mCurrentItem < 0) {
            mCurrentItem = 0;
        }
        return mItems.get(mCurrentItem);
    }

    public ImageItem getCurrentItem() {
        return mItems.get(mCurrentItem);
    }

    public boolean hasNext() {
        return mCurrentItem < mItems.size() - 1;
    }

    public boolean hasPrevious() {
        return mCurrentItem > 0;
    }

    public class ImageItem {
        public String imageUrl;
        public String title;
        public String description;
        public String contentEncoded;
        public String date;
        public String link;

        public void loadData(final TextView titleView, final TextView descriptionView,
                final TextView contentEncodedView, final TextView dateView, final ImageView imageView) {
            new AsyncTask<Void, Void, Bitmap>() {
                @Override
                protected Bitmap doInBackground(Void... params) {
                    return getBitmap();
                }

                @Override
                protected void onPostExecute(Bitmap bitmap) {
                    titleView.setText(title);
                    descriptionView.setText(description);
                    contentEncodedView.setText(contentEncoded);
                    dateView.setText(date);
                    imageView.setImageBitmap(bitmap);
                }
            }.execute();
        }
        
        Integer i = Integer.valueOf(1);

        private Bitmap getBitmap() {
            HttpURLConnection connection = null;
            InputStream input = null;
            try {
                connection = (HttpURLConnection) new URL(imageUrl)
                        .openConnection();
                connection.setDoInput(true);
                connection.connect();

                // Lấy ảnh về.
                input = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(input);
                return bitmap;
            } catch (IOException e) {
                Log.e(TAG, "getBitmap " + e.toString());
            } finally {
                if (input != null) {
                    // Đóng kết nối
                    try {
                        input.close();
                    } catch (IOException e) {
                    }
                }
                
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return null;
        }

        //        private static final int MAX_WIDTH = 1080;
        //        private InputStream getNewInputStream() throws IOException {
        //            // Khởi tạo kết nối để lấy ảnh.
        //            HttpURLConnection connection = (HttpURLConnection) new URL(imageUrl).openConnection();
        //            connection.setDoInput(true);
        //            connection.connect();
        //
        //            // Lấy ảnh về.
        //            InputStream input = connection.getInputStream();
        //            return input;
        //        }
        //
        //        private int calculateInSampleSize(
        //                BitmapFactory.Options options, int reqWidth) {
        //            // Raw height and width of image
        //            final int width = options.outWidth;
        //            int inSampleSize = 1;
        //
        //            if (width > reqWidth) {
        //                final int halfWidth = width / 2;
        //
        //                // Calculate the largest inSampleSize value that is a power of 2 and keeps both
        //                // height and width larger than the requested height and width.
        //                while ((halfWidth / inSampleSize) >= reqWidth) {
        //                    inSampleSize *= 2;
        //                }
        //            }
        //
        //            return inSampleSize;
        //        }
        //        private Bitmap getBitmap() {
        //            HttpURLConnection connection = null;
        //            try {
        //                InputStream input = getNewInputStream();
        //                // Tiến hành decode thành Bitmap
        //                BitmapFactory.Options options = new BitmapFactory.Options();
        //                options.inJustDecodeBounds = true;
        //                BitmapFactory.decodeStream(input, null, options);
        //                input.close();
        //
        //                options.inSampleSize = calculateInSampleSize(options, MAX_WIDTH);
        //                options.inJustDecodeBounds = false;
        //                input = getNewInputStream();
        //                Bitmap bitmap = BitmapFactory.decodeStream(input, null, options);
        //
        //                // Đóng kết nối
        //                input.close();
        //                return bitmap;
        //            } catch (IOException e) {
        //                Log.e(TAG, "getBitmap " + e.toString());
        //            }
        //            return null;
        //        }
    }
}
