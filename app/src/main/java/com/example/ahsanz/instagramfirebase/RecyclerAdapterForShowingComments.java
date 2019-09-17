package com.example.ahsanz.instagramfirebase;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class RecyclerAdapterForShowingComments extends RecyclerView.Adapter<RecyclerAdapterForShowingComments.CommentHolder> {


    ArrayList<Comment> CommentsList;
    Context context;

    RecyclerAdapterForShowingComments(ArrayList<Comment> cList, Context c){

        CommentsList = cList;
        context = c;
    }


    public static class CommentHolder extends RecyclerView.ViewHolder{

        CircularImageView userDP;
        TextView userName;
        TextView userComment;

        public CommentHolder(View itemView) {
            super(itemView);

            userDP = (CircularImageView) itemView.findViewById(R.id.userDpinComment);
            userName = (TextView) itemView.findViewById(R.id.userNameInComment);
            userComment = (TextView) itemView.findViewById(R.id.userCommentText);
        }
    }

    @Override
    public CommentHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View inflatedView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.show_comment_recycler_row, parent, false);

        return new CommentHolder(inflatedView);
    }

    @Override
    public void onBindViewHolder(CommentHolder holder, int position) {

        Picasso.with(context).load(CommentsList.get(position).getCommenterDPurl()).into(holder.userDP);
        holder.userName.setText(CommentsList.get(position).getCommenterUserName());
        holder.userComment.setText(CommentsList.get(position).getcText());

    }

    @Override
    public int getItemCount() {
        return CommentsList.size();
    }

}
