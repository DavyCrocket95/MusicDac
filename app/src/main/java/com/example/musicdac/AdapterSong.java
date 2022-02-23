package com.example.musicdac;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

public class AdapterSong extends RecyclerView.Adapter<AdapterSong.myviewHolder> {

    //Variables
    Context context;        //Moment de son utilisation
    ArrayList<ModelSong> songArrayList;

    public OnItemClickListener myItemClickListener;

    //Constructeur
    public AdapterSong(Context context, ArrayList<ModelSong> songArrayList) {
        this.context = context;
        this.songArrayList = songArrayList;
    }

    @NonNull
    @Override
    public myviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {       //Creation du design row
        View v1 = LayoutInflater.from(context).inflate(R.layout.row_song, parent, false);

        return new myviewHolder(v1);
    }

    @Override
    public void onBindViewHolder(@NonNull myviewHolder holder, int position) {          //lien avec les donnees
        holder.titre.setText(songArrayList.get(position).getSongTitle());
        holder.artiste.setText(songArrayList.get(position).getSongArtist());

        Uri coverUri = songArrayList.get(position).getSongCover();

        RequestOptions opts = new RequestOptions()
                .centerCrop()       // Centrage et decoupage de l'image
                .error(R.drawable.ic_music_note_24)        //gestion error du fichier
                .placeholder(R.drawable.ic_music_note_24);

        Context ctx = holder.cover.getContext();

        Glide.with(ctx)
                .load(coverUri)     //Affiche cover suivant uri
                .apply(opts)        //Appliquer les options données ci-dessus
                .fitCenter()        //Resize et center
                .override(150, 150)     //Resize pour que tous les images à la meme taille
                .diskCacheStrategy(DiskCacheStrategy.ALL)       //Gestio du cache
                .into(holder.cover);            //Emplacement où afficher l'image
    }

    @Override
    public int getItemCount() {
        return songArrayList.size();
    }

    public class myviewHolder extends RecyclerView.ViewHolder {

        TextView titre, artiste;
        ImageView cover;

        public myviewHolder(@NonNull View itemView) {
            super(itemView);
            //lien entre le design et le code
            titre = itemView.findViewById(R.id.tvTitre);
            artiste = itemView.findViewById(R.id.tvArtiste);
            cover = itemView.findViewById(R.id.ivCover);

            //Ajouter le listener
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    myItemClickListener.onItemClick(getAdapterPosition(), v);
                }
            });

        }
    }

  public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.myItemClickListener = onItemClickListener;
  }

  public  interface OnItemClickListener {
    void onItemClick(int pos, View v1);
  }

}
