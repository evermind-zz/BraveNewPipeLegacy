// Created by evermind-zz 2022, licensed GNU GPL version 3 or later

package org.schabi.newpipe.fragments.list.search.filter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import org.schabi.newpipe.extractor.search.filter.FilterContainer;
import org.schabi.newpipe.extractor.search.filter.FilterGroup;
import org.schabi.newpipe.extractor.search.filter.FilterItem;
import org.schabi.newpipe.util.ServiceHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.schabi.newpipe.fragments.list.search.filter.SearchFilterOptionMenuAlikeDialogGenerator.dipToPixels;

public class SearchFilterDialogSpinnerAdapter extends BaseAdapter {

    private final Context context;
    private final FilterGroup group;
    private final BaseSearchFilterUiGenerator.UiWrapperMapDelegate wrapperDelegate;
    private final Spinner spinner;
    private final Map<Integer, Integer> id2PosMap = new HashMap<>();
    private final Map<Integer, UiItemWrapperSpinner>
            viewWrapperMap = new HashMap<>();

    public SearchFilterDialogSpinnerAdapter(
            final Context context,
            final FilterGroup group,
            final BaseSearchFilterUiGenerator.UiWrapperMapDelegate wrapperDelegate,
            final Spinner filterDataSpinner) {
        this.context = context;
        this.group = group;
        this.wrapperDelegate = wrapperDelegate;
        this.spinner = filterDataSpinner;

        createViewWrappers();
    }

    @Override
    public int getCount() {
        return group.getFilterItems().length;
    }

    @Override
    public Object getItem(final int position) {
        return group.getFilterItems()[position];
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        final FilterItem item = group.getFilterItems()[position];
        final TextView view;

        if (convertView != null) {
            view = (TextView) convertView;
        } else {
            view = createViewItem();
        }

        initViewWithData(position, item, view);
        return view;
    }

    @SuppressLint("WrongConstant")
    private void initViewWithData(final int position,
                                  final FilterItem item,
                                  final TextView view) {
        final UiItemWrapperSpinner wrappedView =
                viewWrapperMap.get(position);
        Objects.nonNull(wrappedView);

        view.setId(item.getIdentifier());
        view.setText(ServiceHelper.getTranslatedFilterString(item.getNameId(), context));
        view.setVisibility(wrappedView.getVisibility());
        view.setEnabled(wrappedView.isEnabled());

        if (item instanceof FilterItem.DividerItem) {
            wrappedView.setEnabled(false);
            view.setEnabled(wrappedView.isEnabled());
            final String menuDividerTitle = ">>>"
                    + ServiceHelper.getTranslatedFilterString(item.getNameId(), context) + "<<<";
            view.setText(menuDividerTitle);
        }
    }

    private void createViewWrappers() {
        for (int position = 0; position < this.group.getFilterItems().length; position++) {
            final FilterItem item = this.group.getFilterItems()[position];
            final int initialVisibility = View.VISIBLE;
            final boolean isInitialEnabled = true;

            final UiItemWrapperSpinner wrappedView =
                    new UiItemWrapperSpinner(
                            item,
                            initialVisibility,
                            isInitialEnabled,
                            this.group.getIdentifier(),
                            spinner);

            if (item instanceof FilterItem.DividerItem) {
                wrappedView.setEnabled(false);
            }

            // store wrapper also locally as we refer here regularly
            viewWrapperMap.put(position, wrappedView);
            // store wrapper globally in SearchFilterLogic
            wrapperDelegate.put(item.getIdentifier(), wrappedView);
            id2PosMap.put(item.getIdentifier(), position);
        }
    }

    private TextView createViewItem() {
        final TextView view = new TextView(context);
        view.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        view.setGravity(Gravity.CENTER_VERTICAL);
        view.setPadding(
                (int) dipToPixels(context, 8),
                (int) dipToPixels(context, 4),
                (int) dipToPixels(context, 8),
                (int) dipToPixels(context, 4)
        );
        return view;
    }

    public int getItemPositionForFilterId(final int id) {
        return id2PosMap.get(id);
    }

    @Override
    public boolean isEnabled(final int position) {
        final UiItemWrapperSpinner wrappedView =
                viewWrapperMap.get(position);
        Objects.nonNull(wrappedView);
        return wrappedView.isEnabled();
    }

    private static class UiItemWrapperSpinner
            extends SearchFilterDialogGenerator.UiItemWrapperChip {
        private final Spinner spinner;

        /**
         * We have to store the visibility of the view and if it is enabled.
         * <p>
         * Reason: the Spinner adapter reuses {@link View} elements through the parameter
         * convertView in {@link SearchFilterDialogSpinnerAdapter#getView(int, View, ViewGroup)}
         * -> this is the Android Adapter's time saving characteristic to rather reuse
         * than to recreate a {@link View}.
         * -> so we reuse what Android gives us in above mentioned method.
         */
        private int visibility;
        private boolean enabled;

        UiItemWrapperSpinner(final FilterItem item,
                             final int initialVisibility,
                             final boolean isInitialEnabled,
                             final int groupId, final Spinner spinner) {
            // View set to null as the adapter reuses the View's so the initially set
            // is later no longer reliable.
            super(item, null, groupId, null);
            this.spinner = spinner;

            this.visibility = initialVisibility;
            this.enabled = isInitialEnabled;
        }

        @Override
        public void setVisible(final boolean visible) {
            if (visible) {
                visibility = View.VISIBLE;
            } else {
                visibility = View.GONE;
            }
        }

        @Override
        public boolean isChecked() {
            return spinner.getSelectedItem() == item;
        }

        @Override
        public void setChecked(final boolean checked) {
            if (spinner != null && super.getItemId() != FilterContainer.ITEM_IDENTIFIER_UNKNOWN) {
                final SearchFilterDialogSpinnerAdapter adapter =
                        (SearchFilterDialogSpinnerAdapter) spinner.getAdapter();
                spinner.setSelection(adapter.getItemPositionForFilterId(super.getItemId()));
            }
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(final boolean enabled) {
            this.enabled = enabled;
        }

        public int getVisibility() {
            return visibility;
        }

        public void setVisibility(final int visibility) {
            this.visibility = visibility;
        }
    }
}
