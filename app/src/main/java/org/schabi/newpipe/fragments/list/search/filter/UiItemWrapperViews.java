// Created by evermind-zz 2022, licensed GNU GPL version 3 or later

package org.schabi.newpipe.fragments.list.search.filter;

import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper for views that are either just labels or eg. a RadioGroup container
 * etc. that represent a {@link org.schabi.newpipe.extractor.search.filter.FilterGroup}.
 */
final class UiItemWrapperViews implements SearchFilterLogic.IUiItemWrapper {

    private final int itemId;
    private final int groupId;
    private final List<View> views = new ArrayList<>();

    UiItemWrapperViews(final int itemId, final int groupId) {
        this.itemId = itemId;
        this.groupId = groupId;
    }

    public void add(final View view) {
        this.views.add(view);
    }

    @Override
    public void setVisible(final boolean visible) {
        for (final View view : views) {
            if (visible) {
                view.setVisibility(View.VISIBLE);
            } else {
                view.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemId() {
        return this.itemId;
    }

    @Override
    public int getGroupId() {
        return this.groupId;
    }

    @Override
    public boolean isChecked() {
        boolean isChecked = false;
        for (final View view : views) {
            if (view.isSelected()) {
                isChecked = true;
                break;
            }
        }
        return isChecked;
    }

    @Override
    public void setChecked(final boolean checked) {
        // not relevant as here views are wrapped that are either just labels or eg. a
        // RadioGroup container etc. that represent a FilterGroup.
    }
}
