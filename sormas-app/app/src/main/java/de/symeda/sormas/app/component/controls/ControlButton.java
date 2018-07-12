package de.symeda.sormas.app.component.controls;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.ViewGroup;

import de.symeda.sormas.app.R;
import de.symeda.sormas.app.core.StateDrawableBuilder;

public class ControlButton extends android.support.v7.widget.AppCompatButton {

    private ControlButtonType buttonType;
    private Drawable iconStart;
    private Drawable iconEnd;
    private boolean rounded;
    private boolean slim;
    private boolean iconOnly;

    // Constructors

    public ControlButton(Context context) {
        super(context);
        initialize(context, null);
    }

    public ControlButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs);
    }

    public ControlButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize(context, attrs);
    }

    // Initialization and styling

    private void initialize(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray attributes = context.getTheme()
                    .obtainStyledAttributes(attrs, R.styleable.ControlButton, 0, 0);

            try {
                String buttonTypeAttribute = attributes.getString(R.styleable.ControlButton_buttonType);
                if (buttonTypeAttribute != null) {
                    buttonType = ControlButtonType.valueOf(buttonTypeAttribute);
                }
                iconStart = attributes.getDrawable(R.styleable.ControlButton_iconStart);
                iconEnd = attributes.getDrawable(R.styleable.ControlButton_iconEnd);
                rounded = attributes.getBoolean(R.styleable.ControlButton_rounded, false);
                slim = attributes.getBoolean(R.styleable.ControlButton_slim, false);
                iconOnly = attributes.getBoolean(R.styleable.ControlButton_iconOnly, false);

                if (iconStart != null) {
                    iconStart = iconStart.mutate();
                }
                if (iconEnd != null) {
                    iconEnd = iconEnd.mutate();
                }
            } finally {
                // This is important to free up memory
                attributes.recycle();
            }
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        updateButton();
    }

    private void updateButton() {
        if (buttonType == null) {
            return;
        }

        Resources resources = getResources();

        int textColor = resources.getColor(buttonType.getTextColor());
        float height = getHeight(resources);
        float textSize = getTextSize(resources);
        int horizontalPadding = getHorizontalPadding(resources);
        int verticalPadding = getVerticalPadding(resources);
        int drawablePadding = getDrawablePadding(resources);

        GradientDrawable normalDrawable = buttonType.getDrawable(ControlButtonState.NORMAL, rounded, resources);
        GradientDrawable focusedDrawable = buttonType.getDrawable(ControlButtonState.FOCUSED, rounded, resources);
        GradientDrawable pressedDrawable = buttonType.getDrawable(ControlButtonState.PRESSED, rounded, resources);

        StateListDrawable stateDrawable = new StateDrawableBuilder()
                .setNormalDrawable(normalDrawable)
                .setFocusedDrawable(focusedDrawable)
                .setPressedDrawable(pressedDrawable)
                .build();

        if (iconStart != null) {
            iconStart.setTint(textColor);
        }

        if (iconEnd != null) {
            iconEnd.setTint(textColor);
        }

        setCompoundDrawablesWithIntrinsicBounds(iconStart, null, iconEnd, null);

        setTextColor(textColor);
        setBackground(stateDrawable);
        setCompoundDrawablePadding(drawablePadding);

        ViewGroup.LayoutParams param = getLayoutParams();
        param.height = (int) height;

        setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);
        setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);

        if (iconOnly) {
            setText("");
        }
    }

    // Calculation methods for dimensions

    private float getHeight(Resources resources) {
        if (slim) {
            return resources.getDimension(R.dimen.slimButtonHeight);
        } else {
            return resources.getDimension(R.dimen.defaultButtonHeight);
        }
    }

    private float getTextSize(Resources resources) {
        if (slim) {
            return resources.getDimension(R.dimen.slimButtonTextSize);
        } else {
            return resources.getDimension(R.dimen.buttonTextSize);
        }

    }

    private int getHorizontalPadding(Resources resources) {
        if (iconOnly) {
            return resources.getDimensionPixelSize(R.dimen.iconOnlyButtonHorizontalPadding);
        } else if (slim) {
            return resources.getDimensionPixelSize(R.dimen.slimButtonHorizontalPadding);
        } else {
            return resources.getDimensionPixelSize(R.dimen.buttonHorizontalPadding);
        }
    }

    private int getVerticalPadding(Resources resources) {
        if (slim) {
            return resources.getDimensionPixelSize(R.dimen.slimButtonVerticalPadding);
        } else {
            return resources.getDimensionPixelSize(R.dimen.buttonVerticalPadding);
        }
    }

    private int getDrawablePadding(Resources resources) {
        if (iconOnly) {
            return 0;
        } else {
            return resources.getDimensionPixelSize(R.dimen.contentHorizontalSpacing);
        }
    }

    // Setters - Automatically called by the data binding

    public void setButtonType(ControlButtonType buttonType) {
        if (this.buttonType != buttonType) {
            this.buttonType = buttonType;
            updateButton();
        }
    }

    public void setRounded(boolean rounded) {
        if (this.rounded != rounded) {
            this.rounded = rounded;
            updateButton();
        }
    }

    public void setSlim(boolean slim) {
        if (this.slim != slim) {
            this.slim = slim;
            updateButton();
        }
    }

    public void setIconOnly(boolean iconOnly) {
        if (this.iconOnly != iconOnly) {
            this.iconOnly = iconOnly;
            updateButton();
        }
    }

}