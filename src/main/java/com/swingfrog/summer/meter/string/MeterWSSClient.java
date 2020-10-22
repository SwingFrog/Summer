package com.swingfrog.summer.meter.string;

import com.swingfrog.summer.protocol.ProtocolConst;

/**
 * {@link ProtocolConst#SERVER_PROTOCOL_WEB_SOCKET_STANDARD}
 */
public abstract class MeterWSSClient extends MeterWSClient {

    public MeterWSSClient(int id) {
        super(id, true);
    }

}
