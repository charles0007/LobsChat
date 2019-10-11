package lobschat.hycode.lobschat;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class ChatMessage {

    private String msgText;
    private String msgUser;
    private String read;
    private String date;
    private String day;
    private String time;
    private String otherUser;
    private String type;
    private String method;
    private String city;
    private String state;
    private String msgLat;
    private String msgLng;
    String fulldate = "";
    private String UniqueId = "";
    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public ChatMessage(String msgText, String msgUser, String otherUser,String UniqueId) {
        DateFormat df = new SimpleDateFormat("EEE,dd MM yyyy,HH:mm:ss");
        fulldate = df.format(Calendar.getInstance().getTime());
        String day = fulldate.split(",")[0].trim();
        String date = fulldate.split(",")[1].trim();
        String time = fulldate.split(",")[2].trim();
        this.UniqueId = UniqueId;
        this.msgText = msgText;
        this.msgUser = msgUser;
        this.otherUser = otherUser;
        this.date = date;
        this.day = day;
        this.time = time;
        this.read = "unread";
    }

    public ChatMessage(String msgText, String msgUser, String method, String type,String ull) {

        DateFormat df = new SimpleDateFormat("EEE,dd/MM/yyyy,HH:mm:ss");
        fulldate = df.format(Calendar.getInstance().getTime());
        String day = fulldate.split(",")[0].trim();
        String date = fulldate.split(",")[1].trim();
        String time = fulldate.split(",")[2].trim();
        if (type.equalsIgnoreCase("city")) this.city = method;
        if (type.equalsIgnoreCase("state")) this.state = method;
        this.msgText = msgText;
        this.msgUser = msgUser;
        this.date = date;
        this.day = day;
        this.time = time;
        this.read = "unread";

    }

    public ChatMessage(String msgText, String msgUser, String gpsLat, String gpsLng, String type,String ull) {

        DateFormat df = new SimpleDateFormat("EEE,dd MM yyyy,HH:mm:ss");
        fulldate = df.format(Calendar.getInstance().getTime());
        String day = fulldate.split(",")[0].trim();
        String date = fulldate.split(",")[1].trim();
        String time = fulldate.split(",")[2].trim();

        this.msgText = msgText;
        this.msgUser = msgUser;
        this.msgLat = gpsLat;
        this.msgLng = gpsLng;
        this.date = date;
        this.day = day;
        this.time = time;
        this.read = "unread";

    }


    public ChatMessage() {

    }


    public void setMsgUser(String msgUser) {
        this.msgUser = msgUser;
    }

    public void setMsgText(String msgText) {
        this.msgText = msgText;
    }

    public void setMsgOther(String otherUser) {
        this.otherUser = otherUser;
    }

    public void setMsgState(String state) {
        this.state = state;
    }

    public void setMsgCity(String city) {
        this.city = city;
    }

    public void setMsgDate(String date) {
        this.date = date;
    }

    public void setMsgDay(String day) {
        this.day = day;
    }

    public void setMsgTime(String time) {
        this.time = time;
    }

    public void setMsgLat(String gpsLat) {
        this.msgLat = gpsLat;
    }

    public void setMsgLng(String gpsLng) {
        this.msgLng = gpsLng;
    }

    public String getMsgText() {
        return msgText;
    }

    public String getMsgUser() {
        return msgUser;
    }

    public String getUniqueId() {
        return UniqueId;
    }

    public String getMsgOther() {
        return otherUser;
    }

    public String getMsgState() {
        return state;
    }

    public String getMsgCity() {
        return city;
    }

    public String getMsgLat() {
        return msgLat;
    }

    public String getMsgLng() {
        return msgLng;
    }

    public String getMsgDate() {
        return date;
    }

    public String getMsgTime() {
        return time;
    }

    public String getMsgDay() {
        return day;
    }

    public static String randomAlphaNumeric(int count) {
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }
}
