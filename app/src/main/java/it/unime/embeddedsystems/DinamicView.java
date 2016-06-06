package it.unime.embeddedsystems;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by andfa on 06/06/2016.
 */
public class DinamicView extends RelativeLayout {

    RelativeLayout layout;
    TextView noteLabel;
    ImageView infoImage;

    boolean choose = false;

    public DinamicView(Context context) {
        super(context);
        init();
    }

    public DinamicView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DinamicView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        inflate(getContext(), R.layout.dinamic_view_layout, this);

        layout = (RelativeLayout)findViewById(R.id.layout_view);
        noteLabel = (TextView)findViewById(R.id.note_label);
        infoImage = (ImageView)findViewById(R.id.info_image);

    }

    public boolean isChoose() {
        return choose;
    }

    public void setChoose(boolean choose) {
        this.choose = choose;
    }

    public RelativeLayout getLayout() {
        return layout;
    }

    public void setLayout(RelativeLayout layout) {
        this.layout = layout;
    }

    public TextView getNoteLabel() {
        return noteLabel;
    }

    public void setNoteLabel(TextView noteLabel) {
        this.noteLabel = noteLabel;
    }

    public void setNoteVisibility(int visibility){
        infoImage.setVisibility(visibility);
    }

}