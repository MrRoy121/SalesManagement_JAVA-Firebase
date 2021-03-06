package com.example.merchantmanagement.MyTeam;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.example.merchantmanagement.Chat.PersonalChatActivityManager;
import com.example.merchantmanagement.ImageSetter;
import com.example.merchantmanagement.R;
import com.example.merchantmanagement.SalesManager;
import com.example.merchantmanagement.SalesPerson;
import com.example.merchantmanagement.SessionManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;


public class MyTeam extends AppCompatActivity {

    private SwipeMenuListView listView;
    private MyTeamAdapter myTeamAdapter;
    private ProgressBar spinner;
    private String managerName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_team);
        listView=findViewById(R.id.teamListView);
        spinner=findViewById(R.id.progressBar5);

        final ArrayList<SalesPerson> list=new ArrayList<>();

        spinner.setVisibility(View.VISIBLE);

        SessionManager sm = new SessionManager(getApplicationContext());
        HashMap<String, String> details = sm.getUserDetails();
        final String id = details.get("id");

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Manager");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    if(snapshot.getKey().equals(id))
                    {
                        managerName = snapshot.getValue(SalesManager.class).getName();

                        DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference("Salesperson");
                        databaseReference1.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for(DataSnapshot snapshot1 : dataSnapshot.getChildren()){
                                    if(snapshot1.getValue(SalesPerson.class).getManagerName().equals(managerName)){
                                        list.add(snapshot1.getValue(SalesPerson.class));
                                    }
                                }
                                /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                                SwipeMenuCreator creator = new SwipeMenuCreator() {

                                    @Override
                                    public void create(SwipeMenu menu) {

                                        // create Details item
                                        SwipeMenuItem detailsItem = new SwipeMenuItem(
                                                getApplicationContext());
                                        detailsItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9,
                                                0xCE)));
                                        detailsItem.setWidth(dp2px(90));
                                        detailsItem.setTitle("Details");
                                        detailsItem.setTitleSize(18);
                                        detailsItem.setTitleColor(Color.WHITE);
                                        menu.addMenuItem(detailsItem);

                                        // create delete item
                                        SwipeMenuItem messageItem = new SwipeMenuItem(
                                                getApplicationContext());
                                        messageItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                                                0x3F, 0x25)));
                                        messageItem.setWidth(dp2px(90));
                                        messageItem.setIcon(R.drawable.ic_message);
                                        menu.addMenuItem(messageItem);
                                    }

                                    public  int dp2px(float dips)
                                    {
                                        return (int) (dips * getApplicationContext().getResources().getDisplayMetrics().density + 0.5f);
                                    }
                                };

                                listView.setMenuCreator(creator);

                                listView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                                        switch (index) {
                                            case 0:

                                                final AlertDialog.Builder mBuilder = new AlertDialog.Builder(MyTeam.this);
                                                final View mView = getLayoutInflater().inflate(R.layout.dialog_box_myteam_details, null);

                                                mBuilder.setView(mView);
                                                final AlertDialog dialog = mBuilder.create();

                                                final TextView name, phone, email, org;
                                                final ImageView imageView;

                                                final ProgressBar spinner7 = mView.findViewById(R.id.progressBar6);
                                                final ProgressBar spinnerImage = mView.findViewById(R.id.progressBar10);

                                                name = mView.findViewById(R.id.name);
                                                phone = mView.findViewById(R.id.mobile);
                                                email = mView.findViewById(R.id.emailid);
                                                org = mView.findViewById(R.id.organisation);
                                                imageView=mView.findViewById(R.id.user_pic);
                                                dialog.show();

                                                spinner7.setVisibility(View.VISIBLE);
                                                //spinnerImage.setVisibility(View.VISIBLE);

                                                final SalesPerson currSalesperson = list.get(position);

                                                DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Salesperson");
                                                reference1.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        for(DataSnapshot snapshot1 : dataSnapshot.getChildren()){

                                                            final SalesPerson sp = snapshot1.getValue(SalesPerson.class);

                                                            if(sp.getName().equals(currSalesperson.getName())){

                                                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Manager");
                                                                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                        for(DataSnapshot snapshot2 : dataSnapshot.getChildren()){
                                                                            if(snapshot2.getKey().equals(id)){
                                                                                org.setText(snapshot2.getValue(SalesManager.class).getOrgName());
                                                                                name.setText(sp.getName());
                                                                                phone.setText(sp.getNumber());
                                                                                email.setText(sp.getEmailId());
                                                                                ImageSetter.setImage(mView.getContext(),imageView,sp.getEmailId(),spinnerImage);
                                                                                spinner7.setVisibility(View.GONE);
                                                                                break;
                                                                            }
                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                    }
                                                                });
                                                                break;
                                                            }
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                });
                                                break;
                                            case 1:
                                                //Toast.makeText(getApplicationContext(),"Aa gya hun mai",Toast.LENGTH_LONG).show();
                                                Intent intent=new Intent(getApplicationContext(), PersonalChatActivityManager.class);
                                                intent.putExtra("SalespersonName", list.get(position).getName());
                                                intent.putExtra("ManagerName",managerName);
                                                startActivity(intent);
                                                break;
                                        }
                                        // false : close the menu; true : not close the menu
                                        return false;
                                    }
                                });

                                listView.setCloseInterpolator(new BounceInterpolator());

                                myTeamAdapter= new MyTeamAdapter(getApplicationContext(),list);
                                listView.setAdapter(myTeamAdapter);
                                spinner.setVisibility(View.GONE);
                                /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
