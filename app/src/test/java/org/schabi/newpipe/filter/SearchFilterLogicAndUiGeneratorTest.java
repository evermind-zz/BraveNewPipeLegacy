// Created by evermind-zz 2022, licensed GNU GPL version 3 or later

package org.schabi.newpipe.filter;


import org.junit.Test;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandlerFactory;
import org.schabi.newpipe.extractor.search.filter.FilterContainer;
import org.schabi.newpipe.extractor.search.filter.FilterGroup;
import org.schabi.newpipe.extractor.search.filter.FilterItem;
import org.schabi.newpipe.extractor.services.peertube.search.filter.PeertubeFilters;
import org.schabi.newpipe.extractor.services.youtube.search.filter.YoutubeFilters;
import org.schabi.newpipe.fragments.list.search.filter.BaseSearchFilterUiGenerator;
import org.schabi.newpipe.fragments.list.search.filter.SearchFilterLogic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Test the {@link SearchFilterLogic} and
 * {@link org.schabi.newpipe.extractor.search.filter.SearchFiltersBase}.
 */
public class SearchFilterLogicAndUiGeneratorTest {

    private static final int PEERTUBE_SERVICE_ID = 3;
    private static final int YOUTUBE_SERVICE_ID = 0;
    private final Map<Integer, ElementsWrapper> universalWrapper = new HashMap<>();
    private BaseSearchFilterUiGenerator generator;
    private StreamingService service;
    private SearchFilterGeneratorWorkersClass.FilterWorker sortWorker;
    private List<FilterItem> fromCallbackContentFilterItems;
    private List<FilterItem> fromCallbackSortFilterItems;


    private void setupEach(final boolean withUiWorker,
                           final SearchFilterLogic.Callback callback)
            throws ExtractionException {
        setupEach(withUiWorker, PEERTUBE_SERVICE_ID, callback);
    }

    private void setupEach(final boolean withUiWorker,
                           final int serviceId,
                           final SearchFilterLogic.Callback callback)
            throws ExtractionException {
        service = NewPipe.getService(serviceId);

        if (withUiWorker) {
            generator = new SearchFilterGeneratorWorkersClass(service.getSearchQHFactory(),
                    callback);
        } else {
            generator = new SearchFilterGeneratorNoWorkersClass(service.getSearchQHFactory(),
                    callback);
        }
    }

    @Test
    public void resetAndRestoreTest() throws ExtractionException {
        setupEach(false, null);
        // 1. no data input (eg no previously selected filters set)
        final ArrayList<Integer> contentFilters = generator.getSelectedContentFilters();
        final ArrayList<Integer> sortFilters = generator.getSelectedSortFilters();
        generator.reset();
        final ArrayList<Integer> contentFilters2 = generator.getSelectedContentFilters();
        final ArrayList<Integer> sortFilters2 = generator.getSelectedSortFilters();
        assertTrue(!contentFilters2.isEmpty() && !contentFilters.isEmpty());
        assertTrue(!sortFilters2.isEmpty() && !sortFilters.isEmpty());

        // 2. test if initially set some data that should be present in output
        final ArrayList<Integer> contentFiltersWithNoneDefaultId = new ArrayList<>();
        contentFiltersWithNoneDefaultId.add(PeertubeFilters.ID_CF_MAIN_VIDEOS);
        final ArrayList<Integer> sortFiltersWithNoneDefaultId = new ArrayList<>();
        sortFiltersWithNoneDefaultId.add(PeertubeFilters.ID_SF_SORT_BY_CREATION_DATE);

        generator.restorePreviouslySelectedFilters(contentFiltersWithNoneDefaultId,
                sortFiltersWithNoneDefaultId);

        ArrayList<Integer> contentFilterResetResult = generator.getSelectedContentFilters();
        ArrayList<Integer> sortFilterResetResult = generator.getSelectedSortFilters();

        assertTrue(contentFilterResetResult.contains(PeertubeFilters.ID_CF_MAIN_VIDEOS));
        assertTrue(sortFilterResetResult.contains(PeertubeFilters.ID_SF_SORT_BY_CREATION_DATE));
        assertFalse(contentFilterResetResult.contains(PeertubeFilters.ID_CF_MAIN_ALL));
        assertFalse(sortFilterResetResult.contains(PeertubeFilters.ID_SF_SORT_BY_RELEVANCE));

        generator.reset(); // now go back to default values

        contentFilterResetResult = generator.getSelectedContentFilters();
        sortFilterResetResult = generator.getSelectedSortFilters();
        assertTrue(contentFilterResetResult.contains(PeertubeFilters.ID_CF_MAIN_ALL));
        assertTrue(sortFilterResetResult.contains(PeertubeFilters.ID_SF_SORT_BY_RELEVANCE));

        // 3. test if empty input data results in defaults
        setupEach(false, null);
        generator.restorePreviouslySelectedFilters(new ArrayList<>(),
                new ArrayList<>());
        final ArrayList<Integer> contentFilterResultNoInput =
                generator.getSelectedContentFilters();
        final ArrayList<Integer> sortFilterResultNoInput =
                generator.getSelectedSortFilters();
        assertTrue(contentFilterResultNoInput.contains(PeertubeFilters.ID_CF_MAIN_ALL));
        assertTrue(sortFilterResultNoInput.contains(PeertubeFilters.ID_SF_SORT_BY_RELEVANCE));

        // 4. compare 2 and 3 results
        assertArrayEquals(contentFilterResetResult.toArray(),
                contentFilterResultNoInput.toArray());
        assertArrayEquals(sortFilterResetResult.toArray(),
                sortFilterResultNoInput.toArray());
    }

