package de.ub0r.android.choosebrowser;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ChooserAdapter extends RecyclerView.Adapter<ChooserAdapter.ViewHolder> {

    interface OnItemClickListener {
        void onItemClick(final ComponentName component);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        final View containerView;
        final TextView activityNameView;
        final ImageView iconView;

        ViewHolder(final View itemView) {
            super(itemView);
            activityNameView = itemView.findViewById(R.id.activity_name);
            iconView = itemView.findViewById(R.id.activity_icon);
            containerView = itemView;
        }

    }

    static class ContentHolder {
        private final Context mContext;
        private final ResolveInfo mBrowser;
        private CharSequence mLabel;
        private Drawable mIcon;

        ContentHolder(final Context context, final ResolveInfo browser) {
            mContext = context;
            mBrowser = browser;
        }

        CharSequence getLabel(final PackageManager pm) {
            if (mLabel == null && mBrowser != null) {
                mLabel = mBrowser.loadLabel(pm);
            }
            return mLabel;
        }

        Drawable getIcon(final PackageManager pm) {
            if (mIcon == null && mBrowser != null) {
                mIcon = mBrowser.loadIcon(pm);
            }
            return mIcon;
        }

        ComponentName getComponent() {
            if (mBrowser == null) {
                // Special case for add "Copy Link" button in browser list.
                String copyLink = mContext.getString(R.string.copy_link);
                return new ComponentName(copyLink, copyLink);
            } else{
                return new ComponentName(mBrowser.activityInfo.packageName, mBrowser.activityInfo.name);
            }
        }
    }

    private final LayoutInflater mInflater;
    private final PackageManager mPackageManager;
    private final OnItemClickListener mListener;
    private final List<ContentHolder> mItems;

    @SuppressLint("UseCompatLoadingForDrawables")
    ChooserAdapter(final Context context, final OnItemClickListener listener, final List<ResolveInfo> browsers) {
        mInflater = LayoutInflater.from(context);
        mPackageManager = context.getPackageManager();
        mListener = listener;
        mItems = new ArrayList<>();

        // Special case for add "Copy Link" button in browser list.
        ContentHolder ch = new ContentHolder(context, null);
        ch.mIcon = context.getDrawable(android.R.drawable.ic_menu_edit);
        ch.mLabel = context.getString(R.string.copy_link);
        mItems.add(ch);

        final String myPackageName = context.getPackageName();
        for (ResolveInfo browser : browsers) {
            if (!myPackageName.equals(browser.activityInfo.packageName)) {
                mItems.add(new ContentHolder(context, browser));
            }
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        View itemView = mInflater.inflate(R.layout.item_chooser,
                parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final ContentHolder content = mItems.get(position);
        holder.activityNameView.setText(content.getLabel(mPackageManager));
        holder.iconView.setImageDrawable(content.getIcon(mPackageManager));
        if (mListener != null) {
            holder.containerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    mListener.onItemClick(content.getComponent());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }
}
