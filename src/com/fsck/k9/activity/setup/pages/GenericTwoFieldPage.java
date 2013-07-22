package com.fsck.k9.activity.setup.pages;

import android.support.v4.app.Fragment;
import android.text.TextUtils;
import com.fsck.k9.activity.setup.AccountSetupModel;
import com.fsck.k9.helper.wizard.model.ModelCallbacks;
import com.fsck.k9.helper.wizard.model.Page;
import com.fsck.k9.helper.wizard.model.ReviewItem;

import java.util.ArrayList;

/**
 * A page asking for a name and an email.
 */
public class GenericTwoFieldPage extends Page {

    private final String mKEY1;
    private final String mKEY2;
    private final String mlabel1;
    private final String mlabel2;
    private final String mReviewTitle1;
    private final String mReviewTitle2;
    private final boolean mRequired1;
    private final boolean mRequired2;
    private final int mInputType1;
    private final int mInputType2;

    public GenericTwoFieldPage(ModelCallbacks callbacks, String title,
                               String KEY1, String label1, String reviewTitle1, int inputType1, boolean req1,
                               String KEY2, String label2, String reviewTitle2, int inputType2, boolean req2)
    {
        super(callbacks, title);
        this.mKEY1 = KEY1;
        this.mKEY2 = KEY2;
        this.mlabel1 = label1;
        this.mlabel2 = label2;
        this.mReviewTitle1 = reviewTitle1;
        this.mReviewTitle2 = reviewTitle2;
        this.mRequired1 = req1;
        this.mRequired2 = req2;
        this.mInputType1 = inputType1;
        this.mInputType2 = inputType2;
    }

    @Override
    public Fragment createFragment() {
        return GenericTwoFieldFragment.create(getKey(),
                mKEY1, mlabel1, mInputType1,
                mKEY2, mlabel2, mInputType2);
    }

    @Override
    public void getReviewItems(ArrayList<ReviewItem> dest) {
        dest.add(new ReviewItem(mReviewTitle1, mData.getString(mKEY1), getKey(), -1));
        dest.add(new ReviewItem(mReviewTitle2, mData.getString(mKEY2), getKey(), -1));
    }

    @Override
    public boolean isCompleted() {
        return !((mRequired1 && TextUtils.isEmpty(mData.getString(mKEY1))) ||
                (mRequired2 && TextUtils.isEmpty(mData.getString(mKEY2))));
    }
}
