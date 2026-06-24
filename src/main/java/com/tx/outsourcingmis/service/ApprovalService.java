package com.tx.outsourcingmis.service;

import com.github.pagehelper.PageInfo;
import com.tx.outsourcingmis.dto.ApprovalRequest;
import com.tx.outsourcingmis.dto.ApprovalResponse;

/**
 * 审批服务接口
 *
 * <p>提供上岗申请审批相关业务操作。
 */
public interface ApprovalService {

    /**
     * 分页查询待审批列表
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 待审批列表（分页）
     */
    PageInfo<ApprovalResponse> getPendingList(Integer pageNum, Integer pageSize);

    /**
     * 审批通过（单个申请）
     *
     * @param request 审批请求
     */
    void approve(ApprovalRequest request);

    /**
     * 批量审批通过
     *
     * @param request 审批请求（含多个申请 ID）
     */
    void batchApprove(ApprovalRequest request);

    /**
     * 审批拒绝（单个申请）
     *
     * @param request 审批请求（含拒绝原因）
     */
    void reject(ApprovalRequest request);

    /**
     * 批量审批拒绝
     *
     * @param request 审批请求（含多个申请 ID 和拒绝原因）
     */
    void batchReject(ApprovalRequest request);
}