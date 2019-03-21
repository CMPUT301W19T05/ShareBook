
package ca.ualberta.cmput301w19t05.sharebook.activities;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import ca.ualberta.cmput301w19t05.sharebook.R;
import ca.ualberta.cmput301w19t05.sharebook.models.Book;
import ca.ualberta.cmput301w19t05.sharebook.tools.FirebaseHandler;

/**
 * A addBook screen Allow user adding books into their sheff
 */
public class AddBookActivity extends AppCompatActivity {

    private String titleText;
    private String authorText;
    private String descriptionText;
    private String ISBN;
    private FirebaseHandler firebaseHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseHandler = new FirebaseHandler(AddBookActivity.this);
        setContentView(R.layout.activity_add_book_screen);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Button submitButton = findViewById(R.id.submit);
        Button cancelButton = findViewById(R.id.cancel);
        final EditText editTitle = findViewById(R.id.title);
        final EditText editAuthor = findViewById(R.id.author);
        final EditText editDescription = findViewById(R.id.description);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                boolean valid = true;

                //test case for empty blank
                titleText = editTitle.getText().toString();
                if (TextUtils.isEmpty(titleText)) {
                    editTitle.setError("Need to fill");
                    valid = false;
                }

                authorText = editAuthor.getText().toString();
                if (TextUtils.isEmpty(authorText)) {
                    editAuthor.setError("Need to fill");
                    valid = false;
                }
                descriptionText = editDescription.getText().toString();

                //check ok
                if (valid) {
                    ISBN = "place holder";
                    final Book book = new Book(titleText, authorText, ISBN, firebaseHandler.getCurrentUser());
                    StorageReference reference = FirebaseStorage.getInstance().getReference().child("image/book_placeholder.png");
                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            book.setPhoto(String.valueOf(uri));
                            if (!descriptionText.equals("")) {
                                book.setDescription(descriptionText);
                            }
                            firebaseHandler.addBook(book);
                            firebaseHandler.generateImageFromText(book.getTitle());
                            finish();
                        }
                    })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(AddBookActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                                }
                            });

                }

            }

        });

    }


}