/*
 * Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license.
 * See LICENSE in the project root for license information.
 */
package com.microsoft.office365.meetingfeedback.model.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.microsoft.office365.meetingfeedback.MeetingDetailActivity;
import com.microsoft.office365.meetingfeedback.MeetingFeedbackApplication;
import com.microsoft.office365.meetingfeedback.model.DataStore;
import com.microsoft.office365.meetingfeedback.model.webservice.RatingServiceManager;
import com.microsoft.office365.meetingfeedback.util.SharedPrefsUtil;

import java.util.Map;

import javax.inject.Inject;

import dagger.ObjectGraph;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MyMeetingsService extends IntentService {

    public static final String MY_MEETINGS_SERVICE = "MyMeetingsService";
    private static final String TAG = "MyMeetingsService";
    public static final String EVENT_ID = "EVENT_ID";
    private ObjectGraph applicationGraph;
    public static final int MEETING_REQUEST_CODE = 1;

    @Inject
    DataStore mDataStore;
    @Inject
    RatingServiceManager mRatingServiceManager;
    @Inject
    SharedPrefsUtil mSharedPrefsUtil;

    private Map<String, Double> mSavedMeetingResults;
    private NotificationManager mNotificationManager;

    public MyMeetingsService() {
        super(MY_MEETINGS_SERVICE);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        MeetingFeedbackApplication application = (MeetingFeedbackApplication) getApplication();
        applicationGraph = application.getApplicationGraph();
        applicationGraph.inject(this);
        mSavedMeetingResults = mSharedPrefsUtil.getSavedMeetingResults();
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "Polling for new Meeting Ratings...");
        mRatingServiceManager.loadMyMeetings(mDataStore.getUsername(), new Callback<MyMeetingsResponse>() {
            @Override
            public void success(MyMeetingsResponse meetingResponse, Response response) {
                Log.d(TAG, "success!");
                //todo: compare the shared preferences version with the new version
                Map<String, Double> newMeetingResponse = meetingResponse.toMap();
                for (final String id : newMeetingResponse.keySet()) {
                    Double savedCountForMeeting = mSavedMeetingResults.get(id);
                    Double newCountForMeeting = newMeetingResponse.get(id);
                    //if old meeting response didn't have the key
                    Callback<Void> loadRatingsCallback = new Callback<Void>() {
                        @Override
                        public void success(Void aVoid, Response response) {
                            sendNotificationForEvent(id);
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            Log.e(TAG, error.getMessage());
                        }
                    };
                    if (!mSavedMeetingResults.containsKey(id) && newCountForMeeting > 0) {
                        Log.d(TAG, "RATING COUNT CHANGED! Send a notification for " + id + "!");
                        mRatingServiceManager.loadRatingFromWebservice(id, "", loadRatingsCallback);
                    }
                    if (savedCountForMeeting != null && newCountForMeeting != null
                            && !savedCountForMeeting.equals(newCountForMeeting)) {
                        Log.d(TAG, "RATING COUNT CHANGED! Send a notification for " + id + " !");
                        mRatingServiceManager.loadRatingFromWebservice(id, "", loadRatingsCallback);
                    }
                }
                mDataStore.setMyMeetings(newMeetingResponse);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(TAG, "An error occurred", error);
            }
        });
    }

    private void sendNotificationForEvent(String id) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(android.R.drawable.ic_menu_my_calendar)
                .setTicker("New Rating Received!")
                .setAutoCancel(true)
                .setContentTitle("New Rating Received!")
                .setContentText("Your meeting has received a new rating. Click to view");
        Intent intent = new Intent(this, MeetingDetailActivity.class);
        intent.putExtra(MeetingDetailActivity.EVENT_ID_EXTRA, id);

        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(pIntent);
        mNotificationManager.notify(0, builder.build());
    }

}
