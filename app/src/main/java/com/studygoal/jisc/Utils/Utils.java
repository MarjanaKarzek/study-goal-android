package com.studygoal.jisc.Utils;

import android.content.res.Resources;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;

import com.studygoal.jisc.Managers.DataManager;
import com.studygoal.jisc.Managers.LinguisticManager;
import com.studygoal.jisc.R;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Utils class
 * <p>
 * Provides several helper methods.
 *
 * @author Therapy Box
 * @version 1.5
 * @date unknown
 */
public class Utils {

    /**
     * Validates the given email address.
     *
     * @param email address to be checked
     * @return validation status
     */
    public static boolean validate_email(String email) {
        if (email.matches("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")) {
            return true;
        }
        return false;
    }

    /**
     * Gets the string n between two strings.
     *
     * @param from     beginning string
     * @param to       end string
     * @param original original string
     * @return string in between
     */
    public static String getStringBetween(String from, String to, String original) {
        if (original.contains(from)) {
            String[] _aux = original.split(from);
            if (_aux[1].contains(to)) {
                String[] _aux2 = _aux[1].split(to);
                if (_aux2[0] != null) {
                    return _aux2[0];
                } else {
                    return "";
                }
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

    /**
     * Calculates the distance in between two coordinates
     *
     * @param lat1 latitude of first location
     * @param lon1 longitude of first location
     * @param lat2 latitude of second location
     * @param lon2 longitude of second location
     * @param unit unit of the distance
     * @return distance as a string
     */
    public static String calculate_distance(double lat1, double lon1, double lat2, double lon2, String unit) {
        Location locationA = new Location("gps");

        locationA.setLatitude(lat1);
        locationA.setLongitude(lon1);

        Location locationB = new Location("gps");

        locationB.setLatitude(lat2);
        locationB.setLongitude(lon2);

        DecimalFormat df = new DecimalFormat("#.#");

        if (unit.equals("m"))
            return df.format(locationA.distanceTo(locationB));
        else
            return df.format((locationA.distanceTo(locationB) / 1000));
    }

    /**
     * Generates MD5 string of given string.
     *
     * @param md5 to be converted string
     * @return converted string
     */
    @Nullable
    public static String MD5(String md5) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(md5.getBytes());
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException ignored) {
        }
        return null;
    }

    /**
     * Converts dp to px.
     *
     * @param dp display points to be converted
     * @return pixel
     */
    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    /**
     * Converts px to dp.
     *
     * @param px pixel to be converted
     * @return display points
     */
    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    /**
     * Gets a formatted string about the spent time from time in minutes.
     *
     * @param timeSpent time spent in minutes
     * @return formatted time string
     */
    @NonNull
    public static String getMinutesToHour(String timeSpent) {
        Long minutes = Long.parseLong(timeSpent);
        Long hours = minutes / 60;
        minutes = minutes % 60;
        if (hours == 0 && minutes == 0)
            return DataManager.getInstance().context.getString(R.string.less_than_1_minute);
        if (hours == 0 && minutes == 1)
            return "1 " + DataManager.getInstance().context.getString(R.string.minute);
        if (hours == 0 && minutes > 1)
            return minutes + " " + DataManager.getInstance().context.getString(R.string.minutes);
        if (hours == 1 && minutes == 0)
            return "1 " + DataManager.getInstance().context.getString(R.string.hour);
        if (hours == 1 && minutes == 1)
            return "1 " + DataManager.getInstance().context.getString(R.string.hour) + " " + DataManager.getInstance().context.getString(R.string.and) + " " + minutes + " " + DataManager.getInstance().context.getString(R.string.minute);
        if (hours == 1 && minutes > 1)
            return "1 " + DataManager.getInstance().context.getString(R.string.hour) + " " + DataManager.getInstance().context.getString(R.string.and) + " " + minutes + " " + DataManager.getInstance().context.getString(R.string.minutes);
        if (hours > 1 && minutes == 0)
            return hours + " " + DataManager.getInstance().context.getString(R.string.hours);
        if (hours > 1 && minutes == 1)
            return hours + " " + DataManager.getInstance().context.getString(R.string.hours) + " " + DataManager.getInstance().context.getString(R.string.and) + " 1 " + DataManager.getInstance().context.getString(R.string.minute);
        else
            return hours + " " + DataManager.getInstance().context.getString(R.string.hours) + " " + DataManager.getInstance().context.getString(R.string.and) + " " + minutes + " " + DataManager.getInstance().context.getString(R.string.minutes);

    }

    /**
     * Gets a formatted String from a date string.
     *
     * @param activityDate date string to be formatted
     * @return formatted date string
     */
    @NonNull
    public static String getDate(String activityDate) {
        String[] split = activityDate.split("-");

        return split[2] + " " + LinguisticManager.getInstance().getShortMonth(split[1]) + " " + split[0];
    }

    /**
     * Gets a formatted time String from a date string.
     *
     * @param createdDate date string to be formatted
     * @return formatted time string
     */
    @NonNull
    public static String getTime(String createdDate) {
        String[] split = createdDate.split(" ")[1].split(":");
        return split[0] + ":" + split[1];
    }

    /**
     * Formats date to a string with details.
     *
     * @param year  year number
     * @param month month number
     * @param day   day number
     * @return formatted date string
     */
    public static String formatDate(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);

        String[] suffixes =
                {"th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th",
                        "th", "th", "th", "th", "th", "th", "th", "th", "th", "th",
                        "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th",
                        "th", "st"};

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE d");

        String result = dateFormat.format(calendar.getTime()) + suffixes[day] + " ";
        dateFormat = new SimpleDateFormat("MMMM yyyy");
        result += dateFormat.format(calendar.getTime());

        return result;
    }

