package tw.berlin.englishspelling;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.util.ArrayList;


public class MainActivity extends Activity {
    final int NO_HINT = 0;
    final int ALL_HINET = 5;
    private ArrayList<String> englist = new ArrayList<String>();
    private ArrayList<String> chilist = new ArrayList<String>();
    private ArrayList<String> errenglist = new ArrayList<String>();
    private ArrayList<String> errchilist = new ArrayList<String>();
    private int iOpt = 0; // 0: in seq, 1: random
    private int iNextIdx = 0;
    private int iStartIdx = 0;
    private int iHintCount = 0; // 1~4,  0: no hint,  5: all hint
    private String sEngAnswer;
    private String sChiQuestion;
    private int iCorrect = 0;
    private int iWrong = 0;
    private int iRemaining = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        selectFiles();
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

        switch (id) {
            case R.id.action_selectfiles:
                selectFiles();
                return true;
            case R.id.action_playmode:
                setPlayMode();
                return true;
            case R.id.action_hint:
                setHint();
                return true;
            case R.id.action_startpoint:
                setPlayIndex();
                return true;
            case R.id.action_retryfailed:
                retryErrList();
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
                //Toast.makeText(getApplicationContext(), "載入" + file + "\n" + Integer.toString(iCnt) + " 個單字", Toast.LENGTH_SHORT).show();
            }
            is.close();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "錯誤： " + e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    public void selectFiles() {
        int es, ee, cs, ce, iPairs;
        char c;
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
                    TextView tv;
                    String s = "";
                    if (i == DialogInterface.BUTTON_POSITIVE) {
                        tv = (TextView) findViewById(R.id.textViewFileName);
                        tv.setText("");
                        for (Integer a : mSelectedItems) {
                            loadFile(files[a]);
                            if (s.length() > 0) {
                                s += "\n";
                            }
                            s += files[a];
                        }
                        tv.setText(s);
                    }

                    // reset start index
                    iStartIdx = 0;

                    vStartTest();
                }
            })
            .setNeutralButton("取消", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });

            builder.show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "錯誤： " + e.toString(), Toast.LENGTH_LONG).show();
        }

    }

    private void vSelectPlayMode(int i) {
        iOpt = i;
    }

    private void vSetPlayMode() {
        if (iOpt == 1) {
            LoadEngData.randomData(englist, chilist);
        }
    }

    public void setPlayMode() {
        String[] playseq = {"按照順序", "隨機亂跳"};
        vSelectPlayMode(0);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("請選擇播放模式:")
        .setSingleChoiceItems(playseq, 0, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                vSelectPlayMode(i);
            }
        })
        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                vSetPlayMode();
                vStartTest();
            }
        });

        builder.show();
    }

    public void vStartTest() {
        TextView tvwrong = (TextView) findViewById(R.id.textViewWrong);
        TextView tvcorrect = (TextView) findViewById(R.id.textViewCorrect);
        TextView tvremaining = (TextView) findViewById(R.id.textViewRemaining);

        iNextIdx = iStartIdx;
        iCorrect = 0;
        iWrong = 0;
        iRemaining = englist.size();
        errchilist.clear();
        errenglist.clear();
        tvwrong.setText("答錯"+String.valueOf(iWrong));
        tvcorrect.setText("答對"+String.valueOf(iCorrect));
        tvremaining.setText("還有"+String.valueOf(iRemaining));
        vPlayNext(null);
    }

    public void setHint() {
        String[] hints = {"沒有提示", "提示 1 字", "提示 2 字", "提示 3 字", "提示 4 字", "全部提示"};
        vSetHintCount(0);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("請選擇提示:")
                .setSingleChoiceItems(hints, 0, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        vSetHintCount(i);
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        vStartTest();
                    }
                });

        builder.show();
    }

    public void setPlayIndex() {
        iStartIdx = 0;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText editText = new EditText(this);
        editText.setInputType(InputType.TYPE_CLASS_PHONE);
        editText.setText("0");
        editText.setSelection(editText.length());
        builder.setTitle("請選擇播放點:")
                .setView(editText)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                       iStartIdx = Integer.valueOf(editText.getText().toString());
                       vStartTest();
                    }
                });

        builder.show();
    }

    /**
     *
     * @param i  1~4,  0: no hint,   5: all hint
     */
    public void vSetHintCount(int i) {
        iHintCount = i;
    }

    public void vPlayNext(View view) {
        int i;
        TextView tvchi = (TextView) findViewById(R.id.textViewChi);
        EditText eteng = (EditText) findViewById(R.id.editTextEng);
        TextView tvwrong = (TextView) findViewById(R.id.textViewWrong);
        TextView tvremaining = (TextView) findViewById(R.id.textViewRemaining);
        TextView tvcorrect = (TextView) findViewById(R.id.textViewCorrect);

        if (view != null) {
            //  check eng answer
            if (eteng.getText().toString().equals(sEngAnswer) == false) {
                final String sErrEng = sEngAnswer;
                final String sErrChi = sChiQuestion;

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("正確答案: " + sErrEng)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                errchilist.add(sErrChi);
                                errenglist.add(sErrEng);
                            }
                        });

                builder.show();

                iWrong ++;
                tvwrong.setText("答錯 " + String.valueOf(iWrong));
            } else {
                iCorrect ++;
                tvcorrect.setText("答對 " + String.valueOf(iCorrect));
            }
        }

        // get next question
        if (iNextIdx > englist.size() - 1) {
            tvchi.setText("");
            eteng.setText("");
            Toast.makeText(getApplicationContext(), "測驗結束, 請重新選擇資料", Toast.LENGTH_LONG).show();
            return;
        }

        sEngAnswer = englist.get(iNextIdx);
        sChiQuestion = chilist.get(iNextIdx);
        iNextIdx++;
        iRemaining --;
        tvremaining.setText("還有 " + String.valueOf(iRemaining));

        tvchi.setText(sChiQuestion);
        if (iHintCount >= ALL_HINET || iHintCount >= sEngAnswer.length()) {
            eteng.setText(sEngAnswer);
        } else if (iHintCount == NO_HINT) {
            eteng.setText("");
        } else {
            eteng.setText(sEngAnswer.substring(0, iHintCount));
        }

        eteng.setSelection(eteng.length());
    }

    public void retryErrList() {
        englist.clear();
        englist.addAll(errenglist);
        chilist.clear();
        chilist.addAll(errchilist);
        vStartTest();
    }
}
