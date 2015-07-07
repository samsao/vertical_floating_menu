# Vertical Floating Menu
An animated, customizable floating menu that expands vertically.

Image

##Adding to your project

code

##Usage
###From layout
1) Add the application namespace to the root element in the XML 
	
	xmlns:app="http://schemas.android.com/apk/res-auto"

2) Add the floating menu element 
 
Here you can customize the menu button's *background*, *elevation*, *text*, *textColor* and *icon*:

	<com.samsao.floatingverticalmenu.FloatingMenu
        android:layout_width="75dp"
        android:layout_height="75dp"
        android:layout_alignParentBottom="true"
        android:layout_alignParentRight="true"
        app:fmBackground="@drawable/circle"
        app:fmIcon="@android:drawable/bottom_bar"
        app:fmElevation="10dp"
        app:fmText="Menu"
        app:fmTextColor="@android:color/holo_blue_bright"
        />

3) Add submenus  
They can be any customized view (but remember, they must be smaller than the menu button). Set the *click* action in the same way you would do to any view. 

	<com.samsao.floatingverticalmenu.FloatingMenu
        ...
        >
        <Button
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:onClick="doSomething"
            android:text="Option 1"/>

        <Button
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:onClick="doSomethingDifferent"
            android:text="Option 2"/>
            
        </com.samsao.floatingverticalmenu.FloatingMenu>
        
###From code
1) Create the menu
	
	FloatingMenu floatingMenu = new FloatingMenu(this);
    floatingMenu.setLayoutParams(new RelativeLayout.LayoutParams(200,200));
    
2) Customize the menu's button
	
	floatingMenu.setMenuBackground(ContextCompat.getDrawable(this, R.drawable.pink_circle))
                .setMenuIcon(android.R.drawable.ic_menu_rotate)
                .setMenuText("Menu", R.color.blue)
                .setMenuElevation(10);
3) Customize the menu's animation

	floatingMenu.setAnimationDuration(400)
                .setAnimationInterpolator(new AccelerateInterpolator());
                
4) Create and customize the submenus  
You can set a background for all the submenus and set the distance between them. You can create submenus with an icon or a text and set their action on *click*.

	floatingMenu.setSubMenuBackgroundResource(R.drawable.pink_circle)
                .setSubMenuBetweenPadding(250)
                .addSubMenu(android.R.drawable.ic_menu_my_calendar, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Do something amazing
                    }
                })
                .addSubMenu("Submenu", R.color.green, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Do something amazing
                    }
                });
5) You can still listen to the menu's *click* event if you'd like for example to add a cool animation to it.

	floatingMenu.setOnMenuClickListener(new FloatingMenu.MenuClickListener() {
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
  