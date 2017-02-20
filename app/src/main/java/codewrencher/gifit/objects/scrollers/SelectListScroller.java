package codewrencher.gifit.objects.scrollers;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;

import codewrencher.gifit.R;
import codewrencher.gifit.objects.simple.item.ImageItem;
import codewrencher.gifit.ui.fragments.BaseFragment;

/**
 * Created by Gene on 7/11/2015.
 */
public class SelectListScroller {

    protected View container;
    protected int item_id;
    protected BaseFragment fragment;
    protected LayoutInflater layout_inflater;
    protected int img_width;
    protected int img_height;
    protected int thumb_width;
    protected int thumb_height;
    protected Bitmap image_bitmap;

    public SelectListScroller() {}
    public void setContainer(View container) {
        this.container = container;
    }
    public SelectListScroller(int item_id, View container) {
        this.item_id = item_id;
        this.container = container;
    }
    public SelectListScroller(BaseFragment fragment, int item_id, View container) {
        this.item_id = item_id;
        this.container = container;
        this.fragment = fragment;
        this.layout_inflater = fragment.getSavedActivity().getLayoutInflater();
    }
    public void setImageSize(int img_width, int img_height) {
        this.img_width = img_width;
        this.img_height = img_height;
    }
    public void setThumbnailSize(int thumb_width, int thumb_height) {
        this.thumb_width = thumb_width;
        this.thumb_height = thumb_height;
    }
    public void addItem(View view) {
        LinearLayout item = (LinearLayout) container.findViewById(item_id);
        item.addView(view);
    }
    public void clearItems() {
        LinearLayout view_container = (LinearLayout) container.findViewById( item_id );
        int item_index = view_container.getChildCount();
        for (int i = 0; i < item_index; i++) {
            view_container.removeView(view_container.getChildAt(0));
        }
    }
    public void insertItem(View layout_to_add, int index) {
        LinearLayout view_container = (LinearLayout) container.findViewById(item_id);
        view_container.addView(layout_to_add, index);
    }
    public View getItem(int index) {
        LinearLayout view_container = (LinearLayout) container.findViewById(item_id);
        return view_container.getChildAt(index);
    }
    public void removeItem(View layout_to_remove) {
        ViewGroup parent = (ViewGroup) layout_to_remove.getParent();
        parent.removeView(layout_to_remove);
    }
    public void removeItemByIndex(int view_index) {
        LinearLayout view_container = (LinearLayout) container.findViewById(item_id);
        if (view_container.isShown()) {
            view_container.removeView(view_container.getChildAt(view_index));
        }
    }

    public void addImageItems(ArrayList<ImageItem> image_items) {
        if (image_items.size() > 0) {

            for (final ImageItem image_item : image_items) {

                setItemImgSizes(image_item);

                View item_layout = layout_inflater.inflate(R.layout.item_gallery_image_loading, (ViewGroup) fragment.getFragmentView(), false);
                image_item.setImgThumbnailLayout(item_layout);
                image_item.getThumbnail();

                addItem(item_layout);
            }
        }
    }

    /*
    public void addImageItems(ArrayList<ImageItem> image_items) {
        if ( image_items.size() > 0) {

            for (final ImageItem image_item : image_items) {

                image_item.registerImageItemLoaderListener((GifSpaceFragment) fragment);
                setItemImgSizes( image_item );

                View item_layout = layout_inflater.inflate(R.layout.item_gallery_image_loading, (ViewGroup) fragment.getFragmentView(), false);
                image_item.setImgThumbnailLayout(item_layout);
                image_item.getThumbnail();

                item_layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ImageView overlay = (ImageView) v.findViewById(R.id.overlay);

                        if (((GifSpaceFragment) fragment).checkForActiveFrames()) {     // only switch states if there are more than one active items
                            image_item.switchState();
                            if (image_item.getState().equals("on")) {
                                overlay.setVisibility(View.GONE);

                            } else if (image_item.getState().equals("off")) {
                                overlay.setVisibility(View.VISIBLE);
                            }
                        } else {                                                        // allow for turning items back on if there is only one active
                            if (image_item.getState().equals("off")) {
                                image_item.switchState();
                                overlay.setVisibility(View.GONE);
                            }
                        }
                        ((GifSpaceFragment) fragment).checkForActiveFrames();           // handle showing and hiding check mark after toggling an item
                    }
                });

                addItem(item_layout);
            }
        }
    }
    */
    private void setItemImgSizes(ImageItem image_item) {
        if (this.thumb_width != 0 && this.thumb_height != 0) {
            image_item.setThumbnailSize(this.thumb_width, this.thumb_height);
        }
        if (this.img_width != 0 && this.img_height != 0) {
            image_item.setImageSize(this.img_width, this.img_height);
        }
    }
}