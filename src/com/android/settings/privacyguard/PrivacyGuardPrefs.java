/*
 * Copyright (C) 2013 Slimroms
 * Copyright (C) 2013-2016 The MoKee Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.privacyguard;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import mokee.providers.MKSettings;
import org.mokee.internal.logging.MKMetricsLogger;

public class PrivacyGuardPrefs extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String TAG = "PrivacyGuardPrefs";

    private static final String KEY_PRIVACY_GUARD_DEFAULT = "privacy_guard_default";

    private SwitchPreference mPrivacyGuardDefault;

    public static PrivacyGuardPrefs newInstance() {
        PrivacyGuardPrefs privacyGuardFragment = new PrivacyGuardPrefs();
        return privacyGuardFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.privacy_guard_prefs);

        mPrivacyGuardDefault = (SwitchPreference) findPreference(KEY_PRIVACY_GUARD_DEFAULT);
        mPrivacyGuardDefault.setOnPreferenceChangeListener(this);

        mPrivacyGuardDefault.setChecked(MKSettings.Secure.getInt(getContentResolver(),
                MKSettings.Secure.PRIVACY_GUARD_DEFAULT, 0) == 1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);
        final ListView list = (ListView) view.findViewById(android.R.id.list);
        // our container already takes care of the padding
        int paddingTop = list.getPaddingTop();
        int paddingBottom = list.getPaddingBottom();
        list.setPadding(0, paddingTop, 0, paddingBottom);
        return view;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mPrivacyGuardDefault) {
            boolean value = (Boolean) newValue;
            MKSettings.Secure.putInt(getContentResolver(),
                    MKSettings.Secure.PRIVACY_GUARD_DEFAULT, value ? 1 : 0);
            return true;
        }
        return false;
    }

    @Override
    protected int getMetricsCategory() {
        return MKMetricsLogger.PRIVACY_GUARD_PREFS;
    }
}
