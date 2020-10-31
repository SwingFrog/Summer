package com.swingfrog.summer.test.ecsgameserver.module.player.resource;

import com.swingfrog.summer.annotation.Service;
import com.swingfrog.summer.app.Summer;
import com.swingfrog.summer.test.ecsgameserver.infrastructure.ErrorCode;
import com.swingfrog.summer.test.ecsgameserver.module.player.Player;

@Service
public class PlayerResourceService {

    public void consumeGold(Player player, long consumeGold) {
        PlayerResourceComponent component = player.getComponent(PlayerResourceComponent.class);
        PlayerResource bean = component.getOrCreateBean();
        long remain = bean.getGold() - consumeGold;
        if (remain < 0) {
            throw Summer.createCodeException(ErrorCode.RESOURCE_NOT_ENOUGH.getCodeMsg());
        }
        bean.setGold(remain);
        component.saveBean();
    }

    public void updateGold(Player player, long updateGold) {
        PlayerResourceComponent component = player.getComponent(PlayerResourceComponent.class);
        PlayerResource bean = component.getOrCreateBean();
        bean.setGold(bean.getGold() + updateGold);
        component.saveBean();
    }

    public void updateFriendly(Player player, int updateFriendly) {
        PlayerResourceComponent component = player.getComponent(PlayerResourceComponent.class);
        PlayerResource bean = component.getOrCreateBean();
        bean.setFriendly(bean.getFriendly() + updateFriendly);
        component.saveBean();
    }

}
