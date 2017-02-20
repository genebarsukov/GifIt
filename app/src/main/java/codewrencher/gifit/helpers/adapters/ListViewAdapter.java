package codewrencher.gifit.helpers.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import codewrencher.gifit.R;
import codewrencher.gifit.ui.fragments.BaseFragment;

/**
 * Created by Gene on 3/7/2016.
 * _____          _   __          __                  _
 * / ____|        | |  \ \        / /                 | |
 * | |     ___   __| | __\ \  /\  / / __ ___ _ __   ___| |__   ___ _ __
 * | |    / _ \ / _` |/ _ \ \/  \/ / '__/ _ \ '_ \ / __| '_ \ / _ \ '__|
 * | |___| (_) | (_| |  __/\  /\  /| | |  __/ | | | (__| | | |  __/ |
 * \_____\___/ \__,_|\___| \/  \/ |_|  \___|_| |_|\___|_| |_|\___|_|
 */
public class ListViewAdapter extends ArrayAdapter<Double> {

    protected BaseFragment fragment;
    protected ArrayList<Double> value_list;

    protected int item_layout_id;
    protected int item_img_resource_id;
    protected String item_type;

    public ListViewAdapter(BaseFragment fragment, int item_layout_id, ArrayList<Double> value_list) {
        super(fragment.getContext(), item_layout_id, value_list);

        this.fragment = fragment;
        this.item_layout_id = item_layout_id;
        this.value_list = value_list;
    }
    public void setImageResourceId(int item_img_resource_id) {
        this.item_img_resource_id = item_img_resource_id;
    }
    public void setItemType(String item_type) {
        this.item_type = item_type;
    }
    public Double getValueByIndex(int index) {
        return value_list.get(index);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layout_inflater = fragment.getSavedActivity().getLayoutInflater();

        View item_view = layout_inflater.inflate(this.item_layout_id, parent, false);
       // ViewGroup.LayoutParams view_params = item_view.getLayoutParams();
       // view_params.width = 120;
       // view_params.height = 120;
      //  item_view.setLayoutParams(view_params);




        String item_text = "";
        if (this.item_type.equals("frame_rate")) {
            item_text = String.format("%.1f", value_list.get(position));
        } else if (this.item_type.equals("frame_count")) {
            item_text = String.format("%.0f", value_list.get(position));
        }

        TextView value_view = (TextView) item_view.findViewById(R.id.value);
        value_view.setText( item_text );

        if (this.item_img_resource_id != 0) {
            Bitmap img_bitmap = BitmapFactory.decodeResource(fragment.getSavedActivity().getResources(), item_img_resource_id);

            ImageView image_view = (ImageView) item_view.findViewById(R.id.image);
            image_view.setImageBitmap(img_bitmap);

        }
        item_view.setTag(String.valueOf(value_list.size()));

        return item_view;
    }
}
