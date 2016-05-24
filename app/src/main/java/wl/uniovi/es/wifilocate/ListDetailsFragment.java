package wl.uniovi.es.wifilocate;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.text.DecimalFormat;

import acceso_datos.WiFiAP;

/**
 * Created by search on 07/03/2016.
 */
//Fragmento de detalle de un punto de acceso Wifi concreto
public class ListDetailsFragment extends Fragment {

    public WiFiAP WAP;
    TextView tvDist,tvName,tvl2,tvl3;
    //Boton para cambiar los favoritos
    ToggleButton tb;
    //Boton para enviar por correo la información del punto de acceso
    FloatingActionButton fab;
    public ListDetailsFragment(){}

    public static  ListDetailsFragment newInstance(WiFiAP wap) {

        Bundle args = new Bundle();

        ListDetailsFragment fragment = new ListDetailsFragment();
        fragment.setArguments(args);
        fragment.WAP=wap;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstance){

        View rootView;
        rootView=inflater.inflate(R.layout.fragment_details_list,container,false);

        tvDist=(TextView)rootView.findViewById(R.id.textViewDist);
        tvl2=(TextView)rootView.findViewById(R.id.tvLab2);
        tvName=(TextView)rootView.findViewById(R.id.textViewName);
        tvl3=(TextView)rootView.findViewById(R.id.tvLab3);

        //Configuración del botón de favoritos
        tb=(ToggleButton)rootView.findViewById(R.id.imageStar);
        tb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Click");
                if (tb.isChecked()) {
                    ListDetailsActivity.fav.add(WAP);
                } else {
                    ListDetailsActivity.removeFav(WAP);
                }

            }
        });

        //Se comprueba si el punto de acceso está ya en favoritos o no
        if(ListDetailsActivity.searchFav(WAP))tb.setChecked(true);
        else tb.setChecked(false);
        tb.setPressed(true);

        //Se crea el FloatingActionButton del correo
        fab=(FloatingActionButton)rootView.findViewById(R.id.fab);

        //Se configura la visibilidad de los
        if(WAP!=null) {

            tvDist.setVisibility(View.VISIBLE);
            tvl2.setVisibility(View.VISIBLE);
            tvl3.setVisibility(View.VISIBLE);
            System.out.println("**********************DIST: " + WAP.getDistKm());
            DecimalFormat df=new DecimalFormat("#.###");
            tvDist.setText("" + df.format(WAP.getDistKm()));
            tvName.setVisibility(View.VISIBLE);
            fab.setVisibility(View.VISIBLE);
            tvName.setText(WAP.getNombre());
            tb.setVisibility(View.VISIBLE);

        }
        else{
            tvDist.setVisibility(View.INVISIBLE);
            tvl2.setVisibility(View.INVISIBLE);
            tvl3.setVisibility(View.INVISIBLE);
            tvName.setVisibility(View.INVISIBLE);
            fab.setVisibility(View.INVISIBLE);
            tb.setVisibility(View.VISIBLE);

        }

        //Se configura el FloatingActionButton
        final Activity a=this.getActivity();
        final String [] sendto={"informovil2016@gmail.com"};
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (WAP != null)
                    MapsActivity.enviar(a, sendto, "Info WiFiLocate", WAP);
            }
        });
        return rootView;
    }

}
