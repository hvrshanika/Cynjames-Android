package au.com.cynjames.mainView;


import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import au.com.cynjames.cjtv20.R;
import au.com.cynjames.utils.GenericMethods;


public class ImageFragment extends DialogFragment {
    private Context context;
    private ArrayList<String> imageInfo;
    private WindowManager.LayoutParams params;
    private String type;
    ImageView imageView;
    ImageView btnBack;
    ImageView btnNext;
    ImageView btnDelete;
    String FILE_PATH = Environment.getExternalStorageDirectory() + File.separator + ".CJT-AppData" + File.separator;
    String cuurentImage;
    JobsDetailsFragmentListener listener;
    String deletedImage;
    int index = 0;

    public ImageFragment() {
        // Required empty public constructor
    }

    public ImageFragment(Context context, ArrayList<String> imageInfo, WindowManager.LayoutParams params, String type) {
        this.context = context;
        this.imageInfo = imageInfo;
        this.params = params;
        this.type = type;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = null;
        WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = this.params.width;
        params.height = this.params.height;
        params.gravity = this.params.gravity;
        getDialog().getWindow().setAttributes(params);
        getDialog().setCanceledOnTouchOutside(false);
        rootView = inflater.inflate(R.layout.fragment_image, container, false);
        setButtonListeners(rootView);
        if (imageInfo.size() > 0) {
            setImage(imageInfo.get(index));
        } else {
            GenericMethods.showToast(context, "No Images");
        }
        if (imageInfo.size() > 1) {
            btnNext.setVisibility(View.VISIBLE);
        }
        return rootView;
    }

    private void setButtonListeners(View view) {
        btnNext = view.findViewById(R.id.image_fragment_next_button);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextBtnClicked();
            }
        });
        btnBack = view.findViewById(R.id.image_fragment_back_button);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backBtnClicked();
            }
        });
        btnDelete = view.findViewById(R.id.image_fragment_delete_button);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteBtnClicked();
            }
        });
        imageView = view.findViewById(R.id.image_fragment_image);
    }

    private void setImage(String imageName) {
        cuurentImage = imageName;

        if (type.equals("DeliveryJobs") && cuurentImage.contains("PJ")) {
            btnDelete.setVisibility(View.INVISIBLE);
        }
        else{
            btnDelete.setVisibility(View.VISIBLE);
        }

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 8;
        String strPath = FILE_PATH + "" + imageName + "";
        Bitmap bitmap = BitmapFactory.decodeFile(strPath,options);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            GenericMethods.showToast(context, "No Image");
        }
    }

    private void nextBtnClicked() {
        index++;
        setImage(imageInfo.get(index));
        btnBack.setVisibility(View.VISIBLE);
        if(imageInfo.size() == index+1)
            btnNext.setVisibility(View.INVISIBLE);
    }

    private void backBtnClicked() {
        index--;
        setImage(imageInfo.get(index));
        btnNext.setVisibility(View.VISIBLE);
        if(index == 0)
            btnBack.setVisibility(View.INVISIBLE);
    }

    private void deleteBtnClicked() {
        AlertDialog.Builder build = new AlertDialog.Builder(context);
        build.setMessage("Are you sure you want to Delete this Image?");
        build.setCancelable(false);
        build.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                File file = new File(FILE_PATH, cuurentImage);
                file.delete();
                deletedImage = cuurentImage;
                dismiss();
            }
        });
        build.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        build.create().show();

    }

    public void setListener(JobsDetailsFragmentListener closeListener) {
        this.listener = closeListener;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        listener.handleDialogCloseImage(deletedImage);
    }

}
