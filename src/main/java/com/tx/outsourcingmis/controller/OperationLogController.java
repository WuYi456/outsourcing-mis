package com.tx.outsourcingmis.controller;

import com.tx.outsourcingmis.annotation.RequirePermission;
import com.tx.outsourcingmis.common.ResultVO;
import com.tx.outsourcingmis.dto.OperationLogQueryRequest;
import com.tx.outsourcingmis.dto.OperationLogResponse;
import com.tx.outsourcingmis.es.document.OperationLogDocument;
import com.tx.outsourcingmis.service.OperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

/**
 * 操作日志控制器
 *
 * <p>提供操作日志查询和管理接口：
 * <ul>
 *   <li>高级搜索（支持用户名、操作类型、时间范围组合查询）</li>
 *   <li>按用户名查询</li>
 *   <li>按操作类型查询</li>
 *   <li>按 ID 查询日志详情</li>
 *   <li>删除日志</li>
 * </ul>
 * <p>日志数据存储在 Elasticsearch 中。
 */
@Slf4j
@RestController
@RequestMapping("/api/log")
@RequiredArgsConstructor
@Tag(name = "操作日志模块", description = "操作日志查询和管理（ES存储）")
public class OperationLogController {

    private final OperationLogService operationLogService;

    /**
     * 高级搜索操作日志
     *
     * <p>支持按用户名、操作类型、时间范围组合查询，所有条件均为可选。
     *
     * @param request 查询请求（含用户名、操作类型、时间范围、分页参数）
     * @return 分页日志列表
     */
    @PostMapping("/search")
    @Operation(summary = "高级搜索操作日志")
    @RequirePermission("log:view")
    public ResultVO<Page<OperationLogResponse>> search(@RequestBody OperationLogQueryRequest request) {
        return ResultVO.success(operationLogService.search(request));
    }

    /**
     * 根据用户名查询操作日志
     *
     * @param username 用户名
     * @param pageNum 页码，默认 1
     * @param pageSize 每页大小，默认 20
     * @return 分页日志列表
     */
    @GetMapping("/user/{username}")
    @Operation(summary = "根据用户名查询操作日志")
    @RequirePermission("log:view")
    public ResultVO<Page<OperationLogResponse>> getByUsername(
            @PathVariable String username,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return ResultVO.success(operationLogService.getByUsername(username, pageNum, pageSize));
    }

    /**
     * 根据操作类型查询操作日志
     *
     * @param operation 操作类型（如 UserController.login）
     * @param pageNum 页码，默认 1
     * @param pageSize 每页大小，默认 20
     * @return 分页日志列表
     */
    @GetMapping("/operation/{operation}")
    @Operation(summary = "根据操作类型查询操作日志")
    @RequirePermission("log:view")
    public ResultVO<Page<OperationLogResponse>> getByOperation(
            @PathVariable String operation,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return ResultVO.success(operationLogService.getByOperation(operation, pageNum, pageSize));
    }

    /**
     * 根据 ID 查询日志详情
     *
     * @param id 日志 ID（ES 文档 ID）
     * @return 日志详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询日志详情")
    @RequirePermission("log:view")
    public ResultVO<OperationLogResponse> getById(@PathVariable String id) {
        OperationLogDocument doc = operationLogService.getById(id);
        if (doc == null) {
            return ResultVO.error("日志不存在");
        }
        return ResultVO.success(convertToResponse(doc));
    }

    /**
     * 删除操作日志
     *
     * @param id 日志 ID（ES 文档 ID）
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除操作日志")
    @RequirePermission("log:delete")
    public ResultVO<Void> deleteById(@PathVariable String id) {
        operationLogService.deleteById(id);
        return ResultVO.success();
    }

    /**
     * 将 ES 文档转换为响应对象
     *
     * @param doc ES 日志文档
     * @return OperationLogResponse
     */
    private OperationLogResponse convertToResponse(OperationLogDocument doc) {
        return OperationLogResponse.builder()
                .id(doc.getId())
                .userId(doc.getUserId())
                .username(doc.getUsername())
                .operation(doc.getOperation())
                .method(doc.getMethod())
                .params(doc.getParams())
                .result(doc.getResult())
                .ip(doc.getIp())
                .duration(doc.getDuration())
                .success(doc.getSuccess())
                .errorMsg(doc.getErrorMsg())
                .createTime(doc.getCreateTime())
                .build();
    }
}