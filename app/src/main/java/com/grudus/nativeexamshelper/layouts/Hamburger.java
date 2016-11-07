package com.grudus.nativeexamshelper.layouts;

import android.content.Intent;
import android.support.annotation.IdRes;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;
import android.widget.Toast;

import com.grudus.nativeexamshelper.R;
import com.grudus.nativeexamshelper.activities.ExamsMainActivity;
import com.grudus.nativeexamshelper.activities.LoginPageActivity;
import com.grudus.nativeexamshelper.activities.SettingsActivity;
import com.grudus.nativeexamshelper.activities.StatisticMainActivity;
import com.grudus.nativeexamshelper.activities.SubjectsListActivity;
import com.grudus.nativeexamshelper.dialogs.reusable.CustomAlertDialog;
import com.grudus.nativeexamshelper.helpers.normal.ThemeHelper;
import com.grudus.nativeexamshelper.pojos.UserPreferences;

public class Hamburger {

    private final AppCompatActivity activity;
    private final int menuId;
    private final DrawerLayout drawerLayout;

    private final NavigationView navigationView;
    private final UserPreferences userPreferences;

    private int selectedItem;

    public Hamburger(AppCompatActivity activity, @IdRes int menuId, DrawerLayout drawerLayout) {
        this.menuId = menuId;
        this.activity = activity;
        this.drawerLayout = drawerLayout;
        navigationView = (NavigationView) activity.findViewById(menuId);
        userPreferences = new UserPreferences(activity);
    }

    public void setSelectedItem(int selectedItem) {
        this.selectedItem = selectedItem;
    }

    public void setUpNavigationView() {
        setNavigationViewHeaderSize();
        setUpNavigationViewListener();
        setUpTitle();
        navigationView.getMenu().getItem(selectedItem).setChecked(true);
        navigationView.getMenu().getItem(2)
                .setTitle(userPreferences.getLoggedUser().isLogged() ? R.string.menu_item_logout : R.string.menu_item_login);
    }

    private void setNavigationViewHeaderSize() {
        final View header = navigationView.getHeaderView(0);

        header.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                header.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int width = header.getWidth();

                ViewGroup.LayoutParams params = header.getLayoutParams();
                params.width = width;
                params.height = (int) (width * (9f/16f));

                header.setLayoutParams(params);
            }
        });
    }

    private void setUpNavigationViewListener() {
        navigationView.setNavigationItemSelectedListener(item -> {

            switch (item.getItemId()) {
                case R.id.menu_item_change_theme:
                    ThemeHelper.changeToTheme(activity, ThemeHelper.nextTheme());
                    break;

                case R.id.menu_item_web:
                    if (userPreferences.getLoggedUser().isLogged()) {
                        userPreferences.changeLoginStatus(false);
                        Toast.makeText(activity, R.string.toast_logout_text, Toast.LENGTH_SHORT).show();
                        navigationView.getMenu().getItem(2)
                                .setTitle(R.string.menu_item_login);

                    } else {
                        activity.startActivity(new Intent(activity, LoginPageActivity.class));
                    }
                    break;

                case R.id.menu_item_statistics:
                    if (selectedItem != 0) activity.startActivity(new Intent(activity, StatisticMainActivity.class));
                    break;

                case R.id.menu_item_exams:
                    if (selectedItem != 1) activity.startActivity(new Intent(activity, ExamsMainActivity.class));
                    break;

                case R.id.menu_item_edit_subjects:
                    activity.startActivity(new Intent(activity, SubjectsListActivity.class));
                    break;

                case R.id.menu_item_settings:
                    activity.startActivity(new Intent(activity, SettingsActivity.class));
                    break;
                case R.id.menu_item_info:
                    new CustomAlertDialog()
                            .addTitle(activity.getString(R.string.about_title))
                            .addText(activity.getString(R.string.about_text))
                            .show(activity.getFragmentManager(), activity.getString(R.string.tag_dialog_alert));
                    break;
            }
            drawerLayout.closeDrawers();
            return false;
        });
    }

    private void setUpTitle() {
        TextView title = (TextView) navigationView.getHeaderView(0).findViewById(R.id.hamburger_title);

        UserPreferences.User user = userPreferences.getLoggedUser();

        if (user.isLogged())
            title.setText(user.getUsername());
        else
            title.setText(activity.getString(R.string.hamburger_title_logout));
    }

    public void setUpToolbar(Toolbar toolbar) {
        activity.setSupportActionBar(toolbar);

        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar == null)
            return;

        actionBar.setHomeButtonEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.hamburger_icon);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }


}
