package codewrencher.gifit.objects.simple.item;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.util.ArrayList;

import codewrencher.gifit.R;
import codewrencher.gifit.ui.fragments.BaseFragment;

/**
 * Created by Gene on 3/13/2016.
 * _____          _   __          __                  _
 * / ____|        | |  \ \        / /                 | |
 * | |     ___   __| | __\ \  /\  / / __ ___ _ __   ___| |__   ___ _ __
 * | |    / _ \ / _` |/ _ \ \/  \/ / '__/ _ \ '_ \ / __| '_ \ / _ \ '__|
 * | |___| (_) | (_| |  __/\  /\  /| | |  __/ | | | (__| | | |  __/ |
 * \_____\___/ \__,_|\___| \/  \/ |_|  \___|_| |_|\___|_| |_|\___|_|
 */
public class TextSprite extends Sprite {
    private String text;
    private static final int TEXT_SIZE = 50;
    private static final int TOP_TEXT_MARGIN = 10;

    /**
     * Constructor - Initialize parent fragment and text
     * @param fragment Parent fragment
     * @param text Text to make into a sprite
     */
    public TextSprite( BaseFragment fragment, String text ) {
        super( fragment, text );

        this.text = text;
        this.img_width = fragment.damen.findGifViewWidth();
        this.img_height = fragment.damen.findGifViewHeight();
    }

    public void setText(String text) {
        this.text = text;
    }

    public Bitmap getImage() {
        if (this.image == null) {
            if (this.text != null) {
                this.createImageFromText();
            }
        }
        return this.image;
    }

    /**
     * Creates a fresh Text image
     */
    public void createImage() {
        if (this.text != null) {
            this.createImageFromText();
        }
    }
    private void createImageFromText() {
        if (this.img_thumbnail_layout != null) {
            // set the item thumbnail view if we want it to load the images async and set the view by itself
            this.loadImageFromTextAsync();
        } else {
            // load the thumbnail synchronously. the waiting and the view setting is done elsewhere in the parent
            this.loadImageFromText();
        }
    }
    public void loadImageFromTextAsync() {
        this.setThumbnailViewSize(img_full_size_layout);
        final ProgressBar progress_bar = this.configureProgressBar();

        final ImageView item_image_view = (ImageView) img_full_size_layout.findViewById(R.id.image);

        AsyncTask async_task = new AsyncTask() {
            @Override
            protected void onPreExecute() {
                progress_bar.setVisibility(View.VISIBLE);
            }
            @Override
            protected Object doInBackground(Object[] params) {
                loadImageFromTextAsync();
                return null;
            }
            @Override
            protected void onPostExecute(Object result) {
                if (thumbnail != null) {
                    item_image_view.setImageBitmap(thumbnail);
                }
                progress_bar.setVisibility(View.GONE);

                notifyImageItemLoadedListener();
            }
        };
        async_task.execute();
    }

