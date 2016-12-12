package com.moviemagic.dpaul.android.app

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.support.annotation.NonNull
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import android.widget.RelativeLayout
import com.moviemagic.dpaul.android.app.adapter.ImagePagerAdapter
import com.moviemagic.dpaul.android.app.backgroundmodules.GlobalStaticVariables
import com.moviemagic.dpaul.android.app.backgroundmodules.LogDisplay
import com.moviemagic.dpaul.android.app.backgroundmodules.Utility
import groovy.transform.CompileStatic

@CompileStatic
class ImageViewerActivity extends AppCompatActivity {
    private static final String LOG_TAG = ImageViewerActivity.class.getSimpleName()
    private ArrayList<String> mImageFilePath
    private String mTitle
    private int mAdapterPostion
    private boolean mBackdropImageFlag
    private RelativeLayout mImageViewerMainLayout
//    private View decorView
    private ImagePagerAdapter mAdapter
//    private int mCurrentImageId
//    private String mFileName
    private static final int REQUEST_EXTERNAL_STORAGE = 1

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer)
        ViewPager viewPager = findViewById(R.id.image_viewer_pager) as ViewPager
        mImageViewerMainLayout = findViewById(R.id.image_viewer_main_layout) as RelativeLayout

        if(savedInstanceState == null) {
            final Bundle extras = getIntent().getExtras()
            if(extras) {
                mTitle = extras.getString(GlobalStaticVariables.IMAGE_VIEWER_TITLE)
                mAdapterPostion = extras.getInt(GlobalStaticVariables.IMAGE_VIEWER_ADAPTER_POSITION,0)
                mImageFilePath = extras.getStringArrayList(GlobalStaticVariables.IMAGE_VIEWER_IMAGE_PATH_ARRAY)
                mBackdropImageFlag = extras.getBoolean(GlobalStaticVariables.IMAGE_VIEWER_BACKDROP_IMAGE_FLAG,false)
            }
        } else { //Retrieve it from onSaveInstanceState
            mTitle = savedInstanceState.getString(GlobalStaticVariables.IMAGE_VIEWER_TITLE)
            mAdapterPostion = savedInstanceState.getInt(GlobalStaticVariables.IMAGE_VIEWER_ADAPTER_POSITION,0)
            mImageFilePath = savedInstanceState.getStringArrayList(GlobalStaticVariables.IMAGE_VIEWER_IMAGE_PATH_ARRAY)
            mBackdropImageFlag = savedInstanceState.getBoolean(GlobalStaticVariables.IMAGE_VIEWER_BACKDROP_IMAGE_FLAG,false)
        }
        final Toolbar myToolbar = findViewById(R.id.image_viewer_toolbar) as Toolbar
        setSupportActionBar(myToolbar)
        getSupportActionBar().setDisplayHomeAsUpEnabled(true)
        getSupportActionBar().setTitle(mTitle)

        //Create and set PagerAdapter
        mAdapter = new ImagePagerAdapter(this, mTitle, mImageFilePath as String[],
                new ImagePagerAdapter.ImagePagerAdapterOnClickHandler(){
                    @Override
                    void onClick(int position) {
                        LogDisplay.callLog(LOG_TAG, "ImagePagerAdapter clicked.Position->$position", LogDisplay.IMAGE_VIEWER_ACTIVITY_LOG_FLAG)
                        if(getSupportActionBar() && getSupportActionBar().isShowing()) {
                            final Animation animOut = new TranslateAnimation(0,0,0,-100)
                            animOut.setDuration(100)
                            animOut.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                void onAnimationStart(Animation animation) {}
                                @Override
                                void onAnimationEnd(Animation animation) {
                                    getSupportActionBar().hide()
                                }
                                @Override
                                void onAnimationRepeat(Animation animation) {}
                            })
                            myToolbar.startAnimation(animOut)

                        } else {
                            getSupportActionBar().show()
                            final Animation animIn = new TranslateAnimation(0,0,-100,0)
                            animIn.setDuration(80)
                            myToolbar.startAnimation(animIn)
                        }
                    }
                }, mBackdropImageFlag)

        viewPager.setAdapter(mAdapter)
        //Position it at correct place (the image which is clicked)
        viewPager.setCurrentItem(mAdapterPostion)
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu, this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.image_viewer_activity_menu, menu)
        return true
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId()
        switch (itemId) {
            case android.R.id.home:
                finish()
                return true
            case R.id.menu_action_save:
                // Verify permission (needed for API level 23 and above)
                if(verifyPermission()) {
                    saveImageToExternalStorage()
                }
//                } else if (getPreferences(Context.MODE_PRIVATE).getInt(getString(R.string.app_pref_storage_permission_user_resp_key),0)
//                            == 1) { // Show message only after user denied the permission
//                    Snackbar.make(mImageViewerMainLayout, getString(R.string.write_permission_missing_msg), Snackbar.LENGTH_LONG).show()
//                }
                return true
            default:
                return super.onOptionsItemSelected(item)
        }
    }

    protected void saveImageToExternalStorage() {
        LogDisplay.callLog(LOG_TAG, 'saveImageToExternalStorage is called', LogDisplay.IMAGE_VIEWER_ACTIVITY_LOG_FLAG)

        // Get the ImageView which is currently hosting the image
        final ImageView imageView = findViewById(R.id.image_viewer_image) as ImageView
        // Get the Bitmap from the ImageView
        Bitmap bitmapImage
        if(imageView.getDrawable()) {
            bitmapImage  = ((BitmapDrawable) imageView.getDrawable()).getBitmap()
        } else {
            LogDisplay.callLog(LOG_TAG,'Not able to retrieve the bitmap',LogDisplay.IMAGE_VIEWER_ACTIVITY_LOG_FLAG)
        }
        final String imageFileName = (getString(R.string.app_name)).replaceAll(' ','_') + '_' + Utility.dateTimeForFileName() + '.jpg'
//        final String imageFileName = 'IMG_' + Utility.dateTimeForFileName() + '.jpg'
        LogDisplay.callLog(LOG_TAG,"File name -> $imageFileName",LogDisplay.IMAGE_VIEWER_ACTIVITY_LOG_FLAG)

        if(bitmapImage && Utility.isExternalStorageWritable()) {
            // Create a path where we will place our picture in the user's
            // public pictures directory.  Note that you should be careful about
            // what you place here, since the user often manages these files.
//            final File mImagePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            final File mImagePath = Environment.getExternalStoragePublicDirectory(
                    String.format(getString(R.string.saved_image_folder_name),getString(R.string.app_name),getString(R.string.app_name)))
            final File imageFile = new File(mImagePath, imageFileName)
            LogDisplay.callLog(LOG_TAG,"mImagePath -> ${mImagePath.toString()}",LogDisplay.IMAGE_VIEWER_ACTIVITY_LOG_FLAG)
            LogDisplay.callLog(LOG_TAG,"imageFile -> ${imageFile.toString()}",LogDisplay.IMAGE_VIEWER_ACTIVITY_LOG_FLAG)

            try {
                // Make sure the Pictures directory exists.
                if(!mImagePath.mkdirs()) {
                    LogDisplay.callLog(LOG_TAG,"Directory $mImagePath does not exist, will be created..",LogDisplay.IMAGE_VIEWER_ACTIVITY_LOG_FLAG)
                } else {
                    LogDisplay.callLog(LOG_TAG,"Directory $mImagePath exists",LogDisplay.IMAGE_VIEWER_ACTIVITY_LOG_FLAG)
                }

                final long bitmapSize = bitmapImage.getAllocationByteCount() as long
                final long freeSpace = mImagePath.getFreeSpace()
                LogDisplay.callLog(LOG_TAG,"bitmap size -> $bitmapSize",LogDisplay.IMAGE_VIEWER_ACTIVITY_LOG_FLAG)
                LogDisplay.callLog(LOG_TAG,"free sapce -> $freeSpace",LogDisplay.IMAGE_VIEWER_ACTIVITY_LOG_FLAG)
                if(bitmapSize < freeSpace) {
                    // Very simple code to copy a picture from the application's
                    // resource into the external file.  Note that this code does
                    // no error checking, and assumes the picture is small (does not
                    // try to copy it in chunks).  Note that if external storage is
                    // not currently mounted this will silently fail.
                    final ByteArrayOutputStream stream = new ByteArrayOutputStream()
                    bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                    final InputStream is = new ByteArrayInputStream(stream.toByteArray())

                    final OutputStream os = new FileOutputStream(imageFile)
                    byte[] data = new byte[is.available()]
                    is.read(data)
                    os.write(data)
                    is.close()
                    os.close()

                    // Tell the media scanner about the new file so that it is
                    // immediately available to the user.
//                    MediaScannerConnection.MediaScannerConnectionClient client = new MyMediaScannerConnectionClient(this, imageFile, null)
//                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"+ Environment.getExternalStorageDirectory())))
//                    MediaScannerConnection.scanFile(this, imageFile.toString() as String[], null,
//                            new MediaScannerConnection.OnScanCompletedListener() {
//                                public void onScanCompleted(String path, Uri uri) {
//                                    LogDisplay.callLog(LOG_TAG, "Scanned path -> $path -> uri= $uri", LogDisplay.IMAGE_VIEWER_ACTIVITY_LOG_FLAG)
//                                }
//                            })
                    final Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
//                    File f = new File(mCurrentPhotoPath)
                    final File file = new File(imageFile.getAbsolutePath())
                    LogDisplay.callLog(LOG_TAG,"File absolute path -> ${file.toString()}",LogDisplay.IMAGE_VIEWER_ACTIVITY_LOG_FLAG)
                    final Uri contentUri = Uri.fromFile(file)
                    LogDisplay.callLog(LOG_TAG,"Uri -> ${contentUri.toString()}",LogDisplay.IMAGE_VIEWER_ACTIVITY_LOG_FLAG)
                    mediaScanIntent.setData(contentUri)
                    this.sendBroadcast(mediaScanIntent)
                    Snackbar.make(mImageViewerMainLayout, getString(R.string.successful_image_Save_msg), Snackbar.LENGTH_LONG).show()
                } else {
                    LogDisplay.callLog(LOG_TAG,"Insufficient space. Space needed -> $bitmapSize & space available - > $freeSpace",LogDisplay.IMAGE_VIEWER_ACTIVITY_LOG_FLAG)
                    Snackbar.make(mImageViewerMainLayout, getString(R.string.insufficient_space_msg), Snackbar.LENGTH_LONG).show()
                }
            } catch (IOException e) {
                // Unable to create file, likely because external storage is
                // not currently mounted.
                Snackbar.make(mImageViewerMainLayout, getString(R.string.cannot_perform_operation_message), Snackbar.LENGTH_LONG).show()
                Log.e(LOG_TAG, "IOException error writing $imageFile: $e.message", e)
            }
        } else {
            LogDisplay.callLog(LOG_TAG,'Either bitmap is null or external storage not writable',LogDisplay.IMAGE_VIEWER_ACTIVITY_LOG_FLAG)
            Snackbar.make(mImageViewerMainLayout, getString(R.string.cannot_perform_operation_message), Snackbar.LENGTH_LONG).show()
        }
    }


    protected boolean verifyPermission() {
        LogDisplay.callLog(LOG_TAG,'verifyPermission is called',LogDisplay.IMAGE_VIEWER_ACTIVITY_LOG_FLAG)
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                LogDisplay.callLog(LOG_TAG,'verifyPermission:shouldShowRequestPermissionRationale is called',LogDisplay.IMAGE_VIEWER_ACTIVITY_LOG_FLAG)
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                // This method returns true if the app has requested this permission previously and the user denied the request but
                // if the user turned down the permission request in the past and chose the Don't ask again option in the permission request system dialog,
                // this method returns false
                Snackbar.make(mImageViewerMainLayout, getString(R.string.write_permission_missing_msg), Snackbar.LENGTH_INDEFINITE)
                        .setAction(android.R.string.ok, new View.OnClickListener() {
                    @Override
                    void onClick(View v) {
                        // Request the permission again
                        requestPermissionAgain()
                    }
                }).show()
            } else {
                // No explanation needed, we can request the permission.
                LogDisplay.callLog(LOG_TAG,'verifyPermission:requestPermissions is called',LogDisplay.IMAGE_VIEWER_ACTIVITY_LOG_FLAG)
                ActivityCompat.requestPermissions(this,
                        [Manifest.permission.WRITE_EXTERNAL_STORAGE] as String[],
                        REQUEST_EXTERNAL_STORAGE)
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
            return false
        } else {
            return true
        }
    }

    protected void requestPermissionAgain() {
        ActivityCompat.requestPermissions(this,
                [Manifest.permission.WRITE_EXTERNAL_STORAGE] as String[],
                REQUEST_EXTERNAL_STORAGE)
    }

    @Override
    void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        LogDisplay.callLog(LOG_TAG,'onRequestPermissionsResult is called',LogDisplay.IMAGE_VIEWER_ACTIVITY_LOG_FLAG)
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    saveImageToExternalStorage()
//                    // Write this to SharedPReferences - used to display message
//                    final SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE)
//                    final SharedPreferences.Editor editor = sharedPref.edit()
//                    editor.putInt(getString(R.string.app_pref_storage_permission_user_resp_key), 0)
//                    editor.commit()
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Snackbar.make(mImageViewerMainLayout, getString(R.string.write_permission_not_given_msg), Snackbar.LENGTH_LONG).show()
//                    // Write this to SharedPReferences - used to display message
//                    final SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE)
//                    final SharedPreferences.Editor editor = sharedPref.edit()
//                    editor.putInt(getString(R.string.app_pref_storage_permission_user_resp_key), 1)
//                    editor.commit()
                }
                break
        // other 'case' lines to check for other
        // permissions this app might request
        }
    }


