package com.swingfrog.summer.test.ecsgameserver.module.player;

import com.swingfrog.summer.app.Summer;
import com.swingfrog.summer.ecs.component.Component;
import com.swingfrog.summer.ecs.entity.AbstractAsyncEntity;
import com.swingfrog.summer.test.ecsgameserver.module.login.Account;
import com.swingfrog.summer.test.ecsgameserver.module.login.AccountDao;
import com.swingfrog.summer.test.ecsgameserver.module.player.base.PlayerEvent;

import java.util.concurrent.Executor;

public class Player extends AbstractAsyncEntity<Long> {

    private final AccountDao accountDao;
    private Account account;

    public Player(Long id, AccountDao accountDao) {
        super(id);
        this.accountDao = accountDao;
    }

    @Override
    public Executor getExecutor() {
        return Summer.getSessionTokenExecutor(getId());
    }

    public <C extends Component<Long, ? extends Player>> C getComponent(Class<C> componentClass) {
        return super.getOrCreateComponent(componentClass);
    }

    public Account getAccount() {
        if (account == null) {
            account = accountDao.get(getId());
        }
        return account;
    }

    public void saveAccount() {
        if (account == null)
            return;
        accountDao.forceSave(account);
    }

    public void dispatch(String eventName, PlayerEvent playerEvent) {
        Summer.syncDispatch(eventName, playerEvent);
    }

}
