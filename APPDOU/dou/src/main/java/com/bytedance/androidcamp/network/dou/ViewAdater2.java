package com.bytedance.androidcamp.network.dou;


import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class ViewAdater2 extends PagerAdapter {
    private List<View> datas = new ArrayList<View>();

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view;
        if(position<10)
            view = datas.get(position*2);
        else
            view = datas.get((position*2+1)%10);
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if(position<=4)
            container.removeView(datas.get(position*2));
        else
            container.removeView(datas.get((position*2+1)%10));


    }

    public void setDatas(List<View> list) {
        datas = list;
        notifyDataSetChanged();
    }
}
