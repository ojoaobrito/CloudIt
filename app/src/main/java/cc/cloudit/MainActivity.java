package cc.cloudit;

import androidx.core.content.ContextCompat;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class MainActivity extends Activity {

    private ImageButton firebase;
    private ImageButton ibm_cloud;
    private Button upload;
    private Context context = this;
    private LinearLayout files;
    private String[] file_names;
    private int[] file_sizes;
    private TextView initial_text;
    private boolean error = false;
    private String bucket_url;
    private Handler outside;
    private String file_name;
    private int file_size;
    private boolean permission;
    private SharedPreferences pref;
    private String cloud;

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // COMMON CODE
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        outside = new Handler();
        firebase = findViewById(R.id.firebase);
        ibm_cloud = findViewById(R.id.ibm_cloud);

        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // INITIAL SETUP
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // IBM Cloud bucket url
        bucket_url = "https://s3.eu-gb.cloud-object-storage.appdomain.cloud/testbucket-cc2020";

        // define some policies
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // request permissions for accessing storage components
        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE};
        requestPermissions(permissions, 0);

        // change the background color
        getWindow().getDecorView().setBackgroundColor(Color.parseColor("#ffffff"));

        // change the default notification bar color (black) to a nice shade of blue
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(context, R.color.notificationBar));

        files = findViewById(R.id.linear);
        initial_text = findViewById(R.id.initial_text);

        // ----------------------------------------------------------------------------------------
        // retrieve our preferences and activate the preferred protocol
        // ----------------------------------------------------------------------------------------
        pref = getApplicationContext().getSharedPreferences("CloudItPreferences", 0);
        cloud = pref.getString("cloud", "");

        Log.v("CloudIt",cloud);

        // activate the firebase protocol
        if(cloud.equals("") || cloud.equals("firebase")){

            ibm_cloud.setAlpha(0.15f);

            // TODO: ADD FIREBASE SUPPORT
        }

        // activate the ibm cloud protocol
        else{

            firebase.setAlpha(0.15f);

            // display every file stored in the cloud
            ibmCloudDownloadRoutine();
        }

        // ----------------------------------------------------------------------------------------------------
        // check if wifi is activated (just for informative purposes)
        // ----------------------------------------------------------------------------------------------------
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if(mWifi.isConnected())
            Log.v("CloudIt","Wifi is on");
        else
            Log.v("CloudIt","Wifi is off");

        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // SET THE ONCLICK() LISTENERS FOR ALL THE BUTTONS
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        firebase.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                //upload.setClickable(false);

                Thread button_thread = new Thread(){

                    public void run(){

                        Runnable r = new Runnable() {

                            @Override
                            public void run() {

                                // animação do botão de upload
                                firebase.animate().scaleX(0.93f).scaleY(0.93f).setDuration(80).withEndAction(new Runnable() {
                                    @Override
                                    public void run() {
                                        firebase.animate().scaleX(1.0f).scaleY(1.0f).setDuration(80);
                                    }
                                });
                            }
                        };

                        outside.post(r);

                        // delay
                        try {
                            Thread.sleep(100);

                        } catch (Exception e) {

                            Log.e("CloudIt", e.getMessage());
                        }

                        r = new Runnable() {

                            @Override
                            public void run() {

                                firebaseOn(firebase);
                            }
                        };

                        outside.post(r);
                    }
                };

                button_thread.start();
                firebase.setClickable(true);
            }
        });

        ibm_cloud.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                //upload.setClickable(false);

                Thread button_thread = new Thread(){

                    public void run(){

                        Runnable r = new Runnable() {

                            @Override
                            public void run() {

                                // animação do botão de upload
                                ibm_cloud.animate().scaleX(0.93f).scaleY(0.93f).setDuration(80).withEndAction(new Runnable() {
                                    @Override
                                    public void run() {
                                        ibm_cloud.animate().scaleX(1.0f).scaleY(1.0f).setDuration(80);
                                    }
                                });
                            }
                        };

                        outside.post(r);

                        // delay
                        try {
                            Thread.sleep(100);

                        } catch (Exception e) {

                            Log.e("CloudIt", e.getMessage());
                        }

                        r = new Runnable() {

                            @Override
                            public void run() {

                                ibmCloudOn(ibm_cloud);
                            }
                        };

                        outside.post(r);
                    }
                };

                button_thread.start();
                ibm_cloud.setClickable(true);
            }
        });

        upload = findViewById(R.id.upload);

        upload.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                //upload.setClickable(false);

                Thread button_thread = new Thread(){

                    public void run(){

                        Runnable r = new Runnable() {

                            @Override
                            public void run() {

                                // animação do botão de upload
                                upload.animate().scaleX(0.96f).scaleY(0.96f).setDuration(80).withEndAction(new Runnable() {
                                    @Override
                                    public void run() {
                                        upload.animate().scaleX(1.0f).scaleY(1.0f).setDuration(80);
                                    }
                                });
                            }
                        };

                        outside.post(r);

                        // delay
                        try {
                            Thread.sleep(100);

                        } catch (Exception e) {

                            Log.e("CloudIt", e.getMessage());
                        }

                        r = new Runnable() {

                            @Override
                            public void run() {

                                ibmCloudUploadFile(upload);
                            }
                        };

                        outside.post(r);
                    }
                };

                button_thread.start();
                upload.setClickable(true);
            }
        });
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    // method to activate the Firebase protocol
    public void firebaseOn(View v){

        firebase.setAlpha(1f);
        files.removeAllViews();

        // TODO: chamar a rotina que descarrega os ficheiros através do firebase


        SharedPreferences.Editor editor = pref.edit();
        editor.putString("cloud", "firebase");
        editor.apply();

        if(ibm_cloud.getAlpha()==1f){

            ibm_cloud.setAlpha(1f);
            ibm_cloud.animate().alpha(0.15f).setDuration(300);
        }

        cloud = "firebase";
    }

    // method to activate the IBM Cloud protocol
    public void ibmCloudOn(View v){

        ibm_cloud.setAlpha(1f);
        files.removeAllViews();
        ibmCloudDownloadRoutine();

        SharedPreferences.Editor editor = pref.edit();
        editor.putString("cloud", "ibm_cloud");
        editor.apply();

        if(firebase.getAlpha()==1f){

            firebase.setAlpha(1f);
            firebase.animate().alpha(0.15f).setDuration(300);
        }

        cloud = "ibm_cloud";
    }

    protected void onActivityResult(int reqCode, int resCode, Intent intent){

        if(resCode==RESULT_OK){

            switch (reqCode){

                case 0:

                    Uri uri = intent.getData();
                    String ibm_file_path = uri.getPath();

                    Intent service = new Intent(context,UploadHelper.class);

                    // add the file path to this intent
                    service.putExtra("Path", ibm_file_path);

                    startService(service);
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch(requestCode){

            case 0:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.v("CloudIt", "Permission granted!");
                    permission = true;
                }

                else
                    permission = false;

                break;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // FIREBASE CODE
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // TODO: ACRESCENTAR CÓDIGO FIREBASE

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IBM CLOUD CODE
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // (NOT WORKING) method to upload a file to the IBM Cloud Object Storage Bucket
    public void ibmCloudUploadFile(View v){ // method to upload a file to the remote cloud storage

        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.setType("*/*");
        chooseFile = Intent.createChooser(chooseFile, "Choose a file");
        startActivityForResult(chooseFile, 0);
    }

    // method to download a specific file from the IBM Cloud Object Storage Bucket
    public void ibmClouddownloadFile(View v){ // method to download a file from the remote cloud storage

        file_name = file_names[v.getId()];
        file_size = file_sizes[v.getId()];

        if(!permission)
            Toast.makeText(context,"Can't download, reinstall and grant permission!", Toast.LENGTH_LONG);

        else {

            Thread download_thread = new Thread() {

                public void run() {

                    try {

                        URL url = new URL(bucket_url + "/" + file_name);

                        // -----------------------------------------------------------------------------------------------------------------------------------------------------
                        // JPEG images
                        // -----------------------------------------------------------------------------------------------------------------------------------------------------
                        if(file_name.toLowerCase().contains("jpg".toLowerCase()) || file_name.toLowerCase().contains("jpeg".toLowerCase())){

                            Bitmap image = BitmapFactory.decodeStream(url.openConnection().getInputStream());

                            File file = new File(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/"), file_name);

                            if (file.exists()) file.delete();

                            FileOutputStream out = new FileOutputStream(file);
                            image.compress(Bitmap.CompressFormat.JPEG, 100, out);
                            out.flush();
                            out.close();
                        }

                        // -----------------------------------------------------------------------------------------------------------------------------------------------------
                        // PNG images
                        // -----------------------------------------------------------------------------------------------------------------------------------------------------
                        else if(file_name.toLowerCase().contains("png".toLowerCase())){

                            Bitmap image = BitmapFactory.decodeStream(url.openConnection().getInputStream());

                            File file = new File(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/"), file_name);

                            if (file.exists()) file.delete();

                            FileOutputStream out = new FileOutputStream(file);
                            image.compress(Bitmap.CompressFormat.PNG, 100, out);
                            out.flush();
                            out.close();
                        }

                        // -----------------------------------------------------------------------------------------------------------------------------------------------------
                        // other file formats
                        // -----------------------------------------------------------------------------------------------------------------------------------------------------
                        else{

                            File file = new File(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/"), file_name);

                            if (file.exists()) file.delete();

                            URLConnection ucon = url.openConnection();

                            DataInputStream stream = new DataInputStream(url.openStream());

                            byte[] buffer = new byte[file_size];
                            stream.readFully(buffer);
                            stream.close();

                            DataOutputStream fos = new DataOutputStream(new FileOutputStream(file));
                            fos.write(buffer);
                            fos.flush();
                            fos.close();
                        }

                        Runnable r = new Runnable() {
                            @Override
                            public void run() {

                                Toast.makeText(context, "Download completed!", Toast.LENGTH_SHORT).show();
                            }
                        };

                        outside.post(r);

                    } catch (IOException e) {
                        Log.e("CloudIt", e.getMessage());
                    }
                }
            };

            download_thread.start();
        }
    }

    // method to retrieve a XML document with information regarding the files stored (as well as, list every available file)
    public void ibmCloudDownloadRoutine(){

        // URL that points to our IBM Cloud bucket
        String url = "https://s3.eu-gb.cloud-object-storage.appdomain.cloud/testbucket-cc2020";

        Toast.makeText(context,"Working...",Toast.LENGTH_SHORT).show();

        Thread files_thread = new Thread(){

            public void run(){

                try {

                    // ----------------------------------------------------------------------------------------------------------------------------
                    // get the name of every file in the bucket
                    // ----------------------------------------------------------------------------------------------------------------------------
                    // prepare to parse the XML file
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    DocumentBuilder db = dbf.newDocumentBuilder();
                    Document doc = db.parse(new URL(bucket_url).openStream());

                    doc.getDocumentElement().normalize();
                    NodeList keys_vals = doc.getElementsByTagName("Contents");

                    // create an array containing every filename inside the bucket
                    file_names = new String[keys_vals.getLength()];

                    // create an array containing every file size inside the bucket
                    file_sizes = new int[keys_vals.getLength()];

                    for (int i = 0; i < keys_vals.getLength(); i++) {
                        Node nNode = keys_vals.item(i);

                        if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element eElement = (Element) nNode;

                            // save the filenames
                            file_names[i] = (eElement.getElementsByTagName("Key").item(0).getTextContent());

                            // save the file sizes
                            file_sizes[i] = Integer.parseInt(eElement.getElementsByTagName("Size").item(0).getTextContent());
                        }
                    }

                    // ----------------------------------------------------------------------------------------------------------------------------
                    // dynamically add a line for every stored file
                    // ----------------------------------------------------------------------------------------------------------------------------
                    Runnable r = new Runnable() {
                        @Override
                        public void run() {

                            if(file_names.length!=0)
                                initial_text.setText("");

                            for(int i=0; i<file_names.length; i++){

                                LinearLayout secondary = (LinearLayout) getLayoutInflater().inflate(R.layout.file_line,null);

                                ImageView icon = (ImageView) secondary.findViewById(R.id.file_icon);

                                if(file_names[i].contains("pdf"))
                                    icon.setImageDrawable(getResources().getDrawable(R.drawable.pdf));

                                else if(!file_names[i].contains("jpg") && !file_names[i].contains("jpeg") && !file_names[i].contains("png"))
                                    icon.setImageDrawable(getResources().getDrawable(R.drawable.file_icon));

                                else {

                                    try {
                                        // set the file preview
                                        URL url = new URL(bucket_url + "/" + file_names[i]);

                                        Bitmap image = BitmapFactory.decodeStream(url.openConnection().getInputStream());

                                        Drawable drawable = new BitmapDrawable(getResources(), image);

                                        icon.setImageDrawable(drawable);

                                    } catch(MalformedURLException e){ Log.e("CloudIt",e.getMessage()); error = true; }
                                    catch(IOException e){ Log.e("CloudIt",e.getMessage()); error = true; }
                                }

                                TextView file_size = (TextView) secondary.findViewById(R.id.file_size);

                                if(file_sizes[i]<1024)
                                    file_size.setText(file_sizes[i] + " B");

                                else if(file_sizes[i]>1024 && file_sizes[i]<1048576)
                                    file_size.setText(Float.valueOf(String.format(Locale.getDefault(), "%.1f", file_sizes[i]/1024.0).replace(",",".")) + " KB");

                                else if(file_sizes[i]>1048576 && file_sizes[i]<1073741824)
                                    file_size.setText(Float.valueOf(String.format(Locale.getDefault(), "%.1f", file_sizes[i]/1048576.0).replace(",",".")) + " MB");

                                TextView file_text = (TextView) secondary.findViewById(R.id.file_name);
                                file_text.setText(file_names[i]);

                                final ImageButton button = (ImageButton) secondary.findViewById(R.id.download);
                                button.setId(i);

                                button.setOnClickListener(new View.OnClickListener() {

                                    @Override
                                    public void onClick(View view) {

                                        final Handler handler_fora = new Handler();

                                        Thread nova_thread = new Thread(){

                                            public void run(){

                                                Runnable r = new Runnable() {

                                                    @Override
                                                    public void run() {

                                                        // animação do botão de upload
                                                        button.animate().scaleX(0.93f).scaleY(0.93f).setDuration(80).withEndAction(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                button.animate().scaleX(1.0f).scaleY(1.0f).setDuration(80);
                                                            }
                                                        });
                                                    }
                                                };

                                                outside.post(r);

                                                // delay
                                                try {
                                                    Thread.sleep(100);

                                                } catch (Exception e) {

                                                    Log.e("CloudIt", e.getMessage());
                                                }

                                                r = new Runnable() {

                                                    @Override
                                                    public void run() {

                                                        ibmClouddownloadFile(button);
                                                    }
                                                };

                                                outside.post(r);
                                            }
                                        };

                                        nova_thread.start();
                                        button.setClickable(true);
                                    }
                                });

                                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                                if(i==0)
                                    layoutParams.setMargins(45, 20, 45, 0);

                                else if(i==(file_names.length-1))
                                    layoutParams.setMargins(45, 35, 45, 20);

                                else
                                    layoutParams.setMargins(45, 35, 45, 0);

                                files.addView(secondary, layoutParams);
                            }

                        }
                    };

                    outside.post(r);

                }
                catch(MalformedURLException e){ Log.e("CloudIt",e.getMessage()); error = true; }
                catch(ParserConfigurationException e){ Log.e("CloudIt",e.getMessage()); error = true; }
                catch (IOException e){ Log.e("CloudIt",e.getMessage()); error = true; }
                catch(SAXException e){ Log.e("CloudIt",e.getMessage()); error = true; }

                if(error){

                    Runnable r = new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context,"There was an error! Restart the app...",Toast.LENGTH_SHORT).show();
                        }
                    };

                    outside.post(r);
                }
            }
        };

        files_thread.start();
    }
}
