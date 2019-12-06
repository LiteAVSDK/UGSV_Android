package com.tencent.qcloud.ugckit.module.effect.filter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.tencent.qcloud.ugckit.module.effect.BaseRecyclerAdapter;
import com.tencent.qcloud.ugckit.module.effect.VideoEditerSDK;
import com.tencent.qcloud.ugckit.utils.UIAttributeUtil;
import com.tencent.qcloud.ugckit.R;


import java.util.ArrayList;
import java.util.List;

/**
 * 静态滤镜的Fragment
 */
public class TCStaticFilterFragment extends Fragment implements BaseRecyclerAdapter.OnItemClickListener {
    private static final int[] FILTER_ARR = {
            R.drawable.filter_biaozhun, R.drawable.filter_yinghong,
            R.drawable.filter_yunshang, R.drawable.filter_chunzhen,
            R.drawable.filter_bailan, R.drawable.filter_yuanqi,
            R.drawable.filter_chaotuo, R.drawable.filter_xiangfen,

            R.drawable.filter_langman, R.drawable.filter_qingxin,
            R.drawable.filter_weimei, R.drawable.filter_fennen,
            R.drawable.filter_huaijiu, R.drawable.filter_landiao,
            R.drawable.filter_qingliang, R.drawable.filter_rixi};

    private List<Integer> mFilterList;
    private List<String> mFilerNameList;
    private RecyclerView mRvFilter;
    private StaticFilterAdapter mAdapter;
    private int mCurrentPosition = 0;

    //定制化Icon
    private int originIcon = R.drawable.orginal;
    private int normalIcon = R.drawable.biaozhun;
    private int yinghongIcon = R.drawable.yinghong;
    private int yunchangIcon = R.drawable.yunshang;
    private int chunzhenIcon = R.drawable.chunzhen;
    private int bailanIcon = R.drawable.bailan;
    private int yuanqiIcon = R.drawable.yuanqi;
    private int chaotuoIcon = R.drawable.chaotuo;
    private int xiangfengIcon = R.drawable.xiangfen;
    private int langmanIcon = R.drawable.langman;
    private int qingxinIcon = R.drawable.qingxin;
    private int weimeiIcon = R.drawable.weimei;
    private int fennenIcon = R.drawable.fennen;
    private int huaijiuIcon = R.drawable.huaijiu;
    private int landiaoIcon = R.drawable.landiao;
    private int qingliangIcon = R.drawable.qingliang;
    private int rixiIcon = R.drawable.rixi;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_static_filter, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        originIcon = UIAttributeUtil.getResResources(getContext(), R.attr.editerFilterOriginIcon, R.drawable.orginal);
        normalIcon = UIAttributeUtil.getResResources(getContext(), R.attr.editerFilterNormalIcon, R.drawable.biaozhun);
        yinghongIcon = UIAttributeUtil.getResResources(getContext(), R.attr.editerFilterYinghongIcon, R.drawable.yinghong);
        yunchangIcon = UIAttributeUtil.getResResources(getContext(), R.attr.editerFilterYunchangIcon, R.drawable.yunshang);
        chunzhenIcon = UIAttributeUtil.getResResources(getContext(), R.attr.editerFilterChunzhenIcon, R.drawable.chunzhen);
        bailanIcon = UIAttributeUtil.getResResources(getContext(), R.attr.editerFilterBailanIcon, R.drawable.bailan);
        yuanqiIcon = UIAttributeUtil.getResResources(getContext(), R.attr.editerFilterYuanqiIcon, R.drawable.yuanqi);
        chaotuoIcon = UIAttributeUtil.getResResources(getContext(), R.attr.editerFilterChaotuoIcon, R.drawable.chaotuo);
        xiangfengIcon = UIAttributeUtil.getResResources(getContext(), R.attr.editerFilterXiangfenIcon, R.drawable.xiangfen);
        langmanIcon = UIAttributeUtil.getResResources(getContext(), R.attr.editerFilterLangmanIcon, R.drawable.langman);
        qingxinIcon = UIAttributeUtil.getResResources(getContext(), R.attr.editerFilterQingxinIcon, R.drawable.qingxin);
        weimeiIcon = UIAttributeUtil.getResResources(getContext(), R.attr.editerFilterWeimeiIcon, R.drawable.weimei);
        fennenIcon = UIAttributeUtil.getResResources(getContext(), R.attr.editerFilterFennenIcon, R.drawable.fennen);
        huaijiuIcon = UIAttributeUtil.getResResources(getContext(), R.attr.editerFilterHuaijiuIcon, R.drawable.huaijiu);
        landiaoIcon = UIAttributeUtil.getResResources(getContext(), R.attr.editerFilterLandiaoIcon, R.drawable.landiao);
        qingliangIcon = UIAttributeUtil.getResResources(getContext(), R.attr.editerFilterQingliangIcon, R.drawable.qingliang);
        rixiIcon = UIAttributeUtil.getResResources(getContext(), R.attr.editerFilterRixiIcon, R.drawable.rixi);

