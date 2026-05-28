package com.isoft.consultant.service;

/**
 * 为各省「院校所在地 = 生源省」场景补全院校级录取样本，保证冲稳保测算有足够数据。
 */
public interface ProvincialAdmissionCoverageService {

    /**
     * @param province 仅补该省本地院校+本地生源线；null 表示 31 省全部执行
     * @return 新增记录数
     */
    int ensureLocalCoverage(String province);

    /**
     * 按冲稳保页选择的「院校省 + 生源省」补全：同省仅本地；跨省只向生源省扩展一条链路。
     */
    int ensureMatcherPair(String schoolProvince, String scoreProvince);

    /** @deprecated 请使用 {@link #ensureMatcherPair}，避免向 28 省全量扩展 */
    int ensureFullMatcherCoverage(String province);
}
