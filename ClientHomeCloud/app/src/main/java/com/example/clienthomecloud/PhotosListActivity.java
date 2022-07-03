package com.example.clienthomecloud;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

public class PhotosListActivity extends ListActivity {

    List<String> listPhotos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        // Получаем переданный список фото
        Bundle arguments = getIntent().getExtras();
        if(arguments!=null){
            listPhotos = arguments.getStringArrayList("Photos");
        }

        // создаем адаптер
        ArrayAdapter<String> adapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_multiple_choice, listPhotos);
        setListAdapter(adapter);
    }

    public void onAcceptSelect(View view) {
        SparseBooleanArray selected=getListView().getCheckedItemPositions();

        ArrayList<String> selectedItems = new ArrayList<String>();
        for(int i=0;i < listPhotos.size();i++)
        {
            if(selected.get(i))
                selectedItems.add(listPhotos.get(i));
        }
        Intent intent = new Intent();
        intent.putStringArrayListExtra("SelectedPhotos",selectedItems);
        setResult(RESULT_OK, intent);
        finish();
    }
}