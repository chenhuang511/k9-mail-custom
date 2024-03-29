package vn.bhxh.bhxhmail.activity;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.PorterDuff.Mode;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.AttrRes;
import android.support.annotation.DrawableRes;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.QuickContactBadge;
import android.widget.TextView;

import java.util.List;

import vn.bhxh.bhxhmail.activity.compose.RecipientAdapter;
import vn.bhxh.bhxhmail.view.RecipientSelectView;
import vn.bhxh.bhxhmail.view.ThemeUtils;


public class AlternateRecipientAdapter extends BaseAdapter {
    private static final int NUMBER_OF_FIXED_LIST_ITEMS = 2;
    private static final int POSITION_HEADER_VIEW = 0;
    private static final int POSITION_CURRENT_ADDRESS = 1;


    private final Context context;
    private final AlternateRecipientListener listener;
    private List<RecipientSelectView.Recipient> recipients;
    private RecipientSelectView.Recipient currentRecipient;


    public AlternateRecipientAdapter(Context context, AlternateRecipientListener listener) {
        super();
        this.context = context;
        this.listener = listener;
    }

    public void setCurrentRecipient(RecipientSelectView.Recipient currentRecipient) {
        this.currentRecipient = currentRecipient;
    }

    public void setAlternateRecipientInfo(List<RecipientSelectView.Recipient> recipients) {
        this.recipients = recipients;
        int indexOfCurrentRecipient = recipients.indexOf(currentRecipient);
        if (indexOfCurrentRecipient >= 0) {
            currentRecipient = recipients.get(indexOfCurrentRecipient);
        }
        recipients.remove(currentRecipient);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (recipients == null) {
            return NUMBER_OF_FIXED_LIST_ITEMS;
        }

        return recipients.size() + NUMBER_OF_FIXED_LIST_ITEMS;
    }

