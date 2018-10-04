package dex.xyzreader.data;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.CursorLoader;

/**
 * Helper for loading a list of articles or a single article.
 */
public class ArticleLoader extends CursorLoader {
    private ArticleLoader(Context context, Uri uri) {
        super(context, uri, Query.PROJECTION, null, null, dex.xyzreader.data.ItemsContract.Items.DEFAULT_SORT);
    }

    public static ArticleLoader newAllArticlesInstance(Context context) {
        return new ArticleLoader(context, dex.xyzreader.data.ItemsContract.Items.buildDirUri());
    }

    public static ArticleLoader newInstanceForItemId(Context context, long itemId) {
        return new ArticleLoader(context, dex.xyzreader.data.ItemsContract.Items.buildItemUri(itemId));
    }

    public interface Query {
        String[] PROJECTION = {
                dex.xyzreader.data.ItemsContract.Items._ID,
                dex.xyzreader.data.ItemsContract.Items.TITLE,
                dex.xyzreader.data.ItemsContract.Items.PUBLISHED_DATE,
                dex.xyzreader.data.ItemsContract.Items.AUTHOR,
                dex.xyzreader.data.ItemsContract.Items.THUMB_URL,
                dex.xyzreader.data.ItemsContract.Items.PHOTO_URL,
                dex.xyzreader.data.ItemsContract.Items.ASPECT_RATIO,
                dex.xyzreader.data.ItemsContract.Items.BODY,
        };

        int _ID = 0;
        int TITLE = 1;
        int PUBLISHED_DATE = 2;
        int AUTHOR = 3;
        int THUMB_URL = 4;
        int PHOTO_URL = 5;
        int ASPECT_RATIO = 6;
        int BODY = 7;
    }
}
