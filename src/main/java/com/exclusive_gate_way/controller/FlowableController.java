package com.exclusive_gate_way.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.identitylink.api.IdentityLink;
import org.flowable.identitylink.api.history.HistoricIdentityLink;
import org.flowable.task.api.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  Get 接口为基础操作，Post 接口为封装好的业务请求，以后基础操作将封装到 Service 中便于业务请求调用
 *  当前 demo 便于演示执行效果将基础操作封装成了接口
 *  @author: Lizq
 *  @Date: 2019/12/18 16:56
 */
@RestController
@Slf4j
@Api("Flowable排他网关和用户组的接口")
@RequestMapping("/flowable")
public class FlowableController {

    private Logger logger = LoggerFactory.getLogger(FlowableController.class);

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private RepositoryService repositoryService;

    @Resource
    private ProcessEngine processEngine;

    //1、发布流程,部署好一个流程资源 , 设置好自定义的 key
    @GetMapping("/initflowable/{key}")
    @ApiOperation(value = "发布流程，部署好一个流程资源",notes = "请输入一个 key 值在 url 上")
    public String initFlowable(@PathVariable("key") String key){
        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("process/exclusiveGateWay.bpmn20.xml")
                .addClasspathResource("exclusiveGateWay.bpmn20.png")
                .name("exclusiveGateWay")
                .key(key)
                .deploy();
        /** addClasspathResource 表示从类路径下加载资源文件，一次只加载一个文件 **/
        logger.error("deploymentID:"+deployment.getId() + ",deploymentName:"+deployment.getName());
        return "deploymentID:"+deployment.getId() + ",deploymentName:"+deployment.getName();
    }

    //2、启动流程实例，使用 RuntimeService 启动一个流程实例（一个流程实例是用来走流程图的）
    @GetMapping("/start-process")
    @ApiOperation(value = "启动流程实例",notes = "输入流程定义的 key 来初始化一个流程实例，为xml文件的名称")
    public String startProcess(@RequestParam("key") String key,@RequestParam("usersName") String usersName){
        //通过runtimeService 启动流程，使用流程定义的 Key 启动流程实例，默认会按照最新的版本启动本流程实例
        Map variables = new HashMap();
        usersName = usersName.replace(" ","");
        variables.put("usersName",usersName);
        /** 启动任务建立用户组**/
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(key,variables);
        logger.error("pid:"+processInstance.getId()+",activitId:"+processInstance.getActivityId());
        return "pid:"+processInstance.getId()+",activitId:"+processInstance.getActivityId();

    }

    //3、查看个人任务，TaskService 完成任务的查询
    @GetMapping("/find-persontask")
    @ApiOperation(value = "查看个人任务",notes = "请输入一个办理人Id assignee")
    public String findPersonTask(@RequestParam("assignee") String assignee){
        //指定任务办理人

        //查询任务的列表
        List<Task> tasks = taskService.createTaskQuery()        //创建任务查询对象
                .taskAssignee(assignee)     //指定个人任务办理人
                .list();
        //遍历结果查看内容
        for(Task task:tasks){
            logger.error("taskId:"+task.getId()+",taskName:"+task.getName());
            logger.error("******************************************************************");
        }

        return "返回第一个："+tasks.get(0).toString()+"具体结果查看日志";
    }

    //4、查询组任务列表
    @GetMapping("/find-grouptask")
    @ApiOperation(value = "查询组任务列表",notes = "输入一个userName")
    public String findGroupTask(@RequestParam("userName") String userName){
        List<Task> list = taskService.createTaskQuery()
                                    .taskCandidateUser(userName)
                                    .list();
        for (Task task:list) {
            logger.error("id="+task.getId());
            logger.error("name="+task.getName());
            logger.error("assinee="+task.getAssignee());
            logger.error("assinee="+task.getCreateTime());
            logger.error("executionId="+task.getExecutionId());
            logger.error("##################################");
        }
        return "请查看日志";
    }

