package codewrencher.gifit.tools;

import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import codewrencher.gifit.R;
import codewrencher.gifit.objects.simple.item.ImageItem;

import static java.lang.Math.min;

/**
 * Created by Gene on 7/3/2015.
 *
 * Android ToolBox class containing numerous static methods useful for conversions and various
 * odd jobs
 */
public class ToolBox {

    private static final String tag = "ToolBox";

    static public int getCurrentEpochSeconds() {
        return (int) (ToolBox.getCurrentEpochMillis() / 1000);
    }
    static public String getCurrentDateTimeSafeString(String delimiter) {
        Calendar gregorian_calendar = new GregorianCalendar();
        int year = gregorian_calendar.get(Calendar.YEAR);
        int month = gregorian_calendar.get(Calendar.MONTH) + 1;
        int day_of_month = gregorian_calendar.get(Calendar.DAY_OF_MONTH);
        int hour = gregorian_calendar.get(Calendar.HOUR_OF_DAY);
        int minute = gregorian_calendar.get(Calendar.MINUTE);
        int second = gregorian_calendar.get(Calendar.SECOND);
        String current_date_time_safe_string = year + delimiter + month + delimiter + day_of_month + delimiter +
                hour + delimiter + minute + delimiter + second;

        return current_date_time_safe_string;
    }
    static public String normalToMilitaryTime(String time) {
        if (! valuePresent(time)) return "";
        time = time.replaceAll("[\\s\\u00A0]+$", "");
        time = time.replace(":", "");

        if (time.contains("am")) {
            time = time.replace("am", "");
            time = time.replace(" ", "");
            if (time.equals("1200")) {
                time = "0000";
            } else if (time.equals("1230")) {
                time = "0030";
            }
        } else if (time.contains("pm")) {
            time = time.replace("pm", "");
            time = time.replace(" ", "");
            if (time.equals("1200")) {
                time = "1200";
            } else if (time.equals("1230")) {
                time = "1230";
            } else {
                int military_time = Integer.parseInt(time) + 1200;
                time = String.valueOf(military_time);
            }
        }
        return time;
    }
    static public String militaryToNormalTime(String time) {
        if (! valuePresent(time)) return "";
        double numerical_time = Double.parseDouble(time);
        DecimalFormat one_dig_double = new DecimalFormat("#.#");

        if (numerical_time == 0) {
            time = "12:00 am";
        } else if (numerical_time == 30) {
            time = "12:30 am";
        } else if (numerical_time < 1200 ) {
            time = String.valueOf(Double.valueOf(one_dig_double.format(numerical_time / 100)));

            if (time.contains(".")) {
                time = time.replace(".", ":");
                time += "0";
            } else {
                time += "00";
            }
            time += " am";
        } else if (numerical_time >= 1200 && numerical_time < 1300 ) {
            time = String.valueOf(Double.valueOf(one_dig_double.format(numerical_time / 100)));
        } else {
            time = String.valueOf((numerical_time - 1200) / 100);
            if (time.contains(".")) {
                time = time.replace(".", ":");
                time += "0";
            } else {
                time += "00";
            }
            time += " pm";
        }
        return time;
    }
    static public double minutesToHours(int minutes) {
        return (double) minutes / 60;
    }
    static public int hoursToMinutes(double hours) {
        return (int) (hours * 60);
    }
    static public int hoursToMilitaryTime(double hours) {
        return (int) (hours * 100);
    }
    static public String minutesToNormalTime(int minutes) {
        double hours = minutesToHours(minutes);
        int military = hoursToMilitaryTime(hours);
        String normal = militaryToNormalTime(String.valueOf(military));

        return normal;
    }
    static public long getEpochMilliSecondsFromDate(String date) {
        if (! valuePresent(date)) return 0;
        int year = Integer.parseInt(date.split("-")[0]);
        int month = Integer.parseInt(date.split("-")[1]);
        int day = Integer.parseInt(date.split("-")[2]);

        Calendar gregorian_calendar = new GregorianCalendar();
        gregorian_calendar.set(year, month, day);

        return (gregorian_calendar.getTimeInMillis());
    }
    static public long getEpochMilliSecondsFromDateTime(String date_time) {
        if (! valuePresent(date_time)) return 0;
        if (! date_time.contains(" ")) { return 0; }

        String date = date_time.split(" ")[0];
        String time = date_time.split(" ")[1];

        int year = Integer.parseInt(date.split("-")[0]);
        int month = Integer.parseInt(date.split("-")[1]);
        int day = Integer.parseInt(date.split("-")[2]);

        int hours = Integer.parseInt(time.split(":")[0]);
        int minutes = Integer.parseInt(time.split(":")[1]);
        int seconds = Integer.parseInt(time.split(":")[2]);

        Calendar gregorian_calendar = new GregorianCalendar();
        gregorian_calendar.set(year, month, day, hours,minutes, seconds);

        return (gregorian_calendar.getTimeInMillis());
    }
    static double roundToTwoDecimals(double target_value) {
        DecimalFormat decimal_format = new DecimalFormat("#.##");
        return Double.valueOf(decimal_format.format(target_value));
    }
    static public String extractDateFromDateTime(String sql_date_time) {
        if (! valuePresent(sql_date_time)) return "";
        String date = sql_date_time;
        String[] date_split = sql_date_time.split(" ");
        if (date_split.length > 0) {
            date = date_split[0];
        }
        return date;
    }
    static public String extractTimeFromDateTime(String sql_date_time) {
        if (! valuePresent(sql_date_time)) return "";
        String time = sql_date_time;
        String[] date_split = sql_date_time.split(" ");
        if (date_split.length > 1) {
            time = date_split[1];
        }
        return time;
    }

