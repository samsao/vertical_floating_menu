package com.samsao.verticalfloatingmenu;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Toast;

import com.samsao.floatingverticalmenu.FloatingMenu;


public class MainActivitySample extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_sample);

        FloatingMenu floatingMenu = (FloatingMenu) findViewById(R.id.floatingMenu);
        floatingMenu
                .setSubMenuBackgroundResource(R.drawable.pink_circle_button_click)
                .addSubMenu(android.R.drawable.ic_menu_my_calendar, new Action("Opening calendar..."))
                .addSubMenu(android.R.drawable.ic_menu_call, new Action("Calling Jeffrey..."))
                .addSubMenu(android.R.drawable.ic_menu_manage, new Action("Opening settings..."))
                .setSubMenuBetweenPadding(250)
                .setMenuExtraMargin(50)
                .setMenuTextColor(getResources().getColor(R.color.green))
                .setOnMenuClickListener(new FloatingMenu.MenuClickListener() {
                    @Override
                    public void onMenuClick(View coverView, boolean isOpen) {
                        RotateAnimation rotateAnim = null;
                        if (isOpen) {
                            rotateAnim = new RotateAnimation(45, 0, coverView.getHeight() / 2, coverView.getWidth() / 2);
                        } else {
                            rotateAnim = new RotateAnimation(0, 45, coverView.getHeight() / 2, coverView.getWidth() / 2);
                        }
                        rotateAnim.setDuration(200);
                        rotateAnim.setInterpolator(new OvershootInterpolator());
                        rotateAnim.setFillAfter(true);
                        coverView.startAnimation(rotateAnim);
                    }
                });
    }

    private class Action implements View.OnClickListener{
        String mText;

        public Action(String text){
            mText = text;
        }

        @Override
        public void onClick(View v) {
            Toast.makeText(MainActivitySample.this, mText, Toast.LENGTH_SHORT).show();
        }
    }

}
