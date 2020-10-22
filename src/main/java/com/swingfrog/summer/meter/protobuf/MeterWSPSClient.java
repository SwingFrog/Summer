package com.swingfrog.summer.meter.protobuf;

import com.swingfrog.summer.protocol.ProtocolConst;

/**
 * {@link ProtocolConst#SERVER_PROTOCOL_WEB_SOCKET_PROTOBUF_STANDARD}
 */
public abstract class MeterWSPSClient extends MeterWSPClient {

    public MeterWSPSClient(int id) {
        super(id, true);
    }

}
