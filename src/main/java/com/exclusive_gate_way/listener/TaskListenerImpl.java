package com.exclusive_gate_way.listener;

import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;

/**
 *  尝试用 Listener 创建候选组，用来创建 审批【总经理】 的候选组
 *  @author: Lizq
 *  @Date: 2019/12/18 14:53
 */
public class TaskListenerImpl implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        //将 lowboy 添加进 审批【总经理】的候选组中
        delegateTask.addCandidateUser("lowboy");
    }
}
