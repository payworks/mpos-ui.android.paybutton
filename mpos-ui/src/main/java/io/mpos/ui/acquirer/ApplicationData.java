/*
 * mpos-ui : http://www.payworksmobile.com
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 payworks GmbH
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.mpos.ui.acquirer;

import io.mpos.ui.shared.model.MposUiConfiguration;

public class ApplicationData {

    private ApplicationName mApplicationName;
    private String mIdentifier;
    private String mHelpUrl;
    private int mImageResourceId;
    private MposUiConfiguration mMposUiConfiguration;

    public ApplicationData() {
    }

    public String getIdentifier() {
        return mIdentifier;
    }

    public void setIdentifier(String identifier) {
        mIdentifier = identifier;
    }

    public String getHelpUrl() {
        return mHelpUrl;
    }

    public void setHelpUrl(String helpUrl) {
        mHelpUrl = helpUrl;
    }

    public int getImageResourceId() {
        return mImageResourceId;
    }

    public void setImageResourceId(int imageResourceId) {
        mImageResourceId = imageResourceId;
    }

    public MposUiConfiguration getMposUiConfiguration() {
        return mMposUiConfiguration;
    }

    public void setMposUiConfiguration(MposUiConfiguration mposUiConfiguration) {
        mMposUiConfiguration = mposUiConfiguration;
    }

    public ApplicationName getApplicationName() {
        return mApplicationName;
    }

    public void setApplicationName(ApplicationName mApplicationName) {
        this.mApplicationName = mApplicationName;
    }
}
