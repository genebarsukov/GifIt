package codewrencher.gifit.helpers.files;

import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import codewrencher.gifit.R;
import codewrencher.gifit.helpers.async.interfaces.SharingManagerListener;
import codewrencher.gifit.tools.LayoutBuilder;
import codewrencher.gifit.helpers.async.Animator;
import codewrencher.gifit.helpers.async.interfaces.AnimatorListener;
import codewrencher.gifit.objects.scrollers.SelectListScroller;
import codewrencher.gifit.tools.ToolBox;
import codewrencher.gifit.ui.fragments.BaseFragment;

/**
 * Created by Gene on 12/12/2015.
 */
public class SharingManager implements AnimatorListener {
    private BaseFragment fragment;
    private LayoutBuilder layout_builder;
    public View share_list_view;
    public FrameLayout share_list_parent;
    private SelectListScroller scroller;
    private LayoutInflater layout_inflater;
    private SharingManagerListener helper_listener;

    public SharingManager(BaseFragment fragment) {
        this.fragment = fragment;
        this.layout_builder = new LayoutBuilder(fragment.getActivity().findViewById(R.id.pager), fragment);
        this.layout_inflater = fragment.getActivity().getLayoutInflater();
    }

    public void registerListner(SharingManagerListener helper_listener) {
        this.helper_listener = helper_listener;
    }
    public File takeScreenshot(Bitmap bitmap) {
        try {
            String file_name = "meme_" + ToolBox.getCurrentDateTimeSafeString() + ".jpg";
            String file_path = Environment.getExternalStorageDirectory().toString() + "/DCIM/" + file_name;
            File image_file = new File(file_path);

            FileOutputStream outputStream = new FileOutputStream(image_file);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();
            return image_file;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }
    private void closeSharingSlideMenu() {
        Animator animator = new Animator(fragment.getDisplayWidth(), fragment.getDisplayHeight());
        animator.registerListener(SharingManager.this);
        animator.setAnimation(share_list_view, "slide_down");
        animator.animate(share_list_view);

        helper_listener.onSharingDialogClosed("");
    }
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
                intent.setClassName(sharing_app.context, sharing_app.packageClassName);
                fragment.fragment_pager.image_shared = true;
                fragment.getActivity().startActivity(intent);
                closeSharingSlideMenu();
            }
        });
        return sharing_app_view;
    }
    public void openSharingMenu(ArrayList<ListItem> sharing_apps, View parent_view, Intent intent) {
        share_list_view = layout_builder.addToLayout(R.layout.list_share_via, R.id.image_container, parent_view);
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

        helper_listener.onSharingDialogOpened("");
    }
    public void openShareImageDialog(final File file) {
        final Intent intent = getShareImageIntent(file);

        PackageManager package_manager = fragment.getActivity().getPackageManager();
        List<ResolveInfo> resInfos = package_manager.queryIntentActivities(intent, 0);

        ArrayList<ListItem> sharing_apps = this.getResolveInfoItemsList(resInfos, package_manager);
        if (sharing_apps.size() == 0) return;

        this.openSharingMenu(sharing_apps, fragment.getFragmentView(), intent);
    }
    private ArrayList<ListItem> getResolveInfoItemsList(List<ResolveInfo> resInfos, PackageManager package_manager) {
        ArrayList<ListItem> sharing_apps = new ArrayList<>();

        for (ResolveInfo resInfo : resInfos) {
            String context = resInfo.activityInfo.packageName;
            String packageClassName = resInfo.activityInfo.name;
            CharSequence label = resInfo.loadLabel(package_manager);

            if (this.isAllowedSharingApp(label.toString())) {
                Drawable icon = resInfo.loadIcon(package_manager);
                sharing_apps.add(new ListItem(label.toString(), icon, context, packageClassName));
            }
        }
        return sharing_apps;
    }
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
    // hold parameters for a singular activity item on the list of apps to send to
    class ListItem {
        public final String name;
        public final Drawable icon;
        public final String context;
        public final String packageClassName;
        public ListItem(String text, Drawable icon, String context, String packageClassName) {
            this.name = text;
            this.icon = icon;
            this.context = context;
            this.packageClassName = packageClassName;
        }
        @Override
        public String toString() {
            return name;
        }
    }

    private Intent getShareImageIntent(File file) {
        final ContentValues values = new ContentValues(2);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
        final Uri contentUriFile = fragment.getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        final Intent sharing_intent = new Intent(android.content.Intent.ACTION_SEND);
        sharing_intent.setType("image/jpeg");
        sharing_intent.putExtra(Intent.EXTRA_SUBJECT, "Trumped " + ToolBox.getCurrentDateTime());
        sharing_intent.putExtra(Intent.EXTRA_TEXT, "Got Trumped");
        sharing_intent.putExtra(android.content.Intent.EXTRA_STREAM, contentUriFile);
        sharing_intent.putExtra("type", "sharing");

        return sharing_intent;
    }
    @Override
    public void onAnimationFinished(String animation_type) {
        if (animation_type.equals("slide_down")) {
            share_list_parent.removeView(share_list_view);
        }
    }

    @Override
    public void onAnimationFinished(String animation_type, View animated_view) {

    }
}
