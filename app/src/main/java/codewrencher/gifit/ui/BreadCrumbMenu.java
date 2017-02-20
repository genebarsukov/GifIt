package codewrencher.gifit.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import codewrencher.gifit.R;

/**
 * Created by Gene on 1/9/2016.
 */
public class BreadCrumbMenu {

    public static void createBreadCrumbMenu(View fragment_view, LayoutInflater inflater, ViewGroup container, int tab_index, int fragment_count) {
        LinearLayout bread_crumbs = (LinearLayout) fragment_view.findViewById(R.id.bread_crumbs);

        for(int tab = 0; tab < fragment_count; tab ++) {
            ImageView bread_crumb = (ImageView) inflater.inflate(R.layout.item_bread_crumb, container, false);
            if (tab == tab_index) {
                bread_crumb.setImageResource(R.mipmap.ball_black_shiny);
            } else {
                bread_crumb.setImageResource(R.mipmap.ball_white_shiny);
            }
            bread_crumbs.addView(bread_crumb);
        }
    }
}