    @Override
    public RecipientSelectView.Recipient getItem(int position) {
        if (position == POSITION_HEADER_VIEW || position == POSITION_CURRENT_ADDRESS) {
            return currentRecipient;
        }

        return recipients == null ? null : getRecipientFromPosition(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private RecipientSelectView.Recipient getRecipientFromPosition(int position) {
        return recipients.get(position - NUMBER_OF_FIXED_LIST_ITEMS);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            view = newView(parent);
        }

        RecipientSelectView.Recipient recipient = getItem(position);

        if (position == POSITION_HEADER_VIEW) {
            bindHeaderView(view, recipient);
        } else {
            bindItemView(view, recipient);
        }

        return view;
    }

    public View newView(ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(vn.bhxh.bhxhmail.R.layout.recipient_alternate_item, parent, false);

        RecipientTokenHolder holder = new RecipientTokenHolder(view);
        view.setTag(holder);

        return view;
    }

    @Override
    public boolean isEnabled(int position) {
        return position != POSITION_HEADER_VIEW;
    }

    public void bindHeaderView(View view, RecipientSelectView.Recipient recipient) {
        RecipientTokenHolder holder = (RecipientTokenHolder) view.getTag();
        holder.setShowAsHeader(true);

        holder.headerName.setText(recipient.getNameOrUnknown(context));
        if (!TextUtils.isEmpty(recipient.addressLabel)) {
            holder.headerAddressLabel.setText(recipient.addressLabel);
            holder.headerAddressLabel.setVisibility(View.VISIBLE);
        } else {
            holder.headerAddressLabel.setVisibility(View.GONE);
        }

        RecipientAdapter.setContactPhotoOrPlaceholder(context, holder.headerPhoto, recipient);
        holder.headerPhoto.assignContactUri(recipient.getContactLookupUri());

        holder.headerRemove.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onRecipientRemove(currentRecipient);
            }
        });
    }

    public void bindItemView(View view, final RecipientSelectView.Recipient recipient) {
        RecipientTokenHolder holder = (RecipientTokenHolder) view.getTag();
        holder.setShowAsHeader(false);

        String address = recipient.address.getAddress();
        holder.itemAddress.setText(address);
        if (!TextUtils.isEmpty(recipient.addressLabel)) {
            holder.itemAddressLabel.setText(recipient.addressLabel);
            holder.itemAddressLabel.setVisibility(View.VISIBLE);
        } else {
            holder.itemAddressLabel.setVisibility(View.GONE);
        }

        boolean isCurrent = currentRecipient == recipient;
        holder.itemAddress.setTypeface(null, isCurrent ? Typeface.BOLD : Typeface.NORMAL);
        holder.itemAddressLabel.setTypeface(null, isCurrent ? Typeface.BOLD : Typeface.NORMAL);

        holder.layoutItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onRecipientChange(currentRecipient, recipient);
            }
        });

        configureCryptoStatusView(holder, recipient);
    }

    private void configureCryptoStatusView(RecipientTokenHolder holder, RecipientSelectView.Recipient recipient) {
        switch (recipient.getCryptoStatus()) {
            case AVAILABLE_TRUSTED: {
                setCryptoStatusView(holder, vn.bhxh.bhxhmail.R.drawable.status_lock_dots_3, vn.bhxh.bhxhmail.R.attr.openpgp_green);
                break;
            }
            case AVAILABLE_UNTRUSTED: {
                setCryptoStatusView(holder, vn.bhxh.bhxhmail.R.drawable.status_lock_dots_2, vn.bhxh.bhxhmail.R.attr.openpgp_orange);
                break;
            }
            case UNAVAILABLE: {
                setCryptoStatusView(holder, vn.bhxh.bhxhmail.R.drawable.status_lock_disabled_dots_1, vn.bhxh.bhxhmail.R.attr.openpgp_red);
                break;
            }
            case UNDEFINED: {
                holder.itemCryptoStatus.setVisibility(View.GONE);
                break;
            }
        }
    }

    private void setCryptoStatusView(RecipientTokenHolder holder, @DrawableRes int cryptoStatusRes,
            @AttrRes int cryptoStatusColorAttr) {
        Resources resources = context.getResources();

        Drawable drawable = resources.getDrawable(cryptoStatusRes);
        // noinspection ConstantConditions, we know the resource exists!
        drawable.mutate();

        int cryptoStatusColor = ThemeUtils.getStyledColor(context, cryptoStatusColorAttr);
        drawable.setColorFilter(cryptoStatusColor, Mode.SRC_ATOP);

        holder.itemCryptoStatus.setImageDrawable(drawable);
        holder.itemCryptoStatus.setVisibility(View.VISIBLE);
    }


    private static class RecipientTokenHolder {
        public final View layoutHeader, layoutItem;
        public final TextView headerName;
        public final TextView headerAddressLabel;
        public final QuickContactBadge headerPhoto;
        public final View headerRemove;
        public final TextView itemAddress;
        public final TextView itemAddressLabel;
        public final ImageView itemCryptoStatus;


        public RecipientTokenHolder(View view) {
            layoutHeader = view.findViewById(vn.bhxh.bhxhmail.R.id.alternate_container_header);
            layoutItem = view.findViewById(vn.bhxh.bhxhmail.R.id.alternate_container_item);

            headerName = (TextView) view.findViewById(vn.bhxh.bhxhmail.R.id.alternate_header_name);
            headerAddressLabel = (TextView) view.findViewById(vn.bhxh.bhxhmail.R.id.alternate_header_label);
            headerPhoto = (QuickContactBadge) view.findViewById(vn.bhxh.bhxhmail.R.id.alternate_contact_photo);
            headerRemove = view.findViewById(vn.bhxh.bhxhmail.R.id.alternate_remove);

            itemAddress = (TextView) view.findViewById(vn.bhxh.bhxhmail.R.id.alternate_address);
            itemAddressLabel = (TextView) view.findViewById(vn.bhxh.bhxhmail.R.id.alternate_address_label);
            itemCryptoStatus = (ImageView) view.findViewById(vn.bhxh.bhxhmail.R.id.alternate_crypto_status);
        }

        public void setShowAsHeader(boolean isHeader) {
            layoutHeader.setVisibility(isHeader ? View.VISIBLE : View.GONE);
            layoutItem.setVisibility(isHeader ? View.GONE : View.VISIBLE);
        }
    }

    public interface AlternateRecipientListener {
        void onRecipientRemove(RecipientSelectView.Recipient currentRecipient);
        void onRecipientChange(RecipientSelectView.Recipient currentRecipient, RecipientSelectView.Recipient alternateRecipient);
    }
}
