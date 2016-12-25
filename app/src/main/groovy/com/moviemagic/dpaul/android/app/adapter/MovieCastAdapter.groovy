package com.moviemagic.dpaul.android.app.adapter

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.moviemagic.dpaul.android.app.DetailMovieFragment
import com.moviemagic.dpaul.android.app.PersonMovieActivity
import com.moviemagic.dpaul.android.app.R
import com.moviemagic.dpaul.android.app.backgroundmodules.GlobalStaticVariables
import com.moviemagic.dpaul.android.app.backgroundmodules.LogDisplay
import com.moviemagic.dpaul.android.app.backgroundmodules.PicassoLoadImage
import com.moviemagic.dpaul.android.app.contentprovider.MovieMagicContract
import groovy.transform.CompileStatic

@CompileStatic
class MovieCastAdapter extends RecyclerView.Adapter<MovieCastAdapter.MovieCastAdapterViewHolder> {
    @SuppressWarnings("GroovyConstantNamingConvention")
    private static final String LOG_TAG = MovieCastAdapter.class.getSimpleName()

    private Cursor mCursor
    private final Context mContext = null
    private final TextView mCastGridEmptyTextView = null
    private int mPrimaryDarkColor, mBodyTextColor

    //Empty constructor
    public MovieCastAdapter() {
        LogDisplay.callLog(LOG_TAG, 'MovieCastAdapter empty constructor is called', LogDisplay.MOVIE_CAST_ADAPTER_LOG_FLAG)
    }

    public MovieCastAdapter(final Context ctx, final TextView emptyView) {
        LogDisplay.callLog(LOG_TAG, 'MovieCastAdapter non-empty constructor is called', LogDisplay.MOVIE_CAST_ADAPTER_LOG_FLAG)
        mContext = ctx
        mCastGridEmptyTextView = emptyView
    }

    public class MovieCastAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final ImageView movieCastImageView
        public final TextView movieCastCharacterName
        public final TextView movieCastAsText
        public final TextView movieCastName

        public MovieCastAdapterViewHolder(final View view) {
            super(view)
            movieCastImageView = view.findViewById(R.id.single_movie_cast_grid_image) as ImageView
            movieCastCharacterName = view.findViewById(R.id.single_movie_cast_grid_char_name) as TextView
            movieCastName = view.findViewById(R.id.single_movie_cast_grid_cast_name) as TextView
            movieCastAsText = view.findViewById(R.id.single_movie_cast_grid_cast_as) as TextView
            view.setOnClickListener(this)
        }

        @Override
        public void onClick(final View v) {
            LogDisplay.callLog(LOG_TAG, "onClick is called.LayoutPos=${getLayoutPosition()}.AdapterPos=${getAdapterPosition()}", LogDisplay.MOVIE_CAST_ADAPTER_LOG_FLAG)
            mCursor.moveToPosition(getAdapterPosition())
            final int personId = mCursor.getInt(DetailMovieFragment.COL_MOVIE_CAST_PERSON_ID)
            LogDisplay.callLog(LOG_TAG,"Person id is $personId",LogDisplay.MOVIE_CAST_ADAPTER_LOG_FLAG)
            final Uri moviePersonInfoIdUri = MovieMagicContract.MoviePersonInfo.buildMoviePersonInfoUriWithPersonId(personId)
            final Intent mIntent = new Intent(mContext, PersonMovieActivity.class).setData(moviePersonInfoIdUri)
            //Set the animation
            final Bundle bundle = ActivityOptions.makeCustomAnimation(mContext,R.anim.slide_bottom_in_animation,0).toBundle()
            mContext.startActivity(mIntent,bundle)
        }
    }

    @Override
    MovieCastAdapterViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        LogDisplay.callLog(LOG_TAG, 'onCreateViewHolder is called', LogDisplay.MOVIE_CAST_ADAPTER_LOG_FLAG)
        //noinspection GroovyVariableCanBeFinal
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_movie_cast_grid, parent, false)
        view.setFocusable(true)
        return new MovieCastAdapterViewHolder(view)
    }

    @Override
    void onBindViewHolder(final MovieCastAdapterViewHolder holder, final int position) {
        //Move the cursor to correct position
        mCursor.moveToPosition(position)
        LogDisplay.callLog(LOG_TAG, 'onBindViewHolder is called', LogDisplay.MOVIE_CAST_ADAPTER_LOG_FLAG)
        final String profilePath = "$GlobalStaticVariables.TMDB_IMAGE_BASE_URL/$GlobalStaticVariables.TMDB_IMAGE_SIZE_W185" +
                "${mCursor.getString(DetailMovieFragment.COL_MOVIE_CAST_PROFILE_PATH)}"
        PicassoLoadImage.loadMoviePersonImage(mContext, profilePath, holder.movieCastImageView)
        holder.movieCastCharacterName.setText(mCursor.getString(DetailMovieFragment.COL_MOVIE_CAST_CHARACTER))
        holder.movieCastName.setText(mCursor.getString(DetailMovieFragment.COL_MOVIE_CAST_PERSON_NAME))
        //Apply color only it has got a value
        if(mPrimaryDarkColor && mBodyTextColor) {
            holder.movieCastCharacterName.setBackgroundColor(mPrimaryDarkColor)
            holder.movieCastCharacterName.setTextColor(mBodyTextColor)
            holder.movieCastAsText.setBackgroundColor(mPrimaryDarkColor)
            holder.movieCastAsText.setTextColor(mBodyTextColor)
            holder.movieCastName.setBackgroundColor(mPrimaryDarkColor)
            holder.movieCastName.setTextColor(mBodyTextColor)
        }
    }

    @Override
    int getItemCount() {
        if (null == mCursor) return 0
        return mCursor.getCount()
    }

    public void swapCursor(final Cursor newCursor) {
        LogDisplay.callLog(LOG_TAG, 'swapCursor is called', LogDisplay.MOVIE_CAST_ADAPTER_LOG_FLAG)
        mCursor = newCursor
        if (getItemCount() == 0) {
            mCastGridEmptyTextView.setVisibility(TextView.VISIBLE)
        } else {
            mCastGridEmptyTextView.setVisibility(TextView.INVISIBLE)
            notifyDataSetChanged()
        }
    }

    //Since the color is decided once the poster is downloaded by Picasso
    //but by then adapter might got loaded with data. Hence call notifyDataSetChanged
    //so that it get's recreated with correct color
    public void changeColor(final int primaryDarkColor, final int bodyTextColor) {
        LogDisplay.callLog(LOG_TAG, 'changeColor is called', LogDisplay.MOVIE_CAST_ADAPTER_LOG_FLAG)
        mPrimaryDarkColor = primaryDarkColor
        mBodyTextColor = bodyTextColor
        if (getItemCount() == 0) {
            mCastGridEmptyTextView.setTextColor(mBodyTextColor)
        } else {
            notifyDataSetChanged()
        }
    }
}