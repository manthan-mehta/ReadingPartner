package com.example.textrecognition;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

public class nlp_required extends AppCompatActivity {
    Uri resulturi;
    ImageView mPreview_new;
    static ArrayList<String> words,definitions;
    static DatabaseHelper databaseHelper;

    RecyclerView recyclerView ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nlp);
        mPreview_new = (ImageView)findViewById(R.id.imageIV);
        recyclerView = findViewById(R.id.recyclerView);
        Bundle bundle = getIntent().getExtras();
        String sresulturi = bundle.getString("KEY");
        resulturi = Uri.parse(sresulturi);
        mPreview_new.setImageURI(resulturi);

        words = new ArrayList<String>();
        definitions = new ArrayList<String>();
        String result = donlp(resulturi);

        RecyclerViewAdapter viewAdapter = new RecyclerViewAdapter(this,words,definitions);
        recyclerView.setAdapter(viewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private String donlp(Uri resulturi) {
        BitmapDrawable bitmapDrawable = (BitmapDrawable) mPreview_new.getDrawable();
        Bitmap bmp = bitmapDrawable.getBitmap();
        TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();

        if (!textRecognizer.isOperational()) {
            Toast.makeText(this, "Error", Toast.LENGTH_LONG).show();
        } else {
            Frame frame = new Frame.Builder().setBitmap(bmp).build();
            SparseArray<TextBlock> items = textRecognizer.detect(frame);
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < items.size(); i++) {
                TextBlock tb = items.valueAt(i);
                sb.append(tb.getValue());
                sb.append("\n");
            }

            return fetch_words(sb.toString());
        }
        return "There is nothing to show";
    }

    private String fetch_words(String text) {
        InputStream tokenmodel = null;
        InputStream posmodel = null;
        try {
            AssetManager am = this.getAssets();
            tokenmodel = am.open("en-token.bin");
            TokenizerModel tokenizerModel = new TokenizerModel(tokenmodel);
            Tokenizer tokenizer = new TokenizerME(tokenizerModel);
            String tokens[] = tokenizer.tokenize(text);
            StringBuilder sb1 = new StringBuilder();

            AssetManager am1 = this.getAssets();
            posmodel = am1.open("en-pos-maxent.bin");
            POSModel posModel = new POSModel(posmodel);

            POSTaggerME tagger = new POSTaggerME(posModel);
            String tags[] = tagger.tag(tokens);
            String required_tags[] = {"JJ" , "JJR", "JJS" ,"NN" , "NNS", "RB", "RBR" ,"RBS","VB",
                    "VBD","VBG","VBN" ,"VBP","VBZ"};
            String stop_wors[] = {"a", "about", "above", "after", "again", "against", "ain", "all",
                    "am", "an", "and", "any", "are", "aren", "aren't", "as", "at", "be", "because",
                    "been", "before", "being", "below", "between", "both", "but", "by", "can",
                    "couldn", "couldn't", "d", "did", "didn", "didn't", "do", "does", "doesn",
                    "doesn't", "doing", "don", "don't", "down", "during", "each", "few", "for",
                    "from", "further", "had", "hadn", "hadn't", "has", "hasn", "hasn't", "have",
                    "haven", "haven't", "having", "he", "her", "here", "hers", "herself", "him",
                    "himself", "his", "how", "i", "if", "in", "into", "is", "isn", "isn't", "it",
                    "it's", "its", "itself", "just", "ll", "m", "ma", "me", "mightn", "mightn't",
                    "more", "most", "mustn", "mustn't", "my", "myself", "needn", "needn't", "no",
                    "nor", "not", "now", "o", "of", "off", "on", "once", "only", "or", "other",
                    "our", "ours", "ourselves", "out", "over", "own", "re", "s","\'s","same", "shan",
                    "shan't", "she", "she's", "should", "should've", "shouldn", "shouldn't", "so",
                    "some", "such", "t", "than", "that", "that'll", "the", "their", "theirs", "them",
                    "themselves", "then", "there", "these", "they", "this", "those", "through", "to",
                    "too", "under", "until", "up", "ve", "very", "was", "wasn", "wasn't", "we",
                    "were", "weren", "weren't", "what", "when", "where", "which", "while", "who",
                    "whom", "why", "will", "with", "won", "won't", "wouldn", "wouldn't", "y", "you",
                    "you'd", "you'll", "you're", "you've", "your", "yours", "yourself", "yourselves",
                    "could", "he'd", "he'll", "he's", "here's", "how's", "i'd", "i'll", "i'm", "i've",
                    "let's", "ought", "she'd", "she'll", "that's", "there's", "they'd", "they'll",
                    "they're", "they've", "we'd", "we'll", "we're", "we've", "what's", "when's",
                    "where's", "who's", "why's", "would"};
            HashSet<String> required_set = new HashSet<>(Arrays.asList(required_tags));
            HashSet<String> stop_Words_set = new HashSet<>(Arrays.asList(stop_wors));
            databaseHelper = new DatabaseHelper(this);
            try{
                databaseHelper.openDatabase();
            } catch (SQLException e) {
                throw e;
            }
            for (int i = 0; i < tokens.length; i++) {
                Log.e("Er:" ,tokens[i]);
                if (required_set.contains(tags[i]) && !stop_Words_set.contains(tokens[i])) {
                    Cursor cursor = databaseHelper.getMeaning(tokens[i]);
                    if(cursor.moveToFirst()) {
                        String definitions1 = cursor.getString(cursor.getColumnIndex("definition"));
                        String input_word = cursor.getString(cursor.getColumnIndex("word"));
                        sb1.append(input_word + "::" + definitions1);
                        words.add(input_word);
                        definitions.add(definitions1);
                        sb1.append("\n");
                    }
                }
            }

            String result = sb1.toString();
            return result;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if(tokenmodel != null) {
                try {
                    tokenmodel.close();
                }
                catch (IOException e){

                }
            }
            if(posmodel != null) {
                try {
                    posmodel.close();
                }
                catch (IOException e){

                }
            }
        }
        return "There is nothing to Show";
    }
}
