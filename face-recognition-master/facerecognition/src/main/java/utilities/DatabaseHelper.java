package utilities;

/**
 * Created by wido on 4/30/2016.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper  {

    public static final String userId="user_id";
    public static final String userName="user_name";
    public static final String email="email";
    public static final String emailpassword="email_password";


    private static final String Database_name="userDB";
    private static final String Database_table="users";
    private static final int Dtabase_Version=1;

    private static DbHelper helper;
    private static SQLiteDatabase database;
    private static Context context;


    private static class DbHelper extends SQLiteOpenHelper{

        public DbHelper(Context context	) {

            super(context, Database_name, null, Dtabase_Version);


        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL("CREATE TABLE "+ Database_table+ " ("+
                            userId+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
                            userName+" TEXT NOT NULL, "+
                            email+" TEXT NOT NULL, " +
                            emailpassword+ " TEXT NOT NULL);"

            );

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            db.execSQL("DROP TABLE IF EXISTS "+Database_table);
            onCreate(db);
        }
    }



    public DatabaseHelper(Context t){
        context=t;
    }


    public DatabaseHelper open() throws Exception{
        helper=new DbHelper(context);
        database=helper.getWritableDatabase();
        return this;

    }

    public void close(){
        helper.close();
    }


    public long createEntry(String name, String em, String pw) {

        ContentValues cv=new  ContentValues(3);
        cv.put(userName, name);
        cv.put(email,em);
        cv.put(emailpassword,pw);
        return database.insert(Database_table, null, cv);

    }


    /*
    public String getData(){

        String columns[]={userName,email, emailpassword};
        Cursor c=database.query(Database_table, columns, null, null, null, null,null);
        String result="";
        int iUserName=c.getColumnIndex(userName);
        int iEmail=c.getColumnIndex(email);
        int iEmailPassword=c.getColumnIndex(emailpassword);

        for(c.moveToFirst();!c.isAfterLast();c.moveToNext()){

            result=result+c.getString(iUserName)+" "+c.getString(iEmail)+" "+c.getString(iEmailPassword)+"\n";
        }

        return result;
    }
    */


    public String getEmail(String name) {

        String columns[]={email};

        Cursor c;

        try {
         c=database.query(Database_table, columns, userName + "='" + name+"'", null, null, null, null);

            if(c!=null){

                int iEmail=c.getColumnIndex(email);

                c.moveToFirst();
                return c.getString(iEmail);
            }

        }

        catch(Exception e){
            return "not found";

        }

        return "not found";
    }



    public String getPassword(String name) {

        String columns[]={emailpassword};

        Cursor c=database.query(Database_table, columns, userName+"='"+name+"'", null, null, null,null);
        int iPassword=c.getColumnIndex(emailpassword);

        if(c!=null){
            c.moveToFirst();
            return c.getString(iPassword);
        }
        return null;

    }



/*
    public void updateEntry(long lrow, String mName, String mHotness) {

        ContentValues cvUpdate= new ContentValues();
        cvUpdate.put(,);
        cvUpdate.put(, );

        ourDatabase.update(Database_table, cvUpdate, rowId+"="+lrow, null);



    }


    public void deleteEntry(long lrow) {
        ourDatabase.delete(Database_table, rowId+"="+lrow, null);

    }

*/


    public void printAll()
    {

        String columns[]={userName,email, emailpassword};
        Cursor c=database.query(Database_table, columns, null, null, null, null,null);
        String result="";
        int iUserName=c.getColumnIndex(userName);
        int iEmail=c.getColumnIndex(email);
        int iEmailPassword=c.getColumnIndex(emailpassword);

        for(c.moveToFirst();!c.isAfterLast();c.moveToNext()){

            result=result+c.getString(iUserName)+" "+c.getString(iEmail)+" "+c.getString(iEmailPassword)+"\n";

            System.out.println(result);
        }



    }



}
