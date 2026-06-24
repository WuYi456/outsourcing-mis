package com.tx.outsourcingmis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tx.outsourcingmis.entity.JobApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 上岗申请表 Mapper
 *
 * <p>提供上岗申请表的数据库操作接口，继承 MyBatis-Plus BaseMapper 获得基础 CRUD 能力。
 */
@Mapper
public interface JobApplicationMapper extends BaseMapper<JobApplication> {

    /**
     * 查询用户的所有申请（按时间倒序）
     *
     * @param applicantId 申请人 ID
     * @return 申请列表
     */
    @Select("SELECT * FROM job_application WHERE applicant_id = #{applicantId} ORDER BY apply_time DESC")
    List<JobApplication> selectByApplicantId(@Param("applicantId") Long applicantId);

    /**
     * 查询某审批人的待审批列表（状态为待审批，按时间升序）
     *
     * @param approverId 审批人 ID
     * @return 待审批申请列表
     */
    @Select("SELECT * FROM job_application WHERE approver_id = #{approverId} AND status = 0 ORDER BY apply_time ASC")
    List<JobApplication> selectPendingByApproverId(@Param("approverId") Long approverId);

    /**
     * 更新申请状态并记录审批时间
     *
     * @param id 申请 ID
     * @param status 状态：1-已通过，2-已拒绝
     * @return 影响行数
     */
    @Update("UPDATE job_application SET status = #{status}, approve_time = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 统计各状态的申请数量
     */
    @Select("SELECT COUNT(*) FROM job_application WHERE status = #{status}")
    int countByStatus(@Param("status") Integer status);
}