    //5、查询组任务成员列表
    @GetMapping("/find-groupusers")
    @ApiOperation(value = "查询组任务成员列表",notes = "输入一个任务id taskId")
    public String findGroupUsers(@RequestParam("taskId") String taskId){
        List<IdentityLink> links = taskService.getIdentityLinksForTask(taskId);
        for (IdentityLink identityLink :links) {
           logger.error("userId="+identityLink.getUserId());
           logger.error("taskId="+identityLink.getTaskId());
           logger.error("piId="+identityLink.getProcessInstanceId());
           logger.error("######################");
        }
        return "请查看日志";
    }

    //6、查询组任务成员历史列表
    @GetMapping("/find-history-groupusers")
    @ApiOperation(value = "查询组任务成员历史列表",notes = "输入一个id taskId")
    public String findHistoryGroupUsers(@RequestParam("taskId") String taskId){
        List<HistoricIdentityLink> historicIdentityLinks = processEngine.getHistoryService()
                                                                        .getHistoricIdentityLinksForTask(taskId);
        for (HistoricIdentityLink identityLink:historicIdentityLinks) {
            logger.error("userId="+identityLink.getUserId());
            logger.error("taskId="+identityLink.getTaskId());
            logger.error("piId="+identityLink.getProcessInstanceId());
            logger.error("######################");
        }
        return "请查看日志";
    }

    //7、将组任务分配给个人任务，拾取任务
    @GetMapping("/grouptask-to-persontask")
    @ApiOperation(value = "将当前的组任务分配给个人去完成",notes = "输入一个任务Id taskId，和输入一个用户名称userName userName")
    public String grouptaskToPersonTask(@RequestParam("taskId") String taskId,@RequestParam("userName") String userName){
        /** 任务Id，办理人名称 **/
        taskService.claim(taskId,userName);
        return "ok";
    }

    //8、可以分配个人任务回退到组任务，（前提是之前这是个组任务）
    @GetMapping("/persontask-to-grouptask")
    @ApiOperation(value = "可以分配个人任务回退到组任务，（前提是之前这是个组任务）",notes = "输入任务Id taskId")
    public String personTaskToGroupTask(@RequestParam("taskId") String taskId){
        taskService.claim(taskId,null);
        return "ok";
    }

    //9、向某个组任务中添加成员
    @GetMapping("/adduser-grouptask")
    @ApiOperation(value = "向某个组任务中添加成员",notes = "输入一个任务Id taskId，和输入一个用户名称userName userName")
    public String addUserGroupTask(@RequestParam("taskId") String taskId,@RequestParam("userName") String userName){
        taskService.addCandidateUser(taskId,userName);
        return "ok";
    }

    //10、向某个组任务中删除成员
    @GetMapping("/deleteuser-grouptask")
    @ApiOperation(value = "向某个组任务中删除成员",notes = "输入一个任务Id taskId，和输入一个用户名称userName userName")
    public String deleteUserGroupTask(@RequestParam("taskId") String taskId,@RequestParam("userName") String userName){
        taskService.deleteCandidateUser(taskId,userName);
        return "ok";
    }

    //11、办理任务 TaskService api 来完成任务
    @GetMapping("/complete-task")
    @ApiOperation(value = "完成当前办理的任务",notes = "请输入一个任务ID taskId")
    public String completeTask(@RequestParam("taskId") String taskId){
        //通过 taskService 完成任务
        taskService.complete(taskId);
        logger.error("完成了任务");
        return "完成了任务，请继续查询任务流程";
    }

    //12、业务请求人张三，向上级发起报销单请求，提交报销订单，（即完成当前他的任务并设置）
    @PostMapping("/submit-reimbursement-form")
    @ApiOperation(value = "业务请求人张三，向上级发起报销单请求，提交报销订单",notes = "输入任务Id taskId，需要报销的金额 money")
    public String SubmitReimbursementForm(@RequestParam("taskId") String taskId,@RequestParam("money") int money){
        taskService.setVariableLocal(taskId,"报销金额",String.valueOf(money));  //定义了一个变量熟练一下操作实际可能没必要
        Map map = new HashMap();
        map.put("money",money);
        taskService.complete(taskId,map);
        return money>1000?"您的报销单将由 审批【总经理】 审批":money>=500?"您的报销单将由 审批【部门经理】 审批":"您的订单由财务审批";
    }

}
