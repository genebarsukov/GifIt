package codewrencher.gifit.objects.simple.item;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import codewrencher.gifit.R;
import codewrencher.gifit.helpers.Damen;
import codewrencher.gifit.helpers.async.interfaces.ImageItemLoadedListener;
import codewrencher.gifit.ui.fragments.BaseFragment;

/**
 * Created by Gene on 12/19/2015.
 */
public class ImageItem extends Item {

    protected int screen_width;
    protected int screen_height;
    protected int img_resource_id;
    protected int img_width;
    protected int img_height;
    protected int thumb_width;
    protected int thumb_height;
    protected String title;
    protected String image_path;
    protected String finished_loading_message;
    protected Bitmap image;
    protected Bitmap thumbnail;
    protected View img_thumbnail_layout;
    protected View img_full_size_layout;

    protected BaseFragment fragment;
    protected ImageItemLoadedListener image_item_loaded_Listener;
    protected Object parent_object;
    protected Damen damen;

    /***********************************************************************************************
     * Constructors
     * ImageItem is a flexible object inherited by many higher level classes and used prolifically
     * Different items have different needs, and hence the variety of overloaded Constructors
     */
    public ImageItem() {}
    public ImageItem( BaseFragment fragment, String image_path, Bitmap image, String title ) {
        super();
        this.setCommonAttributes(fragment, title);
        this.image_path = image_path;
        this.thumbnail = image;
    }
    public ImageItem( BaseFragment fragment, String image_path, String title ) {
        super();
        this.setCommonAttributes(fragment, title);
        this.image_path = image_path;
    }
    public ImageItem( BaseFragment fragment, int img_resource_id, String title ) {
        super();
        this.setCommonAttributes(fragment, title);
        this.img_resource_id = img_resource_id;

    }
    public ImageItem( BaseFragment fragment, String title ) {
        super();
        this.setCommonAttributes( fragment, title );
    }

    /**
     * Helps out with setting common properties among the many overloaded Constructors
     * @param fragment  : The fragment where this item resides
     * @param title     : An optional title of the item. Rarely used nowadays
     */
    private void setCommonAttributes( BaseFragment fragment, String title ) {
        this.fragment = fragment;
        this.damen = fragment.damen;
        this.title = title;
        this.screen_width = fragment.getDisplayWidth();
        this.screen_height = fragment.getDisplayHeight();

        this.state = "on";
    }

    /**---------------------------------------------------------------------------------------------
     * Register Listener
     */
    /***********************************************************************************************
     * Register a listener to wait for the image to load
     * @param image_item_loaded_Listener ImageItemLoadedListener: notifies when the image has finished loading
     */
    public void registerImageItemLoaderListener(ImageItemLoadedListener image_item_loaded_Listener) {
        this.image_item_loaded_Listener = image_item_loaded_Listener;
    }
    public void setFinishedLoadingMessage( String finished_loading_message ) { this.finished_loading_message = finished_loading_message; }

