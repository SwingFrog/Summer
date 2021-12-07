package com.swingfrog.summer.test.repository.task;

import com.swingfrog.summer.annotation.Autowired;
import com.swingfrog.summer.annotation.IntervalTask;
import com.swingfrog.summer.annotation.Task;
import com.swingfrog.summer.test.repository.dao.TestLogDao;
import com.swingfrog.summer.test.repository.model.TestLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

@Task
public class TestLogTask {

    private static final Logger log = LoggerFactory.getLogger(TestLogTask.class);

    @Autowired
    private TestLogDao testLogDao;

    @IntervalTask(value = 1000, delay = 1000)
    public void onTick() {
        log.debug("tick");
        TestLog testLog = new TestLog();
        testLog.setValue(ThreadLocalRandom.current().nextInt());
        testLog.setTime(new Date());
        testLogDao.add(testLog);
    }

}
