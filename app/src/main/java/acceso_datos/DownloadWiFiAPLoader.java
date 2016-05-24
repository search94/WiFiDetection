package acceso_datos;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

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


/**
 * Created by uo231722 on 09/03/2016.
 */
//Clase Loader para la carga de datos
public class DownloadWiFiAPLoader extends android.support.v4.content.AsyncTaskLoader<ArrayList<WiFiAP>> {




    Activity cont;
    String URL;
    public DownloadWiFiAPLoader(Context context) {
        super(context);
        cont=(Activity)context;

    }

    public DownloadWiFiAPLoader(Context context,String url) {
        super(context);
        cont=(Activity)context;
        URL=url;
    }

    //Método para obtención de los datos en JSON a partir de la URL
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

    //Método de carga que procesará el JSON
    @Override
    public ArrayList<WiFiAP> loadInBackground() {

        ArrayList<WiFiAP> listAP=new ArrayList<WiFiAP>();
        JSONObject jo=null;
        WiFiAP wapelement=null;

        try {
            //Se obtienen los datos en JSON
            jo=openUrl(URL);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            //Se obtiene el array de elmentos JSON
            jo = jo.getJSONObject("directorios");
            JSONArray jarray = jo.getJSONArray("directorio");
            JSONObject jelement = null;
            System.out.println(jarray.length());
            //Se añade un elemento WiFiAP por cada elemento JSON con la información que pueda interesar
            for (int i = 0; i < jarray.length(); i++) {

                wapelement = new WiFiAP();
                jelement = jarray.getJSONObject(i);
                wapelement.setTipo(jelement.getString("tipo"));
                wapelement.setIdentificador(jelement.getLong("identificador"));
                wapelement.setNombre(jelement.getJSONObject("nombre").getString("content"));
                wapelement.setSend(jelement.getString("send"));
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
                } catch (JSONException e1){
                }
                if(!loc.isEmpty() && loc!="") {
                    String[] locarray = loc.split(" ");
                    wapelement.setLocalizacion(new LatLng(Double.parseDouble(locarray[0]), Double.parseDouble(locarray[1])));
                    listAP.add(wapelement);
                }
                else{


                    Geocoder g=new Geocoder(cont.getApplicationContext(), Locale.getDefault());
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

        return listAP;
    }

    }

