package lobschat.hycode.lobschat;



import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PostRequestData {
    @SerializedName("data")
    @Expose
    private DataMessage data;
    @SerializedName("to")
    @Expose
    private String to;

    public DataMessage getData() {
        return data;
    }

    public void setData(DataMessage data) {
        this.data = data;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

}