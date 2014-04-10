package es.uca.tabu;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.Paint.Align;
import android.util.AttributeSet;
import android.widget.ImageView;

public class MarkableImageView extends ImageView {
    private boolean checked = false;
    private int mId;
    private Boolean number_view = false;
    static Bitmap check;
    static Bitmap uncheck;   
    static Bitmap number;
    Boolean mInitialized = false;
    Paint p = new Paint();
    
    public MarkableImageView(Context context) {
        super(context);
    }
    
    public MarkableImageView(Context context, Integer id, Boolean number_view) {
        super(context);
        mId = id;
        this.number_view = number_view;
        if(!mInitialized) {
        	initialize();
        }
        Paint p = new Paint();
    }

    public MarkableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MarkableImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void initialize() {
    	mInitialized = true;
        check = BitmapFactory.decodeResource(
                getResources(), R.drawable.accept);
        uncheck = BitmapFactory.decodeResource(
                getResources(), R.drawable.reject);   
        number = BitmapFactory.decodeResource(
                getResources(), R.drawable.numbers);
    }
    
    public void setChecked(boolean checked) {
        this.checked = checked;
        invalidate();
    }

    public boolean isChecked() {
        return checked;
    }
    
    public int getId() {
    	return mId;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(number_view) {
    		Paint imagePaint = new Paint();
    		imagePaint.setTextSize(50f * getResources().getDisplayMetrics().density);
    		imagePaint.setColor(Color.WHITE);
    		imagePaint.setFlags(Paint.FAKE_BOLD_TEXT_FLAG);
    		imagePaint.setTextAlign(Align.CENTER);
        	
    		int width = number.getWidth();
            int height = number.getHeight();
            
            height *= 1.50;
            
            canvas.drawText(String.valueOf(mId+1), width/2, height/2, imagePaint);
        }
        if(checked) {
            int width = check.getWidth();
            int height = check.getHeight();
            int margin = 0;
            int x = canvas.getWidth() - width - margin;
            int y = canvas.getHeight() - height - margin;
            canvas.drawBitmap(check, x, y, p);
        }
        else {
        	if(number_view) {
                int width = uncheck.getWidth();
                int height = uncheck.getHeight();
                int margin = 0;
                int x = canvas.getWidth() - width - margin;
                int y = canvas.getHeight() - height - margin;
                canvas.drawBitmap(uncheck, x, y, p);
        	}
        }
    }
    
}