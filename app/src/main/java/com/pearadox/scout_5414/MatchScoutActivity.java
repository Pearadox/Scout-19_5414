package com.pearadox.scout_5414;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.app.Activity;
import android.view.Menu;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Iterator;

import static android.view.View.VISIBLE;


public class MatchScoutActivity extends AppCompatActivity {

    String TAG = "MatchScout_Activity";      // This CLASS name
    boolean onStart = false;
    public static String device = " ";
    public static String studID = " ";
    TextView txt_dev, txt_stud, txt_Match, txt_MyTeam, text_HGSeekBarValue, text_LGSeekBarValue;
    CheckBox chk_baseline, chk_highGoal, chkBox_balls, chkBox_gears, chkBox_rope, chk_lowGoal, checkbox_automode;
    SeekBar seekBar_HighGoal, seekBar_LowGoal;
    ImageView imgScoutLogo;
    public String matchID = "T00";      // Type + #
    String team_num, team_name, team_loc;
    p_Firebase.teamsObj team_inst = new p_Firebase.teamsObj(team_num, team_name, team_loc);
    private FirebaseDatabase pfDatabase;
    private DatabaseReference pfTeam_DBReference;
    private DatabaseReference pfMatch_DBReference;
    private DatabaseReference pfDevice_DBReference;
    private DatabaseReference pfCur_Match_DBReference;
    TextView txt_TeamName;
    TextView txt_GearsPlaced;
    private Button button_GearsMinus, button_GearsPlus, button_GoToTeleopActivity, button_GoToArenaLayoutActivity, button_GoToOtherActivityFromAuto;
    int gearNum = 0;
    int HGSvalue = 0;
    int LGSvalue = 0;
    String key = null;
    ArrayAdapter<String> adapter_autostartpos;
    ArrayAdapter<String> adapter_autostoppos;
    public String startPos = " ";
    public String stopPos = " ";





    //    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "<< Match Scout >>");
        onStart = false;
        setContentView(R.layout.activity_match_scout);
        Bundle bundle = this.getIntent().getExtras();
        String device = bundle.getString("dev");
        String studID = bundle.getString("stud");
        Log.d(TAG, device + " " + studID);      // ** DEBUG **
        pfDatabase = FirebaseDatabase.getInstance();
        pfTeam_DBReference = pfDatabase.getReference("teams");              // Tteam data from Firebase D/B
//        pfStudent_DBReference = pfDatabase.getReference("students");        // List of Students
//        pfDevice_DBReference = pfDatabase.getReference("devices");          // List of Students
        pfMatch_DBReference = pfDatabase.getReference("matches");           // List of Students
        pfCur_Match_DBReference = pfDatabase.getReference("current-match"); // _THE_ current Match
        pfDevice_DBReference = pfDatabase.getReference("devices");          // List of Students
        txt_GearsPlaced = (TextView) findViewById(R.id.txt_GearsPlaced);
        chk_baseline = (CheckBox) findViewById(R.id.chk_baseline);
        chk_highGoal = (CheckBox) findViewById(R.id.chk_highGoal);
        chk_lowGoal = (CheckBox) findViewById(R.id.chk_LowGoal);
        seekBar_HighGoal = (SeekBar) findViewById(R.id.seekBar_HighGoal);
        seekBar_LowGoal = (SeekBar) findViewById(R.id.seekBar_LowGoal);
        checkbox_automode = (CheckBox) findViewById(R.id.checkbox_automode);
        chkBox_balls = (CheckBox) findViewById(R.id.chk_balls);
        chkBox_gears = (CheckBox) findViewById(R.id.chk_gears);
        chkBox_rope = (CheckBox) findViewById(R.id.chk_rope);
        button_GearsMinus = (Button) findViewById(R.id.button_GearsMinus);
        button_GearsPlus = (Button) findViewById(R.id.button_GearsPlus);
        button_GoToTeleopActivity = (Button) findViewById(R.id.button_GoToTeleopActivity);
        button_GoToArenaLayoutActivity = (Button) findViewById(R.id.button_GoToArenaLayoutActivity);
        button_GoToOtherActivityFromAuto = (Button) findViewById(R.id.button_GoToFinalActivity);
        txt_GearsPlaced.setText(Integer.toString(gearNum));
        seekBar_HighGoal.setEnabled(false);
        seekBar_HighGoal.setVisibility(View.GONE);
        chk_highGoal.setChecked(false);
        seekBar_LowGoal.setEnabled(false);
        seekBar_LowGoal.setVisibility(View.GONE);
        chk_lowGoal.setChecked(false);
        text_HGSeekBarValue = (TextView) findViewById(R.id.text_HGSeekBarValue);
        text_LGSeekBarValue = (TextView) findViewById(R.id.text_LGSeekBarValue);
        text_HGSeekBarValue.setVisibility(View.GONE);
        text_LGSeekBarValue.setVisibility(View.GONE);

