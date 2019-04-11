package com.bottomtab.bottomtab;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/*
 * Created by qianli.ma on 2019/4/4 0004.
 */
public class BottomTab extends LinearLayout {

    private final AttributeSet attrs;
    private Context context;
    private int item_count = 1;// 默认item个数: 1个
    private int images_res[] = {};// 非选中图片资源
    private int string_res[] = {};// 非选中字符资源

    private float DEFAULT_WIDGET_WIDTH = 100;// 默认100dp
    private float DEFAULT_WIDGET_HEIGHT = 75;// 默认75dp
    private float ITEM_IV_SCALE_WIDTH_PERCENT = 0.5f;// 默认0.5f
    private float ITEM_IV_SCALE_HEIGHT_PERCENT = 0.5f;// 默认0.5f
    private String ITEM_TV_COLOR_UNCHECK = "#CCCCCC";// 默认灰色(非选中)
    private String ITEM_TV_COLOR_CHECK = "#009688";// 默认兰色(选中)
    private String ITEM_IV_CHECK_COLOR = "#009688";// 默认兰色(选中)
    private int ITEM_TV_MARGIN_TOP = 5;// 默认5PX
    private float ITEM_TV_SIZE_PERCENT = 0.15f;// 默认0.15f
    private int ITEM_DEFAULT_POSITION = 0;// 默认position = 0

    private float item_iv_scale_width_percent = ITEM_IV_SCALE_WIDTH_PERCENT; // 图标缩放比例(宽度)
    private float item_iv_scale_height_percent = ITEM_IV_SCALE_HEIGHT_PERCENT; // 图标缩放比例(高度)
    private boolean item_iv_scale_same;// 是否采用相同比例(基于高度:true则 宽度 = 高度)
    private float item_tv_size_percent = ITEM_TV_SIZE_PERCENT; // 字体大小比例
    private int item_tv_margin_top = ITEM_TV_MARGIN_TOP; // 字体margintop
    private float widget_width = DEFAULT_WIDGET_WIDTH;// 默认100dp
    private float widget_height = DEFAULT_WIDGET_HEIGHT;// 默认75dp
    private String item_tv_color_uncheck = ITEM_TV_COLOR_UNCHECK; // 字体默认颜色(灰色)
    private String item_tv_color_check = ITEM_TV_COLOR_CHECK; // 字体选中颜色(兰色)
    private String item_iv_check_color = ITEM_IV_CHECK_COLOR;// 图标选中时渲染颜色(兰色)
    private int item_default_position = ITEM_DEFAULT_POSITION;// 默认选中第1个

    private boolean isObtainParentSize;// 是否已经获取到父布局宽高(@onMeasure)
    private boolean isCheckAttrFinish = false;// 是否校验属性完毕(@create)
    private int screenWidth;// 屏幕宽度
    private int screenHeight;// 屏幕高度
    private String layout_widthPercent;// 百分比属性宽
    private String layout_heightPercent;// 百分比属性高
    private String layout_width;// 自带属性宽
    private String layout_height;// 自带属性高
    private String appSpace;// 百分比命名空间
    private String andriodSpace;// 自带命名空间

    private int temp_currentItemPosition = 0;// 临时记录当前选中位标

    public BottomTab(Context context) {
        this(context, null, 0);
    }

