package codewrencher.gifit.objects.scrollers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import codewrencher.gifit.R;
import codewrencher.gifit.ui.fragments.BaseFragment;

/**
 * Created by Gene on 3/6/2016.
 * _____          _   __          __                  _
 * / ____|        | |  \ \        / /                 | |
 * | |     ___   __| | __\ \  /\  / / __ ___ _ __   ___| |__   ___ _ __
 * | |    / _ \ / _` |/ _ \ \/  \/ / '__/ _ \ '_ \ / __| '_ \ / _ \ '__|
 * | |___| (_) | (_| |  __/\  /\  /| | |  __/ | | | (__| | | |  __/ |
 * \_____\___/ \__,_|\___| \/  \/ |_|  \___|_| |_|\___|_| |_|\___|_|
 */
public class ValueScroller {
    protected BaseFragment fragment;
    protected View root_view;
    protected LayoutInflater layout_inflater;
    protected ArrayList<Integer> value_list;
    protected ListView scroll_item;          // the layout inside the scroll view

    protected int scroll_container_id;           // the scroll view id
    protected int scroll_item_id;                // id of the layout inside the scroll view
    protected int item_layout_id;                // the floating layout of the item we are going to inflate
    protected int item_img_resource_id;          // the image resource to add to the item layout, if any


    public ValueScroller(BaseFragment fragment, View root_view, int scroll_container_id, int item_layout_id) {
        this.fragment = fragment;
        this.root_view = root_view;
        this.scroll_container_id = scroll_container_id;
        this.item_layout_id = item_layout_id;
        this.layout_inflater = fragment.getSavedActivity().getLayoutInflater();
        this.scroll_item = (ListView) root_view.findViewById(scroll_container_id);
        this.value_list = new ArrayList<>();
    }
    public void setImageResourceId(int item_img_resource_id) {
        this.item_img_resource_id = item_img_resource_id;
    }
    // add items -----------------------------------------------------------------------------------
    public void addValue(int value) {
        View item_view = layout_inflater.inflate(item_layout_id, (ViewGroup) root_view, false);
        TextView value_view = (TextView) item_view.findViewById(R.id.value);
        value_view.setText(String.valueOf(value));

        if (this.item_img_resource_id != 0) {
            Bitmap img_bitmap = BitmapFactory.decodeResource(fragment.getSavedActivity().getResources(), item_img_resource_id);

            ImageView image_view = (ImageView) item_view.findViewById(R.id.image);
            image_view.setImageBitmap(img_bitmap);

        }
        item_view.setTag( String.valueOf(value_list.size()) );
        value_list.add(value);
        scroll_item.addView(item_view);
    }
    // get items -----------------------------------------------------------------------------------
    public Integer getValueByIndex(int index) {
        return value_list.get(index);
    }
    public View getViewByIndex(int index) {
        return scroll_item.getChildAt(index);
    }
    public View getCurrentItem() {
        return scroll_item.getFocusedChild();
    }
    // remove items --------------------------------------------------------------------------------
    public void removeItemByIndex(int index) {
        View view_to_remove = scroll_item.getChildAt(index);

        scroll_item.removeView(view_to_remove);
        value_list.remove(index);
    }
    public void removeItemByView(View view_to_remove) {
        String item_tag = (String) view_to_remove.getTag();
        int item_index = 0;
        boolean index_set = false;

        if (item_tag != null) {
            item_index = Integer.parseInt(item_tag);
            index_set = true;
        }
        if (index_set) {
            value_list.remove(item_index);
        }
        scroll_item.removeView(view_to_remove);
    }
}
