package es.uca.tabu;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.Button;

public class MarkableButton extends Button {
    private boolean checked = false;
    Bitmap check = BitmapFactory.decodeResource(
            getResources(), R.drawable.accept);
    Paint p = new Paint();

    public MarkableButton(Context context) {
        super(context);
    }

    public MarkableButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MarkableButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
        invalidate();
    }

    public boolean isChecked() {
        return checked;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(checked) {
            int width = check.getWidth();
            int height = check.getHeight();
            int margin = 0;
            int x = canvas.getWidth() - width - margin;
            int y = canvas.getHeight() - height - margin;
            canvas.drawBitmap(check, x, y, p);
        }
    }
}