package com.lyubenk.helloworld;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;


public class MainActivity extends AppCompatActivity {

    private int sets = 0;
    private int reps = 0;
    private TextView mSetCount;
    private TextView mRepCount;
    private TextView mWeightNumber;
    private TextView mWorkoutName;
    String tag = "Debugging";
    private boolean allClickable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(tag,"in onCreate(Bundle).");
        mSetCount= (TextView) findViewById(R.id.set_count);
        mRepCount= (TextView) findViewById(R.id.rep_count);
        mWeightNumber= (TextView) findViewById(R.id.weight_number);
        mWorkoutName = (TextView) findViewById(R.id.workout_name);
    }
    public void countSetUp(View view) {
        setSets(sets+1);
    }
    public void countSetDown(View view) {
        setSets(sets-1);
    }
    public void countRepUpBulk(View view){
        setReps(Math.min(reps+5,99));
    }
    public void countRepUp(View view) {
        setReps(reps+1);
    }

    public void countRepDown(View view) {
        setReps(reps-1);
    }
    public void countRepDownBulk(View view){
        setReps(Math.max(reps-5,0));
    }

    private void setSets(int number){
        if(number < 0 || number > 9) return;
        sets = number;
        if(mSetCount != null){
            mSetCount.setText(Integer.toString(sets));
        }
    }
    private void setReps(int number){
        if(number < 0) return;
        reps = number;
        if(mRepCount != null){
            mRepCount.setText(Integer.toString(reps));
        }
    }
    public void weightCursorReset(View view){
        EditText et = (EditText)view;
        et.setSelection(et.length());
    }
    public void onStart(){
        super.onStart();
        Log.d(tag,"in onStart().");
    }
    public void onRestart(){
        super.onRestart();
        Log.d(tag,"in onRestart().");
    }
    public void onResume(){
        super.onResume();
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        String weight = sharedPref.getString("weightNumber","0");
        if(weight != "0")
            mWeightNumber.setText(weight);
        setReps(Integer.parseInt(sharedPref.getString("repCount","0").toString()));
        setSets(Integer.parseInt(sharedPref.getString("setCount","0").toString()));
        Log.d(tag,"in onResume().");
    }
    public void onPause(){
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("weightNumber", mWeightNumber.getText().toString());
        editor.putString("repCount", mRepCount.getText().toString());
        editor.putString("setCount", mSetCount.getText().toString());
        editor.apply();
        super.onPause();
        Log.d(tag,"in onPause().");
    }
    public void onStop(){
        super.onStop();
        Log.d(tag,"in onStop().");
    }
    public void onDestroy(){
        super.onDestroy();
        SharedPreferences sharedPref  = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.commit();
        Log.d(tag,"in onDestroy().");
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void read(View view){
        Toast toast = new Toast(this);

        File[] files = getFilesDir().listFiles();
        Set<String> list = new HashSet<>();
        for(int i =0;i<files.length;++i){
            if(files[i].getName().contains(".json")){
                list.add(files[i].getName());
            }
        }
        toast.setText("Files found: "+ list);
        toast.show();
    }
    public void save(View view){
        File file = new File(this.getFilesDir(), mWorkoutName.getText().toString() + ".json");

        try {
            Workout workout = new Workout();
            workout.name = mWorkoutName.getText().toString();
            workout.reps = Integer.parseInt(mRepCount.getText().toString());
            workout.sets = Integer.parseInt(mSetCount.getText().toString());
            workout.weight = Float.parseFloat(mWeightNumber.getText().toString());

            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            String data = gson.toJson(workout);

            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(file));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
        Toast toast = new Toast(this);
        toast.setText("Successfully saved exercise "+mWorkoutName.getText().toString());
        toast.show();

        FileInputStream fis = null;
        try {
            fis = openFileInput(file.getName());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        InputStreamReader inputStreamReader =
                new InputStreamReader(fis, StandardCharsets.UTF_8);
        StringBuilder stringBuilder = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
            String line = reader.readLine();
            while (line != null) {
                stringBuilder.append(line).append('\n');
                line = reader.readLine();
            }
        } catch (IOException e) {
            // Error occurred when opening raw file for reading.
        } finally {
            Log.d(tag,"Read successfully: "+ stringBuilder.toString());
        }
    }
}