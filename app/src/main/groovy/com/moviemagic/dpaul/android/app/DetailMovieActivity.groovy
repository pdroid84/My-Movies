package com.moviemagic.dpaul.android.app

import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CollapsingToolbarLayout
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import com.moviemagic.dpaul.android.app.utility.LogDisplay
import com.squareup.picasso.Callback
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import groovy.transform.CompileStatic

@CompileStatic
class DetailMovieActivity extends AppCompatActivity implements DetailMovieFragment.BackdropCallback, DetailMovieFragment.MovieTitleAndColorCallback {
    private static final String LOG_TAG = DetailMovieActivity.class.getSimpleName()

    CollapsingToolbarLayout mCollapsingToolbar
    Toolbar mToolbar
    AppBarLayout mAppBarLayout
    ImageView mBackdrop
    ViewPager mViewPager
    final static int MAX_WIDTH = 1024
    final static int MAX_HEIGHT = 768
    final android.os.Handler mHandler = new android.os.Handler()

    public static int mPrimaryDarkColor
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_detail)
        mToolbar = (Toolbar) findViewById(R.id.movie_detail_toolbar)
        setSupportActionBar(mToolbar)
        //Enable back to home button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true)

        mCollapsingToolbar = findViewById(R.id.movie_detail_collapsing_toolbar) as CollapsingToolbarLayout
        //Just clear off to be on the safe side
        mCollapsingToolbar.setTitle(" ")
        mAppBarLayout = findViewById(R.id.movie_detail_app_bar_layout) as AppBarLayout

        if (savedInstanceState == null) {
            //Create a Bundle to pass the data to Fragment
            Bundle args = new Bundle()
            //Get the data from the intent and put that to Bundle
            args.putParcelable(DetailMovieFragment.MOVIE_BASIC_INFO_MOVIE_ID_URI, getIntent().getData())
            LogDisplay.callLog(LOG_TAG,"Intent data -> ${getIntent().getData()}",LogDisplay.DETAIL_MOVIE_ACTIVITY_LOG_FLAG)
            //Create a movie detail fragment
            DetailMovieFragment movieDetailFragment = new DetailMovieFragment()
            movieDetailFragment.setArguments(args)
            getSupportFragmentManager().beginTransaction().add(R.id.movie_detail_container,movieDetailFragment).commit()
        }

        mBackdrop = findViewById(R.id.movie_detail_backdrop_image) as ImageView
//        mViewPager = findViewById(R.id.movie_detail_backdrop_view_pager) as ViewPager

