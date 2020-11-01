package com.swingfrog.summer.test.ecsgameserver.module.player;

import com.swingfrog.summer.app.Summer;
import com.swingfrog.summer.ecs.annotation.BindRepository;
import com.swingfrog.summer.ecs.component.Component;
import com.swingfrog.summer.ecs.entity.AbstractAsyncBeanEntity;
import com.swingfrog.summer.test.ecsgameserver.module.login.Account;
import com.swingfrog.summer.test.ecsgameserver.module.login.AccountDao;
import com.swingfrog.summer.test.ecsgameserver.module.player.base.PlayerEvent;

import java.util.concurrent.Executor;

@BindRepository(AccountDao.class)
public class Player extends AbstractAsyncBeanEntity<Long, Account> {

    public Player(Long id) {
        super(id);
    }

    @Override
    public Executor getExecutor() {
        return Summer.getSessionTokenExecutor(getId());
    }

    public <C extends Component<Long, ? extends Player>> C getComponent(Class<C> componentClass) {
        return super.getOrCreateComponent(componentClass);
    }

    public void dispatch(String eventName, PlayerEvent playerEvent) {
        Summer.syncDispatch(eventName, playerEvent);
    }

    public String getName() {
        Account account = getBean();
        if (account == null)
            return "ERROR";
        return account.getName();
    }

}
