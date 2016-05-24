package wl.uniovi.es.wifilocate;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import acceso_datos.WiFiAP;

/**
 * Created by search on 22/05/2016.
 */
//Clase para mostrar los favoritos señalados hasta el momento
public class Favorites extends AppCompatActivity{

    //Lista de favoritos
    ArrayList<WiFiAP> fav;

    public static final String key_fav="FAVOURITES";
    ListView lvfav;
    ArrayAdapter<String> adapter;

    //Menú a mostrar en favoritos
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.favmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        //Opción para volver a la lista
        if(item.getItemId() == R.id.action_list){
            Intent intent=new Intent(Favorites.this,ListActivity.class);
            intent.putParcelableArrayListExtra(ListActivity.key_fav_intent,fav);
            startActivity(intent);
            return true;
        }
        else{
            //Opción para acceder al menú de configuración
            if(item.getItemId() == R.id.action_settings){
                Intent intent=new Intent(Favorites.this,Preferences.class);
                startActivity(intent);
                return true;
            }
            else{
                //Opción para acceder al mapa
                if(item.getItemId() == R.id.action_map){
                    Intent intent=new Intent(Favorites.this,MapsActivity.class);
                    intent.putParcelableArrayListExtra(ListActivity.key_fav_intent,fav);
                    startActivity(intent);
                    return true;
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favourites);

        lvfav=(ListView)findViewById(R.id.listViewFav);

        Intent intent=getIntent();
        //Se obtiene la lista de favoritos del intent,bundle o se crea una nueva
        if(intent.hasExtra(ListActivity.key_fav_intent))
            fav=intent.getParcelableArrayListExtra(ListActivity.key_fav_intent);
        else {
            if (savedInstanceState != null)
                fav = savedInstanceState.getParcelableArrayList(key_fav);
            else
                fav = new ArrayList<WiFiAP>(100);
        }
        //Se obtienen los nombres de los puntos de acceso de la lista de favoritos
        final ArrayList<String> names=new ArrayList<String>();
        for(int i=0;i<fav.size();i++){
            names.add(fav.get(i).getNombre());
        }
        final Favorites f=this;
        //Se configura el adaptador con los nombres anteriores
        adapter=new ArrayAdapter<String>(f,R.layout.list_item_fav,R.id.TVnameF,names);
        lvfav.setAdapter(adapter);
        /*
        lvfav.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        lvfav.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println("Pulsado");
                names.remove(position);
                adapter=new ArrayAdapter<String>(f,R.layout.list_item_fav,R.id.TVnameF,names);
                adapter.notifyDataSetChanged();
                return true;
            }
        });*/



    }
}