    /**---------------------------------------------------------------------------------------------
     * Setters
     */
    /**
     * Set Stuff
     */
    public void setFragment( BaseFragment fragment ) {
        this.fragment = fragment;
    }
    public void setImgThumbnailLayout(View item_thumbnail_layout) {
        this.img_thumbnail_layout = item_thumbnail_layout;
    }
    public void setImgFullSizeLayout(View item_full_size_layout) {
        this.img_full_size_layout = item_full_size_layout;
    }
    public void setImageSize(int img_width, int img_height) {
        this.img_width = img_width;
        this.img_height = img_height;
    }
    public void setThumbnailSize(int thumb_width, int thumb_height) {
        this.thumb_width = thumb_width;
        this.thumb_height = thumb_height;
    }
    public void setThumbnailViewSize(View thumbnail_layout) {
        if (this.thumb_width != 0 && this.thumb_height != 0) {
            ViewGroup.LayoutParams image_view_params = thumbnail_layout.getLayoutParams();
            image_view_params.width = this.thumb_width;
            image_view_params.height = this.thumb_height;
        }
    }
    public void setImagePath(String image_path) {
        this.image_path = image_path;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public void setThumbnail(Bitmap thumbnail) {
        this.thumbnail = thumbnail;
    }
    public void setImage(Bitmap image) {
        this.image = image;
    }
    public void setImgResourceId(int img_resource_id) {
        this.img_resource_id = img_resource_id;
    }
    public void setParentObject(Object parent_object) {
        this.parent_object = parent_object;
    }

    /**---------------------------------------------------------------------------------------------
     * Getters
     */
    /**
     * Get Stuff
     */
    public View getImgThumbnailLayout() {
        return this.img_thumbnail_layout;
    }
    public View getImgFullSizeLayout() {
        return this.img_full_size_layout;
    }
    public int getImageWidth() {
        return this.img_width;
    }
    public int getImageHeight() {
        return this.img_height;
    }
    public int getThumbnailWidth() {
        return this.thumb_width;
    }
    public int getThumbnailHeight() {
        return this.thumb_height;
    }
    public String getImagePath() {
        return image_path;
    }
    public String getTitle() {
        return title;
    }
    public int getImgResourceId() {
        return img_resource_id;
    }
    public Object getParentObject() {
        return this.parent_object;
    }

    public Bitmap getThumbnail() {
        Log.d("IMAGE ITEM", "getThumbnail");
        if (this.thumbnail == null) {
            // create the thumbnail asynchronously
            this.createThumbnail();
        } else { // do a simple loading of the thumbnail if no view is present to render
            if (this.img_thumbnail_layout != null) {
                Log.d("IMAGE ITEM", "this.img_thumbnail_layout != null");
                ImageView item_image_view = (ImageView) img_thumbnail_layout.findViewById(R.id.image);
                item_image_view.setImageBitmap(thumbnail);
            }
        }
        return thumbnail;
    }
    public Bitmap getImage() {

        if (this.image == null) {

            if (this.img_resource_id != 0) {

                this.image = BitmapFactory.decodeResource(fragment.getSavedActivity().getResources(), this.img_resource_id);
            }
            else if (this.image_path != null) {

                this.image = fragment.file_manager.loadBitmapFromFilePath(this.image_path);
            }
            if (this.image != null) {

                int img_width = this.img_width;
                int img_height = this.img_height;
                if (img_width == 0) img_width = 500;
                if (img_height == 0) img_height = 500;

                if (this.image.getWidth() < img_width)    img_width = this.image.getWidth();
                if (this.image.getHeight() < img_height)  img_height = this.image.getHeight();

                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                this.image = Bitmap.createBitmap(this.image, 0, 0, img_width, img_height, matrix, true);
            }
        }
        return this.image;
    }

    public void createThumbnail() {
        if (this.img_thumbnail_layout != null) {
            // set the item thumbnail view if we want it to load the images async and set the view by itself
            this.loadThumbnailAsync();
        } else {
            // load the thumbnail synchronously. the waiting and the view setting is done elsewhere in the parent
            this.loadThumbnail();
        }
    }
    public void loadThumbnail() {
        Bitmap full_image = this.getImage();
        if (full_image == null) {
            this.thumbnail = null;
        } else {
            int thumb_width = this.thumb_width;
            int thumb_height = this.thumb_height;
            if (thumb_width == 0) thumb_width = screen_width / 3;
            if (thumb_height == 0) thumb_height = screen_width / 3;

            if (full_image.getWidth() < thumb_width) thumb_width = full_image.getWidth();
            if (full_image.getHeight() < thumb_height) thumb_height = full_image.getHeight();

            this.thumbnail = Bitmap.createScaledBitmap(full_image, thumb_width, thumb_height, false);
        }
    }
    public void loadThumbnailAsync() {
        this.setThumbnailViewSize(img_thumbnail_layout);
        final ProgressBar progress_bar = this.configureProgressBar();

        final ImageView item_image_view = (ImageView) img_thumbnail_layout.findViewById(R.id.image);

        AsyncTask async_task = new AsyncTask() {
            @Override
            protected void onPreExecute() {
                progress_bar.setVisibility(View.VISIBLE);
            }
            @Override
            protected Object doInBackground(Object[] params) {

                loadThumbnail();
                return null;
            }
            @Override
            protected void onPostExecute(Object result) {
                if (thumbnail != null) {
                    item_image_view.setImageBitmap(thumbnail);
                }
                progress_bar.setVisibility(View.GONE);

                notifyImageItemLoadedListener();
            }
        };
        async_task.execute();
    }

    public ProgressBar configureProgressBar() {

        ProgressBar progress_bar = (ProgressBar) img_thumbnail_layout.findViewById(R.id.progress_bar);
        progress_bar.getIndeterminateDrawable().setColorFilter(fragment.getSavedActivity().getResources().getColor(R.color.jobber_main_blue), android.graphics.PorterDuff.Mode.SRC_IN);
        progress_bar.setVisibility(View.GONE);

        ViewGroup.LayoutParams progress_params = progress_bar.getLayoutParams();
        progress_params.width = this.getThumbnailWidth();
        progress_params.height = this.getThumbnailHeight();
        progress_bar.setLayoutParams(progress_params);

        return progress_bar;
    }

    protected void notifyImageItemLoadedListener() {

        if ( this.finished_loading_message == null ) {
            this.image_item_loaded_Listener.onImageItemLoad("thumbnail_loaded", this.index);
        }
        else {
            this.image_item_loaded_Listener.onImageItemLoad(finished_loading_message, this.index);
        }
    }
}
