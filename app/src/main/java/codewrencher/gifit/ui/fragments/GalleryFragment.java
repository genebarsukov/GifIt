package codewrencher.gifit.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;

import java.util.ArrayList;

import codewrencher.gifit.R;
import codewrencher.gifit.helpers.async.image_loaders.FileImageLoader;
import codewrencher.gifit.helpers.async.interfaces.ImageItemLoadedListener;
import codewrencher.gifit.helpers.async.interfaces.ImageLoadedListener;
import codewrencher.gifit.objects.simple.item.ImageItem;

/**
 * Created by Gene on 12/24/2015.
 */
public class GalleryFragment extends BaseFragment implements ImageLoadedListener, ImageItemLoadedListener {
    public GridView grid_view;
    protected FileImageLoader image_loader;
    protected int start_index = 0;
    protected int screen_size;
    protected ProgressBar progress_bar_top;
    protected ProgressBar progress_bar_bottom;
    public String storage_type;
    public int image_layout;
    public int gallery_layout;
    protected int scroll_position;
    protected String detail_type;
    private boolean just_scrolled;
    public Context context;

    public GalleryFragment() {}

    public static GalleryFragment newInstance(Bundle args) {
        GalleryFragment fragment = new GalleryFragment();
        fragment.setArguments(args);

        return fragment;
    }
    public void setScrollPosition(int scroll_position) {
        this.scroll_position = scroll_position;
        fragment_pager.updateArgs(storage_type, scroll_position, start_index);
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.fragment_layout_id = R.layout.fragment_gallery;
        this.tab_index = 1;

        ArrayList<String> saved_file_paths = null;
        if (getArguments() != null) {
            this.storage_type = getArguments().getString("type");
            this.detail_type = getArguments().getString("detail_type");
            this.scroll_position = getArguments().getInt("scroll_position");
            this.start_index = getArguments().getInt("start_position");
            saved_file_paths = getArguments().getStringArrayList("saved_file_paths");
        } else {
            this.scroll_position = 0;
        }
        this.image_layout = R.layout.item_gallery_image;
        this.gallery_layout = R.layout.fragment_gallery;
        this.context = getContext();

        this.calculateScreenSize();
        just_scrolled = true;
        image_loader = new FileImageLoader(this, screen_size * 2, start_index);
        image_loader.registerListener(this);
        image_loader.setSavedFilePaths(saved_file_paths);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        progress_bar_top = (ProgressBar) fragment_view.findViewById(R.id.progress_bar_top);
        progress_bar_top.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.jobber_main_blue), android.graphics.PorterDuff.Mode.SRC_IN);
        progress_bar_top.setVisibility(View.GONE);

        progress_bar_bottom = (ProgressBar) fragment_view.findViewById(R.id.progress_bar_bottom);
        progress_bar_bottom.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.jobber_main_blue), android.graphics.PorterDuff.Mode.SRC_IN);
        progress_bar_bottom.setVisibility(View.GONE);

        grid_view = (GridView) fragment_view.findViewById(R.id.grid_view);
        grid_view.setColumnWidth(getDisplayWidth() / 3 - 15);
        grid_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                if (position < image_loader.getImageBatch().size()) {
                    ImageItem item = image_loader.getImageBatch().get(position);
                    if (item != null && item.getTitle() != null) {
                        setScrollPosition(position);
                        openDetailsFragment(position, detail_type, item.getTitle(), item.getImagePath());
                    }
                }
            }
        });
        return fragment_view;
    }
    private void calculateScreenSize() {
        int image_size = getDisplayWidth() / 3;
        int visible_image_rows = getDisplayHeight() / image_size;

        this.screen_size = visible_image_rows * 3 + 3;
    }
    @Override
    public void onResume() {
        super.onResume();
        just_scrolled = true;
        this.loadImages();
    }
    private void loadImages() {
        ProgressBar progress_bar_corner = (ProgressBar) fragment_view.findViewById(R.id.progress_bar_corner);
        progress_bar_corner.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.jobber_main_blue), android.graphics.PorterDuff.Mode.SRC_IN);

        image_loader.setProgressBarCorner(progress_bar_corner);
        image_loader.setProgressBar(progress_bar_bottom);
        image_loader.loadImages();

        grid_view.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                just_scrolled = true;
                if (!image_loader.isLoading()) {
                    if (firstVisibleItem > start_index || firstVisibleItem < start_index) {
                        start_index = firstVisibleItem;
                        screen_size = visibleItemCount;
                        if (firstVisibleItem > start_index) {
                            image_loader.setProgressBar(progress_bar_bottom);
                        } else if (firstVisibleItem < start_index) {
                            image_loader.setProgressBar(progress_bar_top);
                        }
                        image_loader.setBatchSize(screen_size * 2);
                        image_loader.setStartIndex(start_index);
                        image_loader.loadImages();
                    }
                }
            }
        });
    }
    @Override
    public void onInitialImageLoad(String message) {
    }

    @Override
    public void onImageLoad(String message) {
    }

    @Override
    public void onImageItemLoad(String message, int item_index) {

    }
}
