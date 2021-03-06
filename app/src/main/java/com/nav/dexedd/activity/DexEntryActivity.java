package com.nav.dexedd.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Joiner;
import com.nav.dexedd.R;
import com.nav.dexedd.adapter.TreeAdapter;
import com.nav.dexedd.model.Ability;
import com.nav.dexedd.model.EvolutionCondition;
import com.nav.dexedd.model.Pokemon;
import com.nav.dexedd.model.Stat;
import com.nav.dexedd.model.StatSpread;
import com.nav.dexedd.persistence.access.DexEntry;
import com.nav.dexedd.structure.Tree;
import com.nav.dexedd.ui.BetterScrollView;
import com.nav.dexedd.ui.TreeLayout;
import com.nav.dexedd.ui.TypeTagView;
import com.nav.dexedd.util.AssetUtil;
import com.nav.dexedd.util.ConversionUtil;
import com.nav.dexedd.util.PokemonTextUtil;
import com.nav.dexedd.util.TypeUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Dex entry activity, manages fragments that show entry information.
 *
 * @author Eduardo Naveda
 * @since 0.0.1
 */
public class DexEntryActivity extends ActionBarActivity {

    private static final String TAG = DexEntryActivity.class.getSimpleName();

    public static final String DEX_ENTRY_TYPE_ID = "dex_entry_type_id";
    public static final String POKEMON_ID = "pokemon_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Bundle extras = getIntent().getExtras();
        TypeUtil.TypeValue dexEntryType = null;
        int pokemonId = 0;
        if (extras != null) {
            dexEntryType = TypeUtil.TypeValue.getTypeValueByValue(extras.getInt(DEX_ENTRY_TYPE_ID));
            pokemonId = extras.getInt(POKEMON_ID);
        }
        // Set the proper theme for this Pokémon's type
        setTheme(TypeUtil.getTypeStyleRes(dexEntryType));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dex_entry);

        Toolbar dexToolBar = (Toolbar) findViewById(R.id.dexedd_tool_bar);
        setSupportActionBar(dexToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            DexEntryFragment dexEntryFragment = new DexEntryFragment();
            Bundle args = new Bundle();
            args.putInt(POKEMON_ID, pokemonId);
            dexEntryFragment.setArguments(args);
            getSupportFragmentManager().beginTransaction().add(R.id.container, dexEntryFragment).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dex_entry, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Fragment containing a dex entry.
     */
    public static class DexEntryFragment extends Fragment {

        private static final String TAG = DexEntryFragment.class.getSimpleName();

        @InjectView(R.id.dex_entry_image) ImageView dexEntryImage;
        @InjectView(R.id.dex_entry_head) FrameLayout dexEntryHead;
        @InjectView(R.id.dex_entry_image_proxy) FrameLayout dexEntryImageProxy;
        @InjectView(R.id.dex_entry_name) TextView dexEntryName;
        @InjectView(R.id.dex_entry_genus) TextView dexEntryGenus;
        @InjectView(R.id.dex_entry_flavor_text) TextView dexEntryFlavorText;
        @InjectView(R.id.dex_entry_primary_type) TypeTagView dexEntryPrimaryType;
        @InjectView(R.id.dex_entry_secondary_type) TypeTagView dexEntrySecondaryType;
        @InjectView(R.id.dex_entry_abilities_content) LinearLayout dexEntryAbilitiesContent;
        @InjectView(R.id.dex_entry_height_meters) TextView dexEntryHeightMeters;
        @InjectView(R.id.dex_entry_height_feet_inches) TextView dexEntryHeightFeetInches;
        @InjectView(R.id.dex_entry_weight_kilograms) TextView dexEntryWeightKilograms;
        @InjectView(R.id.dex_entry_weight_pounds) TextView dexEntryWeightPounds;
        @InjectView(R.id.dex_entry_catch_rate_text) TextView dexEntryCatchRateText;
        @InjectView(R.id.dex_entry_egg_groups_text) TextView dexEntryEggGroupsText;
        @InjectView(R.id.dex_entry_gender_ratio_bar) ProgressBar dexEntryGenderRatioBar;
        @InjectView(R.id.dex_entry_male_ratio) TextView dexEntryMaleRatio;
        @InjectView(R.id.dex_entry_female_ratio) TextView dexEntryFemaleRatio;
        @InjectView(R.id.dex_entry_gender_ratio_content) LinearLayout dexEntryGenderRatioContent;
        @InjectView(R.id.dex_entry_health_points_bar) ProgressBar dexEntryHealthPointsBar;
        @InjectView(R.id.dex_entry_attack_bar) ProgressBar dexEntryAttackBar;
        @InjectView(R.id.dex_entry_defense_bar) ProgressBar dexEntryDefenseBar;
        @InjectView(R.id.dex_entry_special_attack_bar) ProgressBar dexEntrySpecialAttackBar;
        @InjectView(R.id.dex_entry_special_defense_bar) ProgressBar dexEntrySpecialDefenseBar;
        @InjectView(R.id.dex_entry_speed_bar) ProgressBar dexEntrySpeedBar;
        @InjectView(R.id.dex_entry_evolution_chain_content) TreeLayout dexEntryEvolutionChainContent;
        @InjectView(R.id.dex_entry_scroller) BetterScrollView dexEntryScroller;

        private ActionBar toolBar;

        /**
         * Tool bar drawable.
         */
        private ColorDrawable toolBarTypeColorDrawable;

        /**
         * Alpha level for the tool bar drawable.
         */
        private int toolBarDrawableAlpha = 0;

        public DexEntryFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            toolBar = ((ActionBarActivity) getActivity()).getSupportActionBar();

            View rootView = inflater.inflate(R.layout.fragment_dex_entry, container, false);

            ButterKnife.inject(this, rootView);

            // Always set the scrolling to the top when creating a new view for this fragment
            dexEntryScroller.post(new Runnable() {
                public void run() {
                    dexEntryScroller.scrollTo(0, 0);
                }
            });

            if (getArguments() != null) {

                final DexEntry dexEntry = DexEntry.create(getActivity().getApplicationContext(),
                        getArguments().getInt(DexEntryActivity.POKEMON_ID));

                // Get the pokemon object
                Pokemon pokemon = dexEntry.getPokemon();

                //  Setting up the tool bar
                initToolBar(PokemonTextUtil.getFormattedDexNumber(pokemon.getDexNumber()), pokemon.getPrimaryType());

                // Setting up the parallax scrolling effect
                initParallaxScrolling(PokemonTextUtil.getFormattedDexNumber(pokemon.getDexNumber()), pokemon.getName());

                // Setting up Pokémon information views
                setUpPokemonInformationViews(inflater, pokemon);

                return rootView;
            } else {
                return rootView;
            }
        }

        /**
         * Sets the tool bar drawable and text.
         *
         * @param dexNumber   The Pokémon's dex number
         * @param primaryType The Pokémon's primary type, used to get the actual drawable
         */
        private void initToolBar(String dexNumber, com.nav.dexedd.model.Type primaryType) {
            toolBarTypeColorDrawable =
                    new ColorDrawable(getResources().getColor(
                            TypeUtil.getTypeColorRes(TypeUtil.TypeValue.getTypeValueByValue(primaryType.getId()))));

            toolBarTypeColorDrawable.setAlpha(toolBarDrawableAlpha);

            toolBar.setBackgroundDrawable(toolBarTypeColorDrawable);

            // Drawable callback for the tool bar drawable so it can register itself to the
            // tool bar on each invalidation, this is only necessary for API level =< 17
            Drawable.Callback drawableCallback = new Drawable.Callback() {
                @Override
                public void invalidateDrawable(Drawable drawable) {
                    toolBar.setBackgroundDrawable(drawable);
                }

                @Override
                public void scheduleDrawable(Drawable who, Runnable what, long when) {
                }

                @Override
                public void unscheduleDrawable(Drawable who, Runnable what) {
                }
            };

            // Registering the callback
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                toolBarTypeColorDrawable.setCallback(drawableCallback);
            }

            toolBar.setTitle(dexNumber);
        }

        /**
         * Sets up the parallax scrolling-like behavior.
         *
         * @param dexNumber The Pokémon's dex number, needed to change the tool bar text on scroll changes
         * @param name      The Pokémon's name, needed to change the tool bar text on scroll changes
         */
        private void initParallaxScrolling(String dexNumber, String name) {

            final String _name = name;
            final String _dexNumber = dexNumber;

            FrameLayout.LayoutParams dexEntryImageLayoutParams = (FrameLayout.LayoutParams) dexEntryImage
                    .getLayoutParams();

            // Normal and max image bottom margins for the main dex entry image these are used to create
            // some sort of parallax scrolling
            final int dexEntryImageMarginBottom = dexEntryImageLayoutParams.bottomMargin;
            final int dexEntryImageMaxMarginBottom = getResources()
                    .getDimensionPixelSize(R.dimen.dex_entry_image_max_bottom_margin);

            // These top padding values are used for the same thing as the margins
            final int dexEntryNamePaddingTop = dexEntryName.getPaddingTop();
            final int dexEntryNameMaxPaddingTop = getResources()
                    .getDimensionPixelSize(R.dimen.dex_entry_name_max_top_padding);

            // Same story with these normal and max scale values
            TypedValue outValue = new TypedValue();
            getResources().getValue(R.dimen.dex_entry_image_min_scale, outValue, true);
            final float dexEntryPicMinScale = outValue.getFloat();

            // An animation is created using the a variant of ScrollView that notifies the changes in its
            // internal scrolling
            BetterScrollView.OnScrollChangedListener onScrollChangedListener = new BetterScrollView
                    .OnScrollChangedListener() {

                private boolean isLimitHeightSet = false;
                private int limitHeight = 0;
                private Rect boundsRect = new Rect();

                @Override
                public void onScrollChanged(ScrollView scrollView, int l, int t, int oldl, int oldt) {

                    // Sets the clamping height
                    if (!isLimitHeightSet) {
                        limitHeight = dexEntryHead.getHeight() - toolBar.getHeight();
                        isLimitHeightSet = true;
                    }

                    // Get the ScrollView touchable bounds on the screen
                    scrollView.getHitRect(boundsRect);

                    // Compute the a ratio relative to the limit height
                    float ratio = (float) Math.min(Math.max(t, 0), limitHeight) / limitHeight;
                    toolBarDrawableAlpha = (int) (ratio * 255);
                    toolBarTypeColorDrawable.setAlpha(toolBarDrawableAlpha);

                    // Calculate new dex image scale relative to the ratio
                    float dexEntryImageNewScale = Math.max(dexEntryPicMinScale, ((1 - ratio / 2) * 1));
                    dexEntryImage.setScaleX(dexEntryImageNewScale);
                    dexEntryImage.setScaleY(dexEntryImageNewScale);

                    // Calculate new image bottom margin relative to the ratio
                    int dexEntryImageNewBottomMargin = Math.max(dexEntryImageMarginBottom,
                            Math.min(dexEntryImageMaxMarginBottom,
                                    (int) ((ratio * 1.5) *
                                            dexEntryImageMaxMarginBottom)));
                    ((FrameLayout.LayoutParams) dexEntryImage
                            .getLayoutParams()).bottomMargin = dexEntryImageNewBottomMargin;
                    dexEntryImage.requestLayout(); // Request the layout with new parameters

                    // Same thing as with the margin
                    int dexEntryNameNewTopPadding = Math.max(dexEntryNamePaddingTop,
                            Math.min(dexEntryNameMaxPaddingTop,
                                    (int) ((ratio) * dexEntryNameMaxPaddingTop)));
                    dexEntryName.setPadding(dexEntryName.getPaddingLeft(), dexEntryNameNewTopPadding,
                            dexEntryName.getPaddingRight(), dexEntryName.getPaddingBottom());

                    // Determine if the Pokémon name is on the screen so it's shown on the tool bar instead of the number
                    if (!dexEntryName.getLocalVisibleRect(boundsRect)) {
                        toolBar.setTitle(_name);
                    } else {
                        toolBar.setTitle(_dexNumber);
                    }
                }
            };

            // Set that overly complicated listener from above
            dexEntryScroller.setOnScrollChangedListener(onScrollChangedListener);
        }

        /**
         * Sets up pokemon information views.
         *
         * @param inflater The fragment's layout inflater.
         * @param pokemon  The dex entry Pokémon object
         */
        private void setUpPokemonInformationViews(final LayoutInflater inflater, Pokemon pokemon) {

            final Integer speciesId = pokemon.getSpeciesId();
            final String dexNumber = PokemonTextUtil.getFormattedDexNumber(pokemon.getDexNumber());
            final String name = pokemon.getName();
            final String genus = pokemon.getGenus();
            final String flavorText = pokemon.getFlavorText();
            final com.nav.dexedd.model.Type primaryType = pokemon.getPrimaryType();
            final com.nav.dexedd.model.Type secondaryType = pokemon.getSecondaryType();
            final List<Ability> abilities = pokemon.getAbilities();
            final StatSpread statSpread = pokemon.getBaseStats();
            final Tree<Pokemon> evolutionChain = pokemon.getEvolutionChain();

            // Set basic stuff
            dexEntryHead.setBackgroundResource(
                    TypeUtil.getTypeBackgroundRes(TypeUtil.TypeValue.getTypeValueByValue(primaryType.getId())));

            dexEntryName.setText(name);
            dexEntryGenus.setText(genus + " " + getResources().getString(R.string.pokemon));
            dexEntryFlavorText.setText(flavorText);

            Drawable art = AssetUtil.getDrawableAsset(getActivity(),
                    "images/pokemon/art/" + speciesId + ".png");
            if (art != null) {
                dexEntryImage.setImageDrawable(art);
            }

            // todo change images among the available ones for the species
            dexEntryImageProxy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(getActivity(), "Doesn't work yet", Toast.LENGTH_LONG).show();
                }
            });

            // Set type views
            dexEntryPrimaryType.setType(TypeUtil.TypeValue.getTypeValueByValue(primaryType.getId()));

            if (secondaryType != null) {
                dexEntrySecondaryType.setType(TypeUtil.TypeValue.getTypeValueByValue(secondaryType.getId()));
            } else {
                dexEntrySecondaryType.setType(TypeUtil.TypeValue.NONE);
            }

            // Set up ability rows
            for (final Ability ability : abilities) {
                View dexEntryAbilityRowView =
                        inflater.inflate(R.layout.dex_entry_ability_row, dexEntryAbilitiesContent, false);
                dexEntryAbilityRowView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        View abilityDialogView = inflater.inflate(R.layout.ability_dialog, null);
                        AlertDialog.Builder builder =
                                new AlertDialog.Builder(getActivity()).setView(abilityDialogView);
                        final AlertDialog dialog = builder.create();
                        TextView abilityName = ButterKnife.findById(abilityDialogView, R.id.ability_name);
                        TextView abilityEffect = ButterKnife.findById(abilityDialogView, R.id.ability_effect);
                        abilityEffect.setMovementMethod(LinkMovementMethod.getInstance());
                        abilityName.setText(ability.getName());
                        if (ability.isHidden()) {
                            abilityName.append(" (" + getString(R.string.ability_hidden) + ")");
                        }
                        abilityEffect.setText(PokemonTextUtil.processPokemonText(getActivity(),
                                ability.getEffect()));
                        dialog.show(); // todo when this dialog gets moved to its rightful place, destroy the dialog on onDestroy
                    }
                });
                TextView abilityName = (TextView) dexEntryAbilityRowView.findViewById(R.id.ability_name);
                TextView abilityFlavorText =
                        (TextView) dexEntryAbilityRowView.findViewById(R.id.ability_flavor_text);
                abilityName.setText(ability.getName());
                if (ability.isHidden()) {
                    abilityName.append(" (" + getString(R.string.ability_hidden_short) + ")");
                }
                abilityFlavorText.setText(ability.getFlavorText());
                dexEntryAbilitiesContent.addView(dexEntryAbilityRowView);
            }

            // Height
            dexEntryHeightMeters.setText(
                    String.format(getString(R.string.standard_number_format), pokemon.getHeight()) +
                            getString(R.string.meter_unit));
            dexEntryHeightFeetInches.setText(ConversionUtil.toFeetInches(pokemon.getHeight()));

            // Weight
            dexEntryWeightKilograms.setText(
                    String.format(getString(R.string.standard_number_format), pokemon.getWeight()) +
                            getString(R.string.kilogram_unit));
            dexEntryWeightPounds.setText(String.format(getString(R.string.standard_number_format),
                    ConversionUtil.toPounds(pokemon.getWeight())) +
                    getString(R.string.pound_unit));

            // Catch rate
            dexEntryCatchRateText.setText(pokemon.getCatchRate().toString());

            // Egg groups
            dexEntryEggGroupsText.setText(pokemon.getEggGroupsAsString("•"));

            // Hatch counter
            // Todo: hatch counter

            // Gender rate
            Double genderRatio = pokemon.getGenderRatio();
            if (genderRatio < 0) {
                dexEntryGenderRatioContent.getChildAt(0).setVisibility(View.GONE);
                dexEntryGenderRatioContent.getChildAt(1).setVisibility(View.GONE);
                dexEntryGenderRatioContent.getChildAt(2).setVisibility(View.VISIBLE);
            } else {
                dexEntryGenderRatioBar.setProgress(100 - genderRatio.intValue());
                dexEntryMaleRatio.setText(
                        String.format(getString(R.string.standard_number_format), 100 - genderRatio) + "%");
                dexEntryFemaleRatio
                        .setText(String.format(getString(R.string.standard_number_format), genderRatio) + "%");
            }

            // Base stats
            dexEntryHealthPointsBar.setProgress(
                    Double.valueOf(((double) statSpread.getHealthPoints().getBase() / Stat.MAX_STAT_VALUE) * 100)
                            .intValue());
            dexEntryAttackBar.setProgress(
                    Double.valueOf(((double) statSpread.getAttack().getBase() / Stat.MAX_STAT_VALUE) * 100).intValue());
            dexEntryDefenseBar.setProgress(
                    Double.valueOf(((double) statSpread.getDefense().getBase() / Stat.MAX_STAT_VALUE) * 100).intValue());
            dexEntrySpecialAttackBar.setProgress(
                    Double.valueOf(((double) statSpread.getSpecialAttack().getBase() / Stat.MAX_STAT_VALUE) * 100)
                            .intValue());
            dexEntrySpecialDefenseBar.setProgress(
                    Double.valueOf(((double) statSpread.getSpecialDefense().getBase() / Stat.MAX_STAT_VALUE) * 100)
                            .intValue());
            dexEntrySpeedBar.setProgress(
                    Double.valueOf(((double) statSpread.getSpeed().getBase() / Stat.MAX_STAT_VALUE) * 100).intValue());

            // Evolution chain
            dexEntryEvolutionChainContent.setTreeAdapter(new TreeAdapter<Pokemon>(
                    getActivity(),
                    R.layout.dex_entry_evolution_chain_row,
                    evolutionChain, TreeAdapter.TraversalType.PRE_ORDER) {

                @Override
                public View getView(int layoutResourceId, Tree<Pokemon> evolution) {
                    View view = LayoutInflater.from(getContext())
                            .inflate(layoutResourceId, dexEntryEvolutionChainContent, false);

                    LinearLayout.LayoutParams layoutParams = ((LinearLayout.LayoutParams) view.getRootView().getLayoutParams());
                    layoutParams.setMargins(
                            getContext().getResources()
                                    .getDimensionPixelSize(R.dimen.dex_entry_evolution_chain_row_indentation) * evolution.getRank(),
                            layoutParams.topMargin, layoutParams.rightMargin, layoutParams.bottomMargin);

                    ImageView evolutionPartyIcon = ButterKnife.findById(view, R.id.evolution_party_icon);
                    TextView evolutionName = ButterKnife.findById(view, R.id.evolution_name);
                    TextView evolutionConditions = ButterKnife.findById(view, R.id.evolution_conditions);

                    Drawable partyIcon = AssetUtil.getDrawableAsset(getContext(),
                            "images/pokemon/party_icons/" + evolution.getData().getSpeciesId() + ".png");
                    if (partyIcon != null) {
                        evolutionPartyIcon.setImageDrawable(partyIcon);
                    }

                    evolutionName.setText(evolution.getData().getName());

                    List<String> conditionStrings = new ArrayList<>();
                    for (EvolutionCondition condition : evolution.getData().getEvolutionConditions()) {
                        conditionStrings.add(condition.toString(getContext(), evolution.getData()));
                    }

                    evolutionConditions.setText(Joiner.on("\n")
                            .join(conditionStrings));

                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getContext(), DexEntryActivity.class);
                        }
                    });

                    return view;
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
    }

}
