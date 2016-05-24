package acceso_datos;

import android.app.Activity;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

/**
 * Created by search on 17/02/2016.
 */
public class DownloadWiFiAP extends AsyncTask<Void, Void, ArrayList<WiFiAP>> {
    public Activity context;
    public static ArrayList<WiFiAP> list;
    private static DownloadWiFiAP dWap;
    private static String URL="http://datos.gijon.es/doc/ciencia-tecnologia/zona-wifi.json";

    private DownloadWiFiAP(){}

    public static DownloadWiFiAP getInstance(Activity c){
        if (dWap==null)
        {
            dWap=new DownloadWiFiAP();


        }
        dWap.context=c;
        if(list==null){
            try {
                dWap.execute().get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        return dWap;

}



    protected JSONObject openUrl(String urlString) throws IOException {
        java.net.URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Content-Type",
                "application/x-www-form-urlencoded");
        System.out.println("Codigo respuesta:" + conn.getResponseCode());
        InputStream i=conn.getInputStream();

        BufferedReader r = new BufferedReader(new InputStreamReader(i));
        StringBuilder total = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) {
            total.append(line);

        }
        System.out.println(total.length());

        try {
            return new JSONObject(total.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;

    }

    @Override
    protected ArrayList<WiFiAP> doInBackground(Void ...params) {

        ArrayList<WiFiAP> listAP=new ArrayList<WiFiAP>();
        JSONObject jo=null;
        WiFiAP wapelement=null;

        try {
            jo=openUrl(URL);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            jo = jo.getJSONObject("directorios");
            JSONArray jarray = jo.getJSONArray("directorio");
            JSONObject jelement = null;
            System.out.println(jarray.length());
            for (int i = 0; i < jarray.length(); i++) {

                wapelement = new WiFiAP();
                jelement = jarray.getJSONObject(i);
                wapelement.setTipo(jelement.getString("tipo"));
                wapelement.setIdentificador(jelement.getLong("identificador"));
                wapelement.setDireccion(jelement.getJSONArray("direccion").getString(0).toLowerCase());

                int inicio=wapelement.getDireccion().indexOf("- planta");

                if(inicio!=-1){
                    int fin=wapelement.getDireccion().substring(inicio+1).indexOf("-");
                    wapelement.setDireccion(wapelement.getDireccion().substring(0,inicio-1)+wapelement.getDireccion().substring(inicio+fin+1));
                }
                if(!wapelement.getDireccion().contains("gijón"))
                    wapelement.setDireccion(wapelement.getDireccion()+" gijón");
                String loc="";

                try {
                    loc = jelement.getJSONObject("localizacion").getString("content");
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
                if(!loc.isEmpty() && loc!="") {
                    String[] locarray = loc.split(" ");
                    wapelement.setLocalizacion(new LatLng(Double.parseDouble(locarray[0]), Double.parseDouble(locarray[1])));
                    listAP.add(wapelement);
                }
                else{


                    Geocoder g=new Geocoder(context.getApplicationContext(), Locale.getDefault());
                    try {

                        if(!wapelement.getDireccion().isEmpty() && !wapelement.getDireccion().contains("{}") && g.getFromLocationName(wapelement.getDireccion(), 1).size()!=0) {
                            Address ad = g.getFromLocationName(wapelement.getDireccion(), 1).get(0);
                            wapelement.setLocalizacion(new LatLng(ad.getLatitude(), ad.getLongitude()));
                            listAP.add(wapelement);
                        }

                    } catch (IOException e1) {
                        e1.printStackTrace();
                        System.out.println("IO");
                    }
                }
            }



        } catch (JSONException e) {
            e.printStackTrace();
        }

        list=listAP;
        return list;
    }


}