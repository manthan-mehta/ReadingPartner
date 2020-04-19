package com.example.textrecognition;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static android.icu.text.MessagePattern.ArgType.SELECT;

public class DatabaseHelper extends SQLiteOpenHelper {
    private String db_path = null;
    private static String db_name = "Dictionary.db";
    private SQLiteDatabase mydatabase;
    private final Context context;

    public DatabaseHelper(Context context) {
        super(context,db_name,null,1);
        this.context = context;
        this.db_path = "/data/data/" + context.getPackageName() + "/" + "databases/";
    }

    public void createDatabase() throws IOException {
        boolean dbExist = checkDatabase();
        if(!dbExist) {
            this.getReadableDatabase();
            try {
                copyDatabase();
            } catch (IOException e){
                throw new Error("Error Copying Database ...");
            }
        }
    }
    public boolean checkDatabase() {
        SQLiteDatabase checkDB = null;
        try{
            String mypath = db_path + db_name;
            checkDB = SQLiteDatabase.openDatabase(mypath,null,SQLiteDatabase.OPEN_READONLY);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if(checkDB!=null) {
            checkDB.close();
        }

        return checkDB != null ? true : false;
    }
    private void copyDatabase() throws IOException {
        InputStream myInput = context.getAssets().open(db_name);
        String outFileName = db_path + db_name;
        OutputStream myOutput = new FileOutputStream(outFileName);
        byte[] buffer = new byte[1024];
        int length;
        while((length=myInput.read(buffer))>0) {
            myOutput.write(buffer,0,length);
        }
        myOutput.flush();
        myOutput.close();
        myInput.close();
        Log.i("CopyDatabase","Database copied");
    }

    public void openDatabase() throws SQLException {
        String mypath = db_path + db_name;
        mydatabase = SQLiteDatabase.openDatabase(mypath,null,SQLiteDatabase.OPEN_READWRITE);
    }

    @Override
    public synchronized void close() {
        if(mydatabase!=null)
            mydatabase.close();
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            this.getReadableDatabase();
            context.deleteDatabase(db_name);
            copyDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Cursor getMeaning(String word) {
        Cursor cursor = mydatabase.rawQuery("SELECT word,definition FROM entries WHERE word==LOWER('" + word + "')",null);
        return cursor;
    }
}
