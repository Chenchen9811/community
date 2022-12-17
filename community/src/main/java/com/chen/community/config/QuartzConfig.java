package com.chen.community.config;

import com.chen.community.quartz.AlphaJob;
import com.chen.community.quartz.DiscussPostScoreRefreshJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

// 第一次调用quartz才使用这个配置，调用完之后就会初始化到数据库，之后就是使用数据库中的数据来执行了。
@Configuration
public class QuartzConfig {
    // FactoryBean可简化Bean的实例化过程
    // 1.通过FactoryBean封装了Bean的实例化过程.
    // 2.将FactoryBean装配到Spring容器里.
    // 3.将FactoryBean注入给其他的Bean.
    // 4.其他的Bean得到的是FactoryBean所管理的示例对象



    // 配置JobDetail
    //@Bean
    public JobDetailFactoryBean alphaJobDetail() {
        JobDetailFactoryBean jobDetailFactoryBean = new JobDetailFactoryBean();
        jobDetailFactoryBean.setJobClass(AlphaJob.class);
        jobDetailFactoryBean.setName("alphaJob");
        jobDetailFactoryBean.setGroup("alphaJobGroup");
        jobDetailFactoryBean.setDurability(true); // 这个任务是否持久保存，这里是true
        jobDetailFactoryBean.setRequestsRecovery(true); // 任务是否可恢复
        return jobDetailFactoryBean;
    }

    // 配置Trigger
    //@Bean
    public SimpleTriggerFactoryBean alphaTrigger(JobDetail alphaJobDetail) {
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(alphaJobDetail);
        factoryBean.setName("alphaTrigger");
        factoryBean.setGroup("alphaTriggerGroup");
        factoryBean.setRepeatInterval(3000);
        factoryBean.setJobDataMap(new JobDataMap());
        return factoryBean;
    }


    // 配置帖子分数刷新
    @Bean
    public JobDetailFactoryBean discusspostScoreRefreshJobDetail() {
        JobDetailFactoryBean jobDetailFactoryBean = new JobDetailFactoryBean();
        jobDetailFactoryBean.setJobClass(DiscussPostScoreRefreshJob.class);
        jobDetailFactoryBean.setName("discusspostScoreRefreshJob");
        jobDetailFactoryBean.setGroup("communityJobGroup");
        jobDetailFactoryBean.setDurability(true); // 这个任务是否持久保存，这里是true
        jobDetailFactoryBean.setRequestsRecovery(true); // 任务是否可恢复
        return jobDetailFactoryBean;
    }

    // 配置Trigger
    @Bean
    public SimpleTriggerFactoryBean discusspostScoreRefreshTrigger(JobDetail discusspostScoreRefreshJobDetail) {
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(discusspostScoreRefreshJobDetail);
        factoryBean.setName("discusspostScoreRefreshJobDetailTrigger");
        factoryBean.setGroup("community" +
                "TriggerGroup");
        factoryBean.setRepeatInterval(1000 * 60 * 5); // 5分钟算一次
        factoryBean.setJobDataMap(new JobDataMap());
        return factoryBean;
    }
}
