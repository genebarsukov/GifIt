package codewrencher.gifit.helpers.async.image_loaders;

import android.os.AsyncTask;
import android.view.View;
import android.widget.GridView;
import android.widget.ProgressBar;

import java.util.ArrayList;

import codewrencher.gifit.R;
import codewrencher.gifit.helpers.adapters.GridViewAdapter;
import codewrencher.gifit.helpers.async.image_loaders.ImageLoader;
import codewrencher.gifit.objects.simple.item.ImageItem;
import codewrencher.gifit.ui.fragments.BaseFragment;

/**
 * Created by Gene on 2/6/2016.
 * _____          _   __          __                  _
 * / ____|        | |  \ \        / /                 | |
 * | |     ___   __| | __\ \  /\  / / __ ___ _ __   ___| |__   ___ _ __
 * | |    / _ \ / _` |/ _ \ \/  \/ / '__/ _ \ '_ \ / __| '_ \ / _ \ '__|
 * | |___| (_) | (_| |  __/\  /\  /| | |  __/ | | | (__| | | |  __/ |
 * \_____\___/ \__,_|\___| \/  \/ |_|  \___|_| |_|\___|_| |_|\___|_|
 */
public class ResourceImageLoader extends ImageLoader {
    private GridViewAdapter grid_adapter;
    private GridView grid_view;

    public ResourceImageLoader(BaseFragment fragment, ArrayList image_items, GridView grid_view, ProgressBar progress_bar) {
        super(fragment, image_items);
        super.setProgressBar(progress_bar);
        this.grid_view = grid_view;
    }
    // MAIN ----------------------------------------------------------------------------------------
    public void loadImages() {
        loading = true;
        AsyncTask async_task = new AsyncTask() {
            @Override
            protected void onPreExecute() {
                if (initial_load) {
                    progress_bar.setVisibility(View.VISIBLE);
                }
            }
            @Override
            protected Object doInBackground(Object[] params) {
                loadImageBatch();
                return null;
            }
            @Override
            protected void onPostExecute(Object result) {
                if (initial_load) {
                    grid_adapter = new GridViewAdapter(fragment, R.layout.item_gallery_image, image_items);
                    grid_view.setAdapter(grid_adapter);
                    grid_view.setSelection(0);
                } else {
                    grid_adapter.setImageData(image_items);
                    grid_adapter.notifyDataSetChanged();
                }
                loading = false;
                progress_bar.setVisibility(View.GONE);
                notifyListener("gif_images_loaded");
            }
        };
        async_task.execute();
    }
    private void loadImageBatch() {
        for (Object image_item : this.image_items) {
            ((ImageItem) image_item).getThumbnail();
        }
    }
}
