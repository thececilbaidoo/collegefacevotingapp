package com.biometricsx.adap;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.biometricsx.PollVoteActivity;
import com.biometricsx.R;
import com.biometricsx.pojo.CandidatePojo;
import com.biometricsx.pojo.CandidatePojoExpired;
import com.biometricsx.utils.LoginSharedPref;
import com.biometricsx.utils.UtilConstants;

import java.util.ArrayList;

import static com.biometricsx.utils.UtilConstants.TAG;

public class CandAssignAdap extends BaseAdapter {
    private View pastView;
    private boolean isExpiredPoll;
    private boolean canVote;
    private Context context;
    private ArrayList<CandidatePojo> userPojoArrayList;
    private ArrayList<CandidatePojoExpired> userPojoArrayListEx;
    private ArrayList<Boolean> listSingleSelection;     //ignore
    private PollVoteActivity activity;
    private String myUID;

    public CandAssignAdap(Context context, ArrayList<CandidatePojo> userPojoArrayList,
                          ArrayList<Boolean> listSingleSelection, boolean canVote,
                          PollVoteActivity activity) {
        this.context = context;
        this.userPojoArrayList = userPojoArrayList;
        this.listSingleSelection = listSingleSelection;
        this.activity = activity;
        this.canVote = canVote;
        myUID = LoginSharedPref.getUIdKey(context);
    }

    //Expired poll
    public CandAssignAdap(Context context, ArrayList<CandidatePojoExpired> userPojoArrayList,
                          ArrayList<Boolean> listSingleSelection,
                          PollVoteActivity activity, boolean isExpiredPoll) {
        this.context = context;
        this.userPojoArrayListEx = userPojoArrayList;
        this.listSingleSelection = listSingleSelection;
        this.activity = activity;
        this.isExpiredPoll = isExpiredPoll;
    }

    @Override
    public int getCount() {
        if (!isExpiredPoll) return userPojoArrayList.size();
        else return userPojoArrayListEx.size();
    }

    @Override
    public Object getItem(int position) {
        if (!isExpiredPoll) return userPojoArrayList.get(position);
        else return  userPojoArrayListEx.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewH viewH = new ViewH();
        if (convertView == null) {
            //inflate
            convertView = LayoutInflater.from(context).inflate(R.layout.item_view_vendors, parent, false);
            viewH.uid = convertView.findViewById(R.id.tv_user_uid);
            viewH.name = convertView.findViewById(R.id.tv_user_name);
            viewH.btnCandProfile = convertView.findViewById(R.id.info_icon);
            viewH.relativeLayout = convertView.findViewById(R.id.rl_background);
            viewH.iVprofile = convertView.findViewById(R.id.iv_cand_profile);
            convertView.setTag(viewH);
        } else {
            viewH = (ViewH) convertView.getTag();
        }
        //populate
        if (!isExpiredPoll) {
            final CandidatePojo candiPojo;
            if ((candiPojo = (CandidatePojo) userPojoArrayList.get(position)) != null) {
                viewH.uid.setText("#" + candiPojo.getCid());
                viewH.name.setText(candiPojo.getCname());
                viewH.iVprofile.setImageBitmap(UtilConstants.decodeBitmap(candiPojo.getCphoto()));
                viewH.btnCandProfile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (activity != null)
                            activity.onPersonSelected(candiPojo);
                        Log.i(TAG, "onClick: [" + position + "]: " + candiPojo);
                    }
                });
                final ViewH finalViewH = viewH;
                if (candiPojo.getHavescored().equalsIgnoreCase(candiPojo.getCid())) {    //api changes made?
                    Log.i(TAG, "onClick: " + candiPojo.getVotedByUid() + myUID);
                    finalViewH.relativeLayout.setBackgroundColor(context.getResources().getColor(R.color.creamy));
                }
                if (canVote)    //in current if not yet voted, canVote- not reliable
                    convertView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (activity != null) {
                                finalViewH.relativeLayout.setBackgroundColor(context.getResources().getColor(R.color.creamy));
                                if (pastView != null) {
                                    //deselect pastview
                                    Log.i(TAG, "onClick: deselect pastview " + pastView);
                                    RelativeLayout layout = (RelativeLayout) pastView;
                                    layout.setBackgroundColor(context.getResources().getColor(R.color.grey_bg));
                                }
                                activity.onCandidateSelected(candiPojo.getCid());
                                pastView = v;
                            }
                        }
                    });
            }
        } else {
            //expired poll
            final CandidatePojoExpired candiPojo;
            if ((candiPojo = (CandidatePojoExpired) userPojoArrayListEx.get(position)) != null) {
                viewH.uid.setText("#" + candiPojo.getCid());
                viewH.name.setText(candiPojo.getCname());
                viewH.iVprofile.setImageBitmap(UtilConstants.decodeBitmap(candiPojo.getCphoto()));
                viewH.btnCandProfile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (activity != null)
                            activity.onPersonSelectedEx(candiPojo);
                        Log.i(TAG, "onClick: [" + position + "]: " + candiPojo);
                    }
                });
            }
        }
        return convertView;
    }

    private static class ViewH {
        private TextView uid, name;
        private ImageView btnCandProfile;
        private RelativeLayout relativeLayout;
        private ImageView iVprofile;
        // private ImageView photo;
    }
}
