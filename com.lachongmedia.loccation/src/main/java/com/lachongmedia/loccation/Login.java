package com.lachongmedia.loccation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by Unoken on 23/12/13.
 */
public class Login extends Activity {
    private Button btnLogin;
    private EditText etId;
    private EditText etPass;
    private CheckBox cbRemember;
    private SharedPreferences loginPreferences;
    private SharedPreferences.Editor editor;
    private Boolean saveLogin;

    private String xml;
    private Document doc;
    private String checkLogin;
    private XMLParser parser;
    private Element e;
    private String URL;

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        btnLogin = (Button) findViewById(R.id.btnLogin);
        etId = (EditText) findViewById(R.id.etId);
        etPass = (EditText) findViewById(R.id.etPass);
        cbRemember = (CheckBox) findViewById(R.id.cbRemember);

        loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        editor = loginPreferences.edit();

        saveLogin = loginPreferences.getBoolean("saveLogin", false);
        if(saveLogin == true){
            etId.setText(loginPreferences.getString("username", ""));
            etPass.setText(loginPreferences.getString("password", ""));
            cbRemember.setChecked(true);
        }

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(cbRemember.isChecked()){
                    editor.putBoolean("saveLogin", true);
                    editor.putString("username", etId.getText().toString());
                    editor.putString("password", etPass.getText().toString());
                    editor.commit();
                }
                else{
                    editor.clear();
                    editor.commit();
                }

                if(!isNetworkAvailable(getBaseContext())){
                    Toast.makeText(getBaseContext(), "Không có kết nối mạng", Toast.LENGTH_SHORT).show();
                } else if(etPass.getText().toString().equals("") || etId.getText().toString().equals("")){
                    Toast.makeText(getBaseContext(), "Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu", Toast.LENGTH_SHORT).show();
                }
                else{
                    URL = "http://118.70.171.240/GPSMobile/Login.aspx?Username=" + etId.getText().toString() + "&Password=" +etPass.getText().toString();
                    new loginTask().execute();
                }
            }
        });
    }

    public class loginTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            try{
                parser = new XMLParser();
                xml = parser.getXmlFromUrl(URL);
                doc = parser.getDomElement(xml);
                e = doc.getDocumentElement();
                checkLogin = parser.getElementValue(e);
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if(checkLogin.equals("False")){
                Toast.makeText(getBaseContext(), "Tên đăng nhập hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show();
            }
            else if(checkLogin.equals("True")){
                Intent i = new Intent(getBaseContext(), MainActivity.class);
                i.putExtra("_id", etId.getText().toString());
                startActivityForResult(i, 10);
                Toast.makeText(getBaseContext(), "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                finish();
            }
        }

    }

    public boolean isNetworkAvailable(Context ctx){
        ConnectivityManager cm = (ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()&& cm.getActiveNetworkInfo().isAvailable()&& cm.getActiveNetworkInfo().isConnected())
        {
            return true;
        }
        else
        {
            return false;
        }
    }

}
