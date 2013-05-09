package com.fsck.k9.activity.setup;

import android.content.Context;
import com.fsck.k9.R;
import com.fsck.k9.activity.setup.pages.BasicAccountInfoPage;
import com.fsck.k9.helper.wizard.model.AbstractWizardModel;
import com.fsck.k9.helper.wizard.model.BranchPage;
import com.fsck.k9.helper.wizard.model.PageList;

public class AccountSetupModel extends AbstractWizardModel {
    public AccountSetupModel(Context context) {
        super(context);
    }

    @Override
    protected PageList onNewRootPageList() {
        /*
            Account Type
                - add all accounts detected on the device
                - add all exported accounts detected
                - add "other" option to manually enter email + password
                - add "import" option to ask for a file
         */
        BranchPage accountTypePage =  new BranchPage(this, mContext.getString(R.string.wizard_which_account_title));
        accountTypePage.setRequired(true);

        // TODO scan and add all accounts
        // TODO scan and add all import files
        // TODO add import option

        // add "other option"
        accountTypePage.addBranch(mContext.getString(R.string.wizard_which_account_other),
                new BasicAccountInfoPage(this, "Your info").setRequired(true));//,
                //new CustomerInfoPage(this, "Your info").setRequired(true));

        return new PageList(
            accountTypePage
            //setupIncoming,
            //setupOutgoing,
            //
        );
    }
}