    @Test
    public void checkIfInitResultsInDefaultSortAndContentFiltersTest() throws ExtractionException {
        setupEach(false, null);

        // after setupEach() there should be default entries.
        final ArrayList<Integer> defaultContentFilters =
                generator.getSelectedContentFilters();
        final ArrayList<Integer> defaultSortFilters =
                generator.getSelectedSortFilters();

        assertTrue(defaultContentFilters.contains(PeertubeFilters.ID_CF_MAIN_ALL));
        assertTrue(defaultSortFilters.contains(PeertubeFilters.ID_SF_SORT_BY_RELEVANCE));
    }

    @Test
    public void contentFilterItemsIdsMatchIdsAndCallbackTest() throws ExtractionException {
        setupEach(false, new SearchFilterLogic.Callback() {
            @Override
            public void selectedFilters(final List<FilterItem> userSelectedContentFilter,
                                        final List<FilterItem> userSelectedSortFilter) {
                fromCallbackContentFilterItems = userSelectedContentFilter;
                fromCallbackSortFilterItems = userSelectedSortFilter;
            }
        });

        // reset to null
        fromCallbackContentFilterItems = null;
        fromCallbackSortFilterItems = null;

        // after setupEach() there should be default entries.
        ArrayList<Integer> defaultContentFiltersIds = generator.getSelectedContentFilters();
        ArrayList<Integer> defaultSortFiltersIds = generator.getSelectedSortFilters();
        List<FilterItem> defaultContentFilterItems = generator.getSelectedContentFilterItems();
        List<FilterItem> defaultSortFilterItems = generator.getSelectedSortFiltersItems();

        assertNotEquals(defaultContentFiltersIds.size(), defaultContentFilterItems.size());
        assertNotEquals(defaultSortFiltersIds.size(), defaultSortFilterItems.size());

        assertNull(fromCallbackContentFilterItems);
        assertNull(fromCallbackSortFilterItems);

        generator.prepareForSearch(); // callback variables are now being initialized

        assertNotNull(fromCallbackContentFilterItems);
        assertNotNull(fromCallbackSortFilterItems);

        defaultContentFiltersIds = generator.getSelectedContentFilters();
        defaultSortFiltersIds = generator.getSelectedSortFilters();
        defaultContentFilterItems = generator.getSelectedContentFilterItems();
        defaultSortFilterItems = generator.getSelectedSortFiltersItems();

        assertTrue(defaultContentFilterItems.size() > 0);
        assertTrue(defaultSortFilterItems.size() > 0);

        assertEquals(defaultContentFiltersIds.size(), defaultContentFilterItems.size());
        assertEquals(defaultSortFiltersIds.size(), defaultSortFilterItems.size());

        compareFilterIdsWithFilterItems(defaultContentFiltersIds, defaultContentFilterItems);
        compareFilterIdsWithFilterItems(defaultSortFiltersIds, defaultSortFilterItems);

        compareFilterIdsWithFilterItems(defaultContentFiltersIds, fromCallbackContentFilterItems);
        compareFilterIdsWithFilterItems(defaultSortFiltersIds, fromCallbackSortFilterItems);
    }

