package com.bupt.edison.autocompletetextviewandpopwindow;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by qianhailong on 17/5/24.
 * 自定义的输入框。<br>
 * 可以自动格式化输入，可以弹出候选列表，还提供了一键清除按钮。这些功能均可通过配置自由开启。<br>
 *
 * <ul>
 * <li>如果通过 app:splitCharacter 属性，设置了分隔符，则表示开启自动格式化功能。否则，不开启。</li>
 * <li>通过 app:showClearIcon 属性，设置是否显示一键清除按钮。默认不显示。</li>
 * <li>如果要使用自动补全的功能，需要结合 {@link BaseFilterAdapter}。继承{@link BaseFilterAdapter}，根据自己的过滤规则，实现其中的getFilterCharSequence方法。并且根据格式化规则(如果开启自动格式化功能)重写getPrefix方法。</li>
 * </ul>
 *
 * <p>有一点需要注意：中文输入，禁止使用自动格式化功能。如果处于中文输入状态下，开启自动格式化功能，可能会导致错误或者崩溃。</p>
 *
 * <p>典型的用途，作为银行卡号的输入框。对银行卡号的自动格式化效果和对光标的控制，与支付宝中的相应控件完全一样。而且还具有自动完全功能，和一键清除功能，综合来说，比支付宝的控件更强大。</p>
 */

public class FormatAutoCompleteTextView extends AppCompatAutoCompleteTextView {
    //以下变量全部是为自动格式化服务，不要修改
    private int preSelectedIndex;
    private CharSequence preInputText;
    private CharSequence postInputText;
    private boolean isInput;
    private int preLength;
    private int curLength;
    private CharSequence virtualVariableChar;
    private CharSequence trueVariableChar;

    //以下变量用于控制一键清除图标
    private boolean hasFocus;
    private boolean isClearIconShowing = false;

    /**
     * 每多少位，插一个分隔符
     */
    private int lengthUnit;
    /**
     * 分隔符
     */
    private String splitChar;
    /**
     * 是否自动分隔
     */
    private boolean isAutoSplit;
    /**
     * 是否自动显示清除图标
     */
    private boolean isAutoShowCloseIcon;
    /**
     * 清除图标
     */
    private Drawable clearIcon;



    public FormatAutoCompleteTextView(Context context) {
        super(context);
        init(context);
    }

