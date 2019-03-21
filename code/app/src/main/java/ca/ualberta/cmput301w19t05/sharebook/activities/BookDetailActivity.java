package ca.ualberta.cmput301w19t05.sharebook.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import ca.ualberta.cmput301w19t05.sharebook.R;
import ca.ualberta.cmput301w19t05.sharebook.models.Book;
import ca.ualberta.cmput301w19t05.sharebook.models.User;
import ca.ualberta.cmput301w19t05.sharebook.tools.FirebaseHandler;
/**
 * A book detial screen allow user to edit book
 */
public class BookDetailActivity extends AppCompatActivity {
    private RadioGroup title;
    private RadioGroup author;
    private RadioGroup owner;
    private RadioGroup description;
    private ImageView bookImage;
    private Book book;
    private FirebaseHandler firebaseHandler;
    private Button delete;
    private ProgressDialog progressDialog;
    private Boolean inProgress;

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            final TextView prompt = v.findViewWithTag("prompt");
            final TextView content = v.findViewWithTag("content");
            View view = View.inflate(BookDetailActivity.this, R.layout.content_edit, null);
            final EditText userInput = view.findViewById(R.id.user_input);
            userInput.setText(content.getText());
            userInput.requestFocus();
            userInput.selectAll();

            new AlertDialog.Builder(BookDetailActivity.this)
                    .setTitle(prompt.getText()).setView(view)
                    .setPositiveButton("submit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (!userInput.getText().toString().equals("") && !userInput.getText().equals(content.getText())) {
                                content.setText(userInput.getText());
                                DatabaseReference ref = firebaseHandler.getMyRef().child("books").child(book.getOwner().getUserID()).child(book.getBookId());
                                if (v.equals(title)) {
                                    book.setTitle(userInput.getText().toString());
                                    ref.child("title").setValue(userInput.getText().toString());

                                } else if (v.equals(author)) {
                                    book.setAuthor(userInput.getText().toString());
                                    ref.child("author").setValue(userInput.getText().toString());
                                } else if (v.equals(description)) {
                                    book.setDescription(userInput.getText().toString());
                                    ref.child("description").setValue(userInput.getText().toString());
                                }

                            }

                        }
                    }).setNegativeButton("cancel", null).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);
        initViews();

        Intent intent = getIntent();
        book = intent.getParcelableExtra("book");
        firebaseHandler = new FirebaseHandler(this);

        setBookInfo();
        setClickListener();


    }

    private void setClickListener() {
        title.setOnClickListener(onClickListener);
        author.setOnClickListener(onClickListener);
        description.setOnClickListener(onClickListener);
        if (book.getOwner().getUserID().equals(firebaseHandler.getCurrentUser().getUserID())) {
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(BookDetailActivity.this).setMessage("Are you sure?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    firebaseHandler.getMyRef().child("books").child(book.getOwner()
                                            .getUserID())
                                            .child(book.getBookId()).setValue(null);
                                    finish();
                                }
                            }).setNegativeButton("No", null)
                            .show();
                }
            });
        } else {
            delete.setVisibility(View.GONE);
        }

        TextView ownerText = owner.findViewWithTag("content");
        final String ownerName = ownerText.getText().toString();
        owner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!inProgress) {
                    showDialog();
                    Query query = firebaseHandler.getMyRef().child(getString(R.string.db_username_email_tuple))
                            .orderByChild("username").equalTo(ownerName);
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getValue() != null) {
                                for (DataSnapshot data : dataSnapshot.getChildren()) {
                                    User user = data.getValue(User.class);
                                    Intent intent = new Intent(BookDetailActivity.this, UserProfile.class);
                                    intent.putExtra("owner", user);
                                    hideDialog();
                                    startActivity(intent);

                                }

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(BookDetailActivity.this, databaseError.toString(),
                                    Toast.LENGTH_SHORT).show();
                            hideDialog();
                        }


                    });
                }

            }
        });


    }

    private void initViews() {
        title = findViewById(R.id.title);
        author = findViewById(R.id.author);
        owner = findViewById(R.id.owner_name);
        description = findViewById(R.id.description);
        bookImage = findViewById(R.id.book_cover);
        delete = findViewById(R.id.delete_book);
        progressDialog = new ProgressDialog(BookDetailActivity.this);
        progressDialog.setMessage("Searching user...");
        inProgress = false;


    }

    private void setBookInfo() {
        TextView titleContent = title.findViewWithTag("content");
        TextView authorContent = author.findViewWithTag("content");
        TextView ownerContent = owner.findViewWithTag("content");
        TextView desContent = description.findViewWithTag("content");

        titleContent.setText(book.getTitle());
        authorContent.setText(book.getAuthor());
        ownerContent.setText(book.getOwner().getUsername());
        desContent.setText(book.getDescription());
        //bookImage.setImageURI(Uri.parse(book.getPhoto()));
        Glide.with(BookDetailActivity.this).load(Uri.parse(book.getPhoto()))
                .into(bookImage);


    }

    private void showDialog() {
        inProgress = true;
        if (!progressDialog.isShowing())
            progressDialog.show();
    }

    private void hideDialog() {
        inProgress = false;
        if (progressDialog.isShowing())
            progressDialog.dismiss();
    }

}