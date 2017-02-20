package codewrencher.gifit.helpers.async.image_loaders;

import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.Collections;

import codewrencher.gifit.R;
import codewrencher.gifit.helpers.adapters.GridViewAdapter;
import codewrencher.gifit.helpers.files.FilePathComparator;
import codewrencher.gifit.objects.simple.item.ImageItem;
import codewrencher.gifit.objects.scrollers.SelectListScroller;
import codewrencher.gifit.ui.fragments.BaseFragment;
import codewrencher.gifit.ui.fragments.GalleryFragment;
import codewrencher.gifit.ui.fragments.GifSpaceFragment;

/**
 * Created by Gene on 12/20/2015.
 */
public class FileImageLoader extends ImageLoader {
    private GridViewAdapter grid_adapter;
    private int batch_size;
    private int start_index;
    private ProgressBar progress_bar_corner;
    private ArrayList<String> saved_file_paths;

    public FileImageLoader(GalleryFragment fragment, int batch_size, int start_index) {
        super(fragment);

        this.batch_size = batch_size;
        this.start_index = start_index;
    }
    public FileImageLoader(BaseFragment fragment, int batch_size, int start_index) {
        super(fragment);

        this.batch_size = batch_size;
        this.start_index = start_index;
    }
    public FileImageLoader(BaseFragment fragment, int scroll_item_id, int batch_size, int start_index) {
        super(fragment);
        super.scroll_item_id = scroll_item_id;

        this.batch_size = batch_size;
        this.start_index = start_index;
    }
    public void setProgressBarCorner(ProgressBar progress_bar_corner) {
        this.progress_bar_corner = progress_bar_corner;
    }
    public void setSavedFilePaths(ArrayList<String> saved_file_paths) {
        this.saved_file_paths = saved_file_paths;
    }
    public void setBatchSize(int batch_size) {
        this.batch_size = batch_size;
    }
    public void setStartIndex(int start_index) {
        this.start_index = start_index;
    }
    public ArrayList<ImageItem> getImageBatch() {
        return this.image_items;
    }
    // MAIN ----------------------------------------------------------------------------------------
    public void loadImages() {
        loading = true;
        AsyncTask async_task = new AsyncTask() {
            @Override
            protected void onPreExecute() {
                if (initial_load) {
                    progress_bar.setVisibility(View.VISIBLE);
                } else {
                    if (progress_bar_corner != null) {
                        progress_bar_corner.setVisibility(View.VISIBLE);
                    }
                }
            }
            @Override
            protected Object doInBackground(Object[] params) {
                loadImageBatch();
                return null;
            }
            @Override
            protected void onPostExecute(Object result) {
                if(scroll_container_type == "scroll") {
                    createImageScrollView();
                } else {
                    createImageGrid();
                }
                loading = false;
                progress_bar.setVisibility(View.GONE);
                if (progress_bar_corner != null) {
                    progress_bar_corner.setVisibility(View.GONE);
                }
                notifyListener("gif_images_loaded");
            }
        };
        async_task.execute();
    }
    private void createImageGrid() {
        if (initial_load) {
            grid_adapter = new GridViewAdapter(fragment, R.layout.item_gallery_image_loading, image_items);
            if (fragment instanceof GalleryFragment) {
                ((GalleryFragment) fragment).grid_view.setAdapter(grid_adapter);
                ((GalleryFragment) fragment).grid_view.setSelection(start_index);
            } else {
                if (fragment instanceof GifSpaceFragment) {
                    ((GifSpaceFragment) fragment).grid_view.setAdapter(grid_adapter);
                    ((GifSpaceFragment) fragment).grid_view.setSelection(start_index);
                }
            }
        } else {
            grid_adapter.setImageData(image_items);
            grid_adapter.notifyDataSetChanged();
        }
    }
    private void createImageScrollView() {
        if (initial_load) {
            list_scroller = new SelectListScroller(fragment, scroll_item_id, fragment.getFragmentView());
            list_scroller.setThumbnailSize(fragment.getDisplayWidth() / 6, fragment.getDisplayWidth() / 6);
            list_scroller.setImageSize(fragment.getDisplayWidth() / 2, fragment.getDisplayWidth() / 2);
            list_scroller.addImageItems(image_items);
        }
    }
    public void loadSavedFilePaths() {
        try {
            saved_file_paths = fragment.file_manager.getSavedFilePaths(((GalleryFragment) fragment).storage_type);
            if (saved_file_paths != null) {
                FilePathComparator file_path_comparator = new FilePathComparator();
                try {
                    Collections.sort(saved_file_paths, file_path_comparator);
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {}
        finally {
            fragment.fragment_pager.updateArgs(((GalleryFragment) fragment).storage_type, saved_file_paths);
        }
    }
    private void loadImageBatch() {
        if(saved_file_paths == null || saved_file_paths.size() == 0) {
            loadSavedFilePaths();
        }
        int file_count;
        if (saved_file_paths != null) {
            file_count = saved_file_paths.size();
            for (int i = 0; i < file_count; i++) {

                if ((i < (start_index - batch_size)) ||
                        (i >= (start_index + batch_size))) {
                    // load a blank if scrolled outside current view range

                    if (image_items.size() > i) {
                        // replace old if the array has previously been filled
                        image_items.remove(i);
                        image_items.add(i, null);
                    } else {
                        // add new blank
                        image_items.add(i, null);
                    }
                }
                else {
                    // load an ImageItem if within batch bounds
                    if (image_items.size() > i) {
                        // replace old blank with new item if the array has previously been filled
                        if (image_items.get(i) == null) {
                            image_items.remove(i);
                            image_items.add( i, new ImageItem( fragment, saved_file_paths.get(i), "img " + i ) );
                        }
                    } else {
                        //add new
                        image_items.add( new ImageItem( fragment, saved_file_paths.get(i), "img " + i ) );
                    }
                }
            }
        }
    }
}
