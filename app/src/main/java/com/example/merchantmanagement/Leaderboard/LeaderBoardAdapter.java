package com.example.merchantmanagement.Leaderboard;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.merchantmanagement.ImageSetter;
import com.example.merchantmanagement.R;
import com.example.merchantmanagement.SalesPerson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LeaderBoardAdapter extends RecyclerView.Adapter<LeaderBoardAdapter.MyViewHolder> {

    private ArrayList<SalesPerson> personArrayList;
    private ArrayList<String> performanceIndex;
    private Context context;

    public LeaderBoardAdapter()
    {

    }

    public LeaderBoardAdapter(Context context,ArrayList<SalesPerson> personArrayList,ArrayList<String> performanceIndex)
    {
        this.context=context;
        this.personArrayList=personArrayList;
        this.performanceIndex=performanceIndex;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View item= LayoutInflater.from(parent.getContext()).inflate(R.layout.leaderboard_item,parent,false);

        return new MyViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        List<Pair<String,SalesPerson>> container = new ArrayList<>();
        for(int i=0;i<performanceIndex.size();i++){
            container.add(new Pair<String, SalesPerson>(performanceIndex.get(i), personArrayList.get(i)));
        }
        Collections.sort(container, new Comparator<Pair<String, SalesPerson>>() {
            @Override
            public int compare(Pair<String, SalesPerson> stringSalesPersonPair, Pair<String, SalesPerson> t1) {
                return -stringSalesPersonPair.first.compareTo(t1.first);
            }
        });

        ArrayList<String> PI = new ArrayList<>();
        ArrayList<SalesPerson> SP = new ArrayList<>();
        for(int i=0; i< Math.min(10,performanceIndex.size()); i++){
                PI.add(container.get(i).first);
                SP.add(container.get(i).second);
        }

        SalesPerson salesPerson = SP.get(position);
        holder.performanceIndex.setText(PI.get(position));
        holder.name.setText(salesPerson.getName());
        holder.rank.setText(String.valueOf(position+1)+".");
        ImageSetter.setImage(context,holder.imageView,salesPerson.getEmailId(),holder.progressBar);
    }

    @Override
    public int getItemCount() {
        return Math.min(performanceIndex.size(),10);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView rank,name,performanceIndex;
        private ImageView imageView;
        private ProgressBar progressBar;
        public MyViewHolder(View itemView) {
            super(itemView);
            rank=itemView.findViewById(R.id.rank);
            name=itemView.findViewById(R.id.salesperson_name);
            performanceIndex=itemView.findViewById(R.id.performance_index);
            imageView=itemView.findViewById(R.id.salesperson_pic);
            progressBar=itemView.findViewById(R.id.leaderboard_progress);
        }
    }
}