//    /**
//     * Show a dialog to the user explaining why permission is required
//     */
//    private void explainStoragePermission() {
//        final AlertDialog.Builder builder = new AlertDialog.Builder(this)
//        builder.setTitle(R.string.storage_permission_dialog_title)
//                .setMessage(R.string.storage_permission_dialog_message)
//
//        builder.setPositiveButton(R.string.storage_permission_allow_button, new DialogInterface.OnClickListener() {
//            @Override
//            void onClick(DialogInterface dialog, int which) {
//                LogDisplay.callLog(LOG_TAG, 'Dialog permission ALLOW is clicked', LogDisplay.IMAGE_VIEWER_ACTIVITY_LOG_FLAG)
//                ActivityCompat.requestPermissions(getBaseContext(),
//                        [Manifest.permission.WRITE_EXTERNAL_STORAGE] as String[],
//                        REQUEST_EXTERNAL_STORAGE)
//            }
//        })
//
//        builder.setNegativeButton(R.string.storage_permission_deny_button, new DialogInterface.OnClickListener(){
//            @Override
//            void onClick(DialogInterface dialog, int which) {
//                LogDisplay.callLog(LOG_TAG, 'Dialog permission DENY is clicked', LogDisplay.IMAGE_VIEWER_ACTIVITY_LOG_FLAG)
//            }
//        })
//
//        // Create the AlertDialog
//        final AlertDialog dialog = builder.create()
//        dialog.show()
//    }

    @Override
    protected void onStart() {
        super.onStart()
        // Check if the user is online or not, if not then show a message
        final boolean isOnline = Utility.isOnline(this)
        if(!isOnline) {
            Snackbar.make(mImageViewerMainLayout, getString(R.string.no_internet_connection_message), Snackbar.LENGTH_LONG).show()
        } else if(Utility.isOnlyWifi(this) & !GlobalStaticVariables.WIFI_CONNECTED) {
            // If user has selected only WiFi but user is online without WiFi then show a dialog
            Snackbar.make(mImageViewerMainLayout, getString(R.string.internet_connection_without_wifi_message), Snackbar.LENGTH_LONG).show()
        } else if (Utility.isReducedDataOn(this)) {
            // If user has selected reduced data
            Snackbar.make(mImageViewerMainLayout, getString(R.string.reduced_data_use_on_message), Snackbar.LENGTH_LONG).show()
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putStringArrayList(GlobalStaticVariables.IMAGE_VIEWER_IMAGE_PATH_ARRAY,mImageFilePath)
        outState.putString(GlobalStaticVariables.IMAGE_VIEWER_TITLE,mTitle)
        outState.putBoolean(GlobalStaticVariables.IMAGE_VIEWER_BACKDROP_IMAGE_FLAG,mBackdropImageFlag)

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(outState)
    }

    @Override
    void onBackPressed() {
        super.onBackPressed()
        //Start the exit animation
        overridePendingTransition(0, R.anim.slide_bottom_out_animation)
    }

//    final class MyMediaScannerConnectionClient
//            implements MediaScannerConnection.MediaScannerConnectionClient {
//
//        private String mFilename
//        private String mMimetype
//        private MediaScannerConnection mConn
//
//        public MyMediaScannerConnectionClient(Context ctx, File file, String mimetype) {
//            this.mFilename = file.getAbsolutePath()
//            mConn = new MediaScannerConnection(ctx, this)
//            mConn.connect()
//        }
//        @Override
//        public void onMediaScannerConnected() {
//            mConn.scanFile(mFilename, mMimetype)
//        }
//
//        @Override
//        public void onScanCompleted(String path, Uri uri) {
//            mConn.disconnect()
//        }
//    }
}