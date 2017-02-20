package codewrencher.gifit.tools;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import codewrencher.gifit.R;
import codewrencher.gifit.ui.MainActivity;

/**
 * Created by Gene on 9/23/2015.
 */
public class Torch extends Activity {

    View root_view;
    Context context;
    MainActivity activity;
    private PopupWindow status_window;

    public Torch(View root_view) {
        this.root_view = root_view;
        this.context = root_view.getContext();
    }

    public void setActivity( MainActivity activity ) {
        this.activity = activity;
    }

    public static void toast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }
    public void torch(String msg) {
        View layout = this.getLayout();
        TextView text = (TextView) layout.findViewById(R.id.text);
        text.setText(msg);
        displayMsg(layout, Toast.LENGTH_SHORT);
    }
    public void torch(int msg) {
        View layout = this.getLayout();
        TextView text = (TextView) layout.findViewById(R.id.text);
        text.setText(String.valueOf(msg));
        displayMsg(layout, Toast.LENGTH_SHORT);
    }
    public void torch(double msg) {
        View layout = this.getLayout();
        TextView text = (TextView) layout.findViewById(R.id.text);
        text.setText(String.valueOf(msg));
        displayMsg(layout, Toast.LENGTH_SHORT);
    }

    public void longTorch(String msg) {
        View layout = this.getLayout();
        TextView text = (TextView) layout.findViewById(R.id.text);
        text.setText(msg);
        displayMsg(layout, Toast.LENGTH_LONG);
    }
    public void longTorch(int msg) {
        View layout = this.getLayout();
        TextView text = (TextView) layout.findViewById(R.id.text);
        text.setText(String.valueOf(msg));
        displayMsg(layout, Toast.LENGTH_LONG);
    }
    public void longTorch(double msg) {
        View layout = this.getLayout();
        TextView text = (TextView) layout.findViewById(R.id.text);
        text.setText(String.valueOf(msg));
        displayMsg(layout, Toast.LENGTH_LONG);
    }
    public void torchError(String msg) {
        LayoutInflater inflater = LayoutInflater.from(root_view.getContext());
        View layout = inflater.inflate(R.layout.notice_torch_error,
                (ViewGroup) root_view.findViewById(R.id.toast_layout_root));

        TextView text = (TextView) layout.findViewById(R.id.text);
        text.setText(msg);
        text.setTextColor(context.getResources().getColor(R.color.rich_red));

        displayMsg(layout, Toast.LENGTH_LONG);
    }
    public void displayMsg(View layout, int torch_length) {
        Toast toast = new Toast(context);
        toast.setGravity(Gravity.TOP, Gravity.CENTER_HORIZONTAL, 0);
        toast.setDuration(torch_length);
        toast.setView(layout);
        toast.show();
    }
    public void displayMsg(View layout, int torch_length, int gravity, int x_offset, int y_offset ) {
        Toast toast = new Toast(context);
        toast.setGravity( gravity, x_offset, y_offset);
        toast.setDuration(torch_length);
        toast.setView(layout);
        toast.show();
    }
    private View getLayout() {
        LayoutInflater inflater = LayoutInflater.from(root_view.getContext());
        View layout = inflater.inflate(R.layout.notice_torch,
                (ViewGroup) root_view.findViewById(R.id.toast_layout_root));
        return layout;
    }
    private View getStatusLayout() {
        LayoutInflater inflater = LayoutInflater.from(root_view.getContext());
        View layout = inflater.inflate(R.layout.status_torch,
                (ViewGroup) root_view.findViewById(R.id.toast_layout_root));
        return layout;
    }
    public void statusBlink(String msg) {
        View layout = this.getStatusLayout();
        TextView text = (TextView) layout.findViewById(R.id.text);
        text.setText(String.valueOf(msg));
        displayMsg( layout, Toast.LENGTH_LONG, Gravity.TOP, 0, 200 );
    }
    public void statusTorch(String msg) {
        View layout = this.getStatusLayout();
        TextView text = (TextView) layout.findViewById(R.id.text);
        text.setText(msg);
 //       displayMsg(layout, Toast.LENGTH_LONG);

        final float scale = activity.getResources().getDisplayMetrics().density;

        this.statusPopup( layout, this.root_view, false, 0, (int) (75 * scale), activity.getDisplayWidth(), (int) (35 * scale) );
    }

    public View statusPopup(View popup_view, final View anchor_view, Boolean persist, int center_x, int center_y, int popup_width, int popup_height) {
       // LayoutInflater inflater = LayoutInflater.from(anchor_view.getContext());
     //   View popup_view = inflater.inflate(layout_id, null);

        if (center_x == 0) {
            center_x = anchor_view.getWidth() / 2;
        }
        if (center_y == 0) {
            center_y = anchor_view.getHeight() / 2;
        }
        if (popup_width == 0 || popup_height == 0) {
            anchor_view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            popup_width = anchor_view.getWidth();
            popup_height = anchor_view.getHeight();
        }
        status_window = new PopupWindow( popup_view, popup_width, popup_height );

        popup_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                status_window.dismiss();
            }
        });
        status_window.setFocusable(false);
        if ( !persist ) {
            status_window.setBackgroundDrawable( new ColorDrawable() );
        }
        int location[] = new int[2];

        popup_view.getLocationOnScreen(location);
        anchor_view.getLocationOnScreen(location);

        int x_offset = center_x - status_window.getWidth() / 2;
        int y_offset = center_y - status_window.getHeight() / 2;

        if (anchor_view.isShown()) {

            status_window.showAtLocation(this.root_view, Gravity.NO_GRAVITY, location[0] + x_offset, location[1] + y_offset);
        }

        return popup_view;
    }

    public void dismissStatusWindow() {

        if ( this.status_window != null) {
            if ( this.status_window.isShowing() ) {
                this.status_window.dismiss();
            }
        }
    }
}
