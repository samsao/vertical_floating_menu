package com.samsao.floatingverticalmenu;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * A menu that expands vertically revealing clickable submenus. The expanded menu is displayed in front of other components of the layout.
 * You can add as many submenus as you want but be keep in mind that Google's design guidelines recommend a maximum of six submenus fot this type of menu.
 * <p>
 * Created by lcampos on 2015-05-29.
 */
public class FloatingMenu extends FrameLayout {

    private static final String STATIC_VIEW = "StaticView";
    private static final float DEFAULT_ELEVATION = 10f;
    private static final int DEFAULT_TEXT_SIZE = 14;

    private FrameLayout mCoverView;
    private ImageView mCoverImageView;
    private TextView mCoverTextView;
    private int mSubMenuBackgroundResource;
    private Interpolator mAnimationInterpolator;
    private int mAnimationDuration = 400;
    private int mSubMenuBetweenPadding = 300;
    private int mMenuExtraMargin = 0;

    private boolean mIsOpen;
    private boolean mLockedCLick;

    private MenuClickListener mOnMenuClickListener;

    private float mMenuElevation = DEFAULT_ELEVATION;
    private float mTextSize = DEFAULT_TEXT_SIZE;
    private int mTextColor;

    private int mOriginalWidth;
    private int mOriginalHeight;

    public interface MenuClickListener {
        /**
         * Called when the user opens or closes the menu
         *
         * @param coverView The main view with which the user interacts to open and close the menu
         * @param isOpen    Whether the menu is open or closed
         */
        public void onMenuClick(View coverView, boolean isOpen);
    }

    public FloatingMenu(Context context) {
        super(context);
        init(context, null);
    }

