package banbutsu.kyoto.com.simplecontentprovider.data;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by Yasuaki on 2017/06/22.
 */

public class TodoContentProvider extends ContentProvider {

  // database
  private TodoDbHelper database;

  // used for the UriMacher
  private static final int TODOS = 10;
  private static final int TODO_ID = 20;

  // match with AndroidManifest
  private static final String AUTHORITY =
      "banbutsu.kyoto.com.simplecontentprovider.contentprovider";

  private static final String BASE_PATH = "todos";
  public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
      + "/" + BASE_PATH);

  public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
      + "/todos";
  public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
      + "/todo";




  private static final UriMatcher sURIMatcher = new UriMatcher(
      UriMatcher.NO_MATCH);
  static {
    sURIMatcher.addURI(AUTHORITY, BASE_PATH, TODOS);
    sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", TODO_ID);
  }

  @Override
  public boolean onCreate() {
    database = new TodoDbHelper(getContext());
    return false;
  }

  @Nullable
  @Override
  public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
      @Nullable String[] selectionArgs, @Nullable String sortOrder) {

    // Uisng SQLiteQueryBuilder instead of query() method
    SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

    // check if the caller has requested a column which does not exists
    checkColumns(projection);

    // Set the table
    queryBuilder.setTables(TodoTable.TABLE_TODO);

    int uriType = sURIMatcher.match(uri);
    switch (uriType) {
      case TODOS:
        break;
      case TODO_ID:
        // adding the ID to the original query
        queryBuilder.appendWhere(TodoTable.COLUMN_ID + "="
            + uri.getLastPathSegment());
        break;
      default:
        throw new IllegalArgumentException("Unknown URI: " + uri);
    }

    SQLiteDatabase db = database.getWritableDatabase();
    Cursor cursor = queryBuilder.query(db, projection, selection,
        selectionArgs, null, null, sortOrder);
    // make sure that potential listeners are getting notified
    cursor.setNotificationUri(getContext().getContentResolver(), uri);

    return cursor;
  }


  @Nullable
  @Override
  public String getType(@NonNull Uri uri) {
    return null;
  }

  @Nullable
  @Override
  public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {

    int uriType = sURIMatcher.match(uri);
    SQLiteDatabase sqlDB = database.getWritableDatabase();

    long id = 0;
    switch (uriType) {
      case TODOS:
        id = sqlDB.insert(TodoTable.TABLE_TODO, null, values);
        break;
      default:
        throw new IllegalArgumentException("Unknown URI: " + uri);
    }

    getContext().getContentResolver().notifyChange(uri, null);
    return Uri.parse(BASE_PATH + "/" + id);
  }

  @Override
  public int delete(@NonNull Uri uri, @Nullable String selection,
      @Nullable String[] selectionArgs) {

    int uriType = sURIMatcher.match(uri);
    SQLiteDatabase sqlDB = database.getWritableDatabase();

    int rowsDeleted = 0;
    switch (uriType) {
      case TODOS:
        rowsDeleted = sqlDB.delete(TodoTable.TABLE_TODO, selection,
            selectionArgs);
        break;
      case TODO_ID:
        String id = uri.getLastPathSegment();
        if (TextUtils.isEmpty(selection)) {
          rowsDeleted = sqlDB.delete(
              TodoTable.TABLE_TODO,
              TodoTable.COLUMN_ID + "=" + id,
              null);
        } else {
          rowsDeleted = sqlDB.delete(
              TodoTable.TABLE_TODO,
              TodoTable.COLUMN_ID + "=" + id
                  + " and " + selection,
              selectionArgs);
        }
        break;
      default:
        throw new IllegalArgumentException("Unknown URI: " + uri);
    }

    getContext().getContentResolver().notifyChange(uri, null);
    return rowsDeleted;
  }

  @Override
  public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
      @Nullable String[] selectionArgs) {

    int uriType = sURIMatcher.match(uri);
    SQLiteDatabase sqlDB = database.getWritableDatabase();

    int rowsUpdated = 0;
    switch (uriType) {
      case TODOS:
        rowsUpdated = sqlDB.update(TodoTable.TABLE_TODO,
            values,
            selection,
            selectionArgs);
        break;
      case TODO_ID:
        String id = uri.getLastPathSegment();
        if (TextUtils.isEmpty(selection)) {
          rowsUpdated = sqlDB.update(TodoTable.TABLE_TODO,
              values,
              TodoTable.COLUMN_ID + "=" + id,
              null);
        } else {
          rowsUpdated = sqlDB.update(TodoTable.TABLE_TODO,
              values,
              TodoTable.COLUMN_ID + "=" + id
                  + " and "
                  + selection,
              selectionArgs);
        }
        break;
      default:
        throw new IllegalArgumentException("Unknown URI: " + uri);
    }

    getContext().getContentResolver().notifyChange(uri, null);
    return rowsUpdated;
  }

  private void checkColumns(String[] projection){

    String[] available = { TodoTable.COLUMN_CATEGORY,
        TodoTable.COLUMN_SUMMARY, TodoTable.COLUMN_DESCRIPTION,
        TodoTable.COLUMN_ID };

    if (projection != null) {
      HashSet<String> requestedColumns = new HashSet<String>(
          Arrays.asList(projection));

      HashSet<String> availableColumns = new HashSet<String>(
          Arrays.asList(available));

      // check if all columns which are requested are available
      if (!availableColumns.containsAll(requestedColumns)) {
        throw new IllegalArgumentException(
            "Unknown columns in projection");
      }
    }
  }

}
