package lobschat.hycode.lobschat;

/**
 * Created by HyCode on 5/28/2018.
 */

class DataMessage {
    private String message;
    private String title;
    private String image;
    private String timestamp;
    private String user;
    private String recipient;

    DataMessage(String recipient, String message, String title, String image, String timestamp, String user) {
        this.message = message;
        this.timestamp = timestamp;
        this.title = title;
        this.image = image;
        this.user = user;
        this.recipient = recipient;
    }

}
