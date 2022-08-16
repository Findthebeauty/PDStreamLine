package com.shepherdboy.pdstreamline.view;

import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.shepherdboy.pdstreamline.R;
import com.shepherdboy.pdstreamline.activities.TraversalTimestreamActivity;
import com.shepherdboy.pdstreamline.beans.Shelf;

import java.util.List;

public class ShelfAdapter extends RecyclerView.Adapter<ShelfAdapter.ViewHolder> {

    private List<Shelf> mShelvesList;

    private static Shelf currentShelf;

    public ShelfAdapter(List<Shelf> mShelvesList) {
        this.mShelvesList = mShelvesList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.shelf_item, parent, false);

        final ViewHolder viewHolder = new ViewHolder(view);

        viewHolder.shelfView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                currentShelf = viewHolder.shelf;
                postMessage(viewHolder.shelf, TraversalTimestreamActivity.SHOW_SHELF);
            }
        });

        viewHolder.shelfView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                currentShelf = viewHolder.shelf;

                if ("默认".equals(viewHolder.shelf.getName())) {

                    Toast.makeText(v.getContext(), "默认货架禁止修改", Toast.LENGTH_SHORT).show();
                    return true;
                }

                postMessage(viewHolder.shelf, TraversalTimestreamActivity.MODIFY_SHELF);

                return true;
            }
        });

        return viewHolder;
    }

    private void postMessage(Shelf shelf, int what) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.obj = shelf;
        TraversalTimestreamActivity.handler.sendMessage(msg);
    }

    public static Shelf getCurrentShelf() {
        return currentShelf;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Shelf shelf = mShelvesList.get(position);
        holder.nameTv.setText(shelf.getName());
        holder.classifyTv.setText(shelf.getClassify());
        holder.maxRowTv.setText(String.valueOf(shelf.getMaxRowCount()));
        holder.shelf = shelf;
    }

    @Override
    public int getItemCount() {
        return mShelvesList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        TextView nameTv,classifyTv,maxRowTv;
        View shelfView;
        Shelf shelf;

        public ViewHolder(@NonNull View itemView) {

            super(itemView);

            shelfView = itemView;
            nameTv = itemView.findViewById(R.id.shelf_name);
            classifyTv = itemView.findViewById(R.id.classify);
            maxRowTv = itemView.findViewById(R.id.max_row);
        }
    }
}
