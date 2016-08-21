package com.zgh.pagegridview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * 可分页的gridview
 * Created by zhuguohui on 2016/8/20 0020.
 */
public class PageGridView extends RecyclerView {
    private int mRows = 0;
    private int mColums = 0;
    private int mPageSize = 0;
    private int mOnePageSize = 0;
    private int mWidth = -1;
    //是否需要重排序
    private boolean needReorder = false;


    public PageGridView(Context context) {
        this(context, null);
    }

    public PageGridView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PageGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        //根据行数和列数判断是否
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.PageGridView);
        mRows = array.getInteger(R.styleable.PageGridView_PagingRows, 0);
        mColums = array.getInteger(R.styleable.PageGridView_PagingColums, 0);
        Drawable diver = array.getDrawable(R.styleable.PageGridView_PagingDiver);
        if (mRows < 0 || mColums < 0) {
            throw new RuntimeException("行数或列数不能为负数");
        }
        if (mRows == 0 && mColums == 0) {
            throw new RuntimeException("行数和列数不能都为0");
        }
        LayoutManager layoutManager;
        if (mRows > 0) {
            if (mColums > 0) {
                needReorder = true;
                //设置滚动监听器
                addOnScrollListener(new PagingScrollListener());
            }
            layoutManager = new StaggeredGridLayoutManager(mRows, HORIZONTAL);
        } else {
            layoutManager = new StaggeredGridLayoutManager(mColums, VERTICAL);
        }
        array.recycle();
        setLayoutManager(layoutManager);
        //添加分割线
        if (diver != null) {
            addItemDecoration(new DividerGridItemDecoration(diver));
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        mWidth = getWidth();
    }

    @Override
    public final void setAdapter(Adapter adapter) {
        //对数据进行重排序
        if (needReorder) {
            if (!(adapter instanceof PagingAdapter)) {
                throw new RuntimeException("must use PagingAdapter");
            }
            PagingAdapter pagingAdapter = (PagingAdapter) adapter;
            List data = pagingAdapter.getData();
            List formatData = new ArrayList();

            //  Collections.copy(formatData, data);
            mOnePageSize = mRows * mColums;
            mPageSize = data.size() / mOnePageSize;
            if (data.size() % mOnePageSize != 0) {
                mPageSize++;
            }
            for (int p = 0; p < mPageSize; p++) {
                for (int c = 0; c < mColums; c++) {
                    for (int r = 0; r < mRows; r++) {
                        int index = c + r * mColums + p * mOnePageSize;
                        if (index > data.size() - 1) {
                            formatData.add(pagingAdapter.getEmpty());
                        } else {
                            formatData.add(data.get(index));
                        }
                    }
                }
            }
            data.clear();
            data.addAll(formatData);
        }
        super.setAdapter(adapter);
        if (pageIndicator != null&&pageIndicaotrNeedInit) {
            pageIndicator.InitIndicatorItems(mPageSize);
            pageIndicator.onPageSelected(0);
            pageIndicaotrNeedInit=false;
        }
        if (onPageChangeListenerList != null) {
            for (OnPageChangeListener listener : onPageChangeListenerList) {
                listener.onPageChanged(0);
            }
        }
    }


    int dX, dY;
    long dTime;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (onItemClickListener != null) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    dX = (int) ev.getRawX();
                    dY = (int) ev.getRawY();
                    dTime = System.currentTimeMillis();

                    break;
                case MotionEvent.ACTION_UP:
                    int mx = (int) Math.abs(ev.getRawX() - dX);
                    int my = (int) Math.abs(ev.getRawY() - dY);
                    int time = (int) (System.currentTimeMillis() - dTime);
                    if (mx <= 10 && my <= 10 && time < 200) {
                        int position = getPositionByXY((int) ev.getRawX(), (int) ev.getRawY());
                        if (position != -1) {
                            onItemClickListener.onItemClick(this, position);
                        }
                    }
                    break;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private int getPositionByXY(int x, int y) {
        int position = -1;
        Rect rect = new Rect();
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            view.getGlobalVisibleRect(rect);
            if (rect.contains(x, y)) {
                position = i;
                break;
            }
        }
        if (mRows > 0) {
            int offset = getChildPosition(getLayoutManager().getChildAt(0));
            position += offset;
        }
        return position;
    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }

    private PageIndicator pageIndicator;
    //需要初始化pageIndicator
    private boolean pageIndicaotrNeedInit = false;

    public void setPageIndicator(PageIndicator pageIndicator) {
        this.pageIndicator = pageIndicator;
        pageIndicaotrNeedInit = true;
        if (getAdapter() != null && needReorder) {
            pageIndicator.InitIndicatorItems(mPageSize);
            pageIndicator.onPageSelected(currentPage);
            pageIndicaotrNeedInit = false;
        }
    }

    //分页指示器
    public interface PageIndicator {

        void InitIndicatorItems(int itemsNumber);

        void onPageSelected(int pageIndex);

        void onPageUnSelected(int pageIndex);
    }

    private List<OnPageChangeListener> onPageChangeListenerList;

    public interface OnPageChangeListener {
        void onPageChanged(int index);
    }

    public void addOnPageChangeListener(OnPageChangeListener listener) {
        if (onPageChangeListenerList == null) {
            onPageChangeListenerList = new ArrayList();
        }
        onPageChangeListenerList.add(listener);
    }

    public void removeOnPageChangeListener(OnPageChangeListener listener) {
        if (onPageChangeListenerList != null) {
            onPageChangeListenerList.remove(listener);
        }
    }


    public int getPageSize() {
        return mPageSize;
    }

    public interface OnItemClickListener {
        void onItemClick(PageGridView pageGridView, int position);
    }

    public static abstract class PagingAdapter<VH extends ViewHolder> extends Adapter<VH> {
        /**
         * 获取数据集
         *
         * @return
         */
        public abstract List getData();

        /**
         * 获取空对象
         *
         * @return
         */
        public abstract Object getEmpty();
    }


    int scrollX = 0;
    boolean isAuto = false;
    int Target = 0;
    int currentPage = 0;
    int lastPage = 0;

    public class PagingScrollListener extends RecyclerView.OnScrollListener {


        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

            if (newState == 0) {
                if (!isAuto) {
                    int p = scrollX / getWidth();
                    int offset = scrollX % getWidth();
                    if (offset > getWidth() / 2) {
                        p++;
                    }
                    Target = p * getWidth();
                    isAuto = true;
                    currentPage = p;
                    if (pageIndicator != null) {
                        pageIndicator.onPageUnSelected(lastPage);
                        pageIndicator.onPageSelected(currentPage);
                    }
                    if (onPageChangeListenerList != null) {
                        for (OnPageChangeListener listener : onPageChangeListenerList) {
                            listener.onPageChanged(currentPage);
                        }
                    }
                    recyclerView.smoothScrollBy(Target - scrollX, 0);
                }
            } else if (newState == 2) {
                isAuto = false;
                lastPage = currentPage;
            }

        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            scrollX += dx;

        }

    }

    //分割线
    public static class DividerGridItemDecoration extends RecyclerView.ItemDecoration {

        private static final int[] ATTRS = new int[]{android.R.attr.listDivider};
        private Drawable mDivider;


        public DividerGridItemDecoration(Drawable diver) {
            mDivider = diver;
        }

        @Override
        public void onDraw(Canvas c, RecyclerView parent, State state) {

            drawHorizontal(c, parent);
            drawVertical(c, parent);

        }

        private int getSpanCount(RecyclerView parent) {
            // 列数
            int spanCount = -1;
            LayoutManager layoutManager = parent.getLayoutManager();
            if (layoutManager instanceof GridLayoutManager) {

                spanCount = ((GridLayoutManager) layoutManager).getSpanCount();
            } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                spanCount = ((StaggeredGridLayoutManager) layoutManager)
                        .getSpanCount();
            }
            return spanCount;
        }

        public void drawHorizontal(Canvas c, RecyclerView parent) {
            int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = parent.getChildAt(i);
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                        .getLayoutParams();
                final int left = child.getLeft() - params.leftMargin;
                final int right = child.getRight() + params.rightMargin
                        + mDivider.getIntrinsicWidth();
                final int top = child.getBottom() + params.bottomMargin;
                final int bottom = top + mDivider.getIntrinsicHeight();
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }

        public void drawVertical(Canvas c, RecyclerView parent) {
            final int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = parent.getChildAt(i);

                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                        .getLayoutParams();
                final int top = child.getTop() - params.topMargin;
                final int bottom = child.getBottom() + params.bottomMargin;
                final int left = child.getRight() + params.rightMargin;
                final int right = left + mDivider.getIntrinsicWidth();

                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }

        private boolean isLastColum(RecyclerView parent, int pos, int spanCount,
                                    int childCount) {
            LayoutManager layoutManager = parent.getLayoutManager();
            if (layoutManager instanceof GridLayoutManager) {
                if ((pos + 1) % spanCount == 0)// 如果是最后一列，则不需要绘制右边
                {
                    return true;
                }
            } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                int orientation = ((StaggeredGridLayoutManager) layoutManager)
                        .getOrientation();
                if (orientation == StaggeredGridLayoutManager.VERTICAL) {
                    if ((pos + 1) % spanCount == 0)// 如果是最后一列，则不需要绘制右边
                    {
                        return true;
                    }
                } else {
                    childCount = childCount - childCount % spanCount;
                    if (pos >= childCount)// 如果是最后一列，则不需要绘制右边
                        return true;
                }
            }
            return false;
        }

        private boolean isLastRaw(RecyclerView parent, int pos, int spanCount,
                                  int childCount) {
            LayoutManager layoutManager = parent.getLayoutManager();
            if (layoutManager instanceof GridLayoutManager) {
                childCount = childCount - childCount % spanCount;
                if (pos >= childCount)// 如果是最后一行，则不需要绘制底部
                    return true;
            } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                int orientation = ((StaggeredGridLayoutManager) layoutManager)
                        .getOrientation();
                // StaggeredGridLayoutManager 且纵向滚动
                if (orientation == StaggeredGridLayoutManager.VERTICAL) {
                    childCount = childCount - childCount % spanCount;
                    // 如果是最后一行，则不需要绘制底部
                    if (pos >= childCount)
                        return true;
                } else
                // StaggeredGridLayoutManager 且横向滚动
                {
                    // 如果是最后一行，则不需要绘制底部
                    if ((pos + 1) % spanCount == 0) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public void getItemOffsets(Rect outRect, int itemPosition,
                                   RecyclerView parent) {
            int spanCount = getSpanCount(parent);
            int childCount = parent.getAdapter().getItemCount();
            if (isLastRaw(parent, itemPosition, spanCount, childCount))// 如果是最后一行，则不需要绘制底部
            {
                outRect.set(0, 0, mDivider.getIntrinsicWidth(), 0);
            } else if (isLastColum(parent, itemPosition, spanCount, childCount))// 如果是最后一列，则不需要绘制右边
            {
                outRect.set(0, 0, 0, mDivider.getIntrinsicHeight());
            } else {
                outRect.set(0, 0, mDivider.getIntrinsicWidth(),
                        mDivider.getIntrinsicHeight());
            }
        }
    }


}