//        BitmapFactory.Options options = new BitmapFactory.Options()
//        options.inJustDecodeBounds = true;
//        BitmapFactory.decodeResource(getResources(), R.id.movie_detail_backdrop_image, options)
//        int imageHeight = options.outHeight
//        int imageWidth = options.outWidth
//        String imageType = options.outMimeType
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //Override the callback methods of DetailMovieFragment
    @Override
    public void initializeActivityHostedTitleAndColor(String movieTitle, int primaryColor, int primaryDarkColor, int titleColor) {
//        mCollapsingToolbar.setTitle(movieTitle)
        mCollapsingToolbar.setStatusBarScrimColor(primaryDarkColor)
        mCollapsingToolbar.setContentScrimColor(primaryColor)
        mCollapsingToolbar.setBackgroundColor(primaryColor)
        mCollapsingToolbar.setCollapsedTitleTextColor(titleColor)

        //Show the title only when image is collapsed
        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false
            int scrollRange = -1
            @Override
            void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange()
                }
                if (scrollRange + verticalOffset == 0) {
                    mCollapsingToolbar.setTitle(movieTitle)
                    isShow = true
                } else if(isShow) {
                    mCollapsingToolbar.setTitle("")
                    isShow = false
                }
            }
        })
    }

    @Override
    public void initializeActivityHostedBackdrop(List<String> backdropImagePathList) {
//    public void initializeActivityHostedFields(String movieTitle, BackdropPagerAdapter backdropPagerAdapter) {
//        mCollapsingToolbar.setTitle(movieTitle)
//        mCollapsingToolbar.setBackgroundColor(prominentColor)
//        mAppBarLayout.setBackgroundColor(prominentColor)
//        mToolbar.setBackgroundColor(prominentColor)
//        final Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.backdrop_slide_anim)
        Animation animation = new TranslateAnimation(1000, 0,0, 0)
        animation.setDuration(200)
        animation.setFillAfter(true)
//        mViewPager.setAdapter(backdropPagerAdapter)
        int counter = 0
//        final android.os.Handler handler = new android.os.Handler()
        final Runnable runnable = new Runnable() {
            @Override
            void run() {
//                mBackdrop.clearAnimation()
                String backdropPath = "http://image.tmdb.org/t/p/w500${backdropImagePathList[counter]}"
                loadBackdropImage(backdropPath, animation)
                counter++
                if (counter >= backdropImagePathList.size()) {
                    //Do not cycle through if the backdrop image count is 1
                    if (backdropImagePathList.size() > 1) {
                        counter = 0
                    }
                }
                mHandler.postDelayed(this, 8000) //Interval time is 8 seconds
            }
        }
        mHandler.postDelayed(runnable, 1000) //Initial delay is 1 seconds

//        String backdropImagePath = "http://image.tmdb.org/t/p/w500${backdropImagePathList[counter]}"
//        Picasso.with(this)
//                .load(backdropImagePath)
//                .placeholder(R.drawable.grid_image_placeholder)
//                .error(R.drawable.grid_image_error)
//                .into(mBackdrop, new Callback() {
//            @Override
//            void onSuccess() {
//                final android.os.Handler handler = new android.os.Handler()
//                Runnable runnable = new Runnable() {
//                    @Override
//                    void run() {
//                        String backdropPath = "http://image.tmdb.org/t/p/w500${backdropImagePathList[counter+1]}"
//                        loadBackdropImage(backdropPath)
//                        counter++
//                        if (counter >= backdropImagePathList.size()) {
//                            counter = 0
//                        }
//                        handler.postDelayed(this, 5000) //Interval time is 5 seconds
//                    }
//                }
//                handler.postDelayed(runnable, 3000) //Initial delay is 3 seconds
//            }
//
//            @Override
//            void onError() {
//
//            }
//        })

//        Runnable runnable = new Runnable() {
//            @Override
//            void run() {
//                String backdropPath = "http://image.tmdb.org/t/p/w500${backdropImagePathList[counter]}"
//                loadBackdropImage(backdropPath)
//                counter++
//                if(counter >= backdropImagePathList.size()) {
//                    counter = 0
//                }
//                mBackdrop.postDelayed(this,5000) //Interval time is 5 seconds
//            }
//        }
//        mBackdrop.postDelayed(runnable,3000) //Initial delay is 3 seconds
    }

    void loadBackdropImage(String backdropPath, Animation animation) {
//        LogDisplay.callLog(LOG_TAG,"backfropPath -> $backdropPath",LogDisplay.DETAIL_MOVIE_ACTIVITY_LOG_FLAG)
//        Target target = new Target() {
//            @Override
//            void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
//               // mBackdrop.clearAnimation()
////                mBackdrop.setImageBitmap(bitmap)
//                Resources resources = getResources()
//                mBackdrop.setImageBitmap(
//                        BackdropBitmapTransform.decodeSampledBitmapFromResource(resources, R.id.movie_detail_backdrop_image, 100, 100))
//            }
//
//            @Override
//            void onBitmapFailed(Drawable errorDrawable) {
//
//            }
//
//            @Override
//            void onPrepareLoad(Drawable placeHolderDrawable) {
////                mBackdrop.startAnimation(animation)
//            }
//        }

//        int size = (int) Math.ceil(Math.sqrt(MAX_WIDTH * MAX_HEIGHT))
        Picasso.with(this)
                .load(backdropPath)
                .noPlaceholder()
//                .transform(new BitmapTransform(MAX_WIDTH, MAX_HEIGHT))
                .centerInside()
//                .noFade()
                .fit()
                .memoryPolicy(MemoryPolicy.NO_STORE, MemoryPolicy.NO_CACHE)
                .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
//                .resize(size,size)
//                .placeholder(R.drawable.grid_image_placeholder)
//                .error(R.drawable.grid_image_error)
//                .into(target)
                .into(mBackdrop, new Callback() {
            @Override
            void onSuccess() {
                mBackdrop.startAnimation(animation)
            }

            @Override
            void onError() {

            }
        })
    }

    @Override
    protected void onStop() {
        LogDisplay.callLog(LOG_TAG,'onStop is called',LogDisplay.DETAIL_MOVIE_ACTIVITY_LOG_FLAG)
        super.onStop()
        mHandler.removeCallbacksAndMessages(null)
    }
}