package com.tx.outsourcingmis.service;

import com.tx.outsourcingmis.dto.JobApplicationRequest;
import com.tx.outsourcingmis.dto.JobApplicationResponse;
import com.tx.outsourcingmis.entity.JobApplication;

import java.util.List;

/**
 * 上岗申请服务接口
 *
 * <p>提供外包人员上岗申请的提交、查询、取消等业务操作。
 */
public interface JobApplicationService {

    /**
     * 提交上岗申请
     *
     * @param request 申请请求
     * @return 申请响应
     */
    JobApplicationResponse submitApplication(JobApplicationRequest request);

    /**
     * 获取当前用户的申请列表
     *
     * @return 申请列表
     */
    List<JobApplicationResponse> getMyApplications();

    /**
     * 根据申请 ID 获取详情
     *
     * @param id 申请 ID
     * @return 申请详情
     */
    JobApplicationResponse getApplicationDetail(Long id);

    /**
     * 取消申请（仅待审批状态可取消）
     *
     * @param id 申请 ID
     */
    void cancelApplication(Long id);

    /**
     * 根据 ID 获取申请实体（内部使用）
     *
     * @param id 申请 ID
     * @return 申请实体
     */
    JobApplication getById(Long id);
}