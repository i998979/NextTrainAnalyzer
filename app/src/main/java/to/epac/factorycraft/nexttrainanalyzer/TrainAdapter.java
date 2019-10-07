package to.epac.factorycraft.nexttrainanalyzer;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import static to.epac.factorycraft.nexttrainanalyzer.MainActivity.line_selected;

public class TrainAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;

    private ArrayList<Train> train_datas;

    public TrainAdapter(Context context, ArrayList<Train> train_datas) {
        this.context = context;
        this.train_datas = train_datas;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.train_cards, parent, false);
        RecyclerView.ViewHolder viewHolder = new ViewHolderContent(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Train train = train_datas.get(position);
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
            int resId = context.getResources().getIdentifier(line_selected.toLowerCase(), "color", context.getPackageName());
            holderContent.tvStation.setBackgroundColor(ContextCompat.getColor(context, resId));
        } else {
            holderContent.tvStation.setVisibility(View.GONE);
            holderContent.tvStation.setText("");
        }

        // Highlight irregular destination
        int arrayId = context.getResources().getIdentifier(line_selected.toLowerCase() + "_stations", "array", context.getPackageName());
        String[] terminus = context.getResources().getStringArray(arrayId);

        holderContent.tvDest.setTextColor(Color.parseColor("#FF0000"));
        for (String station : terminus) {
            if (train.getStation().equals(station))
                holderContent.tvDest.setTextColor(Color.parseColor("#000000"));
        }
        holderContent.tvDest.setText(Utils.getStationName(train.getDest()));

        // Show hh:mm:ss
        String time = train.getTime().split(" ")[1];
        holderContent.tvTime.setText(time);

        // Dispaly ttnt acording to the station and minutes
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

        // Dispaly platform no.
        // Change Platform number according the line selected and the platform
        int resId = context.getResources().getIdentifier(line_selected.toLowerCase() + "_p" + train.getPlat(), "drawable", context.getPackageName());
        holderContent.platImg.setImageResource(resId);
    }
    @Override
    public int getItemCount() {
        return train_datas.size();
    }

    public class ViewHolderContent extends RecyclerView.ViewHolder {
        public TextView tvStation;
        public TextView tvSequence;
        public TextView tvTime;
        public TextView tvDest;
        public ImageView platImg;
        public TextView tvTtnt;

        public LinearLayout TrainDatas;

        public ViewHolderContent(View itemView) {
            super(itemView);
            tvStation = itemView.findViewById(R.id.tvStation);
            tvSequence = itemView.findViewById(R.id.tvSequence);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvDest = itemView.findViewById(R.id.tvDest);
            platImg = itemView.findViewById(R.id.platImg);
            tvTtnt = itemView.findViewById(R.id.tvTtnt);

            TrainDatas = itemView.findViewById(R.id.TrainDatas);
            TrainDatas.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder normalDialog = new AlertDialog.Builder(context);

                    Train train = train_datas.get(getAdapterPosition());
                    normalDialog.setMessage(
                            "方向：" + train.getDir() + "\n" +
                                    "車站：" + Utils.getStationName(train.getStation()) + "\n" +
                                    "班次：" + train.getSeq() + "\n" +
                                    "抵站時間：" + train.getTime() + "\n" +
                                    "目的地：" + Utils.getStationName(train.getDest()) + "\n" +
                                    "月台：" + train.getPlat() + "\n" +
                                    "TTNT：" + train.getTtnt());
                    normalDialog.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    normalDialog.show();
                    return false;
                }
            });
        }
    }
}
