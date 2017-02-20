package codewrencher.gifit.objects.simple.item;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import codewrencher.gifit.R;
import codewrencher.gifit.ui.fragments.BaseFragment;

/**
 * Created by Gene on 2/21/2016.
 * _____          _   __          __                  _
 * / ____|        | |  \ \        / /                 | |
 * | |     ___   __| | __\ \  /\  / / __ ___ _ __   ___| |__   ___ _ __
 * | |    / _ \ / _` |/ _ \ \/  \/ / '__/ _ \ '_ \ / __| '_ \ / _ \ '__|
 * | |___| (_) | (_| |  __/\  /\  /| | |  __/ | | | (__| | | |  __/ |
 * \_____\___/ \__,_|\___| \/  \/ |_|  \___|_| |_|\___|_| |_|\___|_|
 */
public class Sprite extends ImageItem {
    protected ImageView image_view;
    protected String group;

    public static final int NONE = 0;
    public static final int DRAG = 1;
    public static final int ZOOM = 2;
    public int mode = NONE;

    public Matrix matrix;
    public Matrix saved_matrix;
    public PointF start;
    public PointF mid;
    public Float old_distance;
    public Float rotation_degrees;
    public Float new_rotation;
    public float[] last_event;
    public Integer img_size;
    public Float dx;
    public Float dy;
    public Float end_x;
    public Float end_y;

    public Sprite( BaseFragment fragment, String group, int img_resource_id, String title ) {
        super( fragment, img_resource_id, title );

        this.group = group;
        this.initWorkingParams();
    }

    public Sprite( BaseFragment fragment, String title ) {
        super( fragment, title );
        this.initWorkingParams();
    }
    protected void initWorkingParams() {
        this.matrix = new Matrix();
        this.saved_matrix = new Matrix();
        this.start = new PointF();
        this.mid = new PointF();
        this.old_distance = 0f;
        this.rotation_degrees = 0f;
        this.new_rotation = 0f;
        this.last_event = null;
        this.dx = 0f;
        this.dy = 0f;
        this.end_x = 0f;
        this.end_y = 0f;
    }
    public String getGroup() {
        return this.group;
    }
    public void setImageView(ImageView image_view) {
        this.image_view = image_view;
    }
    public ImageView getImageView() {
        return this.image_view;
    }

    public void setOnTouchListener() {
        image_view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ImageView glove = (ImageView) fragment.getFragmentView().findViewById(R.id.glove);
                glove.setVisibility(View.INVISIBLE);
                EditText add_text_view = (EditText) fragment.getFragmentView().findViewById(R.id.add_text);
                add_text_view.setVisibility(View.INVISIBLE);

                fragment.lockPaging();

                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        if (event.getPointerCount() < 2) {
                            handleActionDown(event);
                        }
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        if (event.getPointerCount() == 2) {
                            handleActionPointerDown(event);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (mode == DRAG) {
                            handleActionUp(event);
                        }
                    case MotionEvent.ACTION_POINTER_UP:
                        handleActionPointerUp(event);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (mode == DRAG) {
                            handleTranslate(event);
                        } else if (mode == ZOOM) {
                            handleZoom(event);
                            if (last_event != null && event.getPointerCount() == 2) {
                                handleRotate(event);
                            }
                        }
                        break;
                }

                image_view.setImageMatrix(matrix);
                return true;
            }
        });
    }
    // on touch actions
    public void handleActionDown(MotionEvent event) {
        saved_matrix.set(matrix);
        PointF temp_start = new PointF();
        temp_start.set(event.getX(), event.getY());
        start = temp_start;
        mode = DRAG;
        last_event = null;
    }
    public void handleActionPointerDown(MotionEvent event) {
        old_distance = spacing(event);
        if (old_distance > 10f) {

            saved_matrix.set(matrix);
            midPoint(mid, event);
            mode = ZOOM;
        }
        float[] temp_last_event = new float[4];
        temp_last_event[0] = event.getX(0);
        temp_last_event[1] = event.getX(1);
        temp_last_event[2] = event.getY(0);
        temp_last_event[3] = event.getY(1);

        last_event = temp_last_event;
        rotation_degrees = rotation(event);
    }
    public void handleActionUp(MotionEvent event) {
        end_x = (end_x + event.getX() - start.x);
        end_y = (end_y + event.getY() - start.y);
    }
    public void handleActionPointerUp(MotionEvent event) {
        mode = NONE;
        last_event = null;
    }
    // movement: translate zoom rotate
    public void handleTranslate(MotionEvent event) {
        matrix.set(saved_matrix);

        matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);

        dx = event.getX() - start.x;
        dy = event.getY() - start.y;
    }
    public void handleZoom(MotionEvent event) {
        float new_distance = spacing(event);

        if (new_distance > 10f) {
            matrix.set(saved_matrix);
            float scale = new_distance / this.old_distance;

            matrix.postScale(scale, scale, mid.x, mid.y);
        }
    }
    public void handleRotate(MotionEvent event) {
        midPoint(mid, event);
        new_rotation = rotation(event);
        float r = new_rotation - rotation_degrees;
        float[] values = new float[9];
        matrix.getValues(values);
        matrix.postRotate(r, mid.x, mid.y);
    }
    // calculations: spacing, midpoint rotation
    public float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }
    public void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        x = x  - img_width - dx;
        y = y - img_height - dy;

        point.set((x - img_width) / 2, (y - img_height) / 2);
    }
    public float rotation(MotionEvent event) {
        double delta_x = (event.getX(0) - event.getX(1));
        double delta_y = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);
    }

    public void flipHorizontally() {
        float x = img_width / 2  + end_x;
        float y = img_height / 2 + dy;

        matrix.postScale(-1f, 1f, x, y);

        saved_matrix .set(matrix);
        image_view.setImageMatrix(saved_matrix);
    }
}
