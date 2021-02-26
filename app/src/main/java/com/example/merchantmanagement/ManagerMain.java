package com.example.merchantmanagement;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.alexzaitsev.meternumberpicker.MeterView;
import com.example.merchantmanagement.Chat.ChatRoom;
import com.example.merchantmanagement.Graph.GraphManagerActivity;
import com.example.merchantmanagement.MyTeam.MyTeam;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shashank.sony.fancydialoglib.Animation;
import com.shashank.sony.fancydialoglib.FancyAlertDialog;
import com.shashank.sony.fancydialoglib.FancyAlertDialogListener;
import com.shashank.sony.fancydialoglib.Icon;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;


public class ManagerMain extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    private EditText itemName, profit;
    private MeterView q;
    private DatabaseReference databaseRef;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private InventoryItem it;
    private ProgressBar spinner;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        swipeRefreshLayout=findViewById(R.id.swiperefresh);
        spinner = (ProgressBar)findViewById(R.id.progressBar);

        SessionManager sm = new SessionManager(getApplicationContext());
        HashMap<String, String> details = sm.getUserDetails();
        final String id = details.get("id");
        final String role = details.get("role");
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateList(id,role,false);
                swipeRefreshLayout.setRefreshing(false);
            }
        });



        updateList(id,role,true);

        spinner.setVisibility(View.VISIBLE);

        databaseRef = FirebaseDatabase.getInstance().getReference(role);
        mRecyclerView=findViewById(R.id.items_list);
        final ArrayList<InventoryItem> list= new ArrayList<>();
        databaseRef.child(id).child("Inventory")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            InventoryItem it1 = snapshot.getValue(InventoryItem.class);
                            list.add(it1);
                        }
                                        /* CustomAdapter mAdapter = new CustomAdapter(getApplicationContext(),data);
                                        listView.setAdapter(mAdapter); */
                        mAdapter=new InventoryAdapter(getApplicationContext(), list, ManagerMain.this);
                        mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        mRecyclerView.setAdapter(mAdapter);
                        mAdapter.notifyDataSetChanged();
                        spinner.setVisibility(View.GONE);
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // dialog box to add item on dashboard
                final AlertDialog.Builder mBuilder = new AlertDialog.Builder(ManagerMain.this);
                final View mView = getLayoutInflater().inflate(R.layout.dialog_box_add_item_inventory, null);
                mBuilder.setTitle("Add an item");

                Button ok = (Button) mView.findViewById(R.id.ok);
                itemName = mView.findViewById(R.id.item_name);
                profit = mView.findViewById(R.id.profit);
                q = mView.findViewById(R.id.quantity);

                mBuilder.setView(mView);
                final AlertDialog dialog = mBuilder.create();
                dialog.show();

                ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        String item = itemName.getText().toString();
                        String q1 = String.valueOf(q.getValue());
                        int profitAmount = Integer.parseInt(profit.getText().toString());

                        if(TextUtils.isEmpty(item) || TextUtils.isEmpty(q1))
                        {
                            Toast.makeText(getApplicationContext(), "Please fill all the details!", Toast.LENGTH_LONG).show();
                        }
                        else {

                            int quant = Integer.parseInt(q1);
                            it = new InventoryItem(item, quant, 0, profitAmount);

                            String key = databaseRef.child(id).child("Inventory").push().getKey();
                            databaseRef.child(id).child("Inventory").child(key).setValue(it);
                            dialog.dismiss();

                            spinner.setVisibility(View.VISIBLE);

                            mRecyclerView = findViewById(R.id.items_list);
                            final ArrayList<InventoryItem> list = new ArrayList<>();
                            databaseRef.child(id).child("Inventory")
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                InventoryItem it1 = snapshot.getValue(InventoryItem.class);
                                                list.add(it1);
                                            }

                                            mAdapter = new InventoryAdapter(getApplicationContext(), list, ManagerMain.this);
                                            mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                                            mRecyclerView.setAdapter(mAdapter);
                                            mAdapter.notifyDataSetChanged();

                                           spinner.setVisibility(View.GONE);

                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                        }
                                    });

                            Toast.makeText(getApplicationContext(), "Item added successfully !", Toast.LENGTH_LONG).show();

                            // get current manager name and compare across all salesperson's
                            databaseRef = FirebaseDatabase.getInstance().getReference("Manager");
                            databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    SalesManager sm1;
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        sm1 = snapshot.getValue(SalesManager.class);
                                        if (snapshot.getKey().equals(id)) {

                                            final String ManagerName = sm1.getName();

                                            // compare manager name and insert item in his inventory
                                            final DatabaseReference databaseRef1 = FirebaseDatabase.getInstance().getReference("Salesperson");
                                            databaseRef1.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot2) {
                                                    for( DataSnapshot snapshot1 : dataSnapshot2.getChildren())
                                                    {
                                                        SalesPerson sp1 = snapshot1.getValue(SalesPerson.class);
                                                        String salesperson_id = snapshot1.getKey();

                                                        if(sp1.getManagerName().equals(ManagerName))
                                                        {
                                                            String key1 = databaseRef1.child(salesperson_id).child("Inventory").push().getKey();
                                                            databaseRef1.child(salesperson_id).child("Inventory").child(key1).setValue(it);
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
                        }
                    }
                });
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headView = navigationView.getHeaderView(0);
        final TextView headerManagerName = headView.findViewById(R.id.ManagerName);
        final TextView headerManagerEmail = headView.findViewById(R.id.ManagerMail);
        final ImageView headerManagerImage = headView.findViewById(R.id.imageView);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Manager");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    if(snapshot.getKey().equals(id)){
                        SalesManager sm = snapshot.getValue(SalesManager.class);
                        headerManagerName.setText(sm.getName());
                        headerManagerEmail.setText(sm.getEmail());
                        ImageSetter.setImage(getApplicationContext(),headerManagerImage,sm.getEmail(),spinner);
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {

            // exit dialog box
            new FancyAlertDialog.Builder(this)
                    .setTitle("Warning!!!")
                    .setBackgroundColor(Color.parseColor("#5002a4"))  //Don't pass R.color.colorvalue
                    .setMessage("Do you really want to exit?")
                    .setNegativeBtnText("No")
                    .setPositiveBtnBackground(Color.parseColor("#FF4081"))  //Don't pass R.color.colorvalue
                    .setPositiveBtnText("Yes")
                    .setNegativeBtnBackground(Color.parseColor("#FFA9A7A8"))  //Don't pass R.color.colorvalue
                    .setAnimation(Animation.POP)
                    .isCancellable(true)
                    .setIcon(R.drawable.ic_error_outline_black_24dp, Icon.Visible)
                    .OnPositiveClicked(new FancyAlertDialogListener() {
                        @Override
                        public void OnClick() {
                            ManagerMain.super.onBackPressed();
                        }
                    })
                    .OnNegativeClicked(new FancyAlertDialogListener() {
                        @Override
                        public void OnClick() {

                        }
                    })
                    .build();
            // super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.manager_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if(id == R.id.dashboard) {

        }
        else if (id == R.id.my_account) {

            //show the manager's account
            Intent intent = new Intent(ManagerMain.this,AccountManager.class);
            startActivity(intent);

        } else if (id == R.id.my_team) {
            Intent intent= new Intent(this, MyTeam.class);
            startActivity(intent);

        } else if (id == R.id.statistics) {
            Intent intent=new Intent(ManagerMain.this, GraphManagerActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_share) {
            // Share app with others
            ApplicationInfo api = getApplicationContext().getApplicationInfo();
            String apkpath = api.sourceDir;
            Intent share_intent = new Intent(Intent.ACTION_SEND);
            share_intent.setType("application/vnd.android.package-archive");
            share_intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(apkpath)));
            startActivity(Intent.createChooser(share_intent, "Share app using"));

        } else if (id == R.id.nav_send) {
            // Share invite-code with salespersons

            SessionManager sm = new SessionManager(getApplicationContext());
            HashMap<String, String> details = sm.getUserDetails();
            final String key = details.get("id");

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Manager");
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){

                        if(snapshot.getKey().equals(key)){
                            String shareBody = "Hey, signup using my name - " + snapshot.getValue(SalesManager.class).getName()
                                                 + " on SalesManagement App.";
                            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                            sharingIntent.setType("text/plain");
                            // sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Invite code -");
                            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                            startActivity(Intent.createChooser(sharingIntent, "Send invite"));
                            break;
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else if(id == R.id.chat_room){

            SessionManager sessionManager = new SessionManager(getApplicationContext());
            final String idManager = sessionManager.getUserDetails().get("id");
            databaseRef = FirebaseDatabase.getInstance().getReference("Manager");
            databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                        if(dataSnapshot1.getKey().equals(idManager)){

                            SalesManager salesManager = dataSnapshot1.getValue(SalesManager.class);
                            Intent intent = new Intent(ManagerMain.this, ChatRoom.class);
                            intent.putExtra("ManagerNumber", salesManager.getNumber());
                            intent.putExtra("Name",salesManager.getName());
                            startActivity(intent);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void updateList(String id, String role, final boolean spin)
    {
        if(spin==true)
            spinner.setVisibility(View.VISIBLE);
        databaseRef = FirebaseDatabase.getInstance().getReference(role);
        mRecyclerView=findViewById(R.id.items_list);
        final ArrayList<InventoryItem> list= new ArrayList<>();
        databaseRef.child(id).child("Inventory")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            InventoryItem it1 = snapshot.getValue(InventoryItem.class);
                            list.add(it1);
                        }
                        mAdapter=new InventoryAdapter(getApplicationContext(),list, ManagerMain.this);
                        mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        mRecyclerView.setAdapter(mAdapter);
                        mAdapter.notifyDataSetChanged();
                        if(spin==true)
                        spinner.setVisibility(View.GONE);
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

    }
}
