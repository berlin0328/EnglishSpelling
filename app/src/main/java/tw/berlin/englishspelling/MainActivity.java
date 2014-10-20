package tw.berlin.englishspelling;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.InputStream;
import java.util.ArrayList;


public class MainActivity extends Activity {
    private ArrayList<String> englist = new ArrayList<String>();
    private ArrayList<String> chilist = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void loadFile(String file) {
        InputStream is;
        AssetManager am = getAssets();
        int iCnt;
        String path = "EngSpell/" + file;

        try {
            is = am.open(path);
            if ((iCnt = LoadEngData.parseData(is, englist, chilist)) == 0) {
                Toast.makeText(getApplicationContext(), "載入" + file + "資料有誤 !!!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "載入" + file + "\n" + Integer.toString(iCnt) + " 個單字", Toast.LENGTH_SHORT).show();
            }
            is.close();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "錯誤： " + e.toString(), Toast.LENGTH_LONG).show();
        }

    }

    public void selectFiles(View view) {
        int es, ee, cs, ce, iPairs;
        char c;
        String s;
        final ArrayList<Integer> mSelectedItems = new ArrayList<Integer>();
        final InputStream is;
        final AssetManager am = getAssets();
        englist.clear();
        chilist.clear();

        try {
            final String files[] = am.list("EngSpell");
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("請選擇檔案")
            .setMultiChoiceItems(files, null, new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                    if (b == true) {
                        mSelectedItems.add(i);
                    } else if (mSelectedItems.contains(i)) {
                        mSelectedItems.remove(Integer.valueOf(i));
                    }
                }
            })
            .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (i == DialogInterface.BUTTON_POSITIVE) {
                        for (Integer a : mSelectedItems) {
                            loadFile(files[a]);
                        }
                    }
                }
            });

            builder.show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "錯誤： " + e.toString(), Toast.LENGTH_LONG).show();
        }

    }

}
