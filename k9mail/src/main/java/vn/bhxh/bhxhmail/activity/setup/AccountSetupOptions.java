
package vn.bhxh.bhxhmail.activity.setup;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;

import vn.bhxh.bhxhmail.Account;
import vn.bhxh.bhxhmail.K9;
import vn.bhxh.bhxhmail.Preferences;
import vn.bhxh.bhxhmail.activity.Accounts;
import vn.bhxh.bhxhmail.activity.K9Activity;
import vn.bhxh.bhxhmail.mail.Store;

public class AccountSetupOptions extends K9Activity implements OnClickListener {
    private static final String EXTRA_ACCOUNT = "account";

    private static final String EXTRA_MAKE_DEFAULT = "makeDefault";

    private Spinner mCheckFrequencyView;

    private Spinner mDisplayCountView;


    private CheckBox mNotifyView;
    private CheckBox mNotifySyncView;
    private CheckBox mPushEnable;

    private Account mAccount;

    public static void actionOptions(Context context, Account account, boolean makeDefault) {
        Intent i = new Intent(context, AccountSetupOptions.class);
        i.putExtra(EXTRA_ACCOUNT, account.getUuid());
        i.putExtra(EXTRA_MAKE_DEFAULT, makeDefault);
        context.startActivity(i);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(vn.bhxh.bhxhmail.R.layout.account_setup_options);

        mCheckFrequencyView = (Spinner)findViewById(vn.bhxh.bhxhmail.R.id.account_check_frequency);
        mDisplayCountView = (Spinner)findViewById(vn.bhxh.bhxhmail.R.id.account_display_count);
        mNotifyView = (CheckBox)findViewById(vn.bhxh.bhxhmail.R.id.account_notify);
        mNotifySyncView = (CheckBox)findViewById(vn.bhxh.bhxhmail.R.id.account_notify_sync);
        mPushEnable = (CheckBox)findViewById(vn.bhxh.bhxhmail.R.id.account_enable_push);

        findViewById(vn.bhxh.bhxhmail.R.id.next).setOnClickListener(this);

        SpinnerOption checkFrequencies[] = {
            new SpinnerOption(-1,
            getString(vn.bhxh.bhxhmail.R.string.account_setup_options_mail_check_frequency_never)),
            new SpinnerOption(1,
            getString(vn.bhxh.bhxhmail.R.string.account_setup_options_mail_check_frequency_1min)),
            new SpinnerOption(5,
            getString(vn.bhxh.bhxhmail.R.string.account_setup_options_mail_check_frequency_5min)),
            new SpinnerOption(10,
            getString(vn.bhxh.bhxhmail.R.string.account_setup_options_mail_check_frequency_10min)),
            new SpinnerOption(15,
            getString(vn.bhxh.bhxhmail.R.string.account_setup_options_mail_check_frequency_15min)),
            new SpinnerOption(30,
            getString(vn.bhxh.bhxhmail.R.string.account_setup_options_mail_check_frequency_30min)),
            new SpinnerOption(60,
            getString(vn.bhxh.bhxhmail.R.string.account_setup_options_mail_check_frequency_1hour)),
            new SpinnerOption(120,
            getString(vn.bhxh.bhxhmail.R.string.account_setup_options_mail_check_frequency_2hour)),
            new SpinnerOption(180,
            getString(vn.bhxh.bhxhmail.R.string.account_setup_options_mail_check_frequency_3hour)),
            new SpinnerOption(360,
            getString(vn.bhxh.bhxhmail.R.string.account_setup_options_mail_check_frequency_6hour)),
            new SpinnerOption(720,
            getString(vn.bhxh.bhxhmail.R.string.account_setup_options_mail_check_frequency_12hour)),
            new SpinnerOption(1440,
            getString(vn.bhxh.bhxhmail.R.string.account_setup_options_mail_check_frequency_24hour)),

        };

        ArrayAdapter<SpinnerOption> checkFrequenciesAdapter = new ArrayAdapter<SpinnerOption>(this,
                android.R.layout.simple_spinner_item, checkFrequencies);
        checkFrequenciesAdapter
        .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCheckFrequencyView.setAdapter(checkFrequenciesAdapter);

        SpinnerOption displayCounts[] = {
            new SpinnerOption(10, getString(vn.bhxh.bhxhmail.R.string.account_setup_options_mail_display_count_10)),
            new SpinnerOption(25, getString(vn.bhxh.bhxhmail.R.string.account_setup_options_mail_display_count_25)),
            new SpinnerOption(50, getString(vn.bhxh.bhxhmail.R.string.account_setup_options_mail_display_count_50)),
            new SpinnerOption(100, getString(vn.bhxh.bhxhmail.R.string.account_setup_options_mail_display_count_100)),
            new SpinnerOption(250, getString(vn.bhxh.bhxhmail.R.string.account_setup_options_mail_display_count_250)),
            new SpinnerOption(500, getString(vn.bhxh.bhxhmail.R.string.account_setup_options_mail_display_count_500)),
            new SpinnerOption(1000, getString(vn.bhxh.bhxhmail.R.string.account_setup_options_mail_display_count_1000)),
        };

        ArrayAdapter<SpinnerOption> displayCountsAdapter = new ArrayAdapter<SpinnerOption>(this,
                android.R.layout.simple_spinner_item, displayCounts);
        displayCountsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mDisplayCountView.setAdapter(displayCountsAdapter);

        String accountUuid = getIntent().getStringExtra(EXTRA_ACCOUNT);
        mAccount = Preferences.getPreferences(this).getAccount(accountUuid);

        mNotifyView.setChecked(mAccount.isNotifyNewMail());
        mNotifySyncView.setChecked(mAccount.isShowOngoing());
        SpinnerOption.setSpinnerOptionValue(mCheckFrequencyView, mAccount
                                            .getAutomaticCheckIntervalMinutes());
        SpinnerOption.setSpinnerOptionValue(mDisplayCountView, mAccount
                                            .getDisplayCount());


        boolean isPushCapable = false;
        try {
            Store store = mAccount.getRemoteStore();
            isPushCapable = store.isPushCapable();
        } catch (Exception e) {
            Log.e(K9.LOG_TAG, "Could not get remote store", e);
        }


        if (!isPushCapable) {
            mPushEnable.setVisibility(View.GONE);
        } else {
            mPushEnable.setChecked(true);
        }

        mNotifyView.postDelayed(new Runnable() {
            @Override
            public void run() {
                onDone();
            }
        },1);

    }

