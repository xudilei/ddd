package com.runssnail.ddd.demo.domain.event.policyset;

import com.runssnail.ddd.common.event.AbstractEvent;
import lombok.Getter;

/**
 * @author zhengwei
 * @date 2019-11-05 15:08
 **/
@Getter
public class PolicySetCreatedEvent extends AbstractEvent {

    private String policySetId;

    public PolicySetCreatedEvent(String policySetId) {
        this.policySetId = policySetId;
    }


}