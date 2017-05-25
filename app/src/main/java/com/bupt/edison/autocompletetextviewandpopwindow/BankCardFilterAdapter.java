package com.bupt.edison.autocompletetextviewandpopwindow;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by qianhailong on 17/5/17.
 * <p>自动补全的列表的具体Adapter。会根据EditText控件的格式化规则，修改过滤关键字和显示文本。</p>
 */

public class BankCardFilterAdapter extends BaseFilterAdapter<BankCardModel> {
    FormatAutoCompleteTextView formatAutoCompleteTextView;

    public BankCardFilterAdapter(Context context, List<BankCardModel> dataList) {
        super(context, dataList);
    }

    public BankCardFilterAdapter(Context context, List<BankCardModel> dataList, FormatAutoCompleteTextView formatAutoCompleteTextView) {
        super(context, dataList);
        this.formatAutoCompleteTextView = formatAutoCompleteTextView;
    }

    public void setFormatAutoCompleteTextView(FormatAutoCompleteTextView formatAutoCompleteTextView) {
        this.formatAutoCompleteTextView = formatAutoCompleteTextView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.pop_list_item_layout, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.text.setText(formatCharSequence(getItem(position).getBankCardNo()));
        holder.mark.setText(getItem(position).getBankName());
        return convertView;
    }

    /**
     * 根据格式化规则，修改自动补全列表中的显示文本。
     * @param charSequence
     * @return
     */
    private CharSequence formatCharSequence(CharSequence charSequence) {
        if (null == formatAutoCompleteTextView || !formatAutoCompleteTextView.isAutoSplit()) {
            return charSequence;
        }
        StringBuilder builder = new StringBuilder();

        for (int i=0;i<charSequence.length();i++) {
            if (i != 0 && i%formatAutoCompleteTextView.getSplitUnit() == 0) {
                builder.append(formatAutoCompleteTextView.getSplitChar()).append(charSequence.charAt(i));
            } else {
                builder.append(charSequence.charAt(i));
            }
        }
        return builder.toString();
    }

    /**
     * 根据过滤的规则(这里是根据银行卡号过滤)，实现这个方法。
     * @param value
     * @return
     */
    @Override
    protected CharSequence getFilterCharSequence(BankCardModel value) {
        return value==null?"":value.getBankCardNo();
    }

    /**
     * 如果开启了自动格式化功能，返回修改后的关键字。如果没开启，则原样返回。
     * @param prefix
     * @return
     */
    @Override
    protected String getPrefix(CharSequence prefix) {
        if (null == formatAutoCompleteTextView || !formatAutoCompleteTextView.isAutoSplit()) {
            return super.getPrefix(prefix);
        } else {
            return formatAutoCompleteTextView.getRealText();
        }
    }

    static class ViewHolder {
        @Bind(R.id.image)
        ImageView image;
        @Bind(R.id.text)
        TextView text;
        @Bind(R.id.mark)
        TextView mark;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
