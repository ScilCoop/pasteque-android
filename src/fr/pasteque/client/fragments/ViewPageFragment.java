/*
 * Copyright 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.pasteque.client.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;

import junit.framework.Assert;

public abstract class ViewPageFragment extends Fragment {
    public static final String ARG_PAGE = "page";

    protected Context mContext;

    private int mPageNumber;

    public static ViewPageFragment initPageNumber(int pageNumber, ViewPageFragment frag) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, pageNumber);
        frag.setArguments(args);
        return frag;
    }

    public static ViewPageFragment initPageNumber(int pageNumber, ViewPageFragment frag, Bundle args) {
        args.putInt(ARG_PAGE, pageNumber);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();
        Bundle arguments = getArguments();
        Assert.assertNotNull(arguments);
        mPageNumber = arguments.getInt(ARG_PAGE);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public int getPageNumber() {
        return mPageNumber;
    }
}
