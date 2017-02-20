package codewrencher.gifit.objects.scrollers;

import android.view.View;
import android.widget.LinearLayout;

/**
 * Created by Gene on 1/1/2016.
 */
public class ExpandableScroller extends SelectListScroller {
    private int category_id;
    private int category_index = 0;

    public ExpandableScroller(int category_id, int item_id, View container) {
        this.category_id = category_id;
        this.item_id = item_id;
        this.container = container;
    }
    public void addCategory(View view) {
        LinearLayout category = (LinearLayout) container.findViewById(category_id);
        category.addView(view);
        category_index ++;
    }
}
