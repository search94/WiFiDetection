package acceso_datos;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;

import wl.uniovi.es.wifilocate.ListFragment;
import wl.uniovi.es.wifilocate.R;

/**
 * Created by uo231722 on 17/02/2016.
 */
//Clase Adaptadora para la lista de puntos de acceso wifi
public class APRecycledAdapter extends RecyclerView.Adapter <APRecycledAdapter.ViewHolder>{

    public interface ItemClickListener {
        void onClick(View view, int position, boolean isLongClick); //Callback
    }
    public ArrayList<WiFiAP> waps; //Lista de puntos de acceso a mostrar
    public ArrayList<WiFiAP> complete; //Lista de puntos de acceso completa
    public boolean GPSON;
    ListFragment lfrag;
    public static AdapterView.OnItemClickListener listener;
    public APRecycledAdapter(ArrayList<WiFiAP> WAPS,boolean gpson, ListFragment frag){
        complete=(ArrayList<WiFiAP>)WAPS.clone();
        waps=WAPS;
        GPSON=gpson;
        lfrag=frag;
    }

    //Método para filtrar los puntos de acceso por su nombre y por su distancia
    public void getFilter(String s,int v) {

        if(s!=""){
            waps.clear();
            for(WiFiAP w:complete){
                if(w.getNombre().contains(s))
                    waps.add(w);
            }
        }
        else waps=(ArrayList<WiFiAP>)complete.clone();
        System.out.println("*************************"+v);
        if(v!=10000){
            for(int i=0;i<waps.size();i++) {
                WiFiAP w = waps.get(i);
                System.out.print(w.getDistKm()+";");
                if (w.getDistKm() != -1) {
                    if (Double.compare(w.getDistKm(),v/1000)>0){
                        waps.remove(i);

                    }

                }
            }
            System.out.println();
        }
        notifyDataSetChanged();
    }


    //Añadir un punto de acceso a la lista
    public void addAP(WiFiAP wap){
        if(wap == null)
            throw new IllegalArgumentException();
        waps.add(wap);
        //notifyDataSetChanged();
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(APRecycledAdapter.ViewHolder holder, int position) {
        WiFiAP wap=waps.get(position);
        holder.name.setText("" + wap.getNombre());
        DecimalFormat df=new DecimalFormat("#.###");
        holder.dist.setText(""+df.format(wap.getDistKm()));

        holder.setClickListener(new ItemClickListener() {
            @Override
            public void onClick(View view, int position, boolean isLongClick) {
                if (isLongClick) {

                    lfrag.callback.onWAPSelected(lfrag.list.get(position));

                } else {
                    lfrag.callback.onWAPSelected(lfrag.list.get(position));
                }
            }
        });

    }

    //Clase Holder para la correcta adaptación de la información al ListView
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener{
        //Se presenta el nombre y la distancia
        public TextView name;
        public TextView dist;
        private ItemClickListener clickListener;

        public ViewHolder(View itemView){
            super(itemView);
            name=(TextView)itemView.findViewById(R.id.TVname);
            dist=(TextView)itemView.findViewById(R.id.TVdistance);

            itemView.setTag(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

        }
        public void setClickListener(ItemClickListener itemClickListener){
            this.clickListener=itemClickListener;
        }

        @Override
        public void onClick(View v) {

            clickListener.onClick(v, getPosition(), false);

        }
        @Override
        public boolean onLongClick(View v){
            clickListener.onClick(v, getPosition(), true);
            return true;
        }
        public void setClickListener(AdapterView.OnItemClickListener list){
            listener=list;
        }
    }
    @Override
    public APRecycledAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater=LayoutInflater.from(parent.getContext());
        View vista=inflater.inflate(R.layout.list_item_ap,parent,false);

        return new ViewHolder(vista);
    }



    @Override
    public int getItemCount() {
        return waps.size();
    }



}
