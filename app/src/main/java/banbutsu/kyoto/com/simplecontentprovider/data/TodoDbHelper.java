package banbutsu.kyoto.com.simplecontentprovider.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Yasuaki on 2017/06/22.
 */

public class TodoDbHelper extends SQLiteOpenHelper {

  private static final String DATABASE_NAME = "todotable.db";
  private static final int DATABASE_VERSION = 1;

  public TodoDbHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  // Method is called during creation of the database
  @Override
  public void onCreate(SQLiteDatabase database) {
    TodoTable.onCreate(database);
  }

  // Method is called during an upgrade of the database,
  // e.g. if you increase the database version
  @Override
  public void onUpgrade(SQLiteDatabase database, int oldVersion,
      int newVersion) {
    TodoTable.onUpgrade(database, oldVersion, newVersion);
  }
}
