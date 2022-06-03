package to.epac.factorycraft.nexttrainanalyzer;

import static to.epac.factorycraft.nexttrainanalyzer.MainActivity.pref;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class TrainAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context context;
    private final ArrayList<Train> trainData;

    public TrainAdapter(Context context, ArrayList<Train> trainData) {
        this.context = context;
        this.trainData = trainData;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.train_cards, parent, false);

        return new ViewHolderContent(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Train train = trainData.get(position);
        ViewHolderContent holderContent = (ViewHolderContent) holder;

        // Change TrainCard background color
        if (Integer.parseInt(train.getSeq()) % 2 == 0)
            holderContent.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.bgrdBlue));
        else
            holderContent.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.bgrdWhite));

        // Set Sequence Text
        holderContent.tvSequence.setText(train.getSeq());

        // Display station only when the first time
        if (train.getSeq().equals("1")) {
            holderContent.tvStation.setVisibility(View.VISIBLE);
            holderContent.tvStation.setText(Utils.getStationName(train.getStation()));

            // Change background color of Station Name according to the line selected
            holderContent.tvStation.setBackgroundColor(ContextCompat.getColor(context,
                    context.getResources().getIdentifier(pref.getString("selected_line", "EAL").toLowerCase(), "color", context.getPackageName())));
        } else {
            holderContent.tvStation.setVisibility(View.GONE);
            holderContent.tvStation.setText("");
        }

        // Highlight irregular destination
        String[] terminus = context.getResources().getStringArray(R.array.terminus);

        holderContent.tvDest.setTextColor(Color.parseColor("#FF0000"));
        for (String station : terminus) {
            if (train.getDest().equals(station))
                holderContent.tvDest.setTextColor(Color.parseColor("#000000"));
        }

        String dest = Utils.getStationName(train.getDest());
        if (train.getRoute().equals("RAC"))
            dest += " 經馬場";
        holderContent.tvDest.setText(dest);
        if (dest.length() > 5)
            holderContent.tvDest.setTextSize(12.0F);

        // Show hh:mm:ss
        String time = train.getTime().split(" ")[1];
        holderContent.tvTime.setText(time);

        // Display ttnt according to the station and minutes
        if (train.getTtnt().equals("1")) {
            holderContent.tvTtnt.setText("即將抵達");
            for (String station : terminus) {
                if (train.getStation().equals(station)) holderContent.tvTtnt.setText("正在離開");
            }
        } else if (train.getTtnt().equals("0")) {
            holderContent.tvTtnt.setText("");
            for (String station : terminus) {
                if (train.getStation().equals(station)) holderContent.tvTtnt.setText("正在離開");
            }
        } else
            holderContent.tvTtnt.setText(train.getTtnt() + " 分鐘");

        // Display platform no.
        // Change Platform number according the line selected and the platform
        int resId = context.getResources().getIdentifier(pref.getString("selected_line", "EAL").toLowerCase() + "_p" + train.getPlat(), "drawable", context.getPackageName());
        holderContent.platImg.setImageResource(resId);
    }

    @Override
    public int getItemCount() {
        return trainData.size();
    }

    public class ViewHolderContent extends RecyclerView.ViewHolder {
        public TextView tvStation;
        public TextView tvSequence;
        public TextView tvTime;
        public TextView tvDest;
        public ImageView platImg;
        public TextView tvTtnt;
        public LinearLayout TrainData;

        public ViewHolderContent(View view) {
            super(view);

            tvStation = view.findViewById(R.id.tvStation);
            tvSequence = view.findViewById(R.id.tvSequence);
            tvTime = view.findViewById(R.id.tvTime);
            tvDest = view.findViewById(R.id.tvDest);
            platImg = view.findViewById(R.id.platImg);
            tvTtnt = view.findViewById(R.id.tvTtnt);
            TrainData = view.findViewById(R.id.TrainData);

            TrainData.setOnLongClickListener(v -> {
                Train train = trainData.get(getBindingAdapterPosition());

                AlertDialog.Builder normalDialog = new AlertDialog.Builder(context);
                normalDialog.setMessage("方向：" + train.getDir() + "\n" +
                                "車站：" + Utils.getStationName(train.getStation()) + "\n" +
                                "班次：" + train.getSeq() + "\n" +
                                "抵站時間：" + train.getTime() + "\n" +
                                "目的地：" + Utils.getStationName(train.getDest()) + "\n" +
                                "月台：" + train.getPlat() + "\n" +
                                "TTNT：" + train.getTtnt() + "\n" +
                                "路綫：" + train.getRoute())
                        .setPositiveButton("確定", (dialog, which) -> {
                        }).show();
                return false;
            });
        }
    }
}
