package com.qualityalternative.app.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class TestProfileDocumentsProvider extends ContentProvider {
    public static final String AUTHORITY = "com.qualityalternative.app.test.profiledocs";
    public static final String ROOT_ID = "root";

    private static final String[] DOCUMENT_COLUMNS = new String[] {
        DocumentsContract.Document.COLUMN_DOCUMENT_ID,
        DocumentsContract.Document.COLUMN_DISPLAY_NAME,
        DocumentsContract.Document.COLUMN_MIME_TYPE,
        DocumentsContract.Document.COLUMN_FLAGS,
        DocumentsContract.Document.COLUMN_SIZE,
    };

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(
        Uri uri,
        String[] projection,
        String selection,
        String[] selectionArgs,
        String sortOrder
    ) {
        MatrixCursor cursor = new MatrixCursor(projection != null ? projection : DOCUMENT_COLUMNS);
        List<String> segments = uri.getPathSegments();
        if (!segments.isEmpty() && "children".equals(segments.get(segments.size() - 1))) {
            File[] files = storageDir(attachedContext()).listFiles();
            if (files != null) {
                Arrays.sort(files, (left, right) -> left.getName().compareTo(right.getName()));
                for (File file : files) {
                    addDocumentRow(cursor, file.getName());
                }
            }
        } else {
            addDocumentRow(cursor, DocumentsContract.getDocumentId(uri));
        }
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return ROOT_ID.equals(DocumentsContract.getDocumentId(uri))
            ? DocumentsContract.Document.MIME_TYPE_DIR
            : "application/json";
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        File file = documentFile(attachedContext(), DocumentsContract.getDocumentId(uri));
        int accessMode = mode.contains("w")
            ? ParcelFileDescriptor.MODE_CREATE | ParcelFileDescriptor.MODE_WRITE_ONLY | ParcelFileDescriptor.MODE_TRUNCATE
            : ParcelFileDescriptor.MODE_READ_ONLY;
        return ParcelFileDescriptor.open(file, accessMode);
    }

    @Override
    public Bundle call(String method, String arg, Bundle extras) {
        if (!method.endsWith("createDocument")) {
            return super.call(method, arg, extras);
        }
        String displayName = extras != null ? extras.getString("displayName") : null;
        if ((displayName == null || displayName.trim().isEmpty()) && extras != null) {
            displayName = extras.getString(DocumentsContract.Document.COLUMN_DISPLAY_NAME);
        }
        if (displayName == null || displayName.trim().isEmpty()) {
            throw new IllegalStateException("Missing displayName for test document creation.");
        }
        File file = documentFile(attachedContext(), displayName);
        File parent = file.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Could not create test document.", exception);
        }

        Bundle out = new Bundle();
        out.putParcelable(
            "uri",
            DocumentsContract.buildDocumentUriUsingTree(
                DocumentsContract.buildTreeDocumentUri(AUTHORITY, ROOT_ID),
                displayName
            )
        );
        return out;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    public static void reset(Context context) {
        deleteRecursively(storageDir(context));
        storageDir(context).mkdirs();
    }

    public static String readDocument(Context context, String fileName) throws IOException {
        return new String(Files.readAllBytes(documentFile(context, fileName).toPath()), StandardCharsets.UTF_8);
    }

    public static List<String> documentNames(Context context) {
        String[] names = storageDir(context).list();
        if (names == null) {
            return Collections.emptyList();
        }
        Arrays.sort(names);
        return Arrays.asList(names);
    }

    private void addDocumentRow(MatrixCursor cursor, String documentId) {
        File file = documentFile(attachedContext(), documentId);
        cursor.newRow()
            .add(DocumentsContract.Document.COLUMN_DOCUMENT_ID, documentId)
            .add(DocumentsContract.Document.COLUMN_DISPLAY_NAME, ROOT_ID.equals(documentId) ? "root" : documentId)
            .add(
                DocumentsContract.Document.COLUMN_MIME_TYPE,
                ROOT_ID.equals(documentId) ? DocumentsContract.Document.MIME_TYPE_DIR : "application/json"
            )
            .add(DocumentsContract.Document.COLUMN_FLAGS, DocumentsContract.Document.FLAG_SUPPORTS_WRITE)
            .add(DocumentsContract.Document.COLUMN_SIZE, file.exists() ? file.length() : 0L);
    }

    private Context attachedContext() {
        Context current = getContext();
        if (current == null) {
            throw new IllegalStateException("Provider context is not attached.");
        }
        return current;
    }

    private static File storageDir(Context context) {
        return new File(context.getCacheDir(), "test-profile-documents");
    }

    private static File documentFile(Context context, String fileName) {
        return new File(storageDir(context), fileName);
    }

    private static void deleteRecursively(File file) {
        if (!file.exists()) {
            return;
        }
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) {
                deleteRecursively(child);
            }
        }
        file.delete();
    }
}
