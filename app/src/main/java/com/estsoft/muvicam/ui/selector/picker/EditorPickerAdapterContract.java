package com.estsoft.muvicam.ui.selector.picker;


import com.estsoft.muvicam.model.EditorVideo;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017-01-05.
 */

public interface EditorPickerAdapterContract{
    interface  View{
        void notifyAdapter();
        void setOnClickListener(VideoEditorPickerAdapter.OnItemClickListener clickListener);

    }
    interface  Model{
        void clearItem();
        EditorVideo getItem(int position);
        ArrayList<EditorVideo> getItems();
        void addItems(ArrayList<EditorVideo> items);
    }
}
