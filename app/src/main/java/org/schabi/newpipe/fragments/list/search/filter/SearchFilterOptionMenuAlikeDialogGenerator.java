// Created by evermind-zz 2022, licensed GNU GPL version 3 or later

package org.schabi.newpipe.fragments.list.search.filter;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.search.filter.FilterGroup;
import org.schabi.newpipe.extractor.search.filter.FilterItem;
import org.schabi.newpipe.util.ServiceHelper;

import java.util.List;

import androidx.annotation.NonNull;

import static android.util.TypedValue.COMPLEX_UNIT_DIP;

public class SearchFilterOptionMenuAlikeDialogGenerator extends BaseSearchFilterUiDialogGenerator {
    private static final Integer NO_RESIZE_VIEW_TAG = 1;
    private static final float FONT_SIZE_SELECTABLE_VIEW_ITEMS_IN_DIP = 18f;
    private static final float VIEW_ITEMS_MIN_WIDTH_IN_DIP = 168f;
    private final LinearLayout globalLayout;

    public SearchFilterOptionMenuAlikeDialogGenerator(final StreamingService service,
                                                      final ViewGroup root,
                                                      final Context context,
                                                      final Callback callback) {
        super(service.getSearchQHFactory(), callback, context);
        this.globalLayout = createGlobalLayout();
        root.addView(globalLayout);
    }

    @Override
    public void createSearchUI() {
        initContentFiltersUi(contentFilterWorker);
        initSortFiltersUi(sortFilterWorker);
        measureWidthOfChildrenAndResizeToWidest();
        // make sure that only sort filters relevant to selected content filter are shown
        showSortFilterContainerUI();
    }

    /**
     * Resize all width of {@link #globalLayout} children without tag {@link #NO_RESIZE_VIEW_TAG}.
     * <p>
     * Initially this method was only used to resize the {@link #createSeparatorLine()} width
     * but as also children view elements are set to the widest child.
     * <p>
     * Reasons:
     * 1. Separator lines should be as wide as the widest UI element but this
     * can only be determined on runtime
     * 2. Other view elements more specific checkable/selectable should also
     * expand their width over the complete dialog width to be easier to select
     */
    private void measureWidthOfChildrenAndResizeToWidest() {
        showAllAvailableSortFilters();

        // initialize width with a passable default width
        int widestViewInPx = (int) dipToPixels(context, VIEW_ITEMS_MIN_WIDTH_IN_DIP);
        final int noOfChildren = globalLayout.getChildCount();

        for (int x = 0; x < noOfChildren; x++) {
            final View childView = globalLayout.getChildAt(x);
            childView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            final int width = childView.getMeasuredWidth();
            if (width > widestViewInPx) {
                widestViewInPx = width;
            }
        }

        for (int x = 0; x < noOfChildren; x++) {
            final View childView = globalLayout.getChildAt(x);

            if (childView.getTag() != NO_RESIZE_VIEW_TAG) {
                final ViewGroup.LayoutParams layoutParams = childView.getLayoutParams();
                layoutParams.width = widestViewInPx;
                childView.setLayoutParams(layoutParams);
            }
        }
    }

    @Override
    protected void createTitle(final String name,
                               final List<View> titleViewElements) {
        final TextView titleView = createTitleText(name);
        titleView.setTag(NO_RESIZE_VIEW_TAG);
        final View separatorLine = createSeparatorLine();
        final View separatorLine2 = createSeparatorLine();
        final View separatorLine3 = createSeparatorLine();

        globalLayout.addView(separatorLine);
        globalLayout.addView(separatorLine2);
        globalLayout.addView(titleView);
        globalLayout.addView(separatorLine3);

        titleViewElements.add(titleView);
        titleViewElements.add(separatorLine);
        titleViewElements.add(separatorLine2);
        titleViewElements.add(separatorLine3);
    }

