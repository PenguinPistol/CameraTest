package me.penguinpistol.cameratest.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Size;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.os.HandlerCompat;

import com.google.mlkit.vision.face.FaceContour;

import java.util.ArrayList;
import java.util.List;

import me.penguinpistol.cameratest.R;

public class GraphicOverlay extends View {
    private static final String TAG = GraphicOverlay.class.getSimpleName();

    private final Paint faceAreaPaint;
    private final Path faceAreaPath;

    private final RectF targetRectOrigin;
    private final RectF targetRectScaled;

    private final Drawable targetDrawable;

    private List<PointF> landmark;
    private Size imageSize;
    private PointF offset;
    float scaleX = 0;
    float scaleY = 0;

    private final Paint reasonPaint;
    private Rect reasonBound;
    private String reasonText = "";

    // 디버그용
    private final Paint debugPaint;
    private final Paint debugTextPaint;
    private RectF faceBound;
    private boolean isDebug;

    public GraphicOverlay(Context context) {
        this(context, null, 0);
    }

    public GraphicOverlay(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GraphicOverlay(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        float density = context.getResources().getDisplayMetrics().density;

        faceAreaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        faceAreaPaint.setStyle(Paint.Style.FILL);
        // TODO 오버레이 색상 레이아웃에서 변경할 수 있도록 수정필요
        faceAreaPaint.setColor(0x4D00FFFF);

        faceAreaPath = new Path();
        faceBound = new RectF();

        targetRectOrigin = new RectF();
        targetRectScaled = new RectF();

        // TODO 레이아웃에서 변경할 수 있도록 수정필요
        targetDrawable = ContextCompat.getDrawable(context, R.drawable.ic_face_rect_inner);

        reasonPaint = new Paint();
        reasonPaint.setColor(Color.parseColor("#FFFF3333"));
        reasonPaint.setTextSize(20 * density);
        reasonBound = new Rect();

        // 디버그 용 데이터
        debugPaint = new Paint();
        debugPaint.setStyle(Paint.Style.STROKE);
        debugPaint.setColor(Color.MAGENTA);
        debugPaint.setStrokeWidth(2 * density);

        debugTextPaint = new Paint(debugPaint);
        debugTextPaint.setStyle(Paint.Style.FILL);
        debugTextPaint.setTextSize(14 * density);

        isDebug = false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if(imageSize != null) {
            // 뷰의 가로길이를 기준으로 16:9 비율로 세로길이 구하기
            float width = getMeasuredWidth();
            float height = width * ((float)imageSize.getHeight() / (float)imageSize.getWidth());

            // 계산된 세로길이가 화면길이보다 짧으면 세로길이를 기준으로 계산
            if(height < getMeasuredHeight()) {
                height = getMeasuredHeight();
                width = height * ((float)imageSize.getWidth() / (float)imageSize.getHeight());
            }

            // analyzer 영역 : drawing 영역 비율계산
            scaleX = width / (float) imageSize.getWidth();
            scaleY = height / (float) imageSize.getHeight();

            // 가운데 정렬을 위한 offset
            offset.x = (width - getMeasuredWidth()) * 0.5f;
            offset.y = (height - getMeasuredHeight()) * 0.5f;

            // 목표영역(점선) 계산
            targetRectScaled.set(0, 0, targetRectOrigin.width() * scaleX, targetRectOrigin.height() * scaleY);
            targetRectScaled.offset((getMeasuredWidth() - targetRectScaled.width()) * 0.5f, targetRectOrigin.top * scaleY);
//            targetRectScaled.offset((getMeasuredWidth() - targetRectScaled.width()) * 0.5f, (getMeasuredHeight() - targetRectScaled.height()) * 0.5f);

            targetDrawable.setBounds(
                    (int) targetRectScaled.left,
                    (int) targetRectScaled.top,
                    (int) targetRectScaled.right,
                    (int) targetRectScaled.bottom
            );
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(isDebug) {
            drawDebugInfo(canvas);
        }

        if(targetDrawable != null) {
            targetDrawable.draw(canvas);

        }

        if(landmark != null) {
            faceAreaPath.reset();
            for(int i = 0; i < landmark.size(); i++) {
                PointF p = landmark.get(i);
                if(i == 0) {
                    faceAreaPath.moveTo(p.x, p.y);
                } else {
                    faceAreaPath.lineTo(p.x, p.y);
                }
            }
            faceAreaPath.close();

            canvas.drawPath(faceAreaPath, faceAreaPaint);
        }

        if(reasonBound != null) {
            canvas.drawText(
                    reasonText,
                    targetRectScaled.centerX() - (reasonBound.width() >> 1),
                    targetRectScaled.centerY() + (reasonBound.height() >> 1),
                    reasonPaint);
        }
    }

    public void setReason(String reason) {
        this.reasonText = reason;
        reasonPaint.getTextBounds(reason, 0, reason.length(), reasonBound);
        invalidate();
    }

    public void init(Size imageSize, RectF targetRect) {
        this.imageSize = imageSize;
        this.targetRectOrigin.set(targetRect);
        this.offset = new PointF();
        HandlerCompat.createAsync(Looper.getMainLooper()).post(this::requestLayout);
    }

    public void setFaceContour(FaceContour contour) {
        List<PointF> points = contour.getPoints();
        faceBound = getFaceRect(points);

        landmark = new ArrayList<>();
        for (PointF p : points) {
            // 전면카메라의 경우 좌우반전
            p.set(getMeasuredWidth() - (p.x * scaleX), p.y * scaleY);
            p.offset(offset.x, -offset.y);
            landmark.add(p);
        }

        invalidate();
    }

    //===============================================================================
    // 디버깅 관련
    //===============================================================================

    private RectF getFaceRect(List<PointF> landmarks) {
        return new RectF(
                (float)landmarks.stream().mapToDouble(x -> x.x).max().orElse(0),
                (float)landmarks.stream().mapToDouble(x -> x.y).min().orElse(0),
                (float)landmarks.stream().mapToDouble(x -> x.x).min().orElse(0),
                (float)landmarks.stream().mapToDouble(x -> x.y).max().orElse(0)
        );
    }

    private String debugText = "";

    public void setDebug(boolean debug) {
        isDebug = debug;
    }

    public void setDebugText(String text) {
        debugText = text;
    }

    private void drawDebugInfo(Canvas canvas) {
        float textSize = debugTextPaint.getTextSize() + 10;
        int line = 1;

        // 실제 계산 영역 출력
        debugPaint.setColor(Color.BLACK);
        debugTextPaint.setColor(debugPaint.getColor());
        canvas.drawText("*Analyzer Area", 10, textSize * line++, debugTextPaint);
        canvas.drawRect(0, 0, imageSize.getWidth(), imageSize.getHeight(), debugPaint);

        // 계산 얼굴 영역(좌우반전 x) 출력
        debugPaint.setColor(Color.MAGENTA);
        debugTextPaint.setColor(debugPaint.getColor());
        canvas.drawText("*Face Area Origin : " + faceBound.width(), 10, textSize * line++, debugTextPaint);
        canvas.drawRect(faceBound, debugPaint);

        // 계산 목표 영역 출력
        debugPaint.setColor(Color.GREEN);
        debugTextPaint.setColor(debugPaint.getColor());
        canvas.drawText("*Target Origin : " + targetRectOrigin.width(), 10, textSize * line++, debugTextPaint);
        canvas.drawRect(targetRectOrigin, debugPaint);

        // 그려지는 목표 영역 출력
        debugPaint.setColor(Color.BLUE);
        debugTextPaint.setColor(debugPaint.getColor());
        canvas.drawText("*Target", 10, textSize * line++, debugTextPaint);
        canvas.drawRect(targetRectScaled, debugPaint);

        // 디버그 텍스트 출력
        if(debugText != null) {
            debugTextPaint.setColor(Color.RED);
            for (String s : debugText.split("\n")) {
                canvas.drawText(s, 10, textSize * line++, debugTextPaint);
            }
        }
    }
}
