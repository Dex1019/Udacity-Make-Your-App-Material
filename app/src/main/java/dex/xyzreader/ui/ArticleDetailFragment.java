package dex.xyzreader.ui;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dex.xyzreader.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import dex.xyzreader.data.ArticleLoader;

import static android.content.ContentValues.TAG;

/**
 * Created by anant on 21/7/18.
 */

public class ArticleDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    public static final String ARG_ITEM_ID = "item_id";
    private long mItemId;

    private View mRootView;

    private LinearLayout titleLayout;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");

    private SimpleDateFormat outputFormat = new SimpleDateFormat();

    private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2, 1, 1);

    public ArticleDetailFragment() {
    }

    public static ArticleDetailFragment newInstance(long itemId) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItemId = getArguments().getLong(ARG_ITEM_ID);
        }

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);
        mRootView.findViewById(R.id.fab_share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setText("Some sample text")
                        .getIntent(), getString(R.string.action_share)));
            }
        });
        titleLayout = mRootView.findViewById(R.id.title_layout);
        return mRootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return ArticleLoader.newInstanceForItemId(getActivity(), mItemId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (data == null || data.isClosed() || !data.moveToFirst()) {
            return;
        }
        bindViews(data);
    }

    private AppCompatActivity getActivityCast() {
        return (AppCompatActivity) getActivity();
    }

    private void bindViews(Cursor mCursor) {

        final ImageView imageView = mRootView.findViewById(R.id.backdrop);
        Toolbar toolbar = mRootView.findViewById(R.id.toolbar_title);
        TextView authorText = mRootView.findViewById(R.id.author_text_view);
        final WebView bodyText = mRootView.findViewById(R.id.body_text);
        final ProgressBar progressBar = mRootView.findViewById(R.id.indeterminateBar);
        TextView titleTextView = mRootView.findViewById(R.id.title_text_view);

        getActivityCast().setSupportActionBar(toolbar);
        getActivityCast().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getActivityCast().getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivityCast().onBackPressed();
            }
        });

        String photo = mCursor.getString(ArticleLoader.Query.PHOTO_URL);
        String title = mCursor.getString(ArticleLoader.Query.TITLE);
        final String body = Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY).replaceAll("(\r\n|\n)", "<br />")).toString();

        toolbar.setTitle(title);
        if (titleTextView != null) {
            titleTextView.setText(title);
            toolbar.setTitle("");
        }
        Date publishedDate = parsePublishedDate(mCursor);
        if (!publishedDate.before(START_OF_EPOCH.getTime())) {
            authorText.setText(Html.fromHtml(
                    DateUtils.getRelativeTimeSpanString(
                            publishedDate.getTime(),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString()
                            + " by "
                            + mCursor.getString(ArticleLoader.Query.AUTHOR)
            ));

        } else {
            // If date is before 1902, just show the string
            authorText.setText(Html.fromHtml(
                    outputFormat.format(publishedDate) + " by <font color='#ffffff'>"
                            + mCursor.getString(ArticleLoader.Query.AUTHOR)
                            + "</font>"));

        }
        bodyText.loadDataWithBaseURL("", body, "text/html", "UTF-8", "");
        bodyText.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                if (progress == 100) {
                    //do your task
                    progressBar.setVisibility(View.GONE);
                    bodyText.setVisibility(View.VISIBLE);
                }
            }
        });

        Picasso.get()
                .load(photo)
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        imageView.setImageBitmap(bitmap);
                        Palette p = Palette.from(bitmap).generate();
                        int backgroundColor = ContextCompat.getColor(getActivity(),
                                R.color.colorPrimaryDark);
                        if (titleLayout != null)
                            titleLayout.setBackgroundColor(p.getDarkMutedColor(backgroundColor));
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });
    }

    private Date parsePublishedDate(Cursor mCursor) {
        try {
            String date = mCursor.getString(ArticleLoader.Query.PUBLISHED_DATE);
            return dateFormat.parse(date);
        } catch (ParseException ex) {
            Log.e(TAG, ex.getMessage());
            Log.i(TAG, "passing today's date");
            return new Date();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

}