    public static double milesToFeet(double miles) {
        return miles * 5280;
    }
    public static Boolean valuePresent(Object value) {
        if (value == null) return false;
        value = String.valueOf(value);
        if (value == null || value.equals("0") || value.equals("")) {
            return false;
        }
        return true;
    }
    public static String removeFileExtension( String file_name ) {
        int period_index = file_name.lastIndexOf('.');

        if (period_index == -1) {
            return file_name;
        } else {
            return file_name.substring(0, file_name.lastIndexOf('.'));
        }
    }
    public static String getFileAsDirNameFromFilePath( String file_path ) {
        String file_name = getFileNameFromFilePath(file_path);
        String dir_name = removeFileExtension( file_name );

        return dir_name;
    }
    public static ArrayList<ImageItem> combineArrays( ArrayList<ImageItem> first_array, ArrayList<ImageItem> second_array ) {
        for ( ImageItem item : second_array ) {
            first_array.add( item );
        }
        return  first_array;
    }

    /**
     * Selects a color from a range of red to green depending on the percentage value from 0 to 100
     *
     * Requires your gradient of colors to be defined in your 'colors.xml' resource file
     *
     * @param percent (int) The percentage value to pick a color for ( must be in range of 0 - 100 )
     */
    public static int pickColorByPercent( int percent ) {

        int color_id;
        percent = ( (int) Math.round( (double) percent / 10 ) ) * 10;

        switch ( percent ) {

            case 100:
                color_id = R.color.green_100_red_0;
                break;

            case 90:
                color_id = R.color.green_90_red_10;
                break;

            case 80:
                color_id = R.color.green_80_red_20;
                break;

            case 70:
                color_id = R.color.green_70_red_30;
                break;

            case 60:
                color_id = R.color.green_60_red_40;
                break;

            case 50:
                color_id = R.color.green_50_red_50;
                break;

            case 40:
                color_id = R.color.green_20_red_60;
                break;

            case 30:
                color_id = R.color.green_30_red_70;
                break;

            case 20:
                color_id = R.color.green_20_red_80;
                break;

            case 10:
                color_id = R.color.green_10_red_90;
                break;

            case 0:
                color_id = R.color.green_0_red_100;
                break;

            default:
                color_id = R.color.white;
                break;

        }
        return color_id;
    }

