package codewrencher.gifit.helpers.files;

import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;

import codewrencher.gifit.tools.ToolBox;

/**
 * Created by Gene on 1/10/2016.
 */
public class FilePathComparator implements Comparator<String> {
    @Override
    public int compare(String left_string, String right_string) {
        try {
            if (convertPathToSeconds(right_string) > convertPathToSeconds(left_string)) {
                return 1;
            } else if (convertPathToSeconds(right_string) < convertPathToSeconds(left_string)) {
                return -1;
            }
        } catch (Exception e) {
            return 0;
        }
        return 0;
    }
    public long convertPathToSeconds(String file_path) {
        String date_time = this.getDateTimeFromFilePath(file_path);
        return this.getSecondsFromDateTime(date_time);
    }
    public String getDateTimeFromFilePath(String file_path) {
        String[] path_split = file_path.split("_");
        if (path_split != null && path_split.length > 0) {
            String[] file_extension_split = path_split[path_split.length - 1].split("[.]");
            if (file_extension_split != null && file_extension_split.length > 0) {
                return file_extension_split[0];
            }
        }
        return "";
    }
    public long getSecondsFromDateTime(String date_time) {
        String[] date_time_split = date_time.split("-");
        try {
            if (date_time_split != null) {
                if (date_time_split.length == 6) {
                    int year = Integer.parseInt(date_time_split[0]);
                    int month = Integer.parseInt(date_time_split[1]);
                    int day = Integer.parseInt(date_time_split[2]);
                    int hour = Integer.parseInt(date_time_split[3]);
                    int min = Integer.parseInt(date_time_split[4]);
                    int sec = Integer.parseInt(date_time_split[5]);

                    Calendar gregorian_calendar = new GregorianCalendar();
                    gregorian_calendar.set(year, month, day, hour, min, sec);

                    return 1000 * gregorian_calendar.getTimeInMillis();
                } else {
                    if (ToolBox.isNumeric(date_time)) {
                        double file_index = Double.parseDouble("0." + date_time);
                        file_index = file_index * 100000;
                        if (file_index > 0) {
                            return (long) file_index;
                        }
                    }

                }
            }
        } catch (Exception e) {
            return 0;
        }
        return 0;
    }
}
