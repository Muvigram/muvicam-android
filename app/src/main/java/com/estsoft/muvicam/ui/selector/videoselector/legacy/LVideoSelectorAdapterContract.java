package com.estsoft.muvicam.ui.selector.videoselector.legacy;


import com.estsoft.muvicam.model.EditorVideo;
import com.estsoft.muvicam.ui.selector.videoselector.VideoSelectorAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017-01-05.
 */

public interface LVideoSelectorAdapterContract {

    interface View {
        void notifyAdapter();
        void setOnClickListener(VideoSelectorAdapter.OnItemClickListener clickListener);

    }

    interface Model {
        void clearItem();
        EditorVideo getItem(int position);
        ArrayList<EditorVideo> getItems();
        void addItems(List<EditorVideo> items);
        void notifyDataListChanged();
    }
}