        Spinner spinner_startPos = (Spinner) findViewById(R.id.spinner_startPos);
        String[] autostartPos = getResources().getStringArray(R.array.auto_start_array);
        adapter_autostartpos = new ArrayAdapter<String>(this, R.layout.dev_list_layout, autostartPos);
        adapter_autostartpos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_startPos.setAdapter(adapter_autostartpos);
        spinner_startPos.setSelection(0, false);
        spinner_startPos.setOnItemSelectedListener(new MatchScoutActivity.startPosOnClickListener());

        Spinner spinner_stopPos = (Spinner) findViewById(R.id.spinner_stopPos);
        String[] autostopPos = getResources().getStringArray(R.array.auto_stop_array);
        adapter_autostoppos = new ArrayAdapter<String>(this, R.layout.dev_list_layout, autostopPos);
        adapter_autostoppos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_stopPos.setAdapter(adapter_autostoppos);
        spinner_stopPos.setSelection(0, false);
        spinner_stopPos.setOnItemSelectedListener(new MatchScoutActivity.stopPosOnClickListener());



        checkbox_automode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                Log.i(TAG, "checkbox_automode Listener");
                if (buttonView.isChecked()) {
                    //checked
                    Log.i(TAG,"TextBox is checked.");

                }
                else
                {
                    //not checked
                    Log.i(TAG,"TextBox is unchecked.");

                }
            }
        }
        );
        chk_baseline.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                Log.i(TAG, "chk_baseline Listener");
                if (buttonView.isChecked()) {
                    //checked
                    Log.i(TAG,"TextBox is checked.");

                }
                else
                {
                    //not checked
                    Log.i(TAG,"TextBox is unchecked.");

                }
            }
        }
        );
        chkBox_balls.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                Log.i(TAG, "chkBox_balls Listener");
                if (buttonView.isChecked()) {
                    //checked
                    Log.i(TAG,"TextBox is checked.");

                }
                else
                {
                    //not checked
                    Log.i(TAG,"TextBox is unchecked.");

                }
            }
        }
        );
        chkBox_gears.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                Log.i(TAG, "chkBox_gears Listener");
                if (buttonView.isChecked()) {
                    //checked
                    Log.i(TAG,"TextBox is checked.");

                }
                else
                {
                    //not checked
                    Log.i(TAG,"TextBox is unchecked.");

                }
            }
        }
        );
        chkBox_rope.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                Log.i(TAG, "chkBox_rope Listener");
                if (buttonView.isChecked()) {
                    //checked
                    Log.i(TAG,"TextBox is checked.");

                }
                else
                {
                    //not checked
                    Log.i(TAG,"TextBox is unchecked.");

                }
            }
        }
        );
        chk_highGoal.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                Log.i(TAG, "chk_highGoal Listener");
                if (buttonView.isChecked()) {
                    //checked
                    Log.i(TAG,"TextBox is checked.");

                }
                else
                {
                    //not checked
                    Log.i(TAG,"TextBox is unchecked.");

                }
                if (buttonView.isChecked()) {
                    //checked
                    seekBar_HighGoal.setEnabled(true);
                    seekBar_HighGoal.setVisibility(View.VISIBLE);
                    text_HGSeekBarValue.setVisibility(View.VISIBLE);

                }
                else
                {
                    //not checked
                    seekBar_HighGoal.setEnabled(false);
                    seekBar_HighGoal.setVisibility(View.GONE);
                    text_HGSeekBarValue.setVisibility(View.GONE);

                }
            }
        }
        );
        chk_lowGoal.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                Log.i(TAG, "chk_lowGoal Listener");
                if (buttonView.isChecked()) {
                    //checked
                    Log.i(TAG,"TextBox is checked.");

                }
                else
                {
                    //not checked
                    Log.i(TAG,"TextBox is unchecked.");

                }
                if (buttonView.isChecked()) {
                    //checked
                    seekBar_LowGoal.setEnabled(true);
                    seekBar_LowGoal.setVisibility(View.VISIBLE);
                    text_LGSeekBarValue.setVisibility(View.VISIBLE);

                }
                else
                {
                    //not checked
                    seekBar_LowGoal.setEnabled(false);
                    seekBar_LowGoal.setVisibility(View.GONE);
                    text_LGSeekBarValue.setVisibility(View.GONE);


                }
            }
        }
        );


        button_GearsPlus.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // ToDo check to ensure not over MAX # gears
                if (gearNum < 3) {
                    gearNum++;
                }
                Log.d(TAG, "Gears = " + gearNum);      // ** DEBUG **
                txt_GearsPlaced.setText(Integer.toString(gearNum));    // Perform action on click
            }
        });
        button_GearsMinus.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // ToDo make sure not already at zero
                if (gearNum >= 1) {
                    gearNum--;
                }
                Log.d(TAG, "Gears = " + gearNum);      // ** DEBUG **
                txt_GearsPlaced.setText(Integer.toString(gearNum));    // Perform action on click
            }
        });
        button_GoToTeleopActivity.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                updateDev("Tele");

                Intent smast_intent = new Intent(MatchScoutActivity.this, TeleopScoutActivity.class);
                Bundle SMbundle = new Bundle();
                smast_intent.putExtras(SMbundle);
                startActivity(smast_intent);
            }
        });
        Log.i(TAG, "About to Click Button");

        button_GoToArenaLayoutActivity.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Log.i(TAG, "Clicked Sidebar");
                Intent smast_intent = new Intent(MatchScoutActivity.this, ArenaLayoutActivity.class);
                Bundle SMbundle = new Bundle();
                smast_intent.putExtras(SMbundle);
                startActivity(smast_intent);
            }
        });
        Button button_GoToFinalActivityFromAuto = (Button) findViewById(R.id.button_GoToFinalActivityFromAuto);
        button_GoToFinalActivityFromAuto.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent smast_intent = new Intent(MatchScoutActivity.this, FinalActivity.class);
                Bundle SMbundle = new Bundle();
                smast_intent.putExtras(SMbundle);
                startActivity(smast_intent);            }
        });

        txt_dev = (TextView) findViewById(R.id.txt_Dev);
        txt_stud = (TextView) findViewById(R.id.txt_TeamName);
        txt_Match = (TextView) findViewById(R.id.txt_Match);
        txt_MyTeam = (TextView) findViewById(R.id.txt_MyTeam);
        txt_TeamName = (TextView) findViewById(R.id.txt_TeamName);
        ImageView imgScoutLogo = (ImageView) findViewById(R.id.imageView_MS);
        txt_dev.setText(device);
        txt_stud.setText(studID);
        txt_Match.setText("");
        txt_MyTeam.setText("");
        txt_TeamName.setText("");
        String devcol = device.substring(0,3);
        Log.d(TAG, "color=" + devcol);
        if (devcol.equals("Red")) {
            imgScoutLogo.setImageDrawable(getResources().getDrawable(R.drawable.red_scout));
        } else {
            imgScoutLogo.setImageDrawable(getResources().getDrawable(R.drawable.blue_scout));
        }

        seekBar_HighGoal.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar_HighGoal) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar_HighGoal) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onProgressChanged(SeekBar seekBar_HighGoal, int progress,
                                          boolean fromUser) {
                // TODO Auto-generated method stub

                HGSvalue=progress;	//we can use the progress value of pro as anywhere
                text_HGSeekBarValue.setText(Integer.toString(HGSvalue));
            }

        });
        seekBar_LowGoal.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar_LowGoal) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar_LowGoal) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onProgressChanged(SeekBar seekBar_LowGoal, int progress,
                                          boolean fromUser) {
                // TODO Auto-generated method stub

                LGSvalue=progress;	//we can use the progress value of pro as anywhere
                text_LGSeekBarValue.setText(Integer.toString(LGSvalue));
            }

        });



    }
    private void getMatch() {
        Log.d(TAG, "%%%%  getMatch  %%%%");
        pfCur_Match_DBReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "Current Match - onDataChange  %%%%");
                txt_Match = (TextView) findViewById(R.id.txt_Match);
                txt_MyTeam = (TextView) findViewById(R.id.txt_MyTeam);
                txt_TeamName = (TextView) findViewById(R.id.txt_TeamName);
                Iterable<DataSnapshot> snapshotIterator = dataSnapshot.getChildren();   /*get the data children*/
                Iterator<DataSnapshot> iterator = snapshotIterator.iterator();
                while (iterator.hasNext()) {
                    p_Firebase.curMatch match_Obj = iterator.next().getValue(p_Firebase.curMatch.class);
                    matchID = match_Obj.getCur_match();
//                    Log.d(TAG, "***>  Current Match = " + matchID + " " + match_Obj.getR1() + " " + match_Obj.getB3());
                    if (matchID.equals(null)) {
                        txt_Match.setText(" ");
                        txt_MyTeam.setText(" ");
                        txt_TeamName.setText(" ");
                    } else {        // OK!!  Match has started
                        txt_Match.setText(matchID);
//                        Log.d(TAG, "Device = " + Pearadox.FRC514_Device + " ->" + onStart);
                        switch (Pearadox.FRC514_Device) {          // Who am I?!?
                            case ("Red-1"):             //#Red or Blue Scout
                                txt_MyTeam.setText(match_Obj.getR1());
                                break;
                            case ("Red-2"):             //#
                                txt_MyTeam.setText(match_Obj.getR2());
                                break;
                            case ("Red-3"):             //#
                                txt_MyTeam.setText(match_Obj.getR3());
                                break;
                            case ("Blue-1"):            //#
                                txt_MyTeam.setText(match_Obj.getB1());
                                break;
                            case ("Blue-2"):            //#
                                txt_MyTeam.setText(match_Obj.getB2());
                                break;
                            case ("Blue-3"):            //#####
                                txt_MyTeam.setText(match_Obj.getB3());
                                break;
                            default:                //
                                Log.d(TAG, "device is _NOT_ a Scout ->" + device );
                        }
                        String tn = (String) txt_MyTeam.getText();
                        findTeam(tn);   // Find Team info
                        txt_TeamName.setText(team_inst.getTeam_name());
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                /*listener failed or was removed for security reasons*/
            }
        });
    }
    private void findTeam(String tnum) {
        Log.i(TAG, "$$$$$  findTeam " + tnum);
        boolean found = false;
        for (int i = 0; i < Pearadox.numTeams; i++) {        // check each team entry
            if (Pearadox.team_List.get(i).getTeam_num().equals(tnum)) {
                team_inst = Pearadox.team_List.get(i);
//                Log.d(TAG, "===  Team " + team_inst.getTeam_num() + " " + team_inst.getTeam_name() + " " + team_inst.getTeam_loc());
                found = true;
                break;  // found it!
            }
        }  // end For
        if (!found) {
            Log.e(TAG, "****** ERROR - Team _NOT_ found!! = " + tnum);
        }
    }
    private void updateDev(String phase) {     //
        Log.i(TAG, "#### updateDev #### " + phase);
        switch (Pearadox.FRC514_Device) {
            case "Scout Master":         // Scout Master
                key = "0";
                break;
            case ("Red-1"):             //#Red or Blue Scout
                key = "1";
                break;
            case ("Red-2"):             //#
                key = "2";
                break;
            case ("Red-3"):             //#
                key = "3";
                break;
            case ("Blue-1"):            //#
                key = "4";
                break;
            case ("Blue-2"):            //#
                key = "5";
                break;
            case ("Blue-3"):            //#####
                key = "6";
                break;
            case "Visualizer":          // Visualizer
                key = "7";
                break;
            default:                //
                Log.d(TAG, "DEV = NULL" );
        }
             pfDevice_DBReference.child(key).child("phase").setValue(phase);
    }

    private class startPosOnClickListener implements android.widget.AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent,
                                   View view, int pos, long id) {
            startPos = parent.getItemAtPosition(pos).toString();
            Log.d(TAG, ">>>>>  '" + startPos + "'");

        }
        public void onNothingSelected(AdapterView<?> parent) {
            // Do nothing.
        }
    }
    private class stopPosOnClickListener implements android.widget.AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent,
                                   View view, int pos, long id) {
            stopPos = parent.getItemAtPosition(pos).toString();
            Log.d(TAG, ">>>>>  '" + stopPos + "'");

        }
        public void onNothingSelected(AdapterView<?> parent) {
            // Do nothing.
        }
    }



    //###################################################################
//###################################################################
//###################################################################
@Override
public void onStart() {
    super.onStart();
    Log.v(TAG, "onStart");

    onStart = true;
    getMatch();      // Get current match
    Log.d(TAG, "onStart Device = " + device + " ->" + onStart);
}

    public void onPause() {
        super.onPause();
        Log.v(TAG, "onPause");
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("Device", device);
        editor.putString("Student", studID);
        editor.commit(); 		// keep same data
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v(TAG, "onResume");
        onStart = false;
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        String device = prefs.getString("Device", "");
        String studID = prefs.getString("Student", "");
        Log.d(TAG, "Dev=" + device + "  " + "Student=" + studID);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.v(TAG, "onStop");
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "OnDestroy");
        // ToDo - ??????
    }



}
