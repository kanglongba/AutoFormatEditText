# Android - a powerful EditText that can auto format input text  

## 效果图
强大的输入控件，每四位插入一个分隔符，补全提示，一键清除。
 
![](https://github.com/kanglongba/AutoFormatEditText/blob/master/screenshot/autoformatedittext.gif)

## 简介
FormatAutoCompleteTextView控件继承自AutoCompleteTextView，本质上是一个EditText。它具有如下功能：  

* 自动格式化输入文本  
* 提示自动补全列表
* 一键清除所有文本

上述功能都可通过配置自由开启。

## 使用方法
* 开启自动格式化输入文本功能  

  这里的自动格式化形式是，每隔多少位，插入一个分隔符。例如，**1234 5678 9012**这种形式。  
  所以，如果要开启自动格式化功能，必须通过 ***app:splitCharacter*** 属性，设置分隔符(可以是除输入文本以外的任何值)。如果没有设置分隔符，则不开启此功能。  
  除此之外，还可以通过 ***app:splitUnit*** 属性，设置间隔，默认是4。  
  
* 开启自动补全列表

  因为控件继承自AutoCompleteTextView，所以天生就具有自动补全功能，但是这里做了扩展。结合BaseFilterAdapter，可以自定义过滤规则和展示各种形式的候选列表。  
  一般来说，需要继承BaseFilterAdapter，根据自己的过滤规则，实现其中的getFilterCharSequence方法。并且根据格式化规则(如果开启自动格式化功能)重写getPrefix方法。
  
* 一键清除功能

  一键清除功能，利用了 ***android:drawableRight*** 属性，通过它设置清除图标。  
  通过 ***app:showClearIcon*** 属性，开启一键清除功能。如果没有通过它显式开启，即使设置了 ***android:drawableRight*** ，也不会有任何效果。
  
更详细的使用方法，可以参考本工程的示例代码。

## 注意事项

中文输入，禁止使用自动格式化功能。如果处于中文输入状态下，开启自动格式化功能，可能会导致错误或者崩溃。

## 应用

典型的用途，作为银行卡号的输入框。对银行卡号的自动格式化效果和对光标的控制，与支付宝中的相应控件完全一样。而且还具有自动补全功能，和一键清除功能，综合来说，比支付宝的控件更强大。 