    private void compareFilterIdsWithFilterItems(final ArrayList<Integer> filterIds,
                                                 final List<FilterItem> filterItems) {
        int idx = 0;
        for (final FilterItem item : filterItems) {
            final int filterItemId = item.getIdentifier();
            final int filterItemId2 = filterIds.get(idx++);
            assertEquals(filterItemId, filterItemId2);
        }
    }

    @Test(expected = RuntimeException.class)
    public void checkIllegalContentFilterIdsTest() throws ExtractionException {
        setupEach(false, null);
        final ArrayList<Integer> contentFiltersWithIllegalIds = new ArrayList<>();
        contentFiltersWithIllegalIds.add(10000);
        final ArrayList<Integer> sortFiltersEmpty = new ArrayList<>();

        generator.restorePreviouslySelectedFilters(contentFiltersWithIllegalIds,
                sortFiltersEmpty);
    }

    @Test(expected = RuntimeException.class)
    public void checkIllegalSortFilterIdsTest() throws ExtractionException {
        setupEach(false, null);
        // content filter can not be empty
        final ArrayList<Integer> contentFiltersWithValidId = new ArrayList<>();
        contentFiltersWithValidId.add(PeertubeFilters.ID_CF_MAIN_VIDEOS);
        final ArrayList<Integer> sortFiltersWithIllegalIds = new ArrayList<>();
        sortFiltersWithIllegalIds.add(20000);

        generator.restorePreviouslySelectedFilters(contentFiltersWithValidId,
                sortFiltersWithIllegalIds);
    }

    @Test
    public void selectOneContenFilterKeepDefaultSortFilterTest() throws ExtractionException {
        setupEach(false, null);

        // set only one content filter, keep default sort filters
        generator.selectContentFilter(PeertubeFilters.ID_CF_MAIN_PLAYLISTS);
        ArrayList<Integer> contentFilterResetResult = generator.getSelectedContentFilters();
        ArrayList<Integer> sortFilterResetResult = generator.getSelectedSortFilters();
        assertTrue(contentFilterResetResult.contains(PeertubeFilters.ID_CF_MAIN_PLAYLISTS));
        assertTrue(sortFilterResetResult.contains(PeertubeFilters.ID_SF_SORT_BY_RELEVANCE));

        generator.selectContentFilter(PeertubeFilters.ID_CF_MAIN_CHANNELS);
        contentFilterResetResult = generator.getSelectedContentFilters();
        sortFilterResetResult = generator.getSelectedSortFilters();
        assertFalse(contentFilterResetResult.contains(PeertubeFilters.ID_CF_MAIN_PLAYLISTS));
        assertTrue(contentFilterResetResult.contains(PeertubeFilters.ID_CF_MAIN_CHANNELS));
        assertTrue(sortFilterResetResult.contains(PeertubeFilters.ID_SF_SORT_BY_RELEVANCE));
    }

