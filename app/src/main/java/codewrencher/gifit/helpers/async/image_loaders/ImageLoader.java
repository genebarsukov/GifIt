package codewrencher.gifit.helpers.async.image_loaders;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.ProgressBar;

import java.util.ArrayList;

import codewrencher.gifit.helpers.async.interfaces.ImageLoadedListener;
import codewrencher.gifit.objects.simple.item.ImageItem;
import codewrencher.gifit.objects.scrollers.SelectListScroller;
import codewrencher.gifit.ui.fragments.BaseFragment;

/**
 * Created by Gene on 2/28/2016.
 * _____          _   __          __                  _
 * / ____|        | |  \ \        / /                 | |
 * | |     ___   __| | __\ \  /\  / / __ ___ _ __   ___| |__   ___ _ __
 * | |    / _ \ / _` |/ _ \ \/  \/ / '__/ _ \ '_ \ / __| '_ \ / _ \ '__|
 * | |___| (_) | (_| |  __/\  /\  /| | |  __/ | | | (__| | | |  __/ |
 * \_____\___/ \__,_|\___| \/  \/ |_|  \___|_| |_|\___|_| |_|\___|_|
 */
public class ImageLoader {
    protected BaseFragment fragment;
    protected ImageLoadedListener image_loader_listener;
    protected ArrayList<ImageItem> image_items;
    protected ProgressBar progress_bar;
    protected boolean loading;
    protected boolean initial_load;
    protected String scroll_container_type;
    protected int scroll_item_id;
    protected SelectListScroller list_scroller;

    public ImageLoader(BaseFragment fragment) {
        this.fragment = fragment;
        this.image_items = new ArrayList<>();
        this.loading = false;
        this.initial_load = true;
    }
    public ImageLoader(BaseFragment fragment, ArrayList<ImageItem> image_items) {
        this.fragment = fragment;
        this.image_items = image_items;
        this.loading = false;
        this.initial_load = true;
    }
    public void setScrollContainerType(String scroll_container_type) {
        this.scroll_container_type = scroll_container_type;
    }
    public ArrayList<ImageItem> getImageItems() {
        return this.image_items;
    }
    public void setProgressBar(ProgressBar progress_bar) {
        this.progress_bar = progress_bar;
    }
    public boolean isLoading() {
        return loading;
    }

    public void loadImages() {
        loading = true;
        AsyncTask async_task = new AsyncTask() {
            @Override
            protected void onPreExecute() {}
            @Override
            protected Object doInBackground(Object[] params) {
                loadImageBatch();
                return null;
            }
            @Override
            protected void onPostExecute(Object result) {
                loading = false;
                notifyListener("image_loader_1_done");
            }
        };
        async_task.execute();
    }
    private void loadImageBatch() {}

    public void registerListener(ImageLoadedListener image_loader_listener) {
        this.image_loader_listener = image_loader_listener;
    }
    public void notifyListener(String message) {
        if (this.initial_load) {
            this.initial_load = false;
            Log.d("BOTIFYING", "INITIAL LOAD");
            image_loader_listener.onInitialImageLoad(message);
        } else {
            Log.d("BOTIFYING", "REG LOAD");
            image_loader_listener.onImageLoad(message);
        }
    }
}
