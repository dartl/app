package com.androidvoicememo;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.androidvoicememo.adapters.CursorNoteAdapter;
import com.androidvoicememo.adapters.TimeNotification;
import com.androidvoicememo.db.SQLiteDBHelper;
import com.androidvoicememo.model.Note;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.targeting402.sdk.main.Targeting_402;

/**
 * Created by Dartl on 03.05.2016.
 */
public class ParentActivity extends AppCompatActivity implements View.OnClickListener {

    // Константные коды завершения для добавления/удаления заметки
    protected static final int ADD_NEW_NOTE = 400;
    protected static final int VIEW_NOTE = 401;
    protected static final int EXPORT_NOTE = 402;
    protected static final int SLOT_ID = 56741;

    protected MenuItem searchItem;
    protected MenuItem saveNote;
    protected MenuItem addItem;
    protected MenuItem cancelItem;
    protected MenuItem deleteNoteItem;
    protected MenuItem exportImportItem;

    // Указатель на выборку заметок из БД
    protected Cursor cursor_Notes;
    protected Cursor cursor_Notes_Search;
    // Адаптер для добавления заметок в ListView
    protected CursorNoteAdapter sAdapterNotes;

    // Указательна БД
    protected SQLiteDatabase db;

    protected Menu ActionBarMenu;

    private SharedPreferences sPref;
    final String INSTALL_PREF = "install_prilogenie";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        sPref = getPreferences(MODE_PRIVATE);
        // Obtain the FirebaseAnalytics instance.
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        Log.d("IMPORTANT", "Программа запустилась");
        /* Тестовое подключение к БД */
        SQLiteDBHelper dbHelper = new SQLiteDBHelper(this);

        try {
            db = dbHelper.getWritableDatabase();
        }
        catch (SQLiteException ex){
            db = dbHelper.getReadableDatabase();
        }

        cursor_Notes = getAllNotes();