    @Test
    public void selectOneContentFilterAndOneSortFilterTest() throws ExtractionException {
        setupEach(false, null);

        generator.selectContentFilter(PeertubeFilters.ID_CF_MAIN_PLAYLISTS);
        generator.selectSortFilter(PeertubeFilters.ID_SF_SORT_BY_CREATION_DATE);
        ArrayList<Integer> contentFilterResetResult = generator.getSelectedContentFilters();
        ArrayList<Integer> sortFilterResetResult = generator.getSelectedSortFilters();
        assertTrue(contentFilterResetResult.contains(PeertubeFilters.ID_CF_MAIN_PLAYLISTS));
        assertFalse(sortFilterResetResult.contains(PeertubeFilters.ID_SF_SORT_BY_RELEVANCE));
        assertTrue(sortFilterResetResult.contains(PeertubeFilters.ID_SF_SORT_BY_CREATION_DATE));

        generator.selectContentFilter(PeertubeFilters.ID_CF_MAIN_CHANNELS);
        generator.selectSortFilter(PeertubeFilters.ID_SF_SORT_BY_DURATION);
        contentFilterResetResult = generator.getSelectedContentFilters();
        sortFilterResetResult = generator.getSelectedSortFilters();
        assertFalse(contentFilterResetResult.contains(PeertubeFilters.ID_CF_MAIN_PLAYLISTS));
        assertTrue(contentFilterResetResult.contains(PeertubeFilters.ID_CF_MAIN_CHANNELS));
        assertTrue(sortFilterResetResult.contains(PeertubeFilters.ID_SF_SORT_BY_DURATION));
        assertFalse(sortFilterResetResult.contains(PeertubeFilters.ID_SF_SORT_BY_CREATION_DATE));
    }

    @Test
    public void selectTwoContentFiltersTest() throws ExtractionException {
        setupEach(false, null);

        generator.selectContentFilter(PeertubeFilters.ID_CF_MAIN_PLAYLISTS);
        ArrayList<Integer> contentFilterResetResult = generator.getSelectedContentFilters();
        assertTrue(contentFilterResetResult.contains(PeertubeFilters.ID_CF_MAIN_PLAYLISTS));
        assertFalse(contentFilterResetResult.contains(PeertubeFilters.ID_CF_SEPIA_SEPIASEARCH));

        // 2nd content filters added from another group of course as PeertubeFilter.ID_CF_MAIN_GRP
        // is exclusive group -> only one item per group allowed
        generator.selectContentFilter(PeertubeFilters.ID_CF_SEPIA_SEPIASEARCH);
        contentFilterResetResult = generator.getSelectedContentFilters();
        assertTrue(contentFilterResetResult.contains(PeertubeFilters.ID_CF_MAIN_PLAYLISTS));
        assertTrue(contentFilterResetResult.contains(PeertubeFilters.ID_CF_SEPIA_SEPIASEARCH));
    }

    @Test
    public void selectMultipleSortFilterInNonExclusiveGroupTest() throws ExtractionException {
        selectMultipleSortFilterInNonExclusiveGroupHelper(false);
    }

    @Test
    public void selectMultipleSortFilterInNonExclusiveGroupWithUiTest() throws ExtractionException {
        selectMultipleSortFilterInNonExclusiveGroupHelper(true);
    }

    private void selectMultipleSortFilterInNonExclusiveGroupHelper(final boolean withUiWorker)
            throws ExtractionException {
        setupEach(withUiWorker, YOUTUBE_SERVICE_ID, null);

        if (withUiWorker) {
            universalWrapper.clear();
            generator.createSearchUI();
            simulateUiClicking(YoutubeFilters.ID_CF_MAIN_VIDEOS);
        }
        generator.selectContentFilter(YoutubeFilters.ID_CF_MAIN_VIDEOS);
        final ArrayList<Integer> contentFilterResetResult = generator.getSelectedContentFilters();
        assertTrue(contentFilterResetResult.contains(YoutubeFilters.ID_CF_MAIN_VIDEOS));

        // select 1st element from a non-exclusive group
        if (withUiWorker) {
            simulateUiClicking(YoutubeFilters.ID_SF_FEATURES_3D);
        }
        generator.selectSortFilter(YoutubeFilters.ID_SF_FEATURES_3D);
        ArrayList<Integer> sortFilterResetResult = generator.getSelectedSortFilters();
        assertTrue(sortFilterResetResult.contains(YoutubeFilters.ID_SF_FEATURES_3D));
        assertFalse(sortFilterResetResult.contains(YoutubeFilters.ID_SF_FEATURES_4K));

        // select 2nd element from a non-exclusive group
        if (withUiWorker) {
            simulateUiClicking(YoutubeFilters.ID_SF_FEATURES_4K);
        }
        generator.selectSortFilter(YoutubeFilters.ID_SF_FEATURES_4K);
        sortFilterResetResult = generator.getSelectedSortFilters();
        assertTrue(sortFilterResetResult.contains(YoutubeFilters.ID_SF_FEATURES_3D));
        assertTrue(sortFilterResetResult.contains(YoutubeFilters.ID_SF_FEATURES_4K));

        // deselect previous selected element
        if (withUiWorker) {
            simulateUiClicking(YoutubeFilters.ID_SF_FEATURES_4K);
        }
        generator.selectSortFilter(YoutubeFilters.ID_SF_FEATURES_4K);
        sortFilterResetResult = generator.getSelectedSortFilters();
        assertTrue(sortFilterResetResult.contains(YoutubeFilters.ID_SF_FEATURES_3D));
        assertFalse(sortFilterResetResult.contains(YoutubeFilters.ID_SF_FEATURES_4K));
    }