    @Override
    protected void createFilterGroup(final FilterGroup filterGroup,
                                     final UiWrapperMapDelegate wrapperDelegate,
                                     final UiSelectorDelegate selectorDelegate) {
        final UiItemWrapperViews viewsWrapper = new UiItemWrapperViews(
                filterGroup.getIdentifier(), filterGroup.getIdentifier());

        final View separatorLine = createSeparatorLine();
        globalLayout.addView(separatorLine);
        viewsWrapper.add(separatorLine);

        if (filterGroup.getNameId() != null) {
            final TextView filterLabel =
                    createFilterGroupLabel(filterGroup, getLayoutParamsLabelLeft());
            globalLayout.addView(filterLabel);
            viewsWrapper.add(filterLabel);
        }

        if (filterGroup.isOnlyOneCheckable()) {

            final RadioGroup radioGroup = new RadioGroup(context);
            radioGroup.setLayoutParams(getLayoutParamsViews());

            createUiElementsForSingleSelectableItemsFilterGroup(
                    filterGroup, wrapperDelegate, selectorDelegate, radioGroup);

            globalLayout.addView(radioGroup);
            viewsWrapper.add(radioGroup);

        } else { // multiple items in FilterGroup selectable
            createUiElementsForMultipleSelectableItemsFilterGroup(
                    filterGroup, wrapperDelegate, selectorDelegate);
        }

        wrapperDelegate.put(filterGroup.getIdentifier(), viewsWrapper);
    }

    private void createUiElementsForSingleSelectableItemsFilterGroup(
            final FilterGroup filterGroup,
            final UiWrapperMapDelegate wrapperDelegate,
            final UiSelectorDelegate selectorDelegate,
            final RadioGroup radioGroup) {
        for (final FilterItem item : filterGroup.getFilterItems()) {

            final View view;
            if (item instanceof FilterItem.DividerItem) {
                view = createDividerTextView(item, getLayoutParamsViews());
            } else {
                view = createViewItemRadio(item, getLayoutParamsViews());

                wrapperDelegate.put(item.getIdentifier(),
                        new UiItemWrapperCheckBoxAndRadioButton(
                                item, view, filterGroup.getIdentifier(), radioGroup));

                final View.OnClickListener listener = v -> {
                    if (v != null) {
                        selectorDelegate.selectFilter(v.getId());
                    }
                };
                view.setOnClickListener(listener);
                viewListeners.put(view, listener);
            }
            radioGroup.addView(view);
        }
    }

    private void createUiElementsForMultipleSelectableItemsFilterGroup(
            final FilterGroup filterGroup,
            final UiWrapperMapDelegate wrapperDelegate,
            final UiSelectorDelegate selectorDelegate) {
        for (final FilterItem item : filterGroup.getFilterItems()) {
            final View view;
            if (item instanceof FilterItem.DividerItem) {
                view = createDividerTextView(item, getLayoutParamsViews());
            } else {
                final CheckBox checkBox = createCheckBox(item, getLayoutParamsViews());

                wrapperDelegate.put(item.getIdentifier(),
                        new UiItemWrapperCheckBoxAndRadioButton(
                                item, checkBox, filterGroup.getIdentifier(), null));

                final View.OnClickListener listener = v -> {
                    if (v != null) {
                        selectorDelegate.selectFilter(v.getId());
                    }
                };
                checkBox.setOnClickListener(listener);
                viewListeners.put(checkBox, listener);

                view = checkBox;
            }
            globalLayout.addView(view);
        }
    }

    private LinearLayout createGlobalLayout() {
        final LinearLayout linearLayout = new LinearLayout(context);

        linearLayout.setOrientation(LinearLayout.VERTICAL);

        final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(1, 1);
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.setMargins(
                (int) dipToPixels(context, 2),
                (int) dipToPixels(context, 2),
                (int) dipToPixels(context, 2),
                (int) dipToPixels(context, 2));
        linearLayout.setLayoutParams(layoutParams);

        return linearLayout;
    }

    private LinearLayout.LayoutParams getLayoutForSeparatorLine() {
        final LinearLayout.LayoutParams layoutParams = getLayoutParamsLabelLeft();
        layoutParams.width = 0;
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        return layoutParams;
    }

    private View createSeparatorLine() {
        return createSeparatorLine(getLayoutForSeparatorLine());
    }

