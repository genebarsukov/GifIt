package codewrencher.gifit.helpers.async.image_loaders;

import android.os.AsyncTask;
import android.view.View;

import codewrencher.gifit.objects.complex.gif_chain.GifChain;
import codewrencher.gifit.objects.simple.item.VideoFrame;
import codewrencher.gifit.ui.fragments.BaseFragment;

/**
 * Created by Gene on 3/12/2016.
 * _____          _   __          __                  _
 * / ____|        | |  \ \        / /                 | |
 * | |     ___   __| | __\ \  /\  / / __ ___ _ __   ___| |__   ___ _ __
 * | |    / _ \ / _` |/ _ \ \/  \/ / '__/ _ \ '_ \ / __| '_ \ / _ \ '__|
 * | |___| (_) | (_| |  __/\  /\  /| | |  __/ | | | (__| | | |  __/ |
 * \_____\___/ \__,_|\___| \/  \/ |_|  \___|_| |_|\___|_| |_|\___|_|
 */
public class VideoImageLoader extends ImageLoader {

    private String video_file_path;
    private int frame_count;
    public static int MIN_THUMBNAIL_DIVIDER = 4;
    public static int MAX_THUMBNAIL_DIVIDER = 10;
    private GifChain gif_chain;

    public VideoImageLoader(BaseFragment fragment, int scroll_item_id, String video_file_path, int frame_count, GifChain gif_chain ) {
        super(fragment);

        this.scroll_item_id = scroll_item_id;
        this.video_file_path = video_file_path;
        this.frame_count = frame_count;
        this.gif_chain = gif_chain;
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
                progress_bar.setVisibility(View.GONE);

                if(scroll_container_type == "scroll") {
            //        ((GifSpaceFragment) fragment).createImageScrollView();
                }
                notifyListener("video_frames_loaded");
            }
        };
        async_task.execute();
    }
    private void loadImageBatch() {
        try {
            for(int i = 0; i < frame_count; i++) {
                image_items.add( i, new VideoFrame( fragment, video_file_path, i, "img " + i ) );
            }

        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }

    }
}