    private void simulateUiClicking(final int id) {
        final boolean isSelected = universalWrapper.get(id).isChecked();
        universalWrapper.get(id).setChecked(!isSelected);
    }

    private void expectSortFiltersToBeVisible(final int id) {
        final FilterContainer sortFilterVariant = service.getSearchQHFactory()
                .getContentFilterSortFilterVariant(id);
        assertTrue(sortFilterVariant.getFilterGroups().length > 0);
        for (final FilterGroup group : sortFilterVariant.getFilterGroups()) {
            for (final FilterItem item : group.getFilterItems()) {
                final int itemId = item.getIdentifier();
                assertTrue(universalWrapper.containsKey(itemId));
                assertNotNull(universalWrapper.get(itemId));
                assertTrue(universalWrapper.get(itemId).visible);
            }
        }
        assertNotNull(sortWorker.areAnySortFiltersVisible);
        assertTrue(sortWorker.areAnySortFiltersVisible.isPresent());
        assertTrue(sortWorker.areAnySortFiltersVisible.get());
    }

    @Test
    public void checkIfCorrespondingSortFiltersAreDisplayedTest()
            throws ExtractionException {
        setupEach(true, PEERTUBE_SERVICE_ID, null);

        universalWrapper.clear();
        generator.createSearchUI();

        // 1st test:
        // default content filter is PeertubeFilters.ID_CF_MAIN_ALL so we expect all sort filters
        // visible. Get the filters from service and compare with universalWrapper map
        expectSortFiltersToBeVisible(PeertubeFilters.ID_CF_MAIN_ALL);

        // 2nd test:
        // content filter with no sort filters aka Ui element should be not visible.
        // get all sort filters from  and compare with universalWrapper map
        // set content filter with no sort filters available
        final int contentFilterWithNoSortFilters = PeertubeFilters.ID_CF_MAIN_PLAYLISTS;
        generator.selectContentFilter(contentFilterWithNoSortFilters);
        final FilterContainer noSortFiltersAkaNull = service.getSearchQHFactory()
                .getContentFilterSortFilterVariant(contentFilterWithNoSortFilters);
        assertNull(noSortFiltersAkaNull);

        // get content filter with all sort filters visible in two ways
        // first way
        final FilterContainer allSortFilters = service.getSearchQHFactory()
                .getContentFilterSortFilterVariant(PeertubeFilters.ID_CF_MAIN_ALL);
        // second way
        final Optional<FilterGroup> allSortFilters2 = Arrays.stream(service.getSearchQHFactory()
                        .getAvailableContentFilter()
                        .getFilterGroups())
                .filter(filterGroup
                        -> (filterGroup.getIdentifier() == PeertubeFilters.ID_CF_MAIN_GRP))
                .findFirst();

        assertNotNull(allSortFilters);
        assertTrue(allSortFilters2.isPresent());
        assertEquals(allSortFilters, allSortFilters2.get().getAllSortFilters());
        assertTrue(allSortFilters.getFilterGroups().length > 0);
        assertNotNull(sortWorker.areAnySortFiltersVisible);
        assertTrue(sortWorker.areAnySortFiltersVisible.isPresent());
        assertFalse(sortWorker.areAnySortFiltersVisible.get());

        // expect all sort filters not visible
        for (final FilterGroup group : allSortFilters.getFilterGroups()) {
            for (final FilterItem item : group.getFilterItems()) {
                final int id = item.getIdentifier();
                assertTrue(universalWrapper.containsKey(id));
                assertNotNull(universalWrapper.get(id));
                assertFalse(universalWrapper.get(id).visible);
            }
        }

        // 3rd test:
        // select content filter that should have all sort filters visible again
        final int contentFilterWithAllSortFiltersVisible = PeertubeFilters.ID_CF_MAIN_VIDEOS;
        generator.selectContentFilter(contentFilterWithAllSortFiltersVisible);
        expectSortFiltersToBeVisible(contentFilterWithAllSortFiltersVisible);
    }

