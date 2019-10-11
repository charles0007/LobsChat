package lobschat.hycode.lobschat;


import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * Created by HyCode on 14/4/17.
 */

public class chat_rec extends RecyclerView.ViewHolder {


    TextView leftText, rightText;

    public chat_rec(View itemView) {
        super(itemView);

        leftText = (TextView) itemView.findViewById(R.id.leftText);
        rightText = (TextView) itemView.findViewById(R.id.rightText);
        if (leftText.getText().toString().isEmpty()) {
            leftText.setVisibility(View.GONE);
        }
        if (rightText.getText().toString().isEmpty()) {
            rightText.setVisibility(View.GONE);
        }

    }
}
