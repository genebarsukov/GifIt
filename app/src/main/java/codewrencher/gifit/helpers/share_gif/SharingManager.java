package codewrencher.gifit.helpers.share_gif;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import codewrencher.gifit.R;
import codewrencher.gifit.helpers.async.Animator;
import codewrencher.gifit.helpers.async.interfaces.AnimatorListener;
import codewrencher.gifit.objects.scrollers.SelectListScroller;
import codewrencher.gifit.ui.fragments.BaseFragment;

;

/**
 * Created by Gene on 12/12/2015.
 *
 * This is an Old School sharing class that just creates a menu of all the sharing apps you
 * have available. Then you simply use that app to share. THis will be used for thr lite version
 * of GifIt
 */
public class SharingManager implements AnimatorListener {

    private static final String TAG = "SharingManager";

    private BaseFragment fragment;
    public View share_list_view;
    public FrameLayout share_list_parent;
    private SelectListScroller scroller;
    private LayoutInflater layout_inflater;
    public Boolean sharing_menu_active;


    /**
     * Constructor
     * @param fragment Parent Fragment
     */
    public SharingManager(BaseFragment fragment) {
        this.fragment = fragment; 
        this.layout_inflater = fragment.getActivity().getLayoutInflater();
        this.sharing_menu_active = false;
    }

    /**
     * Main method - opens the sharing menu dropdown
     *
     * Gets the sharing intent
     * Builds all of the individual sharing app views
     * Opens the sharing menu
     * @param file: The image file you want to share
     */
    public void openShareImageDialog(final File file) {
        this.sharing_menu_active = true;

        final Intent sharing_intent = getShareImageIntent(file);

        PackageManager package_manager = fragment.getActivity().getPackageManager();
        List<ResolveInfo> resource_info = package_manager.queryIntentActivities(sharing_intent, 0);

        ArrayList<ListItem> sharing_apps = this.getResolveInfoItemsList(resource_info, package_manager);
        if (sharing_apps.size() == 0) return;

        this.openSharingMenu(sharing_apps, fragment.getFragmentView(), sharing_intent);
    }

    /**
     * Close the main sharing menu
     *
     * Animate the window to slide up
     * Set the active menu to false
     */
    private void closeSharingSlideMenu() {

        Animator animator = new Animator(fragment.getDisplayWidth(), fragment.getDisplayHeight());
        animator.registerListener(this);
        animator.setAnimation(share_list_view, "slide_down");
        animator.animate(share_list_view);
    }

