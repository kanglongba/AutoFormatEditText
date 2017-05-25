package com.bupt.edison.autocompletetextviewandpopwindow;

import android.content.Context;
import android.text.TextUtils;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by qianhailong on 17/5/23.
 * <p>自动补全列表的基类Adapter。</p>
 */

public abstract class BaseFilterAdapter<T> extends BaseAdapter implements Filterable {
    private final Object mLock = new Object();
    private List<T> dataList;
    private ArrayList<T> mOriginalValues;
    private BankFilter bankFilter;
    protected Context context;

    public BaseFilterAdapter(Context context, List<T> dataList) {
        this.dataList = dataList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return dataList == null ? 0 : dataList.size();
    }

    @Override
    public T getItem(int position) {
        return dataList == null ? null : dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setDataList(List<T> dataList) {
        this.dataList = dataList;
    }

    /**
     * 从被过滤的数据中，取出用于过滤的字符串。
     * 举个列子，我们要从一个班级中，过滤出姓赵的同学。那么 T  代表一个学生，
     * 每个学生有很多属性（姓，名，身高，性别等），但是姓氏才是主要的过滤标准，
     * 所以，就返回这个学生的姓氏。
     * @param value
     * @return 用于比较的字符串（过滤标准，学生的姓氏）
     */
    protected abstract CharSequence getFilterCharSequence(T value);

    /**
     * 获取过滤的关键字。
     * 一般为用户输入的数据，但是如果控件有自动格式化的功能，
     * 需要根据格式化的规则，处理一下关键字。这时需要重写这个方法。
     * @param prefix
     * @return
     */
    protected String getPrefix(CharSequence prefix) {
        if (TextUtils.isEmpty(prefix)) {
            return "";
        }
        return prefix.toString().toLowerCase();
    }

    @Override
    public Filter getFilter() {
        if (bankFilter == null) {
            bankFilter = new BankFilter();
        }
        return bankFilter;
    }

    private class BankFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();

            if (mOriginalValues == null) {
                synchronized (mLock) {
                    mOriginalValues = new ArrayList<>(dataList);
                }
            }

            if (prefix == null || prefix.length() == 0) {
                final ArrayList<T> list;
                synchronized (mLock) {
                    list = new ArrayList<>(mOriginalValues);
                }
                results.values = list;
                results.count = list.size();
            } else {
                final String prefixString = getPrefix(prefix);

                final ArrayList<T> values;
                synchronized (mLock) {
                    values = new ArrayList<>(mOriginalValues);
                }

                final int count = values.size();
                final ArrayList<T> newValues = new ArrayList<>();

                for (int i = 0; i < count; i++) {
                    final T value = values.get(i);
                    final String valueText = getFilterCharSequence(value).toString().toLowerCase();

                    // First match against the whole, non-splitted value
                    if (valueText.startsWith(prefixString)) {
                        newValues.add(value);
                    } else {
                        final String[] words = valueText.split(" ");
                        for (String word : words) {
                            if (word.startsWith(prefixString)) {
                                newValues.add(value);
                                break;
                            }
                        }
                    }
                }

                results.values = newValues;
                results.count = newValues.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            dataList = (List<T>) results.values;
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }
}
