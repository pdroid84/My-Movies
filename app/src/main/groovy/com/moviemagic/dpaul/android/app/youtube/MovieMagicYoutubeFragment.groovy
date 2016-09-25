package com.moviemagic.dpaul.android.app.youtube

import android.os.Bundle
import android.widget.Toast
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayerSupportFragment
import com.moviemagic.dpaul.android.app.BuildConfig
import com.moviemagic.dpaul.android.app.R
import com.moviemagic.dpaul.android.app.backgroundmodules.LogDisplay
import groovy.transform.CompileStatic

@CompileStatic
class MovieMagicYoutubeFragment extends YouTubePlayerSupportFragment implements YouTubePlayer.OnInitializedListener {
    private static final String LOG_TAG = MovieMagicYoutubeFragment.class.getSimpleName()

    //Error dialog id
    private static final int RECOVERY_ERROR_DIALOG_ID = 1

    public static final String YOUTUBE_VIDEO_ID_KEY = 'youtube_video_id_key'

    private List<String> mVideoIds
    private ArrayList<String> videoIdsArrayList

    //Empty constructor, to be used by the system while creating the fragment when embedded in XML
    MovieMagicYoutubeFragment () {
        LogDisplay.callLog(LOG_TAG,'MovieMagicYoutubeFragment empty constructor is called',LogDisplay.MOVIE_MAGIC_YOUTUBE_FRAGMENT_LOG_FLAG)
    }

    /**
     * Returns a new instance of MovieMagicYoutubeFragment Fragment
     *
     * @param videoId The ID of the YouTube now_playing to play
     */
    public static MovieMagicYoutubeFragment createMovieMagicYouTubeFragment(final List<String> videoIds) {
        LogDisplay.callLog(LOG_TAG,'createMovieMagicYouTubeFragment is called',LogDisplay.MOVIE_MAGIC_YOUTUBE_FRAGMENT_LOG_FLAG)
        final MovieMagicYoutubeFragment movieMagicYouTubeFragment = new MovieMagicYoutubeFragment()
        final Bundle bundle = new Bundle()
        bundle.putStringArrayList(YOUTUBE_VIDEO_ID_KEY, videoIds as ArrayList<String>)
        movieMagicYouTubeFragment.setArguments(bundle)
        return movieMagicYouTubeFragment
    }

    @Override
    public void onCreate(Bundle bundle) {
        LogDisplay.callLog(LOG_TAG,'onCreate is called',LogDisplay.MOVIE_MAGIC_YOUTUBE_FRAGMENT_LOG_FLAG)
        super.onCreate(bundle)

        final Bundle arguments = getArguments()

        if (bundle != null && bundle.containsKey(YOUTUBE_VIDEO_ID_KEY)) {
            mVideoIds = bundle.getStringArrayList(YOUTUBE_VIDEO_ID_KEY)
        } else if (arguments != null && arguments.containsKey(YOUTUBE_VIDEO_ID_KEY)) {
            //TODO: Strangely videoIdsArrayList is no where used but holds data! so changing it, will remove after testing
//            videoIdsArrayList = arguments.getStringArrayList(YOUTUBE_VIDEO_ID_KEY)
            mVideoIds = arguments.getStringArrayList(YOUTUBE_VIDEO_ID_KEY)
        }
        initialize(BuildConfig.YOUTUBE_API_KEY, this)
    }


    /**
     * Set the now_playing id and initialize the player
     * This can be used when including the Fragment in an XML layout
     * @param videoId The ID of the now_playing to play
     */
    public void setVideoId(final List<String> videoIds) {
        LogDisplay.callLog(LOG_TAG,'setVideoId is called',LogDisplay.MOVIE_MAGIC_YOUTUBE_FRAGMENT_LOG_FLAG)
        mVideoIds = videoIds
        initialize(BuildConfig.YOUTUBE_API_KEY, this)
    }

    @Override
    void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean restored) {
        LogDisplay.callLog(LOG_TAG,'onInitializationSuccess is called',LogDisplay.MOVIE_MAGIC_YOUTUBE_FRAGMENT_LOG_FLAG)
        //This flag tells the player to switch to landscape when in fullscreen, it will also return to portrait
        //when leaving fullscreen
        youTubePlayer.setFullscreenControlFlags(YouTubePlayer.FULLSCREEN_FLAG_CONTROL_ORIENTATION)

        //This flag controls the system UI such as the status and navigation bar, hiding and showing them
        //alongside the player UI
        youTubePlayer.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_CONTROL_SYSTEM_UI)

        if (mVideoIds) {
            if (restored) {
                youTubePlayer.play()
            } else {
                youTubePlayer.loadVideos(mVideoIds)
            }
        }
    }

    @Override
    void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
        LogDisplay.callLog(LOG_TAG,'onInitializationFailure is called',LogDisplay.MOVIE_MAGIC_YOUTUBE_FRAGMENT_LOG_FLAG)
        if (youTubeInitializationResult.isUserRecoverableError()) {
            LogDisplay.callLog(LOG_TAG,'onInitializationFailure:user recoverable',LogDisplay.MOVIE_MAGIC_YOUTUBE_FRAGMENT_LOG_FLAG)
            youTubeInitializationResult.getErrorDialog(getActivity(), RECOVERY_ERROR_DIALOG_ID).show()
        } else {
            //Handle the failure
            Toast.makeText(getActivity(), R.string.youtube_initialization_error, Toast.LENGTH_LONG).show()
            LogDisplay.callLog(LOG_TAG,'onInitializationFailure:non-user recoverable',LogDisplay.MOVIE_MAGIC_YOUTUBE_FRAGMENT_LOG_FLAG)
        }
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        LogDisplay.callLog(LOG_TAG,'onSaveInstanceState is called',LogDisplay.MOVIE_MAGIC_YOUTUBE_FRAGMENT_LOG_FLAG)
        if(mVideoIds) {
            bundle.putStringArrayList(YOUTUBE_VIDEO_ID_KEY, new ArrayList<String>(mVideoIds))
        }
        super.onSaveInstanceState(bundle)
    }
}