        mFilterList = new ArrayList<Integer>();
        mFilterList.add(originIcon);
        mFilterList.add(normalIcon);
        mFilterList.add(yinghongIcon);
        mFilterList.add(yunchangIcon);
        mFilterList.add(chunzhenIcon);
        mFilterList.add(bailanIcon);
        mFilterList.add(yuanqiIcon);
        mFilterList.add(chaotuoIcon);
        mFilterList.add(xiangfengIcon);
        mFilterList.add(langmanIcon);
        mFilterList.add(qingxinIcon);
        mFilterList.add(weimeiIcon);
        mFilterList.add(fennenIcon);
        mFilterList.add(huaijiuIcon);
        mFilterList.add(landiaoIcon);
        mFilterList.add(qingliangIcon);
        mFilterList.add(rixiIcon);


        mFilerNameList = new ArrayList<>();
        mFilerNameList.add(getResources().getString(R.string.tc_static_filter_fragment_original));
        mFilerNameList.add(getResources().getString(R.string.tc_static_filter_fragment_standard));
        mFilerNameList.add(getResources().getString(R.string.tc_static_filter_fragment_cheery));
        mFilerNameList.add(getResources().getString(R.string.tc_static_filter_fragment_cloud));
        mFilerNameList.add(getResources().getString(R.string.tc_static_filter_fragment_pure));
        mFilerNameList.add(getResources().getString(R.string.tc_static_filter_fragment_orchid));
        mFilerNameList.add(getResources().getString(R.string.tc_static_filter_fragment_vitality));
        mFilerNameList.add(getResources().getString(R.string.tc_static_filter_fragment_super));
        mFilerNameList.add(getResources().getString(R.string.tc_static_filter_fragment_fragrance));
        mFilerNameList.add(getResources().getString(R.string.tc_static_filter_fragment_romantic));
        mFilerNameList.add(getResources().getString(R.string.tc_static_filter_fragment_fresh));
        mFilerNameList.add(getResources().getString(R.string.tc_static_filter_fragment_beautiful));
        mFilerNameList.add(getResources().getString(R.string.tc_static_filter_fragment_pink));
        mFilerNameList.add(getResources().getString(R.string.tc_static_filter_fragment_reminiscence));
        mFilerNameList.add(getResources().getString(R.string.tc_static_filter_fragment_blues));
        mFilerNameList.add(getResources().getString(R.string.tc_static_filter_fragment_cool));
        mFilerNameList.add(getResources().getString(R.string.tc_static_filter_fragment_Japanese));

        mRvFilter = (RecyclerView) view.findViewById(R.id.paster_rv_list);
        mRvFilter.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        mAdapter = new StaticFilterAdapter(mFilterList, mFilerNameList);
        mAdapter.setOnItemClickListener(this);
        mRvFilter.setAdapter(mAdapter);

        mCurrentPosition = TCStaticFilterViewInfoManager.getInstance().getCurrentPosition();
        mAdapter.setCurrentSelectedPos(mCurrentPosition);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        TCStaticFilterViewInfoManager.getInstance().setCurrentPosition(mCurrentPosition);
    }

    @Override
    public void onItemClick(View view, int position) {
        Bitmap bitmap = null;
        if (position == 0) {
            bitmap = null;  // 没有滤镜
        } else {
            bitmap = BitmapFactory.decodeResource(getResources(), FILTER_ARR[position - 1]);
        }
        mAdapter.setCurrentSelectedPos(position);
        // 设置滤镜图片
        VideoEditerSDK.getInstance().getEditer().setFilter(bitmap);

        mCurrentPosition = position;
    }

}
