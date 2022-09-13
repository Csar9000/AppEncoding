package com.example.myappencoding;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {
    private static final int ACTIVITY_CHOOSE_FILE =666;
    final String LOG_TAG = "myLogs";
    final String FILENAME = "file";

    public String FROM;
    public String TO;

    public Spinner spinner1;
    public Spinner spinner2;



    public String[] formats = {"UTF-8","UTF16-LE"};
    
    public  Button butToCp1251;
    public  Button butToUtf8;
    public EditText textEdit;
    public EditText textEdit2;
    public EditText editText3;
    public Button decodeBtn;
    public TextView textView3;
    public Button SaveBtn;
    public EditText SaveFileName;
    public EditText FromUTF16toUTF8;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        butToCp1251 = findViewById(R.id.button);
        butToUtf8 = findViewById(R.id.button2);
        textEdit = findViewById(R.id.editTextTextPersonName);
        textEdit2 = findViewById(R.id.editTextTextPersonName2);
        decodeBtn = findViewById(R.id.button3);
        editText3 = findViewById(R.id.editTextTextPersonName5);
        SaveBtn = findViewById(R.id.SaveButton4);
        SaveFileName = findViewById(R.id.editTextFileNameToSave);
        FromUTF16toUTF8 = findViewById(R.id.editTextTextPersonName4);



        textView3 = findViewById(R.id.textView3);

        spinner1 = findViewById(R.id.spinner);
        spinner2 = findViewById(R.id.spinner2);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, formats);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, formats);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner1.setAdapter(adapter);
        spinner2.setAdapter(adapter2);


        SaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeFile();
            }
        });
        AdapterView.OnItemSelectedListener itemSelectedListener1 = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = (String)parent.getItemAtPosition(position);
                FROM = item;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };

        AdapterView.OnItemSelectedListener itemSelectedListener2 = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = (String)parent.getItemAtPosition(position);
                TO = item;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };

        spinner1.setOnItemSelectedListener(itemSelectedListener1);
        spinner2.setOnItemSelectedListener(itemSelectedListener2);

        decodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBrowse();
            }
        });




        butToUtf8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = textEdit.getText().toString();
                if (s.length()!=0){
                    s = EncodingIntoUTF16(s);
                }
                textEdit2.setText(s);
            }
        });



        butToCp1251.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = textEdit2.getText().toString();
                if(s.length()!=0){
                    s= EncodingIntoUTF8(s);
                }
                editText3.setText(s);

            }
        });

        textEdit.setTextColor(Color.BLUE);
        textEdit2.setTextColor(Color.BLUE);

        textEdit.setVisibility(View.VISIBLE);
        textEdit2.setVisibility(View.VISIBLE);
    }


    public void onBrowse() {
        Intent chooseFile;
        Intent intent;
        chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
        chooseFile.setType("text/plain");
        intent = Intent.createChooser(chooseFile, "Choose a file");
        startActivityForResult(intent, ACTIVITY_CHOOSE_FILE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTIVITY_CHOOSE_FILE) {
            if (resultCode != RESULT_OK || data == null) {
                super.onActivityResult(requestCode, resultCode, data);
                return;
            }
            Uri uri = data.getData();
            if (uri == null) {
                return;
            }
            String fileName = getFileName(uri);
            String fileContent = getFileContent(uri);

            if(FROM == "UTF-8" && TO=="UTF16-LE"){
                fileContent = new String(fileContent.getBytes(StandardCharsets.UTF_8),StandardCharsets.UTF_16LE);
            }
            if(FROM == "UTF16-LE" && TO=="UTF-8"){
                fileContent = new String(fileContent.getBytes(StandardCharsets.UTF_16LE),StandardCharsets.UTF_8);
            }

            textView3.setText(fileContent);

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public String getFileContent(Uri contentUri) {
        try {
            InputStream in = getContentResolver().openInputStream(contentUri);
            if (in != null) {
                BufferedReader r = new BufferedReader(new InputStreamReader(in));
                StringBuilder total = new StringBuilder();
                for (String line; (line = r.readLine()) != null; ) {
                    total.append(line).append('\n');
                }

                return total.toString();
            } else {
                Log.e("TAG", "Input stream is null");
            }
        } catch (Exception e) {
            Log.e("TAG", "Error while reading file by uri", e);
        }
        return "Could not read content!";
    }

    public String getFileName(Uri contentUri) {
        String result = null;
        if (contentUri.getScheme() != null && contentUri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(contentUri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = contentUri.getPath();
            if (result == null) {
                return null;
            }
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
    
    
    void writeFile() {
        String s  = "";
        try {

            File root = new File(Environment.getExternalStorageDirectory(), "Documents");
            if (!root.exists()) {
                root.mkdirs();
            }
            File gpxfile = new File(root, SaveFileName.getText() + ".txt");
            FileWriter writer = new FileWriter(gpxfile);
            if(TO == "UTF16-LE"){
                s = String.valueOf(SaveFileName.getText());
                s = new String(s.getBytes(StandardCharsets.UTF_8),StandardCharsets.UTF_16LE);
                Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
                writer.write(s);
            }
            if(TO == "UTF-8"){
                s = String.valueOf(SaveFileName.getText());
                s = new String(s.getBytes(StandardCharsets.UTF_16LE),StandardCharsets.UTF_8);
                Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
                writer.write(s);
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public static String EncodingIntoUTF16(String oldString){
        String newString="";
        newString = new String(oldString.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_16LE);

        return newString;
    }
    public static String EncodingIntoUTF8(String oldString){
        String newString="";
        newString = new String(oldString.getBytes(StandardCharsets.UTF_16LE), StandardCharsets.UTF_8);

        return newString;
    }

    public void onClick(View view) {

    }
}