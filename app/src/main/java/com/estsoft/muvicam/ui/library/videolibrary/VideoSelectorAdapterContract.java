package com.estsoft.muvicam.ui.library.videolibrary;


import com.estsoft.muvicam.model.EditorVideo;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017-01-05.
 */

public interface VideoSelectorAdapterContract {
    interface  View{
        void notifyAdapter();
        void setOnClickListener(VideoSelectorAdapter.OnItemClickListener clickListener);

    }
    interface  Model{
        void clearItem();
        EditorVideo getItem(int position);
        ArrayList<EditorVideo> getItems();
        void addItems(ArrayList<EditorVideo> items);
    }
}
