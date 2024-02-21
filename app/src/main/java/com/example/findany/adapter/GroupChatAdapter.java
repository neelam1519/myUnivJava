package com.example.findany.adapter;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.findany.R;
import com.example.findany.model.ChatMessage;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFPictureData;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;


public class GroupChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_TEXT = 1;
    private static final int TYPE_FILE = 2;
    private static final int TYPE_UNKNOWN = 3;
    StorageReference storageRef = FirebaseStorage.getInstance().getReference();
    private Context context;
    private List<ChatMessage> messageList;
    private List<String> adminsList;

    public GroupChatAdapter(Context context, List<ChatMessage> messageList) {
        this.context = context;
        this.messageList = messageList;
        this.adminsList = getListFromSharedPreferences("GROUPCHAT");
    }

    private List<String> getListFromSharedPreferences(String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("ADMINS", Context.MODE_PRIVATE);
        String combinedString = sharedPreferences.getString(key, "");
        return new ArrayList<>(Arrays.asList(combinedString.split(",")));
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view;
        if (viewType == TYPE_TEXT) {
            view = inflater.inflate(R.layout.item_message, parent, false);
            return new TextMessageViewHolder(view);
        } else if (viewType == TYPE_FILE) {
            view = inflater.inflate(R.layout.item_file, parent, false);
            return new FileMessageViewHolder(view);
        } else {
            // Handle other view types if needed
            view = inflater.inflate(R.layout.item_message, parent, false);
            return new TextMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messageList.get(position);

        if (holder instanceof TextMessageViewHolder) {
            bindTextMessage((TextMessageViewHolder) holder, message);
        } else if (holder instanceof FileMessageViewHolder) {
            bindFileMessage((FileMessageViewHolder) holder, message);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    private void bindTextMessage(TextMessageViewHolder textHolder, ChatMessage message) {

        textHolder.senderNameTextView.setText(message.getSenderName());

        int textColor = adminsList.contains(removeDomainFromEmail(message.getRegno())) ? Color.parseColor("#FF0000") : Color.parseColor("#000000");
        textHolder.senderNameTextView.setTextColor(textColor);


        String messageText = message.getMessageText();
        if (messageText != null) {
            textHolder.messageTextView.setText(messageText);
            Linkify.addLinks(textHolder.messageTextView, Linkify.WEB_URLS);
        } else {
            textHolder.messageTextView.setText("");
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
        String formattedDate = dateFormat.format(message.getTimestamp());
        textHolder.timestampTextView.setText(formattedDate);
    }

    private void bindFileMessage(FileMessageViewHolder fileHolder, ChatMessage message) {
        fileHolder.sendername.setText(message.getSenderName());

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
        String formattedDate = dateFormat.format(message.getTimestamp());
        fileHolder.timestamp.setText(formattedDate);

        int textColor = adminsList.contains(message.getRegno()) ? Color.parseColor("#F35656") : Color.BLACK;
        fileHolder.sendername.setTextColor(textColor);

        fileHolder.filename.setText(message.getMessageText());

        downloadOrLoadFile(message.getMessageId(), message.getMessagetype(), fileHolder, message);
    }

    private void downloadOrLoadFile(String fileName, String fileType, FileMessageViewHolder fileHolder, ChatMessage message) {
        Log.d("GroupchatAdapter", "entered bindData");
        StorageReference fileRef = storageRef.child("group_chat/" + fileName);
        File cacheDir = new File(context.getCacheDir(), "groupchat");

        if (!cacheDir.exists() && !cacheDir.mkdirs()) {
            Log.e("Adapter", "Failed to create cache directory");
            return;
        }

        File localFile = new File(cacheDir, fileName);
        int defaultImageResource = getDefaultImageResource(fileType);
        fileHolder.imageView.setImageResource(defaultImageResource);

        if (localFile.exists()) {
            // If the file already exists in the cache, load it
            showInChat(fileType, localFile, fileHolder, message);
        } else {
            // If the file doesn't exist in the cache, download it
            fileRef.getFile(localFile)
                    .addOnSuccessListener(taskSnapshot -> {
                        Log.d("GroupChatAdapter", "File downloaded successfully");
                        showInChat(fileType, localFile, fileHolder, message);
                    })
                    .addOnFailureListener(exception -> {
                        Log.e("GroupChatAdapter", "Error downloading file: " + exception.getMessage());
                    });
        }
    }

    private int getDefaultImageResource(String fileType) {
        switch (fileType) {
            case "application/pdf":
                return R.drawable.pdfdownloading;
            case "application/msword":
                return R.drawable.word;
            case "image/*":
                return R.drawable.imagedownloading;
            case "application/vnd.ms-powerpoint":
                return R.drawable.pptdownloading;
            case "video/*":
                return R.drawable.videoicon;
            case "audio/*":
                return R.drawable.audio;
            default:
                return R.drawable.unknown;
        }
    }

    private void showInChat(String fileType, File filePath, FileMessageViewHolder fileHolder, ChatMessage message) {
        Log.d("GroupChatAdapter", "showInChat");
        Log.d("GroupChatAdapter", "fileType: " + fileType);
        Bitmap image = null;

        switch (fileType) {
            case "application/pdf":
                try {
                    image = extractFirstPDFImage(filePath);
                    Log.d("GroupChatAdapter", "pdf");
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("GroupChatAdapter", "pdf exception");
                }
                break;
            case "application/msword":
                try {
                    image = extractFirstWordImage(filePath);
                    Log.d("GroupChatAdapter", "word");
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("GroupChatAdapter", "word exception");
                }
                break;
            case "image/*":
                if (filePath.exists()) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    image = BitmapFactory.decodeFile(filePath.getAbsolutePath(), options);
                } else {
                    Log.d("GroupChatAdapter", "Image does not exist");
                }
                break;
            default:
                Drawable defaultDrawable = context.getResources().getDrawable(R.drawable.unknown);
                if (defaultDrawable instanceof BitmapDrawable) {
                    image = ((BitmapDrawable) defaultDrawable).getBitmap();
                }
                break;
        }
        if (image != null) {
            Glide.with(context)
                    .load(image)
                    .error(R.drawable.defaultprofile)
                    .into(fileHolder.imageView);
        }

        fileHolder.imageView.setOnClickListener(v -> {
            Log.d("GroupChatAdapter", fileType + " clicked: " + message.getMessageText());
            openFileWithViewer(filePath.getAbsolutePath(), fileType);
        });
    }

    private Bitmap extractFirstPDFImage(File filePath) throws IOException {
        PdfRenderer renderer = new PdfRenderer(ParcelFileDescriptor.open(filePath, ParcelFileDescriptor.MODE_READ_ONLY));

        if (renderer.getPageCount() > 0) {
            PdfRenderer.Page page = renderer.openPage(0);
            Matrix matrix = new Matrix();
            int desiredWidth = 400;
            int desiredHeight = 400;
            float scaleX = (float) desiredWidth / page.getWidth();
            float scaleY = (float) desiredHeight / page.getHeight();
            matrix.setScale(scaleX, scaleY);

            Bitmap bitmap = Bitmap.createBitmap(desiredWidth, desiredHeight, Bitmap.Config.ARGB_8888);
            page.render(bitmap, null, matrix, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

            page.close();
            renderer.close();

            return bitmap;
        } else {
            renderer.close();
            return null;
        }
    }

    private Bitmap extractFirstWordImage(File filePath) throws IOException {
        FileInputStream fis = new FileInputStream(filePath);
        XWPFDocument document = new XWPFDocument(fis);
        fis.close();
        List<XWPFPictureData> pictures = document.getAllPictures();

        if (!pictures.isEmpty()) {
            XWPFPictureData pictureData = pictures.get(0);
            byte[] bytes = pictureData.getData();
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }

        return null;
    }

    private void openFileWithViewer(String filePath, String mimeType) {
        File file = new File(filePath);

        if (file.exists()) {
            Uri contentUri = FileProvider.getUriForFile(context, "com.neelam.findany.fileprovider", file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(contentUri, mimeType);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            try {
                context.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(context, "No app found to open this file.", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(context, "File does not exist.", Toast.LENGTH_SHORT).show();
        }
    }
    private String removeDomainFromEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex != -1) {
            // Extract the substring before '@' to remove the domain
            return email.substring(0, atIndex);
        } else {
            // No '@' found, return the original email
            return email;
        }
    }

    @Override
    public int getItemViewType(int position) {
        String messageType = messageList.get(position).getMessagetype();
        if ("text".equals(messageType)) {
            return TYPE_TEXT;
        } else if (messageType==null || messageType.isEmpty()) {
            return TYPE_UNKNOWN;
        } else {
            return TYPE_FILE;
        }
    }

    public static class TextMessageViewHolder extends RecyclerView.ViewHolder {
        TextView senderNameTextView;
        TextView messageTextView;
        TextView timestampTextView;

        public TextMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            senderNameTextView = itemView.findViewById(R.id.senderNameTextView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);
        }
    }

    public static class FileMessageViewHolder extends RecyclerView.ViewHolder {
        TextView sendername;
        TextView timestamp;
        LinearLayout fileContainer;
        TextView filename;
        ImageView imageView;
        public FileMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            sendername = itemView.findViewById(R.id.sendername);
            timestamp = itemView.findViewById(R.id.timestamp);
            fileContainer = itemView.findViewById(R.id.fileContainer);
            filename=itemView.findViewById(R.id.filename);
            imageView=itemView.findViewById(R.id.imageView);
        }
    }
}