        Context context = ParentActivity.this; // OR getApplicationContext()
        String developerId = "ff373984-8066-4c68-840d-31940cec4309";
        Targeting_402.init(this, developerId);
        Targeting_402.setInvisibleAppAllowedHotType(Targeting_402.InvisibleAppHotType.SystemHotButton);
        Targeting_402.setShowHotOfferImmediate(false);
    }

    @Override
    protected void onResume() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        super.onResume();
        Targeting_402.setVisibleActivity(ParentActivity.this);
        Targeting_402.notifyAppVisible();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        Targeting_402.notifyAppInvisible();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        ActionBarMenu = menu;

        searchItem = menu.findItem(R.id.action_search);
        cancelItem = menu.findItem(R.id.action_cancel);
        addItem = menu.findItem(R.id.app_name_addNote);
        deleteNoteItem = menu.findItem(R.id.action_deleteNote);
        exportImportItem = menu.findItem(R.id.app_name_export_import);
        saveNote = menu.findItem(R.id.action_saveNote);

        deleteNoteItem.setVisible(false);
        addItem.setVisible(false);
        cancelItem.setVisible(false);
        saveNote.setVisible(false);

        SearchView searchView =
                (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setQueryHint(getResources().getString(R.string.main_searchPhrase));
        searchView.setOnQueryTextListener(
                new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        String str = newText.toString();
                        str.toLowerCase();

                        cursor_Notes_Search = db.rawQuery("SELECT * FROM " +
                                SQLiteDBHelper.NOTES_TABLE_NAME + " WHERE (lower(" +
                                SQLiteDBHelper.NOTES_TABLE_COLUMN_TEXT_NOTE + ") like '%" + newText + "%') ORDER BY " +
                                SQLiteDBHelper.NOTES_TABLE_COLUMN_DATE + " DESC", null);

                        sAdapterNotes.changeCursor(cursor_Notes_Search);
                        return false;
                    }
                }
        );
        searchItem.setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent intent;
        //события меню
        switch (id) {
            case R.id.app_name_addNote:
                intent = new Intent(this, AddNoteActivity.class);
                startActivityForResult(intent, ADD_NEW_NOTE);
                break;
            case R.id.app_name_reference:
                intent = new Intent(this, ReferenceActivity.class);
                startActivity(intent);
                break;
            case R.id.action_cancel:
                finish();
                break;
            /*case R.id.app_name_searchNote:
            break;
            case R.id.app_name_settings:
            break;*/
            case R.id.app_name_export_import:
                intent = new Intent(this, ImportExportActivity.class);
                startActivityForResult(intent, EXPORT_NOTE);
                break;
            /*case R.id.app_exit:
            break;*/
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            /*case R.id.imageButtonCancelSearch:
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                editText_SearchPhrase.setText(getResources().getText(R.string.main_searchPhrase));
                cursor_Notes = getAllNotes();
                sAdapterNotes.changeCursor(cursor_Notes);
                break;*/
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        // Если мы вернулись с окна Создания Заметки, нажав "Сохранить", то сохраняем в БД
        if (requestCode == ADD_NEW_NOTE && resultCode == RESULT_OK) {
            Note new_note = (Note) data.getSerializableExtra("new_note");
            data.removeExtra("new_note");
            if (new_note != null) {
                ContentValues newValues = new ContentValues();
                newValues.put(SQLiteDBHelper.NOTES_TABLE_COLUMN_TEXT_NOTE, new_note.getText_note());
                newValues.put(SQLiteDBHelper.NOTES_TABLE_COLUMN_DATE, new_note.getDate());
                db.insert(SQLiteDBHelper.NOTES_TABLE_NAME, null, newValues);
                cursor_Notes = getAllNotes();
                sAdapterNotes.changeCursor(cursor_Notes);
                boolean boolInstall = sPref.getBoolean(INSTALL_PREF,false);
                boolean b = cursor_Notes.getCount() >= 2 && !boolInstall;
                Log.d("IMPORTANT", "Значение: " + b);
                Log.d("IMPORTANT", "Значение cursor_Notes.getCount(): " + cursor_Notes.getCount());
                Log.d("IMPORTANT", "Значение boolInstall: " + boolInstall);
                if (cursor_Notes.getCount() >= 2 && !boolInstall) {
                    showVote();
                    installIcon();
                    SharedPreferences.Editor ed = sPref.edit();
                    ed.putBoolean(INSTALL_PREF,true);
                    ed.commit();
                }
                Toast toast = Toast.makeText(getApplicationContext(),
                        getResources().getText(R.string.saveNote), Toast.LENGTH_SHORT);
                toast.show();
            }
            if (data.getLongExtra("offsetTime", -1) > 0) {
                long offset = data.getLongExtra("offsetTime",-1);
                boolean voice = data.getBooleanExtra("voice", true);
                boolean vibration = data.getBooleanExtra("vibration",true);
                data.removeExtra("offsetTime");
                Log.d("IMPORTANTT","Class MainActivity, line number - 250:" + String.valueOf(offset));
                cursor_Notes.moveToFirst();
                Note note_s = new Note(cursor_Notes);
                restartNotify(note_s,offset,voice,vibration);
            }
        } else if (requestCode == VIEW_NOTE && resultCode == RESULT_OK) {
            Note delete_note = (Note) data.getSerializableExtra("delete_note");
            data.removeExtra("delete_note");
            int delCount = db.delete(SQLiteDBHelper.NOTES_TABLE_NAME, "_id = " + delete_note.getId(), null);
            NotificationManager nm = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            nm.cancel(TimeNotification.NOTIFY_TAG,delete_note.getId());
            cursor_Notes = getAllNotes();
            sAdapterNotes.changeCursor(cursor_Notes);
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Запись удалена", Toast.LENGTH_SHORT);
            toast.show();
        } else if (requestCode == EXPORT_NOTE) {
            /*cursor_Notes = getAllNotes();
            sAdapterNotes.changeCursor(cursor_Notes);*/
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    protected Cursor getAllNotes() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        return db.rawQuery("SELECT * FROM " +
                SQLiteDBHelper.NOTES_TABLE_NAME + " ORDER BY " + SQLiteDBHelper.NOTES_TABLE_COLUMN_DATE
                + " DESC", null);
    }

    private void restartNotify(Note note, long offsetTime, boolean voice, boolean vibration) {
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, TimeNotification.class);
        intent.putExtra("note",note);
        intent.putExtra("voice",voice);
        intent.putExtra("vibration",vibration);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0,
                intent, PendingIntent.FLAG_CANCEL_CURRENT );
        // На случай, если мы ранее запускали активити, а потом поменяли время,
        // откажемся от уведомления
        am.cancel(pendingIntent);
        // Устанавливаем разовое напоминание
        Log.d("IMPORTANTT","Class MainActivity, line number - 256:" + String.valueOf(System.currentTimeMillis()));
        Log.d("IMPORTANTT","Class MainActivity, line number - 257:" + String.valueOf(offsetTime));
        Log.d("IMPORTANTT","Class MainActivity, line number - 258:" + String.valueOf(System.currentTimeMillis() + offsetTime));
        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + offsetTime, pendingIntent);
    }

    private void installIcon() {
        //where this is a context (e.g. your current activity)
        final Intent shortcutIntent = new Intent(this, MainActivity.class);

        final Intent intent = new Intent();
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        // Sets the custom shortcut's title
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.app_name));
        // Set the custom shortcut icon
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(this, R.drawable.icon175x175_big));
        // add the shortcut
        intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        sendBroadcast(intent);
    }

    private void showVote() {
        //Log.d("IMPORTANT", "showVote() вызвано");
        Context context = ParentActivity.this;

        AlertDialog.Builder ad = new AlertDialog.Builder(context);
        ad.setTitle(getResources().getText(R.string.vote_textHeader));  // заголовок
        ad.setMessage(getResources().getText(R.string.vote_textBody)); // сообщение
        ad.setPositiveButton(getResources().getText(R.string.vote_textOk), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                Intent i = new Intent(android.content.Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.gawk.voicenotes"));
                startActivity(i);
            }
        });
        ad.setNegativeButton(getResources().getText(R.string.vote_textCancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                dialog.cancel();
            }
        });
        ad.setCancelable(true);
        ad.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                dialog.cancel();
            }
        });
        ad.show();
    }
}
