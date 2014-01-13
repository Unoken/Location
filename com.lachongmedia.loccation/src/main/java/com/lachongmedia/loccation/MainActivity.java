package com.lachongmedia.loccation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReadWriteLock;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.app.Activity;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.text.format.Time;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    Manager gps;

    TCPClient tcp;
    private TextView txtTime;
    private TextView txtStt;
    private TextView txtKinhdo, txtVido;
    private Button btnStart;
    private Button btnEnd;
    private Button btnExit;
    private Button btnHide;
    private CheckBox cbxDongcua;
    private long startTime = 0l;
    private Timer timer;
    private Handler handler = new Handler();
    private long timeInMillies = 0l;
    private long timeSwap = 0l;
    private long finalTime = 0l;
    String text, time;
    Location location;
    double longitude;
    double latitude;
    double longBD;
    double latBD;
    String timeBD;
    String stt;
    int ret;
    StringBuilder log;
    private String username;

    private Spinner spTinhTP, spQuanHuyen, spPhuongXa, spDuongPho, spSoNha;

    private EditText etDiaChi, etTenCuaHang;
    private String IDGiaoDich;

    private XMLParser xmlParser;
    private String xml;
    private Document doc;

    ArrayList<String> listTinhTP;
    ArrayList<String> listQuanHuyen;
    ArrayList<String> listPhuongXa;
    ArrayList<String> listDuongPho;
    ArrayList<String> listSoNha;

    private HashMap<String, HashMap> hmTinhTP;
    private HashMap<String, HashMap> hmQuanHuyen;
    private HashMap<String, HashMap> hmPhuongXa;
    private HashMap<String, HashMap> hmDuongPho;
    private HashMap<String, String> hmSoNha;

    private ArrayAdapter<String> aaTinhTP;
    private ArrayAdapter<String> aaQuanHuyen;
    private ArrayAdapter<String> aaPhuongXa;
    private ArrayAdapter<String> aaDuongPho;
    private ArrayAdapter<String> aaSoNha;

    String test;

    private String URL_INFO = "";
    static final String KEY_ROOT = "Root";
    static final String KEY_TINHTP = "TinhTP";
    static final String KEY_QUANHUYEN = "QuanHuyen";
    static final String KEY_PHUONGXA = "PhuongXa";
    static final String KEY_DUONGPHO = "DuongPho";
    static final String KEY_SONHA = "SoNha";
    static final String KEY_IDGIAODICH = "IDGiaoDich";
    static final String KEY_TENCUAHANG = "TenCuaHang";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        try{
            Process process = Runtime.getRuntime().exec("logcat -e-");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            log = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null){
                log.append(line);
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }

        Bundle extras = getIntent().getExtras();
        username = extras.getString("_id");
        URL_INFO = "http://118.70.171.240/GPSMobile/GetDiemGiaoDichTheoUser.aspx?Username=" + username;

        new AsynConnect().execute();


        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                TimerMethod();
            }
        }, 0, 10000);

        txtTime = (TextView) findViewById(R.id.tvTime);
        btnStart = (Button) findViewById(R.id.btnStart);
        btnEnd = (Button) findViewById(R.id.btnEnd);
        txtStt = (TextView) findViewById(R.id.tvStt);
        btnExit = (Button) findViewById(R.id.btExit);
        btnHide = (Button) findViewById(R.id.btHide);
        txtKinhdo = (TextView) findViewById(R.id.tvkd);
        txtVido = (TextView) findViewById(R.id.tvvd);

        spTinhTP = (Spinner) findViewById(R.id.spTinh);
        spQuanHuyen = (Spinner) findViewById(R.id.spHuyen);
        spPhuongXa = (Spinner) findViewById(R.id.spXa);
        spDuongPho = (Spinner) findViewById(R.id.spDuong);
        spSoNha = (Spinner) findViewById(R.id.spSonha);
        cbxDongcua = (CheckBox) findViewById(R.id.cbDongCua);

        etDiaChi = (EditText) findViewById(R.id.etDiachi);
        etTenCuaHang = (EditText) findViewById(R.id.etTencuahang);

        new getInfoTask().execute();

        spTinhTP.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(i != 0){
                    String key = adapterView.getItemAtPosition(i).toString();
                    hmQuanHuyen = new HashMap<String, HashMap>();
                    hmQuanHuyen = hmTinhTP.get(key);
                    listQuanHuyen = new ArrayList<String>();
                    listQuanHuyen.add(0, "Chọn Quận/Huyện");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Iterator k = hmQuanHuyen.keySet().iterator();
                            while(k.hasNext()){
                                String tenQuanHuyen = k.next().toString();
                                listQuanHuyen.add(tenQuanHuyen);
                            }
                        }
                    });
                    if(listQuanHuyen.size() <=1 ){
                        Toast.makeText(getBaseContext(), "Không có dữ liệu", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        aaQuanHuyen = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_spinner_item,listQuanHuyen);
                        aaQuanHuyen.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spQuanHuyen.setAdapter(aaQuanHuyen);

                        spQuanHuyen.setClickable(true);
                    }
                }
                spPhuongXa.setClickable(false);
                spPhuongXa.setSelection(0);

                spDuongPho.setClickable(false);
                spDuongPho.setSelection(0);

                spSoNha.setClickable(false);
                spSoNha.setSelection(0);

                etDiaChi.setText("");
                etTenCuaHang.setText("");
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spQuanHuyen.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(i != 0){
                    String key = adapterView.getItemAtPosition(i).toString();
                    hmPhuongXa = new HashMap<String, HashMap>();
                    hmPhuongXa = hmQuanHuyen.get(key);
                    listPhuongXa = new ArrayList<String>();
                    listPhuongXa.add(0, "Chọn Phường/Xã");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Iterator k = hmPhuongXa.keySet().iterator();
                            while(k.hasNext()){
                                String tenPhuongXa = k.next().toString();
                                listPhuongXa.add(tenPhuongXa);
                            }
                        }
                    });
                    if(listPhuongXa.size() <=1 ){
                        Toast.makeText(getBaseContext(), "Không có dữ liệu", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        aaPhuongXa = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_spinner_item,listPhuongXa);
                        aaPhuongXa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spPhuongXa.setAdapter(aaPhuongXa);

                        spPhuongXa.setClickable(true);
                    }
                }
                spDuongPho.setClickable(false);
                spDuongPho.setSelection(0);

                spSoNha.setClickable(false);
                spSoNha.setSelection(0);

                etDiaChi.setText("");
                etTenCuaHang.setText("");
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spPhuongXa.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(i != 0){
                    String key = adapterView.getItemAtPosition(i).toString();
                    hmDuongPho = new HashMap<String, HashMap>();
                    hmDuongPho = hmPhuongXa.get(key);
                    listDuongPho = new ArrayList<String>();
                    listDuongPho.add(0, "Chọn Đường/Phố");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Iterator k = hmDuongPho.keySet().iterator();
                            while(k.hasNext()){
                                String tenDuongPho = k.next().toString();
                                listDuongPho.add(tenDuongPho);
                            }
                        }
                    });
                    if(listDuongPho.size() <=1 ){
                        Toast.makeText(getBaseContext(), "Không có dữ liệu", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        aaDuongPho = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_spinner_item,listDuongPho);
                        aaDuongPho.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spDuongPho.setAdapter(aaDuongPho);

                        spDuongPho.setClickable(true);
                    }
                }
                spSoNha.setClickable(false);
                spSoNha.setSelection(0);

                etDiaChi.setText("");
                etTenCuaHang.setText("");
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spDuongPho.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(i != 0){
                    String key = adapterView.getItemAtPosition(i).toString();
                    hmSoNha = new HashMap<String, String>();
                    hmSoNha = hmDuongPho.get(key);
                    listSoNha = new ArrayList<String>();
                    listSoNha.add(0, "Chọn Số Nhà");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Iterator k = hmSoNha.keySet().iterator();
                            while(k.hasNext()){
                                String tenDuongPho = k.next().toString();
                                listSoNha.add(tenDuongPho);
                            }
                        }
                    });
                    if(listSoNha.size() <=1 ){
                        Toast.makeText(getBaseContext(), "Không có dữ liệu", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        aaSoNha = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_spinner_item,listSoNha);
                        aaSoNha.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spSoNha.setAdapter(aaSoNha);

                        spSoNha.setClickable(true);
                    }
                }
                etDiaChi.setText("");
                etTenCuaHang.setText("");
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spSoNha.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(i != 0){
                    IDGiaoDich = adapterView.getItemAtPosition(i).toString();
                    etDiaChi.setText(spSoNha.getSelectedItem().toString() + "," + spDuongPho.getSelectedItem().toString() + "," + spPhuongXa.getSelectedItem().toString() + "," + spQuanHuyen.getSelectedItem().toString() + "," + spTinhTP.getSelectedItem().toString());
                    etTenCuaHang.setText(hmSoNha.get(IDGiaoDich));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        btnStart.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if(etTenCuaHang.getText().length() == 0){
                    IDGiaoDich = "0";
                }
                btnEnd.setEnabled(true);
                btnStart.setEnabled(false);

                spTinhTP.setClickable(false);
                spQuanHuyen.setClickable(false);
                spPhuongXa.setClickable(false);
                spDuongPho.setClickable(false);
                spSoNha.setClickable(false);

                try{
                    Manager gpsBD = new Manager(MainActivity.this);
                    Location locationBD = gpsBD.getLocation();
                    latBD = locationBD.getLatitude();
                    longBD = locationBD.getLongitude();

                    Time now = new Time(Time.getCurrentTimezone());
                    now.setToNow();

                    timeBD = getTimeNow();

                    startTime = SystemClock.uptimeMillis();
                    txtTime.setText("00:00:00");
                    handler.postDelayed(updateTimer, 0);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        });



        btnEnd.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                btnEnd.setEnabled(false);
                btnStart.setEnabled(true);

                spTinhTP.setClickable(true);
                spQuanHuyen.setClickable(true);
                spPhuongXa.setClickable(true);
                spDuongPho.setClickable(true);
                spSoNha.setClickable(true);

                Manager gpsX = new Manager(MainActivity.this);
                Location locationX = gpsX.getLocation();
                double latX = locationX.getLatitude();
                double longX = locationX.getLongitude();

                String iddivice = gps.getiddivice().toString();
                String sttDongcua;
                if(cbxDongcua.isChecked()){
                    sttDongcua = "1";
                } else{
                    sttDongcua = "0";
                }

                time = getTimeNow();

                String strEnd = "#LHC|" + iddivice + "|" + timeBD +"|" + longBD +"|" + latBD + "|" +time+ "|" + longX + "|" + latX + "|" + IDGiaoDich + "|" + username + "|" + sttDongcua + "|$\r\n";
                tcp.sendMessage(strEnd);
                System.out.println(strEnd);
                timeSwap = 0;
                handler.removeCallbacks(updateTimer);
            }
        });

        btnExit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                /*try{
                    File logFile = new File(Environment.getExternalStorageDirectory(), "logLocation" + getTimeNow() + ".txt");
                    logFile.createNewFile();

                    FileOutputStream outputStream = new FileOutputStream(logFile);
                    OutputStreamWriter streamWriter = new OutputStreamWriter(outputStream);
                    streamWriter.append(log);
                    streamWriter.close();
                }
                catch (IOException e){
                    Toast.makeText(getBaseContext(), "Không thể tạo file log", Toast.LENGTH_SHORT).show();
                }*/
                timer.cancel();
                timer.purge();
                finish();
                System.exit(0);
            }
        });

        btnHide.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                moveTaskToBack(true);
            }
        });

    }

    public void onDestroy(){
        super.onDestroy();
        try{
            File logFile = new File(Environment.getExternalStorageDirectory(), "logLocation" + getTimeNow() + ".txt");
            logFile.createNewFile();

            FileOutputStream outputStream = new FileOutputStream(logFile);
            OutputStreamWriter streamWriter = new OutputStreamWriter(outputStream);
            streamWriter.append(log);
            streamWriter.close();
        }
        catch (IOException e){
            Toast.makeText(getBaseContext(), "Không thể tạo file log", Toast.LENGTH_SHORT).show();
        }
    }

    private void TimerMethod(){
        runOnUiThread(Timer_tick);
    }

    private Runnable Timer_tick = new Runnable() {
        @Override
        public void run() {
            new AsynLocation().execute();
        }
    };

    private Runnable updateTimer = new Runnable() {

        @Override
        public void run() {
            timeInMillies = SystemClock.uptimeMillis() - startTime;
            finalTime = timeSwap + timeInMillies;

            int seconds = (int) (finalTime/1000);
            int minutes = seconds/60;
            seconds = seconds%60;
            int miliseconds = (int) (finalTime%1000);
            txtTime.setText(""+minutes+":"+String.format("%02d", seconds)+":"+String.format("%03d", miliseconds));
            handler.postDelayed(this, 0);
        }
    };

    public class AsynLocation extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                gps = new Manager(MainActivity.this);
                if (gps.canGetLocation()) {
                    location = gps.getLocation();
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    String iddivice = gps.getiddivice().toString();

                    time = getTimeNow();

                    text = "#LHDK|" + iddivice + "|" + longitude + "|" + latitude + "|" + time + "|" + username + "|$\r\n";
                    System.out.println("Location" + text);
                    ret = tcp.sendMessage(text);
                }
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (ret < 0) {
                // Doi status
                stt = "Disconnect";
                System.out.println(stt);
                txtStt.setTextColor(Color.RED);
                txtStt.setText(stt);
                txtKinhdo.setText(String.valueOf(longitude));
                txtVido.setText(String.valueOf(latitude));
                // Reconnect
                new AsynConnect().execute();
            } else {
                stt = "Connected";
                System.out.println(stt);
                txtStt.setTextColor(Color.GREEN);
                txtStt.setText(stt);
                txtKinhdo.setText(String.valueOf(longitude));
                txtVido.setText(String.valueOf(latitude));
            }
        }
    }

    public class AsynConnect extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                tcp = new TCPClient();
                tcp.run();
                xmlParser = new XMLParser();
                xml = xmlParser.getXmlFromUrl(URL_INFO);
                doc = xmlParser.getDomElement(xml);
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
            return null;
        }
    }

    public void onBackPressed(){
        moveTaskToBack(true);
    }

    public String getTimeNow(){
        Time now = new Time(Time.getCurrentTimezone());
        now.setToNow();
        int Y, M, D, h, m, s;
        String YY, MM, DD, hh, mm, ss;
        YY = Integer.toString(now.year);
        M = now.month+1;
        if (M < 10){
            MM = "0" + Integer.toString(M);
        } else MM = Integer.toString(M);
        D = now.monthDay;
        if (D < 10){
            DD = "0" + Integer.toString(D);
        } else DD = Integer.toString(D);
        String shortdate = YY + "-" + MM + "-" + DD;

        h  = now.hour;
        if (h < 10){
            hh = "0" + Integer.toString(h);
        } else hh = Integer.toString(h);
        m = now.minute;
        if (m < 10){
            mm = "0" + Integer.toString(m);
        } else mm = Integer.toString(m);
        s = now.second;
        if (s < 10){
            ss = "0" + Integer.toString(s);
        } else ss = Integer.toString(s);
        String shorttime = hh + "-" + mm + "-" + ss;

        return shortdate + "-" + shorttime;
    }

    public class getInfoTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            try{
                hmTinhTP = new HashMap<String, HashMap>();
                NodeList nlTinhTP = doc.getElementsByTagName(KEY_TINHTP);
                for(int i = 0; i < nlTinhTP.getLength(); i++){
                    Element eTinhTP = (Element) nlTinhTP.item(i);
                    String tenTinhTP = eTinhTP.getAttribute("id").toString();
                    HashMap<String, HashMap> hmQuanHuyen = new HashMap<String, HashMap>();
                    NodeList nlQuanHuyen = ((Element) nlTinhTP.item(i)).getElementsByTagName(KEY_QUANHUYEN);
                    for(int j = 0; j < nlQuanHuyen.getLength(); j++){
                        Element eQuanHuyen = (Element) nlQuanHuyen.item(j);
                        String tenQuanHuyen = eQuanHuyen.getAttribute("id").toString();
                        HashMap<String, HashMap> hmPhuongXa = new HashMap<String, HashMap>();
                        NodeList nlPhuongXa = ((Element) nlQuanHuyen.item(j)).getElementsByTagName(KEY_PHUONGXA);
                        for(int k = 0; k < nlPhuongXa.getLength(); k++){
                            Element ePhuongXa = (Element) nlPhuongXa.item(k);
                            String tenPhuongXa = ePhuongXa.getAttribute("id").toString();
                            HashMap<String, HashMap> hmDuongPho = new HashMap<String, HashMap>();
                            NodeList nlDuongPho = ((Element) nlPhuongXa.item(k)).getElementsByTagName(KEY_DUONGPHO);
                            for(int h = 0; h < nlDuongPho.getLength(); h++){
                                Element eDuongPho = (Element) nlDuongPho.item(h);
                                String tenDuongPho = eDuongPho.getAttribute("id").toString();
                                HashMap<String, String> hmSoNha = new HashMap<String, String>();
                                NodeList nlSoNha = ((Element) nlDuongPho.item(h)).getElementsByTagName(KEY_SONHA);
                                for(int g = 0; g < nlSoNha.getLength(); g++){
                                    Element eSoNha = (Element) nlSoNha.item(g);
                                    NodeList nIDGiaoDich = ((Element) nlSoNha.item(g)).getElementsByTagName(KEY_IDGIAODICH);
                                    NodeList nTenCuaHang = ((Element) nlSoNha.item(g)).getElementsByTagName(KEY_TENCUAHANG);
                                    Element eIDGiaoDich = (Element) nIDGiaoDich.item(0);
                                    Element eTenCuaHang = (Element) nTenCuaHang.item(0);
                                    String IDGiaoDich = xmlParser.getElementValue(eIDGiaoDich);
                                    String TenCuaHang = xmlParser.getElementValue(eTenCuaHang);
                                    hmSoNha.put(IDGiaoDich, TenCuaHang);
                                }
                                hmDuongPho.put(tenDuongPho, hmSoNha);
                            }
                            hmPhuongXa.put(tenPhuongXa, hmDuongPho);
                        }
                        hmQuanHuyen.put(tenQuanHuyen, hmPhuongXa);
                    }
                    hmTinhTP.put(tenTinhTP, hmQuanHuyen);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            listTinhTP = new ArrayList<String>();
            listTinhTP.add(0, "Chọn Tỉnh/Thành Phố");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Iterator l = hmTinhTP.keySet().iterator();
                    while(l.hasNext()){
                        String tenTinhTP = l.next().toString();
                        listTinhTP.add(tenTinhTP);
                    }
                }
            });
            if(listTinhTP.size() <= 1){
                Toast.makeText(getBaseContext(), "Không lấy được danh sách tỉnh/thành phố", Toast.LENGTH_SHORT).show();
                spTinhTP.setClickable(false);
            }
            else
            {
                aaTinhTP = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_spinner_item,listTinhTP);
                aaTinhTP.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spTinhTP.setAdapter(aaTinhTP);
            }
        }

    }

}