    public FormatAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public FormatAutoCompleteTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context) {
        splitChar = "";
        lengthUnit = 4;
        isAutoShowCloseIcon = false;
        initFeature(context);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs,R.styleable.FormatAutoCompleteTextView);
        try {
            splitChar = ta.getString(R.styleable.FormatAutoCompleteTextView_splitCharacter);
            lengthUnit = ta.getInt(R.styleable.FormatAutoCompleteTextView_splitUnit, 4);
            isAutoShowCloseIcon = ta.getBoolean(R.styleable.FormatAutoCompleteTextView_showClearIcon, false);
        } finally {
            ta.recycle();
        }
        initFeature(context);
    }

    private void initFeature(Context context) {
        if (TextUtils.isEmpty(splitChar)) {
            isAutoSplit = false;
            splitChar = "";
        } else {
            if (splitChar.length() > 1) {
                splitChar = String.valueOf(splitChar.charAt(0)); //目前只支持一个分隔符
            }
            if (lengthUnit < 1) {
                lengthUnit = 4;
            }
            isAutoSplit = true;
        }
        if (isAutoSplit) {
            addTextChangedListener(new InputTextWatcher());
        }
        if (isAutoShowCloseIcon) {
            clearIcon = getCompoundDrawables()[2];
            if (null == clearIcon) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    clearIcon = getResources().getDrawable(R.drawable.icon_edittext_delete, context.getTheme());
                } else {
                    clearIcon = getResources().getDrawable(R.drawable.icon_edittext_delete);
                }
            }
            clearIcon.setBounds(0,0, clearIcon.getIntrinsicWidth(), clearIcon.getIntrinsicHeight());
            setOnFocusChangeListener(new ShowIconFocusChangeListener());
            addTextChangedListener(new ShowIconTextWatcher());
        } else {
            setCompoundDrawables(getCompoundDrawables()[0],getCompoundDrawables()[1],null,getCompoundDrawables()[3]); //使drawableRight属性失效
        }
    }

    private class InputTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            virtualVariableChar = s.subSequence(start,start+count);
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        /**
         * 不要尝试重写这个方法。即使是调整内部语句的执行顺序，也可能会导致错误。
         * @param s
         */
        @Override
        public void afterTextChanged(Editable s) {
            CharSequence formatCS = formatCharSequence(s.toString().replaceAll(splitChar,"").trim());
            CharSequence currCS = s.toString();
            postInputText = s.toString();
            if (!TextUtils.equals(preInputText,currCS)) {
                preInputText = formatCS;
                preSelectedIndex = getSelectionStart();
                int sc = postInputText.subSequence(0,preSelectedIndex).toString().replace(splitChar,"").length();
                curLength = formatCS.toString().replace(splitChar,"").trim().length();
                trueVariableChar = virtualVariableChar;
                setText(formatCS);
                isInput = curLength > preLength;
                if (isInput) { //输入字符
                    boolean isAfterSplitChar = false; //排除在分隔符后面插入字符的情况
                    if (preSelectedIndex>=2) {
                        isAfterSplitChar = splitChar.contains(String.valueOf(formatCS.charAt(preSelectedIndex - 2)));
                    }
                    if (preSelectedIndex>= lengthUnit && (sc-1)%lengthUnit == 0 && !isAfterSplitChar) {
                        setSelection(preSelectedIndex + 1); //可以把步进换成splitChar的长度，这样就可以使用双分隔符(或者更多)。
                    } else {
                        setSelection(preSelectedIndex);
                    }
                } else { //删除字符
                    if (preSelectedIndex>= lengthUnit && sc%lengthUnit == 0 && !TextUtils.equals(trueVariableChar,splitChar)) {
                        setSelection(preSelectedIndex - 1); //可以把步进换成splitChar的长度，这样就可以使用双分隔符(或者更多)。
                    } else {
                        try {
                            setSelection(preSelectedIndex); //开启自动格式化功能后，在文本末尾键入回车或空格时，会崩溃。因为，格式化文本的时候，会把末尾的空格、回车都删掉，这样下标就比字符大一个值，会产生数组越界的异常。
                        } catch (IndexOutOfBoundsException e) {
                            e.printStackTrace();
                            setSelection(getText().length());
                        }
                    }
                }
                preLength = curLength;
            }
        }
    }

    private CharSequence formatCharSequence(CharSequence charSequence) {
        StringBuilder builder = new StringBuilder();

        for (int i=0;i<charSequence.length();i++) {
            if (i != 0 && i%lengthUnit == 0) {
                builder.append(splitChar).append(charSequence.charAt(i));
            } else {
                builder.append(charSequence.charAt(i));
            }
        }
        return builder.toString();
    }

    private class ShowIconTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (hasFocus) {
                if (isClearIconShowing) { //控制show icon，提高性能
                    if (TextUtils.isEmpty(s)) {
                        isClearIconShowing = false;
                        setClearIconVisible(isClearIconShowing);
                    }
                } else {
                    isClearIconShowing = s.length() > 0;
                    setClearIconVisible(isClearIconShowing);
                }
            }
        }
    }

    private class ShowIconFocusChangeListener implements OnFocusChangeListener {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            FormatAutoCompleteTextView.this.hasFocus = hasFocus;
            if (hasFocus) {
                setClearIconVisible(getText().length() > 0);
            } else {
                setClearIconVisible(false);
            }
        }
    }

    private void setClearIconVisible(boolean visible) {
        Drawable right = visible ? clearIcon : null;
        setCompoundDrawables(getCompoundDrawables()[0],getCompoundDrawables()[1],right,getCompoundDrawables()[3]);
    }

    /**
     * 没办法为一键清除图标设置点击事件。所以根据点击的位置，模拟它的点击事件
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isAutoShowCloseIcon) {
            if (event.getAction() == MotionEvent.ACTION_UP) { //只根据UP事件判断，可以防止失焦状态下，误触的情况。
                //考虑点击的坐标是否落在一键清除图标上
                float x = event.getX();
                float y = event.getY();
                boolean isInX = (x > (getWidth() - getTotalPaddingRight())) && (x < (getWidth() - getPaddingRight())); //宽度是图标的宽度
                boolean isInY = (y > getPaddingTop()) && (y < (getHeight() - getPaddingBottom())); //高度，仅仅减去上下padding值，不如宽度精确
                if (isInX && isInY) {
                    setText("");
                }
            }
        }
        return super.onTouchEvent(event);
    }

    /**
     * 获得真实的输入内容（去除自动格式化的分隔符）。
     * @return
     */
    public String getRealText() {
        Editable editable = getText();
        if (null != editable) {
            return editable.toString().replace(splitChar,"").trim();
        }
        return "";
    }

    /**
     * 设置内容。会自动把光标定位到文本末尾
     * @param contentText
     */
    public void setContentText(CharSequence contentText) {
        if (TextUtils.isEmpty(contentText)) {
            return;
        }
        setText(contentText);
        int selectedIndex = getText().toString().length();
        setSelection(selectedIndex);
    }

    public int getSplitUnit() {
        if (isAutoSplit) {
            return lengthUnit;
        } else {
            return -1;
        }
    }

    public String getSplitChar() {
        if (isAutoSplit) {
            return splitChar;
        } else {
            return "";
        }
    }

    public boolean isAutoSplit() {
        return isAutoSplit;
    }
}
