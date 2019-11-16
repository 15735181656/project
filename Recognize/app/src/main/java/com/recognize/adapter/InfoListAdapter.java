package com.recognize.adapter;

import android.graphics.Bitmap;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.recognize.entity.Information;
import com.recognize.login.MainActivity;
import com.recognize.recognize.R;
import com.recognize.recognize.UploadImageActivity;
import com.recognize.utils.BitMapUntil;
import com.recognize.utils.UrlJudgeUntil;

import java.io.IOException;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class InfoListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private UploadImageActivity mContext;
    private List<Information> mDataList;
    private LayoutInflater mLayoutInflater;
    private RecyclerView recyclerView;

    public static String focusUrl = "http://182.92.165.130:3000/follow";

    public InfoListAdapter(UploadImageActivity mContext, List<Information> mDataList) {
        this.mContext = mContext;
        this.mDataList = mDataList;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    //Adapter绑定RecyclerView时
    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }
    //Adapter解除绑定RecyclerView时
    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        this.recyclerView = null;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new NormalItemHolder(mLayoutInflater.inflate(R.layout.layout_card_view, viewGroup, false));
    }

    /**
     * 绑定ViewHolder的数据。
     *
     * @param viewHolder
     * @param i          数据源list的下标
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        Information entity = mDataList.get(i);

        if (null == entity)
            return;
        NormalItemHolder holder = (NormalItemHolder) viewHolder;
        bindNormalItem(entity, holder.cardName, holder.cardImage, holder.focus);
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }


    Bitmap img = null;

    void bindNormalItem(final Information entity, final TextView cardName,
                        final CircleImageView cardImage,final TextView focus) {
        if (entity.getUrl() != null && UrlJudgeUntil.isHttpUrl(entity.getUrl())) {
            // 显示图片
            final BitMapUntil bm = new BitMapUntil();
            new Thread() {
                @Override
                public void run() {
                    Log.d("123123", entity.getUrl());
                    img = bm.urlToBitMap(entity.getUrl());
                    mContext.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            cardImage.setImageBitmap(img);
                        }
                    });
                }
            }.start();
        } else {
            //不显示图片
            cardImage.setImageResource(R.drawable.logo);
        }
        if (!entity.getName().isEmpty()) {
            cardName.setText(entity.getName());
            cardName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cardName.setSelected(true);
                    cardName.setMarqueeRepeatLimit(1);
                }
            });
        }
        if (!entity.getIsFocus())
            focus.setText("关注");
        else {
            focus.setText("取消关注");
        }
        //关注
        focus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!entity.getIsFocus()) {
                    focusOrUnfocus(entity, focus, MainActivity.loginUsername, entity.getName(),
                            0, v);
                } else {
                    focusOrUnfocus(entity, focus, MainActivity.loginUsername, entity.getName(),
                            1, v);
                }
            }
        });
    }

    /**
     * 新闻标题
     */
    public class NormalItemHolder extends RecyclerView.ViewHolder {
        TextView focus;
        TextView cardName;
        CircleImageView cardImage;

        public NormalItemHolder(View itemView) {
            super(itemView);
            cardName = itemView.findViewById(R.id.cardName);
            cardImage = itemView.findViewById(R.id.cardImage);
            focus = itemView.findViewById(R.id.focus);
        }
    }

    // 在对应位置增加一个item
    public void addData(int position, String username, String url, boolean isFocus) {
        mDataList.add(position, new Information(username, url, isFocus));
        notifyDataSetChanged();
    }


    // 删除对应item
    public void removeData(int position) {
        mDataList.remove(position);
        notifyItemRemoved(position);
    }

    //关注
    public void focusOrUnfocus(final Information entity, final TextView focus,
                               final String username, final String friend,
                               final int type, final View v) {
        new Thread() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                //整个上传的请求体部分（普通表单+文件上传域）
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("type", "" + type)
                        .addFormDataPart("username", username)
                        .addFormDataPart("friend", friend)
                        .build();
                Request request = new Request.Builder()
                        .url(focusUrl)
                        .post(requestBody)
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    String re = response.body().string();
                    if (type == 0) {
                        if (re.equals("1")) {
                            entity.setIsFocus(true);
                            mContext.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    focus.setText("取消关注");
                                }
                            });
                            Looper.prepare();
                            Toast.makeText(mContext, "关注成功", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        } else {
                            Looper.prepare();
                            Toast.makeText(mContext, "关注失败", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        }
                    } else {
                        if (re.equals("1")) {
                            entity.setIsFocus(false);
                            final int position = mDataList.indexOf(entity);
                            mContext.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    focus.setText("关注");
                                    removeData(position);
                                }
                            });
                            Looper.prepare();
                            Toast.makeText(mContext, "取消成功", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        } else {
                            Looper.prepare();
                            Toast.makeText(mContext, "取消失败", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}