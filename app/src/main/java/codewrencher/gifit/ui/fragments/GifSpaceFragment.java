package codewrencher.gifit.ui.fragments;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;

import codewrencher.gifit.R;
import codewrencher.gifit.helpers.async.Animator;
import codewrencher.gifit.helpers.async.interfaces.SharingManagerListener;
import codewrencher.gifit.helpers.share_gif.GifItSharingController;
import codewrencher.gifit.helpers.share_gif.SharingManager;
import codewrencher.gifit.objects.complex.gif_chain.GifChain;
import codewrencher.gifit.tools.LayoutBuilder;

import static com.google.android.gms.internal.zzip.runOnUiThread;

/**
 * Created by Gene on 2/28/2016.
 * _____          _   __          __                  _
 * / ____|        | |  \ \        / /                 | |
 * | |     ___   __| | __\ \  /\  / / __ ___ _ __   ___| |__   ___ _ __
 * | |    / _ \ / _` |/ _ \ \/  \/ / '__/ _ \ '_ \ / __| '_ \ / _ \ '__|
 * | |___| (_) | (_| |  __/\  /\  /| | |  __/ | | | (__| | | |  __/ |
 * \_____\___/ \__,_|\___| \/  \/ |_|  \___|_| |_|\___|_| |_|\___|_|
 */
public class GifSpaceFragment extends DetailsFragment implements SharingManagerListener {

    private static final String tag = "GifSpaceFragment";

    private GifItSharingController gif_sharing_controller;
    private SharingManager old_sharing_manager;
    public GridView grid_view;
    protected GifChain gif_chain;
    public int top_margin;
    public int bottom_margin;
    private Boolean help_overlay_open;
    private View help_view;

    /***********************************************************************************************
     * Empty Default Constructor
     */
    public GifSpaceFragment() {}

    /***********************************************************************************************
     * Static method used to create the Fragment
     * @param args  : arguments set by Fragment Pager and used onCreate
     * @return      : this fragment instance
     */
    public static GifSpaceFragment newInstance(Bundle args) {
        GifSpaceFragment fragment = new GifSpaceFragment();
        fragment.setArguments(args);

        return fragment;
    }