    /**
     * Build the content of the sharing intent, including the file, the image title, and other text
     * @param file: The file you are sharing
     * @return: The sharing intent - it is used to send the sharing request
     */
    protected Intent getShareImageIntent(File file) {
        final Intent sharing_intent = new Intent(Intent.ACTION_SEND);

        sharing_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        sharing_intent.putExtra("type", "sharing");
        sharing_intent.setType("image/*");
        sharing_intent.putExtra(Intent.EXTRA_SUBJECT, "Sharing A Gif With You!");
       // sharing_intent.putExtra(Intent.EXTRA_TEXT, "Get The App: https://play.google.com/store/apps/details?id=codewrencher.gifit");
        sharing_intent.putExtra(Intent.EXTRA_TEXT, "Created with GifIt app");
        sharing_intent.putExtra(android.content.Intent.EXTRA_STREAM, Uri.fromFile(file));

        return sharing_intent;
    }
    /**
     * Build and display the actual sharing window
     *
     * Build the layout, build a list of sharing apps into it, and animate it to drop down
     * @param sharing_apps: List of sharing apps the user can use to share the image
     * @param parent_view: The parent fragment view
     * @param intent: The sharing intent
     */
    public void openSharingMenu(ArrayList<ListItem> sharing_apps, View parent_view, Intent intent) {
        LayoutInflater inflater = LayoutInflater.from(parent_view.getContext());

        share_list_view = inflater.inflate(R.layout.list_share_via, null);
        FrameLayout container = (FrameLayout) parent_view.findViewById(R.id.image_container);
        container.addView(share_list_view);

        share_list_parent = (FrameLayout) share_list_view.getParent();

        ImageButton close = (ImageButton) share_list_view.findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeSharingSlideMenu();
            }
        });

        scroller = new SelectListScroller(R.id.scroll_item, share_list_view);

        for (ListItem sharing_app : sharing_apps) {
            View sharing_app_view = this.createSharingAppView(sharing_app, intent);
            scroller.addItem(sharing_app_view);
        }
        Animator animator = new Animator(fragment.getDisplayWidth(), fragment.getDisplayHeight());
        animator.setAnimation(share_list_view, "drop_up");
        animator.animate(share_list_view);
    }

    /**
     * Create the individual layouts for each of the shaeing apps
     * @param sharing_app: A list item containing several pieces of info about the app
     * @param intent: Sharing intent
     * @return: An individual sharing app view fully built
     */
    public View createSharingAppView(final ListItem sharing_app, final Intent intent) {
        View sharing_app_view = layout_inflater.inflate(R.layout.item_share_app, null);

        TextView name = (TextView) sharing_app_view.findViewById(R.id.name);
        name.setText(sharing_app.name);

        ImageView icon = (ImageView) sharing_app_view.findViewById(R.id.icon);
        icon.setImageDrawable(sharing_app.icon);

        final LinearLayout share_app_item = (LinearLayout) sharing_app_view.findViewById(R.id.share_app_item);
        share_app_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.setClassName(sharing_app.context, sharing_app.package_class_name);
                fragment.fragment_pager.image_shared = true;
                fragment.getActivity().startActivity(intent);
                closeSharingSlideMenu();
            }
        });
        return sharing_app_view;
    }

    /**
     * Get a list of sharing apps and compile their data
     * @param resource_info: A resolve resource list
     * @param package_manager: That is what it is
     * @return: An array of list items representing sharing app info
     */
    private ArrayList<ListItem> getResolveInfoItemsList(List<ResolveInfo> resource_info, PackageManager package_manager) {
        ArrayList<ListItem> sharing_apps = new ArrayList<>();

        for (ResolveInfo resource : resource_info) {
            String context = resource.activityInfo.packageName;
            String package_class_name = resource.activityInfo.name;
            CharSequence label = resource.loadLabel(package_manager);

            if (this.isAllowedSharingApp(label.toString())) {
                Drawable icon = resource.loadIcon(package_manager);
                sharing_apps.add(new ListItem(label.toString(), icon, context, package_class_name));
            }
        }
        return sharing_apps;
    }

    /**
     * Used to do some filtering on the sharing apps, keeping the most relevant ones
     * @param app_name: The name of the app
     * @return: Boolean - whether or not to add the sharing app to our list
     */
    private boolean isAllowedSharingApp(String app_name) {
        app_name = app_name.toLowerCase();
        if (app_name.contains("twitter")) { return true; }
        else if (app_name.contains("insta")) { return true; }
        else if (app_name.contains("google+")) { return true; }
        else if (app_name.contains("hangouts")) { return true; }
        else if (app_name.contains("mail")) { return true; }
        else if (app_name.contains("facebook")) { return true; }
        else if (app_name.contains("messag")) { return true; }
        else {
            Log.d("REJECTED APP: ", app_name);
        }
        return false;
    }

    /**
     * Animator callback - Handles Sharing Menu closeing
     * @param animation_type: The type of animation performed - sliding, bulging, etc
     */
    @Override
    public void onAnimationFinished(String animation_type) {
        if (animation_type.equals("slide_down")) {
            share_list_parent.removeView(share_list_view);

            this.sharing_menu_active = false;
        }
    }
    @Override
    public void onAnimationFinished(String animation_type, View animated_view) {}

    /**
     * A data object that holds several pieces of data for all the sharing apps
     */
    class ListItem {

        public final String name;
        public final Drawable icon;
        public final String context;
        public final String package_class_name;

        /**
         * Constructor
         * @param text: Sharing app text
         * @param icon: Sharing app icon
         * @param context: Sharing app context
         * @param package_class_name: Sharing app package name
         */
        public ListItem(String text, Drawable icon, String context, String package_class_name) {
            this.name = text;
            this.icon = icon;
            this.context = context;
            this.package_class_name = package_class_name;
        }
        @Override
        public String toString() {
            return name;
        }
    }
}
