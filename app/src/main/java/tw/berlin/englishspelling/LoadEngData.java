package tw.berlin.englishspelling;

import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by berlin on 2014/10/14.
 */
public class LoadEngData {
    /**
     *
     * @param is the input stream that would be parsed
     * @param englist english arraylist, would append data to it.
     * @param chilist chinese arraylist, would append data to it.
     * @return number of pair(eng, chi) or 0 if something wrong
     */
    public static int parseData(InputStream is, ArrayList<String> englist, ArrayList<String> chilist) {

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "BIG5"));
            int i, es=0, ee=0, cs=0, ce=0;
            String sEng = "";
            String sChi = "";
            char cTmp;

            while ((i=br.read()) != -1) {
                // catch english
                if (es == 0) {
                    if ((i >= 'a' && i <= 'z') || (i >= 'A' && i <= 'Z')) {
                        sEng += (char)i; // i will be unicode format
                        es = 1;
                    } else {
                        // do nothing
                    }
                } else if (ee == 0) { // es == 1, ee == 0
                    if ((i >= 'a' && i <= 'z') || (i >= 'A' && i <= 'Z') || (i == ' ') || (i == 0x27)) { // 0x27 = '
                        sEng += (char)i;
                    } else {
                        // ok, close the english string
                        // check if englist == chilist count
                        if (englist.size() != chilist.size()) {
                            Log.d("iParseData", "Error: can't add sEng to englist, as englist.size() != chilist.size()");
                            return 0;
                        }

                        englist.add(sEng.trim());
                        //Log.d("Eng", sEng);
                        ee = 1;

                        // check if 'i' is chinese
                        if (i > 255) {
                            sChi += (char)i;
                            cs = 1;
                        }
                    }
                } else if (cs == 0) { // catch chinese
                    cTmp = (char)i;
                    if (i > 255) {
                        sChi += (char)i;
                        cs = 1;
                    } else {
                        // do nothing
                    }
                } else if (ce == 0) { // cs == 1, ce == 0
                    cTmp = (char)i;
                    if (i == ',') {
                        // close the chinese string
                        if (sChi.length() != 0) {
                            chilist.add(sChi.trim());
                            //Log.d("Cht", sChi);
                        } else {
                            Log.d("iParseData", "Error: can't add empty sChi to chilist");
                            return 0;
                        }

                        //ce = 1;
                        // reset var
                        es = ee = cs = ce = 0;
                        sEng = "";
                        sChi = "";
                    } else if (i != 0x0d && i != 0x0a) {
                        sChi += (char)i;
                    }
                }
            } // while

            if (sChi.length() > 0) {
                chilist.add(sChi.trim()); // the last one
            }
        } catch (Exception e) {
            Log.d("parsing data", e.toString());
            return 0;
        }

        int iEng = englist.size();
        int iChi = chilist.size();
        if (iEng != iChi) {
            Log.d("iParseData", "Error: final check, englist.size != chilist.size");
            return 0;
        }

        return englist.size();
    }

    public static void randomData(ArrayList<String> englist, ArrayList<String> chilist) {
        ArrayList<String> randEngList = new ArrayList<String>(englist.size());
        ArrayList<String> randChiList = new ArrayList<String>(chilist.size());
        int i, j;

        Random rand = new Random();

        for (i = englist.size(); i > 0; i--) {
            j = rand.nextInt(i);
            randEngList.add(englist.get(j));
            randChiList.add(chilist.get(j));

            englist.remove(j);
            chilist.remove(j);
        }

        englist.addAll(randEngList);
        chilist.addAll(randChiList);
    }

    public static void getStartLetter(ArrayList<String> englist, ArrayList<String> chilist, ArrayList<Character> startLetter) {
        ArrayList<String> tmpEngList = new ArrayList<String>(englist.size());
        ArrayList<String> tmpChiList = new ArrayList<String>(chilist.size());
        int i;

        for (Character c : startLetter) {
            for (i=0; i < englist.size(); i++) {
                if (englist.get(i).charAt(0) == Character.toLowerCase(c)) {
                    tmpEngList.add(englist.get(i));
                    tmpChiList.add(chilist.get(i));
                }
            }
        }

        englist.clear();
        chilist.clear();
        englist.addAll(tmpEngList);
        chilist.addAll(tmpChiList);
    }
}
