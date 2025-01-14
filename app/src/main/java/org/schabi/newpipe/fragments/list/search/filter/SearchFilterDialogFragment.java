// Created by evermind-zz 2022, licensed GNU GPL version 3 or later

package org.schabi.newpipe.fragments.list.search.filter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.schabi.newpipe.databinding.SearchFilterDialogFragmentBinding;
import org.schabi.newpipe.extractor.StreamingService;

import java.util.ArrayList;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

/**
 * A search filter dialog that also looks like a dialog aka. 'dialog style'.
 */
public class SearchFilterDialogFragment extends BaseSearchFilterDialogFragment {

    private SearchFilterDialogFragmentBinding binding;

    public static DialogFragment newInstance(
            final int serviceId,
            final ArrayList<Integer> userSelectedContentFilter,
            final ArrayList<Integer> userSelectedSortFilter) {
        return initDialogArguments(
                new SearchFilterDialogFragment(),
                serviceId,
                userSelectedContentFilter,
                userSelectedSortFilter);
    }

    @Override
    protected BaseSearchFilterUiGenerator createSearchFilterDialogGenerator(
            final StreamingService service,
            final SearchFilterLogic.Callback callback) {
        return new SearchFilterDialogGenerator(service,
                binding.verticalScroll, getContext(), callback);
    }

    @Override
    protected Toolbar getToolbar() {
        return binding.toolbarLayout.toolbar;
    }

    @Override
    protected View getRootView(final LayoutInflater inflater,
                               final ViewGroup container) {
        binding = SearchFilterDialogFragmentBinding
                .inflate(inflater, container, false);
        return binding.getRoot();
    }
}
