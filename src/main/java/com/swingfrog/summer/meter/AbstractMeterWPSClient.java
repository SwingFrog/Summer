package com.swingfrog.summer.meter;

import com.swingfrog.summer.protocol.ProtocolConst;

/**
 * {@link ProtocolConst#SERVER_PROTOCOL_WEB_SOCKET_PROTOBUF_STANDARD}
 */
public abstract class AbstractMeterWPSClient extends AbstractMeterWPClient {

    public AbstractMeterWPSClient(int id) {
        super(id, true);
    }

}