    /**
     * Formats time in milliseconds to a detailed date string.
     *
     * @param time time in milliseconds
     * @return formatted date string
     */
    public static String formatDate(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        String[] suffixes =
                {"th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th",
                        "th", "th", "th", "th", "th", "th", "th", "th", "th", "th",
                        "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th",
                        "th", "st"};

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE d");

        String result = dateFormat.format(calendar.getTime()) + suffixes[day] + " ";
        dateFormat = new SimpleDateFormat("MMMM yyyy");
        result += dateFormat.format(calendar.getTime());

        return result;
    }

    /**
     * Checks whether the current date string is in the current week.
     *
     * @param dateString string to be checked
     * @return whether or not it is in the same week
     */
    public static boolean isInSameWeek(String dateString) {
        Calendar calendar = Calendar.getInstance();
        Long currentDateInMs = calendar.getTimeInMillis();
        ArrayList<String> dates = new ArrayList<>();

        switch (calendar.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.SUNDAY: {
                dates.add(calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DAY_OF_MONTH));
                break;
            }
            case Calendar.MONDAY: {
                dates.add(calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DAY_OF_MONTH));
                calendar.setTimeInMillis(currentDateInMs - 86400000);
                dates.add(calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DAY_OF_MONTH));
                break;
            }
            case Calendar.TUESDAY: {
                dates.add(calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DAY_OF_MONTH));
                for (int i = 1; i < Calendar.TUESDAY; i++) {
                    calendar.setTimeInMillis(currentDateInMs -= 86400000);
                    dates.add(calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DAY_OF_MONTH));
                }
                break;
            }
            case Calendar.WEDNESDAY: {
                dates.add(calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DAY_OF_MONTH));
                for (int i = 1; i < Calendar.WEDNESDAY; i++) {
                    calendar.setTimeInMillis(currentDateInMs -= 86400000);
                    dates.add(calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DAY_OF_MONTH));
                }
                break;
            }
            case Calendar.THURSDAY: {
                dates.add(calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DAY_OF_MONTH));
                for (int i = 1; i < Calendar.THURSDAY; i++) {
                    calendar.setTimeInMillis(currentDateInMs -= 86400000);
                    dates.add(calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DAY_OF_MONTH));
                }
                break;
            }
            case Calendar.FRIDAY: {
                dates.add(calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DAY_OF_MONTH));
                for (int i = 1; i < Calendar.FRIDAY; i++) {
                    calendar.setTimeInMillis(currentDateInMs -= 86400000);
                    dates.add(calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DAY_OF_MONTH));
                }
                break;
            }
            case Calendar.SATURDAY: {
                dates.add(calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DAY_OF_MONTH));
                for (int i = 1; i < Calendar.SATURDAY; i++) {
                    calendar.setTimeInMillis(currentDateInMs -= 86400000);
                    dates.add(calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DAY_OF_MONTH));
                }
                break;
            }
        }
        dateString = Integer.parseInt(dateString.split("-")[0]) + "-" + Integer.parseInt(dateString.split("-")[1]) + "-" + Integer.parseInt(dateString.split("-")[2]);
        return dates.contains(dateString);
    }

    /**
     * Converts the given time in minutes to hours.
     *
     * @param necessaryTime to be converted time
     * @return time in hours
     */
    public static String convertToHour(int necessaryTime) {
        String hour = necessaryTime / 60 + "";
        necessaryTime = necessaryTime % 60;
        if (necessaryTime == 0) return hour;

        hour += ".";
        String tmp = ((float) necessaryTime / 60 + "").split("\\.")[1];
        if (tmp.length() > 2) hour += tmp.substring(0, 2);
        else hour += tmp;
        return hour;
    }

    /**
     * Sorts the given map by values.
     *
     * @param unsortedMap to be sorted map
     * @return sorted map
     */
    public static Map<String, Integer> sortByValues(Map<String, Integer> unsortedMap) {

        // Convert Map to List
        List<Map.Entry<String, Integer>> list =
                new LinkedList<Map.Entry<String, Integer>>(unsortedMap.entrySet());

        // Sort mList with comparator, to compare the Map values
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> o1,
                               Map.Entry<String, Integer> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        // Convert sorted map back to a Map
        Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
        for (Iterator<Map.Entry<String, Integer>> it = list.iterator(); it.hasNext(); ) {
            Map.Entry<String, Integer> entry = it.next();
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }

    /**
     * Gets the time period from the given time in milliseconds.
     *
     * @param timeInMillis time to be checked
     * @return time period
     */
    @NonNull
    public static String getWeekPeriod(long timeInMillis) {
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTimeInMillis(timeInMillis);

        Calendar firstDay = GregorianCalendar.getInstance();
        firstDay.setTimeInMillis(timeInMillis - ((calendar.get(Calendar.DAY_OF_WEEK) - calendar.getFirstDayOfWeek()) * 86400000));

        Calendar lastDay = GregorianCalendar.getInstance();
        lastDay.setTimeInMillis(firstDay.getTimeInMillis() + (6 * 86400000));

        if (firstDay.get(Calendar.MONTH) == lastDay.get(Calendar.MONTH))
            return ((firstDay.get(Calendar.MONTH) + 1) < 10 ? "0" + (firstDay.get(Calendar.MONTH) + 1) : (firstDay.get(Calendar.MONTH) + 1)) + "/" + firstDay.get(Calendar.DAY_OF_MONTH) + "-" + lastDay.get(Calendar.DAY_OF_MONTH);
        else
            return ((firstDay.get(Calendar.MONTH) + 1) < 10 ? "0" + (firstDay.get(Calendar.MONTH) + 1) : (firstDay.get(Calendar.MONTH) + 1)) + "/" + firstDay.get(Calendar.DAY_OF_MONTH) + "-" + ((lastDay.get(Calendar.MONTH) + 1) < 10 ? "0" + (lastDay.get(Calendar.MONTH) + 1) : (lastDay.get(Calendar.MONTH) + 1)) + "/" + lastDay.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * Gets the attainment date from a date string.
     *
     * @param date date to be converted
     * @return formatted date string
     */
    @NonNull
    public static String attainmentDate(String date) {
        String[] _one = date.split("T")[0].split("-");
        return _one[2] + "-" + _one[1] + "-" + _one[0].substring(2, 4);
    }

    /**
     * Decodes the given JWT.
     *
     * @param JWTEncoded JWT encoded
     * @return JWT decoded
     */
    @NonNull
    public static String jwtDecoded(String JWTEncoded) {
        try {
            String[] split = JWTEncoded.split("\\.");
            return getJson(split[1]);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Decodes Json with the given json encoding.
     *
     * @param strEncoded encoding used
     * @return json decoded
     * @throws UnsupportedEncodingException gets thrown if the encoding is not supported
     */
    @NonNull
    private static String getJson(String strEncoded) throws UnsupportedEncodingException {
        byte[] decodedBytes = Base64.decode(strEncoded, Base64.URL_SAFE);
        return new String(decodedBytes, "UTF-8");
    }

}