    // helpers
    private static class SearchFilterGeneratorNoWorkersClass extends BaseSearchFilterUiGenerator {

        SearchFilterGeneratorNoWorkersClass(final SearchQueryHandlerFactory linkHandlerFactory,
                                            final Callback callback) {
            super(linkHandlerFactory, callback, null);
        }

        @Override
        protected ICreateUiForFiltersWorker createSortFilterWorker() {
            return null;
        }

        @Override
        protected ICreateUiForFiltersWorker createContentFilterWorker() {
            return null;
        }

        @Override
        public void onResume() {
        }

        @Override
        public void onPause() {
        }
    }

    static class ElementsWrapper implements SearchFilterLogic.IUiItemWrapper {
        public final FilterItem item;
        public final int groupId;
        public boolean isSelected;
        public boolean visible;

        ElementsWrapper(final FilterItem item,
                        final int groupId) {
            this.item = item;
            this.groupId = groupId;
            this.visible = false;
            this.isSelected = false;
        }

        @Override
        public void setVisible(final boolean visible) {
            this.visible = visible;
        }

        @Override
        public int getItemId() {
            return item.getIdentifier();
        }

        @Override
        public int getGroupId() {
            return groupId;
        }

        @Override
        public boolean isChecked() {
            return isSelected;
        }

        @Override
        public void setChecked(final boolean checked) {
            this.isSelected = checked;
        }
    }

    private class SearchFilterGeneratorWorkersClass extends SearchFilterGeneratorNoWorkersClass {

        SearchFilterGeneratorWorkersClass(final SearchQueryHandlerFactory linkHandlerFactory,
                                          final Callback callback) {
            super(linkHandlerFactory, callback);
        }

        @Override
        protected ICreateUiForFiltersWorker createSortFilterWorker() {
            sortWorker = new FilterWorker(true);
            return sortWorker;
        }

        @Override
        protected ICreateUiForFiltersWorker createContentFilterWorker() {
            return new FilterWorker(false);
        }

        class FilterWorker implements SearchFilterLogic.ICreateUiForFiltersWorker {

            private final boolean isSortWorker;
            public Optional<Boolean> areAnySortFiltersVisible = null;

            FilterWorker(final boolean isSortWorker) {
                this.isSortWorker = isSortWorker;
            }

            @Override
            public void prepare() {
            }

            @Override
            public void createFilterGroupBeforeItems(final FilterGroup filterGroup) {
                for (final FilterItem item : filterGroup.getFilterItems()) {
                    final ElementsWrapper element =
                            new ElementsWrapper(item, filterGroup.getIdentifier());
                    universalWrapper.put(item.getIdentifier(), element);
                    if (isSortWorker) {
                        addSortFilterUiWrapperToItemMap(item.getIdentifier(), element);
                    } else {
                        addContentFilterUiWrapperToItemMap(item.getIdentifier(), element);
                    }
                }
            }

            @Override
            public void createFilterItem(final FilterItem filterItem,
                                         final FilterGroup filterGroup) {
            }

            @Override
            public void createFilterGroupAfterItems(final FilterGroup filterGroup) {
            }

            @Override
            public void finish() {
            }

            @Override
            public void filtersVisible(final boolean areFiltersVisible) {
                areAnySortFiltersVisible = Optional.of(areFiltersVisible);
            }
        }
    }
}
