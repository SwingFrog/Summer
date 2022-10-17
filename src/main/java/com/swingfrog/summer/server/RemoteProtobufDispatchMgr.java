package com.swingfrog.summer.server;

import com.google.common.collect.Maps;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.swingfrog.summer.annotation.Optional;
import com.swingfrog.summer.annotation.Remote;
import com.swingfrog.summer.ioc.ContainerMgr;
import com.swingfrog.summer.ioc.MethodParameterName;
import com.swingfrog.summer.protocol.protobuf.Protobuf;
import com.swingfrog.summer.protocol.protobuf.ReqProtobufMgr;
import com.swingfrog.summer.protocol.protobuf.ProtobufRequest;
import com.swingfrog.summer.server.async.AsyncResponse;
import com.swingfrog.summer.server.async.ProcessResult;
import com.swingfrog.summer.server.exception.CodeException;
import com.swingfrog.summer.server.exception.RemoteRuntimeException;
import com.swingfrog.summer.server.exception.SessionException;
import com.swingfrog.summer.server.handler.RemoteProtobufHandler;
import com.swingfrog.summer.struct.AutowireParam;
import com.swingfrog.summer.util.MethodUtil;
import com.swingfrog.summer.util.ParamUtil;
import com.swingfrog.summer.util.ProtobufUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.*;
import java.util.Iterator;
import java.util.Map;

public class RemoteProtobufDispatchMgr {

    private static final Logger log = LoggerFactory.getLogger(RemoteProtobufDispatchMgr.class);

    private final Map<Integer, RemoteMethod> remoteMethodMap;


    private static class SingleCase {
        public static final RemoteProtobufDispatchMgr INSTANCE = new RemoteProtobufDispatchMgr();
    }

    private RemoteProtobufDispatchMgr() {
        remoteMethodMap = Maps.newHashMap();
    }

    public static RemoteProtobufDispatchMgr get() {
        return RemoteProtobufDispatchMgr.SingleCase.INSTANCE;
    }

    public void init() throws Exception {
        Iterator<Class<?>> ite = ContainerMgr.get().iteratorRemoteList();
        while (ite.hasNext()) {
            Class<?> clazz = ite.next();
            log.info("server try register remote protobuf {}", clazz.getSimpleName());
            RemoteClass remoteClass = new RemoteClass(clazz);
            Method[] methods = clazz.getDeclaredMethods();
            MethodParameterName mpn = new MethodParameterName(clazz);
            for (Method method : methods) {
                if (method.getModifiers() != Modifier.PUBLIC
                        || !ProtobufUtil.hasProtobufParam(method)
                        || MethodUtil.contains(RemoteProtobufHandler.class, method)) {
                    continue;
                }
                RemoteMethod remoteMethod = new RemoteMethod(remoteClass, method, mpn);
                int messageId = remoteMethod.getMessageId();
                if (remoteMethodMap.putIfAbsent(messageId, remoteMethod) == null) {
                    log.info("remote protobuf register {}.{} {}", clazz.getSimpleName(), method.getName(), ReqProtobufMgr.get().getProtoName(messageId));
                } else {
                    throw new RemoteRuntimeException("protobuf message repeat %s.%s messageId[%s] protoName[%s]",  clazz.getSimpleName(), method.getName(), messageId, ReqProtobufMgr.get().getProtoName(messageId));
                }
            }
        }
    }

