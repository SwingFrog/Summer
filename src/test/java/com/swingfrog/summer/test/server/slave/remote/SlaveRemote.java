package com.swingfrog.summer.test.server.slave.remote;

import com.swingfrog.summer.annotation.Remote;
import com.swingfrog.summer.app.Summer;
import com.swingfrog.summer.test.server.master.remote.MasterRemote;
import com.swingfrog.summer.test.server.slave.bean.ReqQuery;
import com.swingfrog.summer.test.server.slave.bean.RespQuery;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Remote
public class SlaveRemote {

    public String hello() {
        return "hello world";
    }

    public int add(int a, int b) {
        MasterRemote masterRemote = Summer.getRandomRemoteInvokeObjectWithRetry("Master", MasterRemote.class);
        return masterRemote.add(a, b);
    }

    public void commit(String msg) {
        System.out.println(msg);
    }

    public RespQuery query(int id) {
        return new RespQuery(id, "ha");
    }

    public List<RespQuery> queryList(ReqQuery reqQuery) {
        return IntStream.rangeClosed(1, 10)
                .mapToObj(i -> new RespQuery(i, reqQuery.getId() + "_" + reqQuery.getPage()))
                .collect(Collectors.toList());
    }

}