    /**
     * Retry a task a certain number of times.
     *
     * Takes a piece of code and executes it a number of times or until it succeeds
     *
     * @param try_to The code you want to try
     * @param on_success What you want to do if the retries succeed
     * @param on_failure What to do if the retries fail
     * @param max_retries How many times to retry
     * @param delay How long to wait between retires in milliseconds
     */
    public static void reTry(Runnable try_to, Runnable on_success, Runnable on_failure, int max_retries, int delay) {
        if (max_retries <= 0) {
            max_retries = 5;
        }
        if (delay < 0) {
            delay = 0;
        }
        int retry_count = 0;
        boolean success = false;

        while (retry_count < max_retries) {             // For each try in the number specified

            try {
                if (delay > 0) {                        // If a delay is specified, run with a delay
                    final Handler handler = new Handler();
                    handler.postDelayed(try_to, delay);
                } else {                                // Otherwise just run the Runnables
                    try_to.run();
                }
                success = true;
                break;
            } catch (Exception e) {                     // If we catch an exception, we try again
                retry_count++;
            }
        }
        if (success) {                                  // If we succeed, execute some code
            try {
                on_success.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {                                        // If we fail, execute some code
            try {
                on_failure.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Retry a task a certain number of times - Simplified version.
     *
     * Takes a piece of code and executes it a number of times or until it succeeds
     *
     * @param try_to The code you want to try
     * @param on_success What you want to do if the retries succeed
     * @param on_failure What to do if the retries fail
     */
    public static void reTry(Runnable try_to, Runnable on_success, Runnable on_failure) {
        int max_retries = 5;
        int delay = 10;

        int retry_count = 0;
        boolean success = false;

        while (retry_count < max_retries) {             // For each try in the number specified

            try {
                if (delay > 0) {                        // If a delay is specified, run with a delay
                    final Handler handler = new Handler();
                    handler.postDelayed(try_to, delay);
                } else {                                // Otherwise just run the Runnables
                    try_to.run();
                }
                success = true;
                break;
            } catch (Exception e) {                     // If we catch an exception, we try again
                retry_count++;
            }
        }
        if (success) {                                  // If we succeed, execute some code
            try {
                on_success.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {                                        // If we fail, execute some code
            try {
                on_failure.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Scale the given image so that it fits in the box specified by the given width and height
     *
     * To maintain the same aspect ratio, we will scale the image to to the minimum of the width
     * and height ratios between the input image and the given dimensions
     * scale_factor = min( width / image.getWidth(), height / image.getHeight() )
     *
     * @param input The image to scale
     * @param width The target width to scale to
     * @param height The target height to scale to
     * @return The scaled image. Return the original image if encountering division by 0
     */
    public static Bitmap scaleImageToGivenDimensions(Bitmap input, int width, int height) {
        Bitmap output;

        int input_width = input.getWidth();
        int input_height = input.getHeight();

        if (input_width == 0 || input_height == 0) {
            return input;
        }
        Log.d(tag + " INPUT WIDTH", String.valueOf(input_width));
        Log.d(tag + " INPUT HEIGHT", String.valueOf(input_height));

        Log.d(tag + " TARGET WIDTH", String.valueOf(width));
        Log.d(tag + " TARGET HEIGHT", String.valueOf(height));

        double scale_factor = min((double) width / (double) input_width,
                                  (double) height / (double) input_height);

        Log.d(tag + " SCALE FACTOR", String.valueOf(scale_factor));

        int output_width = (int) ((double) width * scale_factor);
        int output_height = (int) ((double) height * scale_factor);

        Log.d(tag + " OUTPUT WIDTH", String.valueOf(output_width));
        Log.d(tag + " OUTPUT HEIGHT", String.valueOf(output_height));

        output = Bitmap.createScaledBitmap(input, output_width, output_height, false);

    return output;
    }

    /**
     * Return a random number within a specified range
     * @param min lower bound
     * @param max upper bound
     * @return ransom number n so that lower bound < n < upper bound
     */
    public static double getRandomWithinRange(double min, double max) {
        double range = max - min + 1;
        return (Math.random() * range) + min;
    }

    /**
     * Randomly return either a one or a 0
     * @return 1 or 0
     */
    public static int pickOne() {
        if (Math.random() > 0.5) return 1;
        else return 0;
    }

    /**
     * Convert degrees to radians
     * @param degrees
     * @return radians
     */
    public static double degreesToRadians(double degrees) {
        return degrees * Math.PI / 180;
    }

    /**
     * Convert radians to degrees
     * @param radians
     * @return degrees
     */
    public static double radiansToDegrees(double radians) {
        return radians * (180 / Math.PI);
    }

    /**
     * Returns true if a string is numeric and false if not
     * @param string
     * @return true or false
     */
    public static boolean isNumeric(String string) {
        try {
            double parsed_string = Double.parseDouble(string);
        }
        catch(NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    /**
     * Input a file path. Get the file name
     * @param file_path
     * @return file name
     */
    public static String getFileNameFromFilePath ( String file_path ) {
        String file_name = "";
        if ( file_path != null && file_path.contains("/") ) {
            String[] path_split = file_path.split( "[/]+" );
            file_name = path_split[path_split.length - 1];
        }
        return file_name;
    }

    /**
     * Capitalize the first letter of a word
     *
     * @param word The word you want to capitalize
     * @return word returns Word
     */
    public static String capitalize (String word) {
        word = word.toString();
        String capitalized_word = word;
        if (word.length() > 0) {
            String first_char = word.substring(0, 1);
            String remaining_string = word.substring(1, word.length());
            capitalized_word = first_char.toUpperCase() + remaining_string.toLowerCase();
        }
        return capitalized_word;
    }

    /**
     * Get the current milliseconds since the Linux epoch
     * @return Long millis
     */
    static public Long getCurrentEpochMillis() {

        Calendar gregorian_calendar = new GregorianCalendar();
        return gregorian_calendar.getTimeInMillis();
    }

    /**
     * Get the current date and time ads it would be formatted in SQL
     * @return A formatted date and time string
     */
    public static String getCurrentDateTime() {
        Calendar gregorian_calendar = new GregorianCalendar();

        int year = gregorian_calendar.get(Calendar.YEAR);
        int month = gregorian_calendar.get(Calendar.MONTH) + 1;
        int day_of_month = gregorian_calendar.get(Calendar.DAY_OF_MONTH);
        int hour = gregorian_calendar.get(Calendar.HOUR_OF_DAY);
        int minute = gregorian_calendar.get(Calendar.MINUTE);
        int second = gregorian_calendar.get(Calendar.SECOND);
        String current_date_time = year + "-" + month + "-" + day_of_month + " " +
                hour + ":" + minute + ":" + second;

        return current_date_time;
    }

    /**
     * Get the current date and time string, but separated only by underscores
     * @return A formatted date and time string separated by underscores
     */
    public static String getCurrentDateTimeSafeString() {
        Calendar gregorian_calendar = new GregorianCalendar();
        int year = gregorian_calendar.get(Calendar.YEAR);
        int month = gregorian_calendar.get(Calendar.MONTH) + 1;
        int day_of_month = gregorian_calendar.get(Calendar.DAY_OF_MONTH);
        int hour = gregorian_calendar.get(Calendar.HOUR_OF_DAY);
        int minute = gregorian_calendar.get(Calendar.MINUTE);
        int second = gregorian_calendar.get(Calendar.SECOND);
        String current_date_time_safe_string = year + "_" + month + "_" + day_of_month + "_" +
                hour + "_" + minute + "_" + second;

        return current_date_time_safe_string;
    }

    /**
     * Format a SQL style date string to the more common colloquial format
     *
     * @param sql_date sql date string
     * @return forward slash sparated date string
     */
    public static String sqlToNormalDate(String sql_date) {
        if (! valuePresent(sql_date)) return "";
        String[] date_split = sql_date.split("-");
        if (date_split.length < 3) return "";

        String normal_date = date_split[1] + "/" + date_split[2] + "/" + date_split[0];

        return normal_date;
    }
}
