// Created by evermind-zz 2022, licensed GNU GPL version 3 or later

package org.schabi.newpipe.fragments.list.search.filter;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import org.schabi.newpipe.databinding.SearchFilterOptionMenuAlikeDialogFragmentBinding;
import org.schabi.newpipe.extractor.StreamingService;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

public class SearchFilterOptionMenuAlikeDialogFragment extends BaseSearchFilterDialogFragment {

    private SearchFilterOptionMenuAlikeDialogFragmentBinding binding;

    public static DialogFragment newInstance(
            final int serviceId,
            final ArrayList<Integer> userSelectedContentFilter,
            final ArrayList<Integer> userSelectedSortFilter) {
        return initDialogArguments(
                new SearchFilterOptionMenuAlikeDialogFragment(),
                serviceId,
                userSelectedContentFilter,
                userSelectedSortFilter);
    }

    @Override
    protected BaseSearchFilterUiGenerator createSearchFilterDialogGenerator(
            final StreamingService service,
            final SearchFilterLogic.Callback callback) {
        return new SearchFilterOptionMenuAlikeDialogGenerator(service,
                binding.verticalScroll, getContext(), callback);
    }

    @Override
    protected Toolbar getToolbar() {
        return binding.toolbarLayout.toolbar;
    }

    @Override
    protected View getRootView(final LayoutInflater inflater,
                               final ViewGroup container) {
        binding = SearchFilterOptionMenuAlikeDialogFragmentBinding
                .inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // place the dialog in the 'action menu position'
        setDialogGravity(Gravity.END | Gravity.TOP);
    }

    private void setDialogGravity(final int gravity) {
        final Dialog dialog = getDialog();
        if (dialog != null) {
            final Window window = dialog.getWindow();
            if (window != null) {
                final WindowManager.LayoutParams layoutParams = window.getAttributes();
                layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                layoutParams.horizontalMargin = 0;
                layoutParams.gravity = gravity;
                layoutParams.dimAmount = 0;
                layoutParams.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
                window.setAttributes(layoutParams);
            }
        }
    }

    protected void initToolbar(final Toolbar toolbar) {
        super.initToolbar(toolbar);
        // no room for a title
        toolbar.setTitle("");
    }
}