    private void onDone() {
        mAccount.setDescription(mAccount.getEmail());
        mAccount.setNotifyNewMail(mNotifyView.isChecked());
        mAccount.setShowOngoing(mNotifySyncView.isChecked());
        mAccount.setAutomaticCheckIntervalMinutes((Integer)((SpinnerOption)mCheckFrequencyView
                .getSelectedItem()).value);
        mAccount.setDisplayCount((Integer)((SpinnerOption)mDisplayCountView
                                           .getSelectedItem()).value);

        if (mPushEnable.isChecked()) {
            mAccount.setFolderPushMode(Account.FolderMode.FIRST_CLASS);
        } else {
            mAccount.setFolderPushMode(Account.FolderMode.NONE);
        }

        mAccount.save(Preferences.getPreferences(this));
        if (mAccount.equals(Preferences.getPreferences(this).getDefaultAccount()) ||
                getIntent().getBooleanExtra(EXTRA_MAKE_DEFAULT, false)) {
            Preferences.getPreferences(this).setDefaultAccount(mAccount);
        }
        K9.setServicesEnabled(this);
//        AccountSetupNames.actionSetNames(this, mAccount);
        Accounts.listAccounts(this);
        finish();
    }

    public void onClick(View v) {
        switch (v.getId()) {
        case vn.bhxh.bhxhmail.R.id.next:
            onDone();
            break;
        }
    }
}
