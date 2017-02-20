package codewrencher.gifit.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;

import codewrencher.gifit.objects.complex.gif_chain.GifChain;
import codewrencher.gifit.tools.Torch;
import codewrencher.gifit.ui.fragments.BaseFragment;

/**
 * Created by Gene on 4/3/2016.
 * _____          _   __          __                  _
 * / ____|        | |  \ \        / /                 | |
 * | |     ___   __| | __\ \  /\  / / __ ___ _ __   ___| |__   ___ _ __
 * | |    / _ \ / _` |/ _ \ \/  \/ / '__/ _ \ '_ \ / __| '_ \ / _ \ '__|
 * | |___| (_) | (_| |  __/\  /\  /| | |  __/ | | | (__| | | |  __/ |
 * \_____\___/ \__,_|\___| \/  \/ |_|  \___|_| |_|\___|_| |_|\___|_|
 */
public class GeneralActivity extends AppCompatActivity {
    protected int display_width;
    protected int display_height;
    protected FragmentPager fragment_pager;
    protected CustomViewPager view_pager;
    public Torch torch;
    protected GifChain received_gif_chain;
    protected GifChain working_gif_chain;
    protected BaseFragment fragment;
    protected String action;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setDisplayWidth();
        this.setDisplayHeight();
    }

    public int getDisplayWidth() {
        return this.display_width;
    }
    public int getDisplayHeight() {
        return this.display_height;
    }
    public FragmentPager getFragmentPager() {
        return this.fragment_pager;
    }
    public CustomViewPager getViewPager() {
        return this.view_pager;
    }

    /**---------------------------------------------------------------------------------------------
     * Setters
     */
    /***********************************************************************************************
     * Set Global activity variables available from all fragments
     */
    public void setAction( String action ) {
        this.action = action;
    }
    public void setFragment( BaseFragment fragment ) {
        this.fragment = fragment;
    }

    protected void setDisplayWidth() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        this.display_width = metrics.widthPixels;
    }
    protected void setDisplayHeight() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        display_height = metrics.heightPixels;
    }

    public void setReceivedGifChain( GifChain received_gif_chain ) {
        this.received_gif_chain = received_gif_chain;
    }
    public void setWorkingGifChain( GifChain working_gif_chain ) {
        this.working_gif_chain = working_gif_chain;
    }

    /**---------------------------------------------------------------------------------------------
     * Getters
     */
    /***********************************************************************************************
     * Get Global activity variables available from all fragments
     */
    public String getAction() {
        return this.action;
    }
    public BaseFragment getFragment() {
        return this.fragment;
    }
    public GifChain getReceivedGifChain() {
        return this.received_gif_chain;
    }
    public GifChain getWorkingGifChain() {
        return this.working_gif_chain;
    }
    /**---------------------------------------------------------------------------------------------
     * Erasers
     */
    /***********************************************************************************************
     * Clear Global activity variables available from all fragments
     */
    public void clearReceivedGifChain() {
        this.received_gif_chain = null;
    }
    public void clearWorkingGifChain() {
        this.working_gif_chain = null;
    }

    public BaseFragment getCurrentFragment() {
        if (this.fragment_pager != null) {
            return this.fragment_pager.getCurrentFragment();
        } else {
            return null;
        }
    }
    public int getCurrentTab(){
        return view_pager.getCurrentItem();
    }
    public void removeFragmentByIndex(int index){
        if (index != this.getCurrentTab()) {

            fragment_pager.removeFragmentByIndex(index);
        }
    }
    public void removeCurrentTabAndMoveDown(Fragment fragment, View view){
        int current_index = this.getCurrentTab();

        if (current_index > 0) {
            fragment_pager.removeCurrentFragment(fragment, current_index);
            view_pager.setAdapter(fragment_pager);
            view_pager.setCurrentItem(current_index - 1);
        }
    }
}
