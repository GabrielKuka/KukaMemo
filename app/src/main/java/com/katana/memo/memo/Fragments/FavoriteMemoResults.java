package com.katana.memo.memo.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.ListViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.katana.memo.memo.Activities.Memo;
import com.katana.memo.memo.Helper.DatabaseHelper;
import com.katana.memo.memo.R;

public class FavoriteMemoResults extends Fragment implements AdapterView.OnItemClickListener {

    private ArrayAdapter<String> adapter;
    private ListViewCompat listViewCompat;
    private TextView noData;
    private DatabaseHelper dbHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.search_result_favorite_memos, container, false);

        Bundle b = getArguments();

        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, b.getStringArrayList("favoriteMemos"));
        adapter.notifyDataSetChanged();

        noData = (TextView) v.findViewById(R.id.noFavoriteMemosTextViewLabelId);

        listViewCompat = (ListViewCompat) v.findViewById(R.id.favoriteMemosResultId);
        try {
            listViewCompat.setAdapter(adapter);
            listViewCompat.setOnItemClickListener(this);

        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        if (b.getStringArrayList("favoriteMemos") != null) {

            if (b.getStringArrayList("favoriteMemos").size() == 0) {
                noData.setVisibility(View.VISIBLE);
            } else {
                noData.setVisibility(View.GONE);
            }
        }

        dbHelper = new DatabaseHelper(getActivity());

        return v;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent = new Intent(getActivity(), Memo.class);

        String title = listViewCompat.getItemAtPosition(i).toString();
        int id = dbHelper.getFavoriteMemoId(title);

        intent.putExtra("id", id);
        intent.putExtra("title", title);
        intent.putExtra("body", dbHelper.getFavoriteMemoBody(title));
        intent.putExtra("date", dbHelper.getFavoriteMemoDate(title));
        intent.putExtra("location", dbHelper.getFavoriteMemoLocation(title));

        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();

    }
}
