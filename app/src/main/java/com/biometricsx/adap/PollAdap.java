package com.biometricsx.adap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.biometricsx.DashboardCurrentActivity;
import com.biometricsx.DashboardExpiredActivity;
import com.biometricsx.R;
import com.biometricsx.pojo.PollPojo;

import java.util.ArrayList;


public class PollAdap extends BaseAdapter {
    private Context context;
    private ArrayList<PollPojo> pojos;
    private DashboardCurrentActivity dashboardCurrentActivity;
    private DashboardExpiredActivity dashboardExpiredActivity;

    public PollAdap(Context context, ArrayList<PollPojo> userPojoArrayList,
                    DashboardCurrentActivity dashboardCurrentActivity) {
        this.context = context;
        this.pojos = userPojoArrayList;
        this.dashboardCurrentActivity = dashboardCurrentActivity;
    }

    public PollAdap(Context context, ArrayList<PollPojo> userPojoArrayList,
                    DashboardExpiredActivity dashboardExpiredActivity) {
        this.context = context;
        this.pojos = userPojoArrayList;
        this.dashboardExpiredActivity = dashboardExpiredActivity;
    }

    @Override
    public int getCount() {
        return pojos.size();
    }

    @Override
    public Object getItem(int position) {
        return pojos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewH viewH = new ViewH();
        final PollPojo pollPojo;

        if (dashboardCurrentActivity != null) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_poll, parent, false);
                viewH.oIdTV = convertView.findViewById(R.id.tv_oid_transac);
                viewH.dateTimeTV = convertView.findViewById(R.id.tv_datetime);
                viewH.fromtoTV = convertView.findViewById(R.id.tv_fromto);
                viewH.scoreTV = convertView.findViewById(R.id.tv_score);
                viewH.scoreTV.setVisibility(View.VISIBLE);
                convertView.setTag(viewH);
            } else {
                viewH = (ViewH) convertView.getTag();
            }
            if ((pollPojo = pojos.get(position)) != null) {
                //if not an admin then visibility gone for vname
                viewH.oIdTV.setText("#" + pollPojo.getPid());
                viewH.fromtoTV.setText(pollPojo.getPname());
                viewH.scoreTV.setText("Voted: " + pollPojo.getScoredCurrent() + "/" + pollPojo.getTotCurrent());
                viewH.dateTimeTV.setText("closing date: " + pollPojo.getPend());
                viewH.oIdTV.getRootView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dashboardCurrentActivity.onAdapItemClicked(pollPojo);
                    }
                });
            }
        } else if (dashboardExpiredActivity != null) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_poll, parent, false);
                viewH.oIdTV = convertView.findViewById(R.id.tv_oid_transac);
                viewH.dateTimeTV = convertView.findViewById(R.id.tv_datetime);
                viewH.fromtoTV = convertView.findViewById(R.id.tv_fromto);
                viewH.scoreTV = convertView.findViewById(R.id.tv_score);
                viewH.scoreTV.setVisibility(View.GONE);
                convertView.setTag(viewH);
            } else {
                viewH = (ViewH) convertView.getTag();
            }
            if ((pollPojo = pojos.get(position)) != null) {
                //if not an admin then visibility gone for vname
                viewH.oIdTV.setText("#" + pollPojo.getPid());
                viewH.fromtoTV.setText("Poll: " + pollPojo.getPname());
                //  viewH.scoreTV.setText("Score: " + pollPojo.getScoredCurrent() + "/" + pollPojo.getTotCurrent());
                viewH.dateTimeTV.setText(pollPojo.getPstart() + " - " + pollPojo.getPend());
                viewH.oIdTV.getRootView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dashboardExpiredActivity.onAdapItemClicked(pollPojo);
                    }
                });
            }
        }
        return convertView;
    }

    private static class ViewH {
        private TextView oIdTV, dateTimeTV, scoreTV, fromtoTV;
    }
}