    public void loadImageFromText() {
        Paint paint = new Paint();

        Bitmap text_bitmap = Bitmap.createBitmap(this.img_width, this.img_height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(text_bitmap);

        Rect bounds = new Rect();

        final float scale = fragment.context.getResources().getDisplayMetrics().density;
        int screen_text_size = (int) (TEXT_SIZE * scale + 0.5f);

        SharedPreferences shared_preferences = PreferenceManager.getDefaultSharedPreferences(fragment.getActivity());
        String font_file_path = shared_preferences.getString("font_file_path", "fonts/impact.ttf");

        Typeface text_font =  Typeface.createFromAsset(fragment.getActivity().getAssets(), font_file_path);

        paint.setTypeface(text_font);

        paint.setTextSize(screen_text_size);
        paint.getTextBounds("M", 0, 1, bounds);

        //ArrayList<String> text_lines = this.lineBreakString(this.text, this.img_width, bounds.width());
        ArrayList<String> text_lines = new ArrayList<>();
        text_lines.add(this.text);

        this.createText(canvas, paint, screen_text_size, bounds, text_lines);
        this.image = text_bitmap;
    }

    private void createText(Canvas canvas, Paint paint, int text_size, Rect bounds, ArrayList<String> text_lines) {
        paint.setColor(Color.TRANSPARENT);
        canvas.drawPaint(paint);
        paint.setColor(Color.BLACK);

        this.formatText(canvas, paint, text_size, bounds, text_lines);
    }

    private void formatText(Canvas canvas, Paint paint, int text_size, Rect bounds, ArrayList<String> text_lines) {
        int original_x = 0;
        int text_offset = 3;
        int y_offset = 0;

        if (text_size >= 50) {
            text_offset = 3;
        } else if (text_size < 50 && text_size >= 30) {
            text_offset = 2;
        } else if (text_size < 30) {
            text_offset = 1;
        }

        float scale = fragment.context.getResources().getDisplayMetrics().density;
  //      y_offset = (int) (y_offset * scale + 0.5f);

        int original_y1 = y_offset + bounds.height();

        // top left -----------------------------------------------------
        int x = original_x;
        int y = original_y1;
        this.positionTextOnCanvas(canvas, paint, text_lines, x, y);
        //top right -----------------------------------------------------
        x = original_x + 2 * text_offset;
        y = original_y1;
        this.positionTextOnCanvas(canvas, paint, text_lines, x, y);
        //bottom right -----------------------------------------------------
        x = original_x + 2 * text_offset;
        y = original_y1 + 2 * text_offset;
        this.positionTextOnCanvas(canvas, paint, text_lines, x, y);
        //bottom left -----------------------------------------------------
        x = original_x;
        y = original_y1 + 2 * text_offset;
        this.positionTextOnCanvas(canvas, paint, text_lines, x, y);
        //center white -----------------------------------------------------
        paint.setColor(Color.WHITE);
        x = original_x + text_offset;
        y = original_y1 + text_offset;
        this.positionTextOnCanvas(canvas, paint, text_lines, x, y);
    }

    /**
     * Position Multiple Lines
     * @param canvas
     * @param paint
     * @param string_lines
     * @param x
     * @param y
     */
    private void positionTextOnCanvas(Canvas canvas, Paint paint, ArrayList<String> string_lines, int x, int y) {
        for(String line : string_lines) {
            Rect bounds = new Rect();
            paint.getTextBounds(line, 0, line.length(), bounds);

            x += (img_width / 2 -  bounds.width() / 2);
            y += bounds.height() + TOP_TEXT_MARGIN;

            canvas.drawText(line, x, y, paint);

        }
    }
    private ArrayList<String> lineBreakString(String string, int max_pixels, int text_size) {
        int font_adjuster = 0;
        SharedPreferences shared_preferences = PreferenceManager.getDefaultSharedPreferences(fragment.getActivity());
        String font_file_path = shared_preferences.getString("font_file_path", "fonts/impact.ttf");
        // some bolder fonts are wider ang go slightly off the page so they require a minor max character adjustment
        switch (font_file_path) {
            case "fonts/arial_black.ttf":
                font_adjuster = -2;
                break;
            case "fonts/impact.ttf":
                font_adjuster = 1;
                break;
            case "fonts/arial_bold.ttf":
                font_adjuster = 0;
                break;
            case "fonts/verdana_bold.ttf":
                font_adjuster = -2;
                break;
            case "fonts/arial.ttf":
                font_adjuster = 0;
                break;
            case "fonts/franklin_gothic_medium.ttf":
                font_adjuster = 0;
                break;
            case "fonts/sans_serif_regular.ttf":
                font_adjuster = 0;
                break;
            case "fonts/times_new_roman_bold.ttf":
                font_adjuster = 0;
                break;
        }

        int max_chars = (int) ((double) max_pixels / ((double) text_size)) + font_adjuster;
        String[] words = string.split("[ ]+");

        int line_char_count = 0;
        String line = "";
        ArrayList<String> string_lines = new ArrayList();
        int word_count = 0;
        for (String word : words) {
            word_count ++;
            if (line_char_count > 0) {
                line += " ";
                line_char_count ++;
            }

            if (line_char_count >= max_chars) {
                string_lines.add(line);
                line = word;
                line_char_count = word.length();
                if (word_count >= words.length) {
                    string_lines.add(line);
                }
            } else if (word_count >= words.length) {
                line_char_count += word.length();
                line += word;
                string_lines.add(line);
            } else {
                line_char_count += word.length();
                line += word;
            }
        }
        return string_lines;
    }
}