    private TextView createTitleText(final String name) {
        final LinearLayout.LayoutParams layoutParams = getLayoutParamsLabelLeft();
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        final TextView title = createTitleText(name, layoutParams);
        setPadding(title, 5);
        return title;
    }

    private View setPadding(final View view, final int sizeInDip) {
        final int sizeInPx = (int) dipToPixels(context, sizeInDip);
        view.setPadding(
                sizeInPx,
                sizeInPx,
                sizeInPx,
                sizeInPx);
        return view;
    }

    private TextView createFilterGroupLabel(final FilterGroup filterGroup,
                                            final ViewGroup.LayoutParams layoutParams) {
        final TextView filterLabel = new TextView(context);
        filterLabel.setId(filterGroup.getIdentifier());
        filterLabel.setText(ServiceHelper
                .getTranslatedFilterString(filterGroup.getNameId(), context));
        filterLabel.setGravity(Gravity.TOP);
        // resizing not needed as view is not selectable
        filterLabel.setTag(NO_RESIZE_VIEW_TAG);
        filterLabel.setLayoutParams(layoutParams);
        return filterLabel;
    }

    private CheckBox createCheckBox(final FilterItem item,
                                    final ViewGroup.LayoutParams layoutParams) {
        final CheckBox checkBox = new CheckBox(context);
        checkBox.setLayoutParams(layoutParams);
        checkBox.setText(ServiceHelper.getTranslatedFilterString(
                item.getNameId(), context));
        checkBox.setId(item.getIdentifier());
        checkBox.setTextSize(COMPLEX_UNIT_DIP, FONT_SIZE_SELECTABLE_VIEW_ITEMS_IN_DIP);
        return checkBox;
    }

    private TextView createDividerTextView(final FilterItem item,
                                           final ViewGroup.LayoutParams layoutParams) {
        final TextView view = new TextView(context);
        view.setEnabled(true);
        final String menuDividerTitle =
                ServiceHelper.getTranslatedFilterString(item.getNameId(), context);
        view.setText(menuDividerTitle);
        view.setGravity(Gravity.TOP);
        view.setLayoutParams(layoutParams);
        return view;
    }

    private RadioButton createViewItemRadio(final FilterItem item,
                                            final ViewGroup.LayoutParams layoutParams) {
        final RadioButton view = new RadioButton(context);
        view.setId(item.getIdentifier());
        view.setText(ServiceHelper.getTranslatedFilterString(item.getNameId(), context));
        view.setLayoutParams(layoutParams);
        view.setTextSize(COMPLEX_UNIT_DIP, FONT_SIZE_SELECTABLE_VIEW_ITEMS_IN_DIP);
        return view;
    }

    @NonNull
    private LinearLayout.LayoutParams getLayoutParamsViews() {
        final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(
                (int) dipToPixels(context, 4),
                (int) dipToPixels(context, 8),
                (int) dipToPixels(context, 4),
                (int) dipToPixels(context, 8));
        return layoutParams;
    }

    @NonNull
    private LinearLayout.LayoutParams getLayoutParamsLabelLeft() {
        final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(
                (int) dipToPixels(context, 2),
                (int) dipToPixels(context, 2),
                (int) dipToPixels(context, 2),
                (int) dipToPixels(context, 2));
        return layoutParams;
    }

    private static final class UiItemWrapperCheckBoxAndRadioButton
            extends BaseUiItemWrapper {

        private final View group;

        private UiItemWrapperCheckBoxAndRadioButton(final FilterItem item, final View view,
                                                    final int groupId, final View group) {
            super(item, groupId, view);
            this.group = group;
        }

        @Override
        public boolean isChecked() {
            if (view instanceof RadioButton) {
                return ((RadioButton) view).isChecked();
            } else if (view instanceof CheckBox) {
                return ((CheckBox) view).isChecked();
            } else {
                return view.isSelected();
            }
        }

        @Override
        public void setChecked(final boolean checked) {
            if (checked && group instanceof RadioGroup) {
                ((RadioGroup) group).check(view.getId());
            } else if (view instanceof CheckBox) {
                ((CheckBox) view).setChecked(checked);
            } else {
                view.setSelected(checked);
            }
        }
    }
}
