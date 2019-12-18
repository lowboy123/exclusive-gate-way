package com.exclusive_gate_way.listener;

import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;

/**
 *  尝试用 Listener 创建候选人
 *  @author: Lizq
 *  @Date: 2019/12/18 14:53
 */
public class TaskListenerImpl implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        delegateTask.addCandidateUser("lowboy");
    }
}