    public FloatingMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public FloatingMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    /**
     * Initializes the view and sets the attributes from xml
     */
    private void init(Context context, AttributeSet attrs) {
        setBackground(null);
        mAnimationInterpolator = new AccelerateDecelerateInterpolator();

        /**
         * Retrieves xml attributes
         */
        Drawable menuBackground = null;
        int transparentColor = getResources().getColor(android.R.color.transparent);
        mTextColor = transparentColor;

        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.FloatingMenu, 0, 0);
            try {
                String menuTitle = a.getString(R.styleable.FloatingMenu_fmText);
                mTextColor = a.getColor(R.styleable.FloatingMenu_fmTextColor, transparentColor);
                int menuIcon = a.getResourceId(R.styleable.FloatingMenu_fmIcon, transparentColor);
                int backgroundResource = a.getResourceId(R.styleable.FloatingMenu_fmBackground, 0);
                menuBackground = backgroundResource != 0 ? ContextCompat.getDrawable(context, backgroundResource) : null;
                mMenuElevation = a.getDimension(R.styleable.FloatingMenu_fmElevation, DEFAULT_ELEVATION);
                mTextSize = a.getDimensionPixelSize(R.styleable.FloatingMenu_fmTextSize, DEFAULT_TEXT_SIZE);

                createCoverView(menuIcon, menuTitle, menuBackground);

            } finally {
                a.recycle();
            }
        } else {
            createCoverView(transparentColor, null, null);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        /**
         * Allows the expansion of the menu outside of the view
         */
        ((ViewGroup) getParent()).setClipChildren(false);
        ((ViewGroup) getParent()).setClipToPadding(false);

        /**
         * Saves original layout params
         */
        if (mOriginalWidth == 0 && mOriginalHeight == 0) {
            mOriginalWidth = getLayoutParams().width;
            mOriginalHeight = getLayoutParams().height;
        }

        for (int i = 0; i < getChildCount(); i++) {
            centralizeSubmenu(getChildAt(i));
        }

        /**
         * Adapts the layout to allow vertical expansion, maintaining other attributes
         */
        setCoverViewLayoutParams();

        getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
    }

    /**
     * Sets the submenu views in the center of the menu
     */
    private void centralizeSubmenu(View view) {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        params.setMargins(0, 0, 0, mCoverView.getHeight() / 2 - view.getHeight() / 2);
        view.setLayoutParams(params);
    }

    /**
     * Adds a floating submenu with an icon to the menu. It will appear below previously added submenus.
     *
     * @param iconId          The resource identifier of the drawable that will be displayed as the icon
     * @param onClickListener The callback for when this submenu is clicked
     */
    public FloatingMenu addSubMenu(int iconId, OnClickListener onClickListener) {
        ImageButton subMenu = new ImageButton(getContext());
        addSubMenu(subMenu, onClickListener);
        subMenu.setImageResource(iconId);

        return this;
    }

    /**
     * Adds a floating submenu with a text to the menu. It will appear below previously added submenus.
     *
     * @param text            The title of this submenu
     * @param textColor       The color of the title
     * @param onClickListener The callback for when this submenu is clicked
     */
    public FloatingMenu addSubMenu(CharSequence text, int textColor, OnClickListener onClickListener) {
        TextView subMenu = new TextView(getContext());
        subMenu.setGravity(Gravity.CENTER);
        addSubMenu(subMenu, onClickListener);
        subMenu.setText(text);
        subMenu.setTextColor(textColor);

        return this;
    }

    /**
     * Sets other attributes of the submenu
     */
    private void addSubMenu(View view, OnClickListener onClickListener) {
        view.setBackground(ContextCompat.getDrawable(getContext(), mSubMenuBackgroundResource));
        view.setOnClickListener(onClickListener);
        centralizeSubmenu(view);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.setElevation(mMenuElevation - 2);
        }
        addView(view);
    }

    /**
     * Manages the opening and closing menu animations
     */
    OnClickListener mExpandCollapseClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            /**
             * Prevents the user from opening and closing frenetically the menu. The next action will only happen when the previous one is finished
             */
            if (mLockedCLick) {
                return;
            }
            mLockedCLick = true;

            if (mOnMenuClickListener != null) {
                mOnMenuClickListener.onMenuClick(mCoverView, mIsOpen);
            }

            /**
             * Performs a translation animation for each submenu
             */
            int distance = mMenuExtraMargin;
            if(getChildCount() == 1) { //No animation
                mLockedCLick = false;
            }
            for (int i = getChildCount() - 1; i >= 0; i--) {
                Object tag = getChildAt(i).getTag();
                if (tag != null && tag.toString().equals(STATIC_VIEW)) {
                    continue;
                }
                distance += mSubMenuBetweenPadding;
                ObjectAnimator anim = null;
                if (mIsOpen) {
                    anim = ObjectAnimator.ofFloat(getChildAt(i), "translationY", -distance, 0);
                } else {
                    anim = ObjectAnimator.ofFloat(getChildAt(i), "translationY", 0, -distance);
                }
                anim.setInterpolator(mAnimationInterpolator);
                anim.setDuration(mAnimationDuration);
                anim.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mLockedCLick = false;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                    }
                });
                anim.start();
            }
            mIsOpen = !mIsOpen;
        }
    };

    /**
     * Creates the view that will be shown as the main menu, the one which opens the submenus with a click.
     * It is created as an inflated view containing a textview and an imageview to be placed covering the submenus.
     */
    private void createCoverView(int menuIcon, String menuTitle, Drawable menuBackground) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        mCoverView = (FrameLayout) inflater.inflate(R.layout.cover_view, null);

        setCoverViewLayoutParams();

        mCoverImageView = (ImageView) mCoverView.findViewById(R.id.image);

        if (menuIcon != 0) {
            mCoverImageView.setImageResource(menuIcon);
        }

        mCoverTextView = (TextView) mCoverView.findViewById(R.id.text);

        if (menuTitle != null) {
            mCoverTextView.setText(menuTitle);
        }
        mCoverTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
        mCoverTextView.setTextColor(mTextColor);

        mCoverView.setOnClickListener(mExpandCollapseClickListener);
        mCoverView.setBackground(menuBackground);
        mCoverView.setTag(STATIC_VIEW);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mCoverView.setElevation(mMenuElevation);
        }
        addView(mCoverView);
    }

    /**
     * Sets the layout params for the view that covers the submenus.
     */
    private void setCoverViewLayoutParams() {
        FrameLayout.LayoutParams params = null;
        if (mOriginalWidth != 0 && mOriginalHeight != 0) {
            params = new FrameLayout.LayoutParams(mOriginalWidth, mOriginalHeight);
        } else {
            params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        }
        params.gravity = Gravity.CENTER | Gravity.BOTTOM;

        mCoverView.setLayoutParams(params);
    }

    /**
     * Sets a single same background drawable for all submenus
     *
     * @param subMenuBackgroundResource The resource identifier of the drawable to be used as background
     */
    public FloatingMenu setSubMenuBackgroundResource(int subMenuBackgroundResource) {
        mSubMenuBackgroundResource = subMenuBackgroundResource;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getTag() == null || !child.getTag().toString().equals(STATIC_VIEW)) {
                child.setBackground(ContextCompat.getDrawable(getContext(), subMenuBackgroundResource));
            }
        }

        return this;
    }

    /**
     * Removes a submenu
     *
     * @param position The position of the submenu
     */
    public void removeSubmenu(int position) {
        removeViewAt(position + 1);
    }

    /**
     * Removes all submenus from the menu
     */
    public void removeAllSubmenus() {
        int childCount = getChildCount();
        for(int i = childCount - 1; i > 0; i--) {
            removeViewAt(i);
        }
    }

    /**
     * Gets the background set for the submenus
     *
     * @return The resource identifier of the drawable set for the submenus' background
     */
    public int getSubMenuBackground() {
        return mSubMenuBackgroundResource;
    }

    /**
     * Sets the background for the main view of the menu
     *
     * @param menuBackground The drawable to be used as background
     */
    public FloatingMenu setMenuBackground(Drawable menuBackground) {
        mCoverView.setBackground(menuBackground);
        return this;
    }

    /**
     * Gets the menu's background drawable
     *
     * @return The drawable used as the background for the menu, if any.
     */
    public Drawable getMenuBackground() {
        return mCoverView.getBackground();
    }

    /**
     * Sets an icon on the menu's main view
     *
     * @param menuIcon The resource identifier of the drawable to be set as the icon
     */
    public FloatingMenu setMenuIcon(int menuIcon) {
        mCoverImageView.setImageResource(menuIcon);
        return this;
    }

    /**
     * Sets a display text to be shown on the menu's main view
     *
     * @param menuText The display text
     */
    public FloatingMenu setMenuText(CharSequence menuText) {
        mCoverTextView.setText(menuText);
        return this;
    }

    /**
     * Sets the color of the display text
     *
     * @param menuTextColor The display text's color
     */
    public FloatingMenu setMenuTextColor(int menuTextColor) {
        mCoverTextView.setTextColor(menuTextColor);
        return this;
    }

    /**
     * Sets the size of the display text
     *
     * @param menuTextSize The display text's size
     */
    public FloatingMenu setMenuTextSize(int menuTextSize) {
        mCoverTextView.setTextSize(menuTextSize);
        return this;
    }

    /**
     * Sets an interpolator for the opening menu animation
     *
     * @param animationInterpolator
     * @return
     */
    public FloatingMenu setAnimationInterpolator(Interpolator animationInterpolator) {
        mAnimationInterpolator = animationInterpolator;
        return this;
    }

    /**
     * Sets the opening menu animation duration
     *
     * @param animationDuration The duration in milliseconds
     */
    public FloatingMenu setAnimationDuration(int animationDuration) {
        mAnimationDuration = animationDuration;
        return this;
    }

    /**
     * Sets the padding between the submenus when the menu is expanded.
     *
     * @param subMenuBetweenPadding
     * @return
     */
    public FloatingMenu setSubMenuBetweenPadding(int subMenuBetweenPadding) {
        mSubMenuBetweenPadding = subMenuBetweenPadding;
        return this;
    }

    /**
     * Sets the extra margin between the menu and the submenus
     *
     * @param menuExtraMargin
     * @return
     */
    public FloatingMenu setMenuExtraMargin(int menuExtraMargin) {
        mMenuExtraMargin = menuExtraMargin;
        return this;
    }

    /**
     * Sets the menu and submenus elevation. The submenus' elevation is slightly smaller than the menu's.
     *
     * @param menuElevation
     */
    public FloatingMenu setMenuElevation(float menuElevation) {
        mMenuElevation = menuElevation;
        return this;
    }

    /**
     * Sets a listener that will be triggered when the user opens or closes the menu
     *
     * @param onMenuClickListener
     */
    public FloatingMenu setOnMenuClickListener(MenuClickListener onMenuClickListener) {
        this.mOnMenuClickListener = onMenuClickListener;
        return this;
    }


}