    private Object invoke(ServerContext serverContext, ProtobufRequest req, Message reqMessage, SessionContext sctx,
                          AutowireParam autowireParam) throws Throwable {
        Map<Class<?>, Object> objForTypes = autowireParam.getTypes();
        Map<String, Object> objForNames = autowireParam.getNames();
        int messageId = req.getId();
        RemoteMethod remoteMethod = remoteMethodMap.get(messageId);
        if (remoteMethod == null) {
            throw new CodeException(SessionException.METHOD_NOT_EXIST);
        }
        RemoteClass remoteClass = remoteMethod.getRemoteClass();
        if (remoteClass.isFilter() && !remoteClass.getServerName().equals(serverContext.getConfig().getServerName())) {
            throw new CodeException(SessionException.REMOTE_WAS_PROTECTED);
        }

        int messageIndex = remoteMethod.getMessageIndex();
        Object remoteObj = ContainerMgr.get().getDeclaredComponent(remoteClass.getClazz());

        if (remoteObj instanceof RemoteProtobufHandler)
            ((RemoteProtobufHandler) remoteObj).handleReady(sctx, req);

        Method remoteMod = remoteMethod.getMethod();
        String[] params = remoteMethod.getParams();
        Parameter[] parameters = remoteMethod.getParameters();
        boolean auto = ContainerMgr.get().isAutowiredParameter(remoteClass.getClazz());
        Object[] obj = new Object[params.length];
        try {
            for (int i = 0; i < parameters.length; i++) {
                String param = params[i];
                Parameter parameter = parameters[i];
                Type type = parameter.getParameterizedType();
                if (i == messageIndex) {
                    obj[i] = reqMessage;
                } else {
                    if (auto) {
                        Class<?> typeClazz = parameter.getType();
                        if (objForTypes != null && objForTypes.containsKey(typeClazz)) {
                            obj[i] = objForTypes.get(typeClazz);
                        } else if (objForNames != null && objForNames.containsKey(param)) {
                            obj[i] = objForNames.get(param);
                        } else {
                            obj[i] = ContainerMgr.get().getComponent(typeClazz);
                            if (obj[i] == null) {
                                try {
                                    obj[i] = ((Class<?>) type).newInstance();
                                } catch (Exception e) {
                                    log.error(e.getMessage(), e);
                                }
                            }
                        }
                    }
                }
                if (obj[i] == null) {
                    Optional optional = parameter.getAnnotation(Optional.class);
                    if (optional != null) {
                        obj[i] = ParamUtil.convert(parameter.getType(), optional.value());
                    } else {
                        throw new CodeException(SessionException.PARAMETER_ERROR);
                    }
                }
            }
        } catch (Exception e) {
            throw new CodeException(SessionException.PARAMETER_ERROR);
        }
        try {
            return remoteMod.invoke(remoteObj, obj);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    public ProcessResult<Message> process(ServerContext serverContext, ProtobufRequest req, Message reqMessage, SessionContext sctx,
                                                   AutowireParam autowireParam) throws Throwable {
        Map<Class<?>, Object> objForTypes = autowireParam.getTypes();
        objForTypes.putIfAbsent(SessionContext.class, sctx);
        objForTypes.putIfAbsent(ProtobufRequest.class, req);
        Object result = invoke(serverContext, req, reqMessage, sctx, autowireParam);
        if (result instanceof AsyncResponse) {
            return new ProcessResult<>(true, null);
        }
        if (result instanceof Message) {
            return new ProcessResult<>(false, (Message) result);
        }
        return null;
    }

    public Message parse(Protobuf data) throws InvalidProtocolBufferException {
        int messageId = data.getId();
        Message messageTemplate = ReqProtobufMgr.get().getMessageTemplate(messageId);
        if (messageTemplate == null) {
            throw new CodeException(SessionException.PROTOBUF_NOT_EXIST);
        }
        return ProtobufUtil.parseMessage(messageTemplate, data.getBytes());
    }

    public static class RemoteMethod {
        private final RemoteClass remoteClass;
        private final Method method;
        private final String[] params;
        private final Parameter[] parameters;
        private int messageIndex = -1;
        private final int messageId;
        public RemoteMethod(RemoteClass remoteClass, Method method, MethodParameterName mpn) throws Exception {
            this.remoteClass = remoteClass;
            this.method = method;
            params = mpn.getParameterNameByMethod(method);
            parameters = method.getParameters();
            Message messageTemplate = null;
            for (int i = 0; i < parameters.length; i++) {
                Class<?> typeClazz = parameters[i].getType();
                if (Message.class.isAssignableFrom(typeClazz)) {
                    messageTemplate = ProtobufUtil.getDefaultInstance(typeClazz);
                    messageIndex = i;
                    break;
                }
            }
            if (messageTemplate == null)
                throw new RuntimeException("not found protobuf message in remote method");
            messageId = ProtobufUtil.getMessageId(messageTemplate);
            ReqProtobufMgr.get().registerMessage(messageId, messageTemplate);
        }
        public RemoteClass getRemoteClass() {
            return remoteClass;
        }
        public Method getMethod() {
            return method;
        }
        public String[] getParams() {
            return params;
        }
        public Parameter[] getParameters() {
            return parameters;
        }
        public int getMessageIndex() {
            return messageIndex;
        }
        public int getMessageId() {
            return messageId;
        }
    }

    public static class RemoteClass {
        private final boolean filter;
        private final String serverName;
        private final Class<?> clazz;
        public RemoteClass(Class<?> clazz) {
            this.clazz = clazz;
            filter = clazz.getAnnotation(Remote.class).filter();
            serverName = clazz.getAnnotation(Remote.class).serverName();
        }
        public boolean isFilter() {
            return filter;
        }
        public String getServerName() {
            return serverName;
        }
        public Class<?> getClazz() {
            return clazz;
        }
    }

    public Map<Integer, RemoteMethod> getRemoteMethodMap() {
        return remoteMethodMap;
    }

}
