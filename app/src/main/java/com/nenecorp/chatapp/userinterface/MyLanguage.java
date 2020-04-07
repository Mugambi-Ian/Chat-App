package com.nenecorp.chatapp.userinterface;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.FirebaseDatabase;
import com.nenecorp.chatapp.R;
import com.nenecorp.chatapp.assets.resources.Constants;
import com.nenecorp.chatapp.models.DbSocket;

import java.util.ArrayList;

public class MyLanguage extends AppCompatActivity {
    private ArrayList<Language> languages = new ArrayList<>();
    private ListView myLangListView;

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_language);
        myLangListView = findViewById(R.id.AML_lstLang);
        String[] z = lang.split("\n");
        for (String y : z) {
            languages.add(new Language(y));
        }
        LanguageAdapter adapter = new LanguageAdapter(this, R.layout.activity_my_language, languages);
        myLangListView.setAdapter(adapter);
        EditText myLanguage = findViewById(R.id.AML_txtMyLang);
        myLanguage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                LanguageAdapter adapter;
                if (s.length() != 0) {
                    ArrayList<Language> x = new ArrayList<>();
                    for (Language l : languages) {
                        if (l.language.toLowerCase().contains(s.toString().toLowerCase())) {
                            x.add(l);
                        }
                    }
                    adapter = new LanguageAdapter(MyLanguage.this, R.layout.activity_my_language, x);
                } else {
                    adapter = new LanguageAdapter(MyLanguage.this, R.layout.activity_my_language, languages);
                }
                myLangListView.setAdapter(adapter);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }


    static class Language {
        String code;
        String language;

        Language(String l) {
            String[] x = l.split("\t");
            code = x[0];
            language = x[1];
        }
    }

    class LanguageAdapter extends ArrayAdapter<Language> {

        public LanguageAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Language> objects) {
            super(context, resource, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View parentView = convertView;
            if (parentView == null) {
                parentView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_language, parent, false);
            }
            final Language x = getItem(position);
            ((TextView) parentView.findViewById(R.id.LIL_language)).setText(x.language);
            parentView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setAsUsersLanguage(x);
                }
            });
            return parentView;
        }
    }

    private void setAsUsersLanguage(Language x) {
        String userId = DbSocket.dbHandler.appUser.userId;
        FirebaseDatabase.getInstance().getReference().child(Constants.USERS).child(userId).child("userLang").setValue(x.code);
        Toast.makeText(MyLanguage.this, "Language changed to " + x.language, Toast.LENGTH_SHORT).show();
        onBackPressed();
    }

    String lang = "af\tAfrikaans\n" +
            "ar\tArabic\n" +
            "be\tBelarusian\n" +
            "bg\tBulgarian\n" +
            "bn\tBengali\n" +
            "ca\tCatalan\n" +
            "cs\tCzech\n" +
            "cy\tWelsh\n" +
            "da\tDanish\n" +
            "de\tGerman\n" +
            "el\tGreek\n" +
            "en\tEnglish\n" +
            "eo\tEsperanto\n" +
            "es\tSpanish\n" +
            "et\tEstonian\n" +
            "fa\tPersian\n" +
            "fi\tFinnish\n" +
            "fr\tFrench\n" +
            "ga\tIrish\n" +
            "gl\tGalician\n" +
            "gu\tGujarati\n" +
            "he\tHebrew\n" +
            "hi\tHindi\n" +
            "hr\tCroatian\n" +
            "ht\tHaitian\n" +
            "hu\tHungarian\n" +
            "id\tIndonesian\n" +
            "is\tIcelandic\n" +
            "it\tItalian\n" +
            "ja\tJapanese\n" +
            "ka\tGeorgian\n" +
            "kn\tKannada\n" +
            "ko\tKorean\n" +
            "lt\tLithuanian\n" +
            "lv\tLatvian\n" +
            "mk\tMacedonian\n" +
            "mr\tMarathi\n" +
            "ms\tMalay\n" +
            "mt\tMaltese\n" +
            "nl\tDutch\n" +
            "no\tNorwegian\n" +
            "pl\tPolish\n" +
            "pt\tPortuguese\n" +
            "ro\tRomanian\n" +
            "ru\tRussian\n" +
            "sk\tSlovak\n" +
            "sl\tSlovenian\n" +
            "sq\tAlbanian\n" +
            "sv\tSwedish\n" +
            "sw\tSwahili\n" +
            "ta\tTamil\n" +
            "te\tTelugu\n" +
            "th\tThai\n" +
            "tl\tTagalog\n" +
            "tr\tTurkish\n" +
            "uk\tUkrainian\n" +
            "ur\tUrdu\n" +
            "vi\tVietnamese\n" +
            "zh\tChinese";

}