    public BottomTab(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BottomTab(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        this.attrs = attrs;
        /* 注意获取属性的步骤一定要放在构造方法里, 如果放在onMeasure里会报空指针错误 */
        // 0.获取屏幕宽高
        ScreenSize.SizeBean size = ScreenSize.getSize(context);
        screenWidth = size.width;
        screenHeight = size.height;
        // 1.配置空间命名
        appSpace = "http://schemas.android.com/apk/res-auto";// 百分比
        andriodSpace = "http://schemas.android.com/apk/res/android"; // android自带
        // 2.获取属性
        layout_widthPercent = attrs.getAttributeValue(appSpace, "layout_widthPercent");
        layout_heightPercent = attrs.getAttributeValue(appSpace, "layout_heightPercent");
        layout_width = attrs.getAttributeValue(andriodSpace, "layout_width");
        layout_height = attrs.getAttributeValue(andriodSpace, "layout_height");
        // 3.其他属性配置
        item_iv_scale_width_percent = attrs.getAttributeFloatValue(appSpace, "item_iv_scale_width_percent", item_iv_scale_width_percent);
        item_iv_scale_height_percent = attrs.getAttributeFloatValue(appSpace, "item_iv_scale_height_percent", item_iv_scale_height_percent);
        item_tv_size_percent = attrs.getAttributeFloatValue(appSpace, "item_tv_size_percent", item_tv_size_percent);
        item_iv_scale_same = attrs.getAttributeBooleanValue(appSpace, "item_iv_scale_same", false);
        item_tv_color_uncheck = attrs.getAttributeValue(appSpace, "item_tv_color_uncheck");
        item_tv_color_check = attrs.getAttributeValue(appSpace, "item_tv_color_check");
        item_tv_margin_top = attrs.getAttributeIntValue(appSpace, "item_tv_margin_top", item_tv_margin_top);
        item_iv_check_color = attrs.getAttributeValue(appSpace, "item_iv_check_color");
        item_default_position = attrs.getAttributeIntValue(appSpace, "item_default_position", item_default_position);
        Log.v(getClass().getSimpleName(), "BottomTab constuct");
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // 1.获取父布局宽高
        ViewGroup mViewGroup = (ViewGroup) getParent();
        if (mViewGroup != null & !isObtainParentSize) {
            int mParentWidth = mViewGroup.getWidth();
            int mParentHeight = mViewGroup.getHeight();
            // 2.如果已经获取到父布局的值--> 则停止获取(因为onMeasure会一直获取,直到测量完成为止)
            if (mParentWidth != 0 & mParentHeight != 0) {
                isObtainParentSize = true;
            }
            // 3.如果已经获取到了--> 执行业务逻辑
            if (isObtainParentSize) {
                Log.v(getClass().getSimpleName(), "onMeasure :mParentWidth: " + mParentWidth + ";" + "mParentHeight: " + mParentHeight);
                initAttribute(mParentWidth, mParentHeight);
            }
        }
    }

    /**
     * 初始化属性定义
     *
     * @param mParentWidth  父布局宽
     * @param mParentHeight 父布局高
     */
    private void initAttribute(int mParentWidth, int mParentHeight) {
        Log.v(getClass().getSimpleName(), "Method--> " + getClass().getSimpleName() + ":initAttribute()");
        // 1.条件判断
        getWidgetWidth(mParentWidth);
        getWidgetHeight(mParentHeight);
        // 2.检查属性值范围
        checkAttrsValue();
    }


    /**
     * 根据属性类型设定控件宽度
     *
     * @param mParentWidth 父布局宽度
     */
    private void getWidgetWidth(int mParentWidth) {
        Log.v(getClass().getSimpleName(), "Method--> " + getClass().getSimpleName() + ":getWidgetWidth()");
        if (layout_widthPercent != null) {
            String[] split = layout_widthPercent.split("%");
            if (split.length == 1) {// 不带［w］或者［sw］
                float percent = Float.valueOf(split[0]) / 100;
                widget_width = mParentWidth * percent;

            } else if (split.length == 2) {
                float percent = Float.valueOf(split[0]) / 100;
                String unit = split[1];
                if (unit.equalsIgnoreCase("w")) {
                    widget_width = mParentWidth * percent;

                } else if (unit.equalsIgnoreCase("sw")) {
                    widget_width = screenWidth * percent;
                }
            }
        } else {
            if (layout_width.equalsIgnoreCase("-1")) {// match parent
                widget_width = mParentWidth;
            } else if (layout_width.contains("dip")) {
                widget_width = Float.valueOf(layout_width.replace("dip", "").replace(" ", ""));
                widget_width = DipPx.dip2px(context, widget_width);
            } else if (layout_width.contains("px")) {
                widget_width = Float.valueOf(layout_width.replace("px", "").replace(" ", ""));
            }
        }
    }

    /**
     * 根据属性类型设定控件宽度
     *
     * @param mParentHeight 父布局高度
     */
    private void getWidgetHeight(int mParentHeight) {
        Log.v(getClass().getSimpleName(), "Method--> " + getClass().getSimpleName() + ":getWidgetHeight()");
        if (layout_heightPercent != null) {
            String[] split = layout_heightPercent.split("%");
            if (split.length == 1) {// 不带［h］或者［sh］
                float percent = Float.valueOf(split[0]) / 100;
                widget_height = mParentHeight * percent;

            } else if (split.length == 2) {
                float percent = Float.valueOf(split[0]) / 100;
                String unit = split[1];
                if (unit.equalsIgnoreCase("h")) {
                    widget_height = mParentHeight * percent;

                } else if (unit.equalsIgnoreCase("sh")) {
                    widget_height = screenHeight * percent;
                }
            }
        } else {
            if (layout_height.equalsIgnoreCase("-1")) {// match parent
                widget_height = mParentHeight;
            } else if (layout_height.contains("dip")) {
                widget_height = Float.valueOf(layout_height.replace("dip", "").replace(" ", ""));
                widget_height = DipPx.dip2px(context, widget_height);
            } else if (layout_height.contains("px")) {
                widget_height = Float.valueOf(layout_height.replace("px", "").replace(" ", ""));
            }
        }
    }

    /* -------------------------------------------- private -------------------------------------------- */

    /**
     * 检查属性值范围
     */
    private void checkAttrsValue() {
        Log.v(getClass().getSimpleName(), "Method--> " + getClass().getSimpleName() + ":checkAttrsValue()");
        // widget宽度限定
        if (widget_width <= 0) {
            widget_width = DEFAULT_WIDGET_WIDTH;
        } else if (widget_width > screenWidth) {
            widget_width = screenWidth;
        }

        // widget高度限定
        if (widget_height <= 0) {
            widget_height = DEFAULT_WIDGET_HEIGHT;
        } else if (widget_height > screenHeight) {
            widget_height = screenHeight;
        }

        // 图形宽度缩放
        if (item_iv_scale_width_percent <= 0) {
            item_iv_scale_width_percent = ITEM_IV_SCALE_WIDTH_PERCENT;
        } else if (item_iv_scale_width_percent > 1) {
            item_iv_scale_width_percent = 1f;
        }

        // 图形高度缩放
        if (item_iv_scale_height_percent <= 0) {
            item_iv_scale_height_percent = ITEM_IV_SCALE_HEIGHT_PERCENT;
        } else if (item_iv_scale_height_percent > 1) {
            item_iv_scale_height_percent = 1f;
        }

        // 字体大小缩放
        if (item_tv_size_percent <= 0) {
            item_tv_size_percent = ITEM_TV_SIZE_PERCENT;
        } else if (item_tv_size_percent > 1) {
            item_tv_size_percent = 1f;
        }

        // 字体颜色(非选中)
        if (TextUtils.isEmpty(item_tv_color_uncheck)) {
            item_tv_color_uncheck = ITEM_TV_COLOR_UNCHECK;
        } else if (!item_tv_color_uncheck.startsWith("#")) {
            item_tv_color_uncheck = ITEM_TV_COLOR_UNCHECK;
        } else if (item_tv_color_uncheck.length() > 9) {
            item_tv_color_uncheck = ITEM_TV_COLOR_UNCHECK;
        } else if (!item_tv_color_uncheck.contains("#")) {
            item_tv_color_uncheck = ITEM_TV_COLOR_UNCHECK;
        }

        // 字体颜色(选中)
        if (TextUtils.isEmpty(item_tv_color_check)) {
            item_tv_color_check = ITEM_TV_COLOR_CHECK;
        } else if (!item_tv_color_check.startsWith("#")) {
            item_tv_color_check = ITEM_TV_COLOR_CHECK;
        } else if (item_tv_color_check.length() > 9) {
            item_tv_color_check = ITEM_TV_COLOR_CHECK;
        } else if (!item_tv_color_check.contains("#")) {
            item_tv_color_check = ITEM_TV_COLOR_CHECK;
        }

        // 图标选中渲染颜色
        if (TextUtils.isEmpty(item_iv_check_color)) {
            item_iv_check_color = ITEM_IV_CHECK_COLOR;
        } else if (!item_tv_color_check.startsWith("#")) {
            item_iv_check_color = ITEM_IV_CHECK_COLOR;
        } else if (item_tv_color_check.length() > 9) {
            item_iv_check_color = ITEM_IV_CHECK_COLOR;
        } else if (!item_tv_color_check.contains("#")) {
            item_iv_check_color = ITEM_IV_CHECK_COLOR;
        }

        // 字体margintop
        if (item_tv_margin_top < 0) {
            item_tv_margin_top = ITEM_TV_MARGIN_TOP;
        } else if (item_tv_margin_top > widget_height) {
            item_tv_margin_top = (int) widget_height;
        }

        // 默认选中的position
        if (item_default_position < 0) {
            item_default_position = ITEM_DEFAULT_POSITION;
        }

        // 检测属性完毕
        isCheckAttrFinish = true;
    }

    /**
     * 资源校对
     *
     * @param images_res 图片资源(非选中)
     * @param string_res 字符资源(非选中)
     * @return 资源封装
     */
    private ResBean getResBean(int[] images_res,// 未选中图标资源   
                               int[] string_res// 未选中字符资源  
    ) {
        ResBean resBean = new ResBean();
        /* -------------------------------------------- 非选中部分 -------------------------------------------- */
        // 1.全部为null,提示
        if (images_res == null & string_res == null) {
            return null;
        }

        // 2.其中一个为空
        if (images_res == null | string_res == null) {
            resBean.imgRes = images_res;
            resBean.strRes = string_res;
            return resBean;
        }

        // 3.长度比较(img > str)
        if (images_res.length > string_res.length) {
            int[] newStr_res = new int[images_res.length];
            System.arraycopy(string_res, 0, newStr_res, 0, string_res.length);
            int count = images_res.length - string_res.length;
            for (int i = 0; i < count; i++) {
                newStr_res[string_res.length + i] = -1;
            }
            resBean.imgRes = images_res;
            resBean.strRes = newStr_res;
            return resBean;
        }

        // 4.长度比较
        if (images_res.length < string_res.length) {
            int[] newImg_res = new int[string_res.length];
            System.arraycopy(images_res, 0, newImg_res, 0, images_res.length);
            int count = string_res.length - images_res.length;
            for (int i = 0; i < count; i++) {
                newImg_res[images_res.length + i] = -1;
            }
            resBean.imgRes = newImg_res;
            resBean.strRes = string_res;
            return resBean;
        }

        // 5.正常情况
        resBean.imgRes = images_res;
        resBean.strRes = string_res;
        return resBean;
    }

    /**
     * 创建底版
     */
    private void createBottomPanel() {

        // 1.计算每个item的平均宽度以及高度
        int itemWidth = (int) (widget_width * 1f / item_count);
        int itemHeight = (int) widget_height;
        Log.v(getClass().getSimpleName(), "screenWidth: " + screenWidth + "; screenHeight: " + screenHeight);
        Log.v(getClass().getSimpleName(), "itemWidth: " + itemWidth + "; itemHeight: " + itemHeight);

        // 2.设置控件总布局
        LinearLayout allLayout = new LinearLayout(context);
        LayoutParams allLP = new LayoutParams(-1, (int) widget_height);
        allLayout.setOrientation(HORIZONTAL);
        allLayout.setLayoutParams(allLP);
        addView(allLayout);

        final List<LinearLayout> itemLayouts = new ArrayList<>();
        // 3.循环创建item
        for (int i = 0; i < item_count; i++) {
            LinearLayout itemLayout = new LinearLayout(context);
            LayoutParams itemLP = new LayoutParams(i != item_count - 1 ? itemWidth : -1, -1);
            itemLP.gravity = Gravity.CENTER;
            itemLayout.setOrientation(VERTICAL);
            itemLayout.setGravity(Gravity.CENTER);
            itemLayout.setLayoutParams(itemLP);
            itemLayout.setTag("itemlayout=" + i);
            allLayout.addView(itemLayout);
            itemLayouts.add(itemLayout);

            // 4.创建imageview
            if (images_res != null) {
                if (images_res != null & images_res[i] > 0) {
                    ImageView imageView = new ImageView(context);
                    // 4.1.正常取得宽高
                    int ivHeight = (int) (itemHeight * item_iv_scale_height_percent);
                    int ivWidth = (int) (itemWidth * item_iv_scale_width_percent);
                    // 4.2.判断用户是否使用了相同模式 item_iv_scale_same = true --> 取得宽高中的最小值作为等值
                    ivHeight = item_iv_scale_same ? (ivHeight > ivWidth ? ivWidth : ivHeight) : ivHeight;
                    ivWidth = item_iv_scale_same ? (ivWidth > ivHeight ? ivHeight : ivWidth) : ivWidth;
                    LayoutParams ivLP = new LayoutParams(ivWidth, ivHeight);
                    setDefaultBitmap(i, imageView);
                    imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                    imageView.setLayoutParams(ivLP);
                    itemLayout.addView(imageView);
                }
            }

            // 5.创建textview
            if (string_res != null) {
                if (string_res != null & string_res[i] > 0) {
                    TextView textView = new TextView(context);
                    LayoutParams tvLP = new LayoutParams(-2, -2);
                    tvLP.topMargin = item_tv_margin_top;
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, itemHeight * item_tv_size_percent);
                    textView.setText(string_res[i]);
                    setDefaultTextcolor(i, textView);
                    textView.setLayoutParams(tvLP);
                    textView.setMaxLines(1);
                    textView.setLines(1);
                    itemLayout.addView(textView);
                }
            }
        }

        // 6.设置点击监听
        for (LinearLayout itemLayout : itemLayouts) {
            itemLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    String tag = (String) v.getTag();
                    int currentIndex = Integer.valueOf(tag.split("=")[1]);
                    Log.v(BottomTab.this.getClass().getSimpleName(), "click index: " + currentIndex);
                    for (int i = 0; i < itemLayouts.size(); i++) {
                        // 6.1.得到单个itemlayout
                        LinearLayout linearLayout = itemLayouts.get(i);
                        int childCount = linearLayout.getChildCount();
                        // 6.2.同时有imageview和textview
                        if (childCount == 2) {
                            // 6.3.设置imageview
                            /* 注: 一定要将drawable转换成bitmap才进行设置, 否则用户如果使用同一个R.drawable.xx在渲染时就会出现覆盖 */
                            ImageView iv = (ImageView) (linearLayout.getChildAt(0));
                            if (i == currentIndex) {
                                iv.getDrawable().setColorFilter(Color.parseColor(item_iv_check_color), PorterDuff.Mode.SRC_ATOP);
                                iv.setImageBitmap(draw2Bitmap(iv.getDrawable()));
                            } else {
                                iv.setImageBitmap(draw2Bitmap(Objects.requireNonNull(ContextCompat.getDrawable(context, images_res[i]))));
                            }

                            // 6.4.设置textview
                            TextView tv = (TextView) (linearLayout.getChildAt(1));
                            tv.setTextColor(i == currentIndex ? Color.parseColor(item_tv_color_check) : Color.parseColor(item_tv_color_uncheck));
                        }

                        // 6.2.只有一个
                        if (childCount == 1) {
                            View childAt = linearLayout.getChildAt(0);
                            if (childAt instanceof ImageView) {// 图片
                                ImageView iv = (ImageView) childAt;
                                if (i == currentIndex) {
                                    iv.getDrawable().setColorFilter(Color.parseColor(item_iv_check_color), PorterDuff.Mode.SRC_ATOP);
                                    iv.setImageBitmap(draw2Bitmap(iv.getDrawable()));
                                } else {
                                    iv.setImageBitmap(draw2Bitmap(Objects.requireNonNull(ContextCompat.getDrawable(context, images_res[i]))));
                                }
                            }

                            if (childAt instanceof TextView) {// 文本
                                TextView tv = (TextView) childAt;
                                tv.setTextColor(i == currentIndex ? Color.parseColor(item_tv_color_check) : Color.parseColor(item_tv_color_uncheck));
                            }
                        }
                    }

                    // 点击的位标 != 当前已选中的位标 --> 才回调给外部
                    if (currentIndex != temp_currentItemPosition) {
                        // 7.点击回调监听
                        Log.i(BottomTab.this.getClass().getSimpleName(), "item click position: " + currentIndex);
                        bottomTabFinishNext(currentIndex);// 返回当前选中的位标
                        temp_currentItemPosition = currentIndex;// 更新临时位标
                    }

                }
            });
        }

        // 7.完成时回调监听
        Log.i(BottomTab.this.getClass().getSimpleName(), "Bottomtab had inflate finish");
        bottomTabFinishNext(item_default_position);// 返回当前选中的位标
    }

    /**
     * 设置默认选中的UI
     *
     * @param i         当前遍历到的位标
     * @param imageView 图标
     */
    private void setDefaultBitmap(int i, ImageView imageView) {
        /* 注: 一定要将drawable转换成bitmap才进行设置, 否则用户如果使用同一个R.drawable.xx在渲染时就会出现覆盖 */
        Bitmap defaultBitmap = draw2Bitmap(getResources().getDrawable(images_res[i]));
        imageView.setImageBitmap(defaultBitmap);
        // 初始化选中
        if (i == item_default_position) {
            imageView.getDrawable().setColorFilter(Color.parseColor(item_iv_check_color), PorterDuff.Mode.SRC_ATOP);
            imageView.setImageBitmap(draw2Bitmap(imageView.getDrawable()));
        }
    }

    /**
     * 设置默认选中字体色
     *
     * @param i        当前遍历到的位标
     * @param textView 文本
     */
    private void setDefaultTextcolor(int i, TextView textView) {
        try {
            textView.setTextColor(Color.parseColor(item_tv_color_uncheck));
        } catch (Exception e) {
            textView.setTextColor(Color.parseColor(ITEM_TV_COLOR_UNCHECK));
        }
        if (i == item_default_position) {
            textView.setTextColor(Color.parseColor(item_tv_color_check));
        }
    }

    /**
     * drawable 转换 bitmap
     *
     * @param drawable 图原
     * @return 位图
     */
    private Bitmap draw2Bitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * 辅助类
     */
    public class ResBean {
        public int[] imgRes;  // 未选中图标资源
        public int[] strRes;   // 未选中字符资源
    }

    /* -------------------------------------------- public -------------------------------------------- */

    /**
     * 开始创建
     *
     * @param images_res 图片资源(非选中)
     * @param string_res 字符资源(非选中)
     */
    public void create(final int[] images_res, final int[] string_res) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 轮询去检查是否已经完成了初始化--> 完成, 则开始绘制
                while (true) {
                    if (isCheckAttrFinish) {
                        break;
                    }
                }
                Log.v("bottomtab", "bottom create");
                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 0.初始化资源并检测资源合理性
                        ResBean resBean = getResBean(images_res, string_res);
                        if (resBean != null) {
                            // 1.在获取到资源个数后补充校验并调整item_default_position的合理性
                            item_default_position = item_default_position > resBean.imgRes.length ? resBean.imgRes.length - 1 : item_default_position;
                            temp_currentItemPosition = item_default_position;// 赋值给临时位标
                            // 2.设置参数
                            BottomTab.this.images_res = resBean.imgRes;
                            BottomTab.this.string_res = resBean.strRes;
                            // 3.设置参数
                            item_count = BottomTab.this.images_res.length;
                            // 4.开始创建
                            createBottomPanel();
                        } else {
                            Toast.makeText(context, "至少传入一组数据", Toast.LENGTH_LONG * 2500).show();
                        }
                    }
                });

            }
        }).start();
    }

    /* -------------------------------------------- impl -------------------------------------------- */

    private OnBottomTabFinishListener onBottomTabFinishListener;

    // Inteerface--> 接口OnBottomTabFinishListener
    public interface OnBottomTabFinishListener {
        void bottomTabFinish(int position);
    }

    // 对外方式setOnBottomTabFinishListener
    public void setOnBottomTabFinishListener(OnBottomTabFinishListener onBottomTabFinishListener) {
        this.onBottomTabFinishListener = onBottomTabFinishListener;
    }

    // 封装方法bottomTabFinishNext
    private void bottomTabFinishNext(int position) {
        if (onBottomTabFinishListener != null) {
            onBottomTabFinishListener.bottomTabFinish(position);
        }
    }

    private OnBottomTabItemClickListener onBottomTabItemClickListener;

    // Inteerface--> 接口OnBottomTabItemClickListener
    public interface OnBottomTabItemClickListener {
        void bottomTabItemClick(int position);
    }

    // 对外方式setOnBottomTabItemClickListener
    public void setOnBottomTabItemClickListener(OnBottomTabItemClickListener onBottomTabItemClickListener) {
        this.onBottomTabItemClickListener = onBottomTabItemClickListener;
    }

    // 封装方法bottomTabItemClickNext
    private void bottomTabItemClickNext(int position) {
        if (onBottomTabItemClickListener != null) {
            onBottomTabItemClickListener.bottomTabItemClick(position);
        }
    }

}