    public GifChain getGifChain() {
        return this.gif_chain;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.help_overlay_open = false;

        this.tab_index = 1;
        this.fragment_layout_id = R.layout.fragment_gif_space;
        /* App to App code
        this.gif_sharing_controller = new GifItSharingController(this, null);
        */
        this.old_sharing_manager = new SharingManager(this);
        if ( getArguments() != null ) {
            if ( getArguments().get("action") != null) {
                this.action = (String) getArguments().get("action");
            }
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        this.setShareOnClickListener();
        this.setCheckOnClickListener();
        this.setOnGifSpeedChangeListener();
        this.setTListener();
        this.performInitialLoadingActions();

        this.configureGears();

        return fragment_view;
    }

    /**
     * Decide whether to simply load the camera shots you just took, or to chain somone elses gif to these shots
     * Chaining will require first loading the target Gif frames from local storage
     * They get downloaded before the camera and this fragment are initiated
     */
    private void performInitialLoadingActions() {
        /**
         * Received Gif Chain is set only after the user clicks on an incoming notification
         * At this point we are either:
         *                              Viewing the incoming Gif
         *                              Already chose to extend it and loading our framed
         * Received Gif Chain will be Cleared explicitly after the new extended chain is shared
         */

        this.gif_chain = (GifChain) stash.get("gif_chain");

        this.fragment_view.findViewById( R.id.share ).setVisibility(View.VISIBLE);
        this.fragment_view.findViewById(R.id.chain).setVisibility(View.INVISIBLE);
        this.fragment_view.findViewById(R.id.rechain).setVisibility(View.INVISIBLE);

        /** After getting the correct Gif Chain, reset the saved action and received Gif Chain so that
          * the camera can be used as normal once flipped to it */

        this.getSavedActivity().setAction("captured");

        /** Now create the UI for the current Gif Chain*/
        this.gif_chain.setFragment(this);
        this.gif_chain.setActivity(getSavedActivity());
        this.gif_chain.setChainType(this.action);

        if (this.gif_chain.getGif().getSaveFilePath() != null) {
            gif_chain.setFragment(this);
            gif_chain.getGif().setFragment(this);

            gif_chain.getGif().loadUIObject();
            gif_chain.reloadVideoFrames();
        } else {
            this.gif_chain.createUIObject();
        }
    }

    /***********************************************************************************************
     * Encodes images from Frames into a Gif
     * Saves the gif, and resets some flags
     */
    private void createGif(Runnable callback) {

        this.gif_chain.setUpGif();
        this.gif_chain.getGif().setCallback(callback);
        this.gif_chain.getGif().createUIObject();
    }

    /***
     * Set t on click listener - opens and closes edit text box
     */
    private void setTListener() {
        ImageView T = (ImageView) fragment_view.findViewById(R.id.T);

        T.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText add_text_view = (EditText) fragment_view.findViewById(R.id.add_text);

                if (add_text_view.getVisibility() == View.VISIBLE) {
                    add_text_view.setVisibility(View.GONE);
                } else {
                    add_text_view.setVisibility(View.VISIBLE);
                }
            }
        });
    }
    /***********************************************************************************************
     * This text box can be used to add text to your gif
     * The gif must be reloaded after you are done typing so that the text can be added to every frame
     */
    public void showEditTextBox() {

        EditText add_text_view = (EditText) fragment_view.findViewById(R.id.add_text);
        add_text_view.setVisibility(View.VISIBLE);

        add_text_view.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {

                    gif_chain.getGif().createTextSprite(String.valueOf(v.getText()));

                    return false;
                }
                return false;
            }
        });
    }

    private void shareGif() {
        if (! old_sharing_manager.sharing_menu_active) {

            Thread thread = new Thread() {
                @Override
                public void run() {
                    try {
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                gif_chain.getGif().saveGif("perm", null);
                                File gif_file = new File(gif_chain.getGif().getSaveFilePath());

                                old_sharing_manager.openShareImageDialog(gif_file);
                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            thread.start();
        }
    }

    /***********************************************************************************************
     * Animates the whole Layout view in a fling up
     * Removes this fragment from the Activity and switches to the next one down - this is done
     * in the animation listener after the fling animation is finished
     */
    public void destroy() {
        Animator animator = new Animator( getDisplayWidth(),  getDisplayHeight() );
        animator.registerListener(this);
        animator.setAnimatedView( fragment_view );
        animator.setAnimation(fragment_view, "fling_up");
        animator.animate(fragment_view);
    }
    // gesture listeners ---------------------------------------------------------------------------
    private void setCheckOnClickListener() {
        ImageView check = (ImageView) fragment_view.findViewById(R.id.check);
        check.setOnClickListener(new View.OnClickListener() {

            final ImageView gif_text_view = (ImageView) fragment_view.findViewById(R.id.gif_text);
            final ImageView check = (ImageView) fragment_view.findViewById(R.id.check);
            final ImageView glove = (ImageView) fragment_view.findViewById(R.id.glove);

            @Override
            public void onClick(View v) {

                if (gif_chain.getGif().getText() != null && gif_chain.getGif().getText() != "") {
                    gif_chain.addTextToFrames(gif_text_view);
                    createGif(null);
                } else {
                    createGif(null);
                }
                /**
                 * gif_text_view must be made invisible here or our paging will be locked
                 * It is instead made invisible in Gif() when the gif is loaded
                 */
                check.setVisibility(View.GONE);
                glove.setVisibility(View.GONE);

                unlockPaging();
            }
        });
    }
    private void setOnGifSpeedChangeListener() {
        SeekBar seekbar = (SeekBar) fragment_view.findViewById(R.id.gif_animation_speed);
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekbar) {
                ImageView check = (ImageView) fragment_view.findViewById(R.id.check);
                check.setVisibility(View.VISIBLE);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekbar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                int gif_speed_progress = (105 - progress) * 10; /**  << 1050  550  50 >>  */
                gif_chain.getGif().setGifSpeed(gif_speed_progress);
            }
        });
    }

    private void setShareOnClickListener() {
        ImageButton share = (ImageButton) fragment_view.findViewById(R.id.share);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareGif();
            }
        });
    }

    /**
     * Configure actions on gear press
     */
    public void configureGears() {
        ImageButton gears = (ImageButton) fragment_view.findViewById(R.id.gears);
        gears.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (! help_overlay_open) {
                    displayHelpOverlay();
                } else {
                    closeHelpOverlay();
                }
            }
        });
    }

    /**
     * Displays a help overlay for the current fragment
     */
    private void displayHelpOverlay() {
        help_overlay_open = true;
        this.help_view = LayoutBuilder.addToLayout(R.layout.overlay_help_gif_space, R.id.image_container, fragment_view);

        ImageButton close_help = (ImageButton) help_view.findViewById(R.id.close_help);
        close_help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeHelpOverlay();
            }
        });
    }

    /**
     * Closes the help overlay
     */
    private void closeHelpOverlay() {
        help_overlay_open = false;
        LayoutBuilder.removeFromLayout(help_view, R.id.image_container, fragment_view);
    }

    @Override
    public void onFlingUpDetected() {
        shareGif();
    }
    // helper listeners ----------------------------------------------------------------------------
    @Override
    public void onSharingDialogOpened(String msg) {
        fragment_view.findViewById(R.id.loaded_gif_container).setVisibility(View.INVISIBLE);
    }

    @Override
    public void onSharingDialogClosed(String msg) {
        fragment_view.findViewById(R.id.loaded_gif_container).setVisibility(View.VISIBLE);
    }

    @Override
    public void onAnimationFinished( String animation_type, View animated_view ) {

        if ( animation_type.equals("fling_up") && animated_view == fragment_view ) {
   //         this.getSavedActivity().removeCurrentTabAndMoveDown( this, fragment_view );
        }
    }
}

