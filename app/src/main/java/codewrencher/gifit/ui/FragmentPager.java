package codewrencher.gifit.ui;

import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import codewrencher.gifit.ui.fragments.BaseFragment;
import codewrencher.gifit.ui.fragments.CameraFragment;
import codewrencher.gifit.ui.fragments.GalleryFragment;
import codewrencher.gifit.ui.fragments.GifSpaceFragment;

/**
 * Created by Gene on 12/24/2015.
 */
public class FragmentPager extends FragmentStatePagerAdapter {
    private ArrayList<Fragment> fragment_list;
    private ArrayList<String> fragment_tag_list;
    private LinkedHashMap<String, Bundle> fragment_args;
    private BaseFragment current_fragment;

    public Camera camera;
    public boolean image_shared;
    public boolean sharing_same_meme;
    private FragmentManager fragment_manager;
    private Bitmap hair_image;
    private Bitmap stamp_image;
    private ArrayList<String> ordered_meme_objects;
    private int image_width;
    private int image_height;
    private LinkedHashMap <String, LinkedHashMap <String, String>> fragment_attributes;
    private int last_tab_index;

    public FragmentPager(FragmentManager fragment_manager) {
        super(fragment_manager);
        this.fragment_manager = fragment_manager;
        this.fragment_list = new ArrayList<>();
        this.fragment_tag_list = new ArrayList<>();
        this.fragment_args = new LinkedHashMap<>();
        this.ordered_meme_objects = new ArrayList<>();
        this.fragment_attributes = new LinkedHashMap<>();
        this.last_tab_index = 0;
    }
    public int getLastTabIndex() {
        return this.last_tab_index;
    }
    public BaseFragment getCurrentFragment() {
        return current_fragment;
    }
    public void setFragmentAttribute(String fragment_tag, String attribute_key, String attribute_value) {
        if (this.fragment_attributes.get( fragment_tag ) == null) {
            this.fragment_attributes.put( fragment_tag, new LinkedHashMap<String, String>() );
        }
        this.fragment_attributes.get(fragment_tag).put(attribute_key, attribute_value);
    }
    public String getFragmentAttribute(String fragment_tag, String attribute_key) {
        if (this.fragment_attributes.get( fragment_tag ) == null) {
            this.fragment_attributes.put( fragment_tag, new LinkedHashMap<String, String>() );

            return null;
        }
        return this.fragment_attributes.get(fragment_tag).get(attribute_key);
    }
    public void setImageWidth(int image_width) {
        this.image_width = image_width;
    }
    public int getImageWidth() {
        return this.image_width;
    }
    public void setImageHeight(int image_height) {
        this.image_height = image_height;
    }
    public int getImageHeight() {
        return this.image_height;
    }
    public void saveHairImage(Bitmap hair_image) {
        this.hair_image = hair_image;
    }
    public void saveStampImage(Bitmap stamp_image) {
        this.stamp_image = stamp_image;
    }
    public Bitmap getHairCanvasImage() {
        return this.hair_image;
    }
    public Bitmap getStampCanvasImage() {
        return this.stamp_image;
    }
    public void setLastMemeType(String last_meme_type) {
        this.ordered_meme_objects.add(0, last_meme_type);
    }
    public ArrayList<String> getOrderedMemeObjects() {
        return this.ordered_meme_objects;
    }
    public void resetOrderedMemeObjects() {
        this.ordered_meme_objects.clear();
    }
    public Bundle getArgs(String fragment_tag) {
        return fragment_args.get(fragment_tag);
    }
    public void clearArgs(String fragment_tag) {
        fragment_args.put(fragment_tag, null);
    }
    public void updateArgs(String fragment_tag, String text_1, String text_2, boolean meme_created) {
        Bundle args = fragment_args.get(fragment_tag);
        args.putString("quote_line_1", text_1);
        args.putString("quote_line_2", text_2);
        args.putBoolean("meme_created", meme_created);
        fragment_args.put(fragment_tag, args);
    }
    public void updateArgs(String fragment_tag, int scroll_position, int start_position) {
        Bundle args = fragment_args.get(fragment_tag);
        if (args == null) args = new Bundle();
        args.putInt("scroll_position", scroll_position);
        args.putInt("start_position", start_position);
        fragment_args.put(fragment_tag, args);
    }
    public void updateArgs(String fragment_tag, String variable_tag, int variable) {
        Bundle args = fragment_args.get(fragment_tag);
        if (args == null) args = new Bundle();
        args.putInt(variable_tag, variable);
        fragment_args.put(fragment_tag, args);
    }
    public void updateArgs(String fragment_tag, String variable_tag, String variable) {
        Bundle args = fragment_args.get(fragment_tag);
        if (args == null) args = new Bundle();
        args.putString(variable_tag, variable);
        fragment_args.put(fragment_tag, args);
    }
    public void updateArgs(String fragment_tag, ArrayList<String> sorted_file_paths) {
        Bundle args = fragment_args.get(fragment_tag);
        if (args == null) args = new Bundle();
        args.putStringArrayList("saved_file_paths", sorted_file_paths);
        fragment_args.put(fragment_tag, args);
    }
    public void addItem(String fragment_type, Bundle args) {
        switch (fragment_type) {

            case "camera":
                if (fragment_tag_list.size() >  0) fragment_tag_list.remove(0);
                fragment_tag_list.add(0, "camera");
                fragment_args.put("camera", args);
                break;
            case "meme_gallery":
                if (fragment_tag_list.size() >  1) fragment_tag_list.remove(1);
                fragment_tag_list.add(1, "meme_gallery");
                fragment_args.put("meme_gallery", args);
                break;
            case "workspace_gallery":
                if (fragment_tag_list.size() >  1) fragment_tag_list.remove(1);
                fragment_tag_list.add(1, "workspace_gallery");
                fragment_args.put("workspace_gallery", args);
                break;
            case "workspace":
                if (fragment_tag_list.size() >  2) fragment_tag_list.remove(2);
                args.putInt("total_image_rotation", 0);
                fragment_tag_list.add(2, "workspace");
                fragment_args.put("workspace", args);
                break;
            case "gif_space":
                Log.d("FRAGMENT PAGER", "SETTING GIF SPACE PARAMS");
                if (fragment_tag_list.size() >  1) fragment_tag_list.remove(1);
                args.putInt("total_image_rotation", 0);
                fragment_tag_list.add(1, "gif_space");
                if (fragment_args.get("gif_space") == null) {
                    fragment_args.put("gif_space", args);
                }
                break;
            case "meme":
                if (fragment_tag_list.size() >  2) fragment_tag_list.remove(2);
                fragment_tag_list.add(2, "meme");
                fragment_args.put("meme", args);
            case "downloaded_gif":
                int placement_index = 1;

                if (fragment_tag_list.size() >  2) {    // a received gif has already been loaded

                    placement_index = 2;
                    fragment_tag_list.remove(placement_index);
                } else if (fragment_tag_list.size() >  1) { // the gif space or another received gif has been loaded
                    if (this.getCurrentFragment() instanceof GifSpaceFragment) {
                        placement_index = 2;
                    } else {
                        placement_index = 1;
                        fragment_tag_list.remove(placement_index);
                    }
                } else if (fragment_tag_list.size() >  0) { // only the camera has been loaded
                    placement_index = 1;
                }
                fragment_tag_list.add(placement_index, "downloaded_gif");

                args.putInt("tab_index", placement_index);

                this.last_tab_index = placement_index;
                fragment_args.put("downloaded_gif", args);
            default:
                break;
        }
    }
    public void removeCurrentFragment(Fragment fragment, int index) {
        if (fragment_tag_list.size() >  index) fragment_tag_list.remove(index);
        fragment_manager.beginTransaction().remove(fragment).commit();
        this.notifyDataSetChanged();
    }
    public void removeFragmentByIndex(int index) {
        if (fragment_tag_list.size() >  1) {
            fragment_manager.beginTransaction().remove( getItem(index) ).commit();
            fragment_tag_list.remove(index);
        }
        this.notifyDataSetChanged();
    }
    public void replaceFragment(int index_to_replace, Fragment fragment, String tag) {
        fragment_manager.beginTransaction().remove( getItem(index_to_replace) ).commit();
        fragment_manager.beginTransaction().add(fragment,tag).commit();
        this.notifyDataSetChanged();
    }
    @Override
    public Fragment getItem(int position) {
        BaseFragment fragment;

        switch (fragment_tag_list.get(position)) {

            case "camera":
                Log.d("GETTING CAMERA", "--------------------");
                fragment =  CameraFragment.newInstance(fragment_args.get("camera"));
                fragment.setFragmentPager(this);
                break;
            case "meme_gallery":
                fragment =  GalleryFragment.newInstance(fragment_args.get("meme_gallery"));
                fragment.setFragmentPager(this);

                break;
            case "workspace_gallery":
                fragment =  GalleryFragment.newInstance(fragment_args.get("workspace_gallery"));
                fragment.setFragmentPager(this);
                break;
            case "gif_space":
                fragment =  GifSpaceFragment.newInstance(fragment_args.get("gif_space"));
                fragment.setFragmentPager(this);
                break;
            default:
                fragment =  new CameraFragment();
                fragment.setFragmentPager(this);

                break;
        }
        this.current_fragment = fragment;

        return fragment;
    }
    @Override
    public int getCount() {
        return fragment_tag_list.size();
    }
}