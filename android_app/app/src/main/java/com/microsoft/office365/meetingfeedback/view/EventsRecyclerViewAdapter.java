/*
 *  Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license. See full license at the bottom of this file.
 */
package com.microsoft.office365.meetingfeedback.view;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import com.microsoft.office365.meetingfeedback.MeetingDetailActivity;
import com.microsoft.office365.meetingfeedback.R;
import com.microsoft.office365.meetingfeedback.model.DataStore;
import com.microsoft.office365.meetingfeedback.model.meeting.EventDecorator;

import java.util.List;

import de.greenrobot.event.EventBus;

public class EventsRecyclerViewAdapter extends RecyclerView.Adapter<EventsRecyclerViewAdapter.EventsViewHolder> {

    private final List<EventDecorator> mDisplayEvents;
    private DataStore mDataStore;
    private Context mContext;

    public EventsRecyclerViewAdapter(Context context, DataStore dataStore, List<EventDecorator> displayEvents) {
        mDataStore = dataStore;
        mContext = context;
        mDisplayEvents = displayEvents;
    }

    public EventDecorator getItem(int i) {
        return mDisplayEvents.get(i);
    }

    @Override
    public EventsViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.event_view, viewGroup, false);
        return new EventsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(EventsViewHolder eventsViewHolder, final int i) {
        EventDecorator event = getItem(i);
        eventsViewHolder.mItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, MeetingDetailActivity.class);
                intent.putExtra(MeetingDetailActivity.EVENT_ID_EXTRA, getItem(i).mEventId);
                mContext.startActivity(intent);
            }
        });

        eventsViewHolder.mEventName.setText(event.mSubject);
        eventsViewHolder.mEventHost.setText(event.mOrganizerName);
        eventsViewHolder.mEventDate.setText(event.mFormattedDate);
        eventsViewHolder.mEventTime.setText(event.mFormattedTime);
        eventsViewHolder.mEventRatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new ShowRatingDialogEvent(getItem(i).mEventId));
            }
        });
        event.setupEventDisplay(mContext, mDataStore.getUsername(), eventsViewHolder);
    }


    @Override
    public int getItemCount() {
        return mDisplayEvents.size();
    }

    public class EventsViewHolder extends RecyclerView.ViewHolder {

        public final TextView mEventName;
        public final TextView mEventHost;
        public final TextView mEventDate;
        public final TextView mEventTime;
        public final RatingBar mEventRatingBar;
        public final Button mEventRatingButton;
        public final TextView mRatedLabel;
        public final View mItemView;
        public final TextView mRatingsCount;

        public EventsViewHolder(View itemView) {
            super(itemView);
            mItemView = itemView;
            mEventName = (TextView) itemView.findViewById(R.id.event_name);
            mEventHost = (TextView) itemView.findViewById(R.id.event_host);
            mEventDate = (TextView) itemView.findViewById(R.id.event_date);
            mEventTime = (TextView) itemView.findViewById(R.id.event_time);
            mRatedLabel = (TextView) itemView.findViewById(R.id.average_rating_label);
            mEventRatingBar = (RatingBar) itemView.findViewById(R.id.rating_bar);
            mEventRatingButton = (Button) itemView.findViewById(R.id.rate_button);
            mRatingsCount = (TextView) itemView.findViewById(R.id.ratings_count);
        }

    }

}

// *********************************************************
//
// O365-Android-MeetingFeedback, https://github.com/OfficeDev/O365-Android-MeetingFeedback
//
// Copyright (c) Microsoft Corporation
// All rights reserved.
//
// MIT License:
// Permission is hereby granted, free of charge, to any person obtaining
// a copy of this software and associated documentation files (the
// "Software"), to deal in the Software without restriction, including
// without limitation the rights to use, copy, modify, merge, publish,
// distribute, sublicense, and/or sell copies of the Software, and to
// permit persons to whom the Software is furnished to do so, subject to
// the following conditions:
//
// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
// MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
// LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
// OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
// WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
//
// *********************************************************