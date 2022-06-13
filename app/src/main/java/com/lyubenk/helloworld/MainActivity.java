package com.lyubenk.helloworld;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import android.view.View;
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


public class MainActivity extends AppCompatActivity {

    private int sets = 0;
    private int reps = 0;
    private TextView mSetCount;
    private TextView mRepCount;
    private TextView mWeightNumber;
    private TextView mWorkoutName;
    private TextView mSelectedWorkout;
    private int selectedWorkout = 0;
    private File[] exerciseFiles;
    String tag = "Debugging";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSetCount= (TextView) findViewById(R.id.set_count);
        mRepCount= (TextView) findViewById(R.id.rep_count);
        mWeightNumber= (TextView) findViewById(R.id.weight_number);
        mWorkoutName = (TextView) findViewById(R.id.workout_name);
        mSelectedWorkout = (TextView) findViewById(R.id.selected_workout);
        fetchExerciseFiles();

        // Set selectedWorkoutView to first item (default)
        setSelectedWorkout(0);
    }
    public void countSetUp(View view) {
        setSets(sets+1);
    }
    public void countSetDown(View view) { setSets(sets-1); }
    private void setSets(int number){
        if(number < 0 || number > 9) return;
        sets = number;
        if(mSetCount != null){
            mSetCount.setText(Integer.toString(sets));
        }
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
    public void countRepDownBulk(View view){ setReps(Math.max(reps-5,0)); }
    private void setReps(int number){
        if(number < 0) return;
        reps = number;
        if(mRepCount != null){
            mRepCount.setText(Integer.toString(reps));
        }
    }

    public void selectNextWorkout(View view){ setSelectedWorkout(selectedWorkout+1);}
    public void selectPreviousWorkout(View view){ setSelectedWorkout(selectedWorkout-1);}
    public void setSelectedWorkout(int index){
        if(exerciseFiles.length == 0){
            mSelectedWorkout.setText("");
            return;
        }
        else if(index < 0){
            index = 0;
        }
        else if(index >= exerciseFiles.length){
            index = exerciseFiles.length-1;
        }
        selectedWorkout = index;
        String exerciseName = exerciseFiles[index].getName();
        //                                              Remove .json at end
        mSelectedWorkout.setText(exerciseName.substring(0,exerciseName.length()-5));
    }

    private Workout getWorkoutFromFields(){
        Workout workout = new Workout();
        workout.name = mWorkoutName.getText().toString();
        workout.reps = Integer.parseInt(mRepCount.getText().toString());
        workout.sets = Integer.parseInt(mSetCount.getText().toString());
        workout.weight = Float.parseFloat(mWeightNumber.getText().toString());
        return workout;
    }
    private void setFieldsFromWorkout(Workout workout){
        mWorkoutName.setText(workout.name);
        setReps(workout.reps);
        setSets(workout.sets);
        mWeightNumber.setText(Float.toString(workout.weight));
    }
    public void readIntoLayout(View view) throws FileNotFoundException {
        File file = exerciseFiles[selectedWorkout];
        // TODO: Replace with dynamic data structure
        FileInputStream fis = this.openFileInput(file.getName());
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
            String contents = stringBuilder.toString();
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            Workout workout = gson.fromJson(contents,Workout.class);
            setFieldsFromWorkout(workout);
        }
    }
    private void fetchExerciseFiles(){
        HashSet<File> names = new HashSet<>();
        File[] files = getFilesDir().listFiles();
        for(int i =0;i<files.length;++i){
            if(files[i].getName().contains(".json")){
                names.add(files[i]);
            }
        }
        exerciseFiles = new File[names.size()];
        names.toArray(exerciseFiles);
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
    public void clearForViews(View view){clear();}
    public void clear(){
        setReps(0);
        setSets(0);
        mWorkoutName.setText("");
        mWeightNumber.setText("");
    }
    public void save(View view){
        Toast toast = new Toast(this);
        if (mWorkoutName.getText().length() == 0) {
            toast.setText("Couldn't save because of invalid name.");
            toast.show();
            return;
        } else if (reps == 0 || sets == 0) {
            toast.setText("Reps and sets need to be at least one.");
            toast.show();
            return;
        }
        File file = new File(this.getFilesDir(), mWorkoutName.getText().toString() + ".json");

        try {
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            String data = gson.toJson(getWorkoutFromFields());

            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(file));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
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
    public void delete(View view){
        String filename = mWorkoutName.getText().toString();
        if(filename.isEmpty()){
            Toast toast = new Toast(this);
            toast.setText("Cannot delete a nameless file.");
            toast.show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Confirm");
        builder.setMessage("Are you sure you want to delete '"+filename+"'?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                deleteFile(exerciseFiles[selectedWorkout].getName());
                Log.d("Deleting...","Deleting "+filename+".");
                fetchExerciseFiles();
                setSelectedWorkout(selectedWorkout-1);
                clear();
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }
}