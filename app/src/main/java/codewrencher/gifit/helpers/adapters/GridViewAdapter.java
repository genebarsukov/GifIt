package codewrencher.gifit.helpers.adapters;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import codewrencher.gifit.R;
import codewrencher.gifit.objects.simple.item.ImageItem;
import codewrencher.gifit.ui.fragments.BaseFragment;
import codewrencher.gifit.ui.fragments.GalleryFragment;

/**
 * Created by Gene on 12/19/2015.
 */
public class GridViewAdapter extends ArrayAdapter {
    private BaseFragment fragment;
    private Context context;
    private int item_layout_id;
    private ArrayList image_data = new ArrayList<>();
    protected int img_width;
    protected int img_height;
    protected int thumb_width;
    protected int thumb_height;

    public GridViewAdapter(BaseFragment fragment, int item_layout_id, ArrayList image_data) {
        super(fragment.context, item_layout_id, image_data);
        this.fragment = fragment;
        this.item_layout_id = item_layout_id;
        this.context = fragment.context;
        this.image_data = image_data;
        Log.d("GRID ADAPTER", "INITIALIZED!");
    }
    public void setImageData(ArrayList image_data) {
        this.image_data = image_data;
        Log.d("GRID ADAPTER", "DATA SET!");
    }
    public void setImageSize(int img_width, int img_height) {
        this.img_width = img_width;
        this.img_height = img_height;
    }
    public void setThumbnailSize(int thumb_width, int thumb_height) {
        this.thumb_width = thumb_width;
        this.thumb_height = thumb_height;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.d("GRID ADAPTER", "GET VIEW!");
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        ViewHolder view_holder = new ViewHolder();

        ImageItem image_item = null;
        View item_view = convertView;

        if (position < image_data.size()) {
            image_item = (ImageItem) image_data.get(position);
        }

        if (image_item == null) { // image item is null --------------------------------------------
            // create and pass the view holder a blank view
            item_view = inflater.inflate(item_layout_id, parent, false);
            item_view.setTag(view_holder);
            this.setViewHolderViews(view_holder, item_view);
            this.setImageDimensions(view_holder.item_image);
        }
        else { // image item is not null -----------------------------------------------------------
            image_item.registerImageItemLoaderListener((GalleryFragment) fragment);
            // create a new view or set the old view
            if (item_view == null) { // the view has not been created yet
                // set the holder to the view and have image_item set it
                item_view = inflater.inflate(item_layout_id, parent, false);
                item_view.setTag(view_holder);
                this.setViewsAndSizes(view_holder, image_item, item_view);

                image_item.setImgThumbnailLayout(item_view);
                image_item.getThumbnail();

            }
            else { // received convert view from the view holder
                // set the holder to the view and have image_item set it
                ImageView image_view = (ImageView) item_view.findViewById(R.id.image);
                image_view.destroyDrawingCache();
                image_view.setImageBitmap(null);
                item_view.setTag(view_holder);
                view_holder = (ViewHolder) item_view.getTag();
                this.setViewsAndSizes(view_holder, image_item, item_view);

                image_view.setAlpha(0.5f);
                image_item.setImgThumbnailLayout(item_view);
                image_item.getThumbnail();
            }
        }

        item_view.setAlpha(0.5f);
        return item_view;
    }
    private void setViewsAndSizes(ViewHolder view_holder, ImageItem image_item, View item_view) {
        this.setViewHolderViews(view_holder, item_view);
        this.setImageDimensions(view_holder.item_image);
        this.setItemImgSizes(image_item);
    }
    private void setViewHolderViews(ViewHolder view_holder, View item_view) {
        view_holder.thumbnail_layout = item_view;
        view_holder.item_title = (TextView) item_view.findViewById(R.id.text);
        view_holder.item_image = (ImageView) item_view.findViewById(R.id.image);
    }

    private void setImageDimensions(ImageView image) {
        ViewGroup.LayoutParams image_params = image.getLayoutParams();
        image_params.width = (fragment.getDisplayWidth() / 3) - 5;
        image_params.height = (fragment.getDisplayWidth() / 3) - 5;
        image.setLayoutParams(image_params);

        this.thumb_width = image_params.width;
        this.thumb_height = image_params.height;
    }
    private void setItemImgSizes(ImageItem image_item) {
        if (this.thumb_width != 0 && this.thumb_height != 0) {
            image_item.setThumbnailSize(this.thumb_width, this.thumb_height);
        }
        if (this.img_width != 0 && this.img_height != 0) {
            image_item.setImageSize(this.img_width, this.img_height);
        }
    }
    static class ViewHolder {
        View thumbnail_layout;
        TextView item_title;
        ImageView item_image;
    }
}