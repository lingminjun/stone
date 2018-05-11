package com.lmj.stone.lang;


import com.lmj.stone.idl.IDLException;

import java.io.Serializable;
import java.lang.Exception;
import java.lang.reflect.Field;

/**
 * Created by lingminjun on 17/2/7.
 *
 * 远程服务异常申明
 */
public final class RemoteException extends IDLException {

    public RemoteException(String message, String domain, int code, Throwable cause) {
        super(message, domain, code, cause);
    }

    public RemoteException(String message, String domain, int code, String reason) {
        super(message, domain, code, reason);
    }
}
