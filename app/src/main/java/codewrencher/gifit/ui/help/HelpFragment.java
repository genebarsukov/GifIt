package codewrencher.gifit.ui.help;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import codewrencher.gifit.R;
import codewrencher.gifit.ui.BreadCrumbMenu;

public class HelpFragment extends Fragment {
    private static final String TAB_INDEX = "tab_index";
    private int tab_index;
    private int fragment_count;
    private HelpActivity help_activity;

    public static HelpFragment newInstance(int tab_index) {
        HelpFragment fragment = new HelpFragment();
        Bundle args = new Bundle();
        args.putInt(TAB_INDEX, tab_index);
        fragment.setArguments(args);
        return fragment;
    }

    public HelpFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tab_index = getArguments().getInt(TAB_INDEX);
        }
        this.help_activity = (HelpActivity) getActivity();
        fragment_count = help_activity.fragment_pager.getCount();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View fragment_view = inflater.inflate(R.layout.fragment_help, container, false);

        TextView page_index_view = (TextView) fragment_view.findViewById(R.id.page_index);
        TextView page_count_view = (TextView) fragment_view.findViewById(R.id.page_count);
        TextView help_caption_view = (TextView) fragment_view.findViewById(R.id.help_caption);
        ImageView help_image_view = (ImageView) fragment_view.findViewById(R.id.help_image);
        TextView help_text_view = (TextView) fragment_view.findViewById(R.id.help_text);
        ImageView left_arrow = (ImageView) fragment_view.findViewById(R.id.left_arrow);
        ImageView right_arrow = (ImageView) fragment_view.findViewById(R.id.right_arrow);
        Button got_it = (Button) fragment_view.findViewById(R.id.got_it);

        String[] help_caption_list = getResources().getStringArray(R.array.help_captions);
        String[] help_image_list = getResources().getStringArray(R.array.help_images);
        String[] help_text_list = getResources().getStringArray(R.array.help_text);

        String page_index = String.valueOf(tab_index + 1);
        String page_count = String.valueOf(fragment_count);
        String help_caption = help_caption_list[tab_index];
        String help_image_src = help_image_list[tab_index];
        String help_text = help_text_list[tab_index];

        page_index_view.setText(page_index);
        page_count_view.setText(page_count);
        help_caption_view.setText(help_caption);
        help_image_view.setImageResource(help_activity.getResourceId(help_image_src, "drawable"));
        help_text_view.setText(help_text);
        left_arrow.setImageResource(R.mipmap.arrow_single_left_pressed);
        right_arrow.setImageResource(R.mipmap.arrow_single_right_pressed);

        // show correct arrows based on tab position
        if (tab_index == 0) {
            left_arrow.setVisibility(View.INVISIBLE);
        } else if (tab_index >= (fragment_count - 1)) {
            right_arrow.setVisibility(View.INVISIBLE);
        }
        // show correct button text based on tab position
        if (tab_index < (fragment_count - 1)) {
            got_it.setText("Got It");
        } else {
            got_it.setText("Done");
        }
        // show next tab or finish help activity
        got_it.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (tab_index < (fragment_count - 1)) {
                    help_activity.switchTab(tab_index + 1);
                } else {
                    help_activity.finish();
                }
            }
        });
        help_image_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (tab_index < (fragment_count - 1)) {
                    help_activity.switchTab(tab_index + 1);
                } else {
                    help_activity.finish();
                }
            }
        });
        // set up bread crumbs tab position graphics
        BreadCrumbMenu.createBreadCrumbMenu(fragment_view, inflater, container, tab_index, fragment_count);

        setCloseListener(fragment_view);
        return fragment_view;
    }
    private void setCloseListener(final View parent_view) {
        ImageButton close = (ImageButton) parent_view.findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                help_activity.finish();
            }
        });
    }
}
