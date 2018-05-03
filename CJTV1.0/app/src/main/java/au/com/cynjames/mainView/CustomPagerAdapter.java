package au.com.cynjames.mainView;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import au.com.cynjames.cjtv20.R;

/**
 * Created by SHANIKA on 11/19/2016.
 */
public class CustomPagerAdapter extends PagerAdapter {

    private Context mContext;
    private LayoutInflater mInflater;

    public static final int CONCEPT_COUNT = 0;
    public static final int CONCEPT_VIEW = 1;
    public static final int CONCEPT_DELIVERY_COUNT = 2;
    public static final int CONCEPT_DELIVERY_VIEW = 3;
    public static final int ADHOC_COUNT = 4;
    public static final int ADHOC_VIEW = 5;
    public static final int ADHOC_DELIVERY_COUNT = 6;
    public static final int ADHOC_DELIVERY_VIEW = 7;
    public static final int ADHOC_LISTVIEW = 8;
    public static final int ADHOC_NOJOBSVIEW = 9;

    private TextView conceptCount = null;
    private TextView conceptView = null;
    private TextView conceptDeliverCount = null;
    private TextView conceptDeliveryView = null;
//    private TextView adhocCount = null;
//    private TextView adhocView = null;
//    private TextView adhocDeliverCount = null;
//    private TextView adhocDeliveryView = null;
    private ListView adhocListView = null;
    private View adhocNoJobsView = null;
    private RelativeLayout conceptRView = null;
    private RelativeLayout adhocRView = null;
    private RelativeLayout messagesRView  = null;

    public CustomPagerAdapter(Context context, LayoutInflater inflater) {
        mContext = context;
        mInflater = inflater;

        conceptRView = (RelativeLayout) inflater.inflate(R.layout.activity_main_concept,null);
        conceptCount = (TextView) conceptRView.findViewById(R.id.main_concept_count);
        conceptView = (TextView) conceptRView.findViewById(R.id.main_concept_view);
        conceptDeliverCount = (TextView) conceptRView.findViewById(R.id.main_concept_ready_count);
        conceptDeliveryView = (TextView) conceptRView.findViewById(R.id.main_concept_ready_view);

        adhocRView = (RelativeLayout) inflater.inflate(R.layout.activity_main_adhoc_new,null);
        adhocListView = (ListView) adhocRView.findViewById(R.id.main_adhoc_new_list);
        adhocNoJobsView = adhocRView.findViewById(R.id.fragment_jobs_list_no_jobs_view);
//        adhocCount = (TextView) adhocRView.findViewById(R.id.main_adhoc_count);
//        adhocView = (TextView) adhocRView.findViewById(R.id.main_adhoc_view);
//        adhocDeliverCount = (TextView) adhocRView.findViewById(R.id.main_adhoc_ready_count);
//        adhocDeliveryView = (TextView) adhocRView.findViewById(R.id.main_adhoc_ready_view);

        messagesRView = (RelativeLayout) inflater.inflate(R.layout.activity_main_messages,null);
    }

    public View getViewAtPosition(int position) {
        View view = null;
        switch (position){
            case CONCEPT_COUNT:
                view = conceptCount;
                break;
            case CONCEPT_VIEW:
                view = conceptView;
                break;
            case CONCEPT_DELIVERY_COUNT:
                view = conceptDeliverCount;
                break;
            case CONCEPT_DELIVERY_VIEW:
                view = conceptDeliveryView;
                break;
            case ADHOC_LISTVIEW:
                view = adhocListView;
                break;
            case ADHOC_NOJOBSVIEW:
                view = adhocNoJobsView;
                break;
//            case ADHOC_COUNT:
//                view = adhocCount;
//                break;
//            case ADHOC_VIEW:
//                view = adhocView;
//                break;
//            case ADHOC_DELIVERY_COUNT:
//                view = adhocDeliverCount;
//                break;
//            case ADHOC_DELIVERY_VIEW:
//                view = adhocDeliveryView;
//                break;
            default:
                break;
        }
        return view;
    }

    @Override
    public Object instantiateItem(ViewGroup collection, int position) {
        RelativeLayout view = null;
        switch (position){
            case 0:
                view = conceptRView;
                break;
            case 1:
                view = adhocRView;
                break;
            case 2:
                view = messagesRView;
                break;
            default:
                break;
        }
        collection.addView(view, 0);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
        collection.removeView((View) view);
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        int title = 0;
        switch (position){
            case 0:
                title = R.string.main_concept;
                break;
            case 1:
                title = R.string.main_adhocs;
                break;
            case 2:
                title = R.string.main_messages;
                break;
            default:
                break;
        }
        return mContext.getString(title);
    }

}
