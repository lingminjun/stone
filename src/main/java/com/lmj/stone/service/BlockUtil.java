package com.lmj.stone.service;

import com.lmj.stone.cache.RemoteCache;
import com.lmj.stone.idl.IDLException;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: lingminjun
 * Date: 2018-06-20
 * Time: 下午8:51
 */
public final class BlockUtil {

    public interface Call<T> {
        T run() throws Throwable;
    }

    public static <T> T en(DataSourceTransactionManager transaction, Call<T> call) throws IDLException {
        return en(transaction,TransactionDefinition.PROPAGATION_REQUIRED,call);
    }

    public static <T> T en(DataSourceTransactionManager transaction, int transactionDefinition, Call<T> call) throws IDLException {
        if (call == null) {
            throw new IDLException("执行事务错误","transaction",-1,"执行事务，没有检查到执行体");
        }

        boolean hasTransaction = transaction != null;

        //有无事务
        TransactionStatus status = null;
        if (hasTransaction) {
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setPropagationBehavior(transactionDefinition/*TransactionDefinition.PROPAGATION_REQUIRED*/);
            status = transaction.getTransaction(def); //
        }

        T rt = null;
        try {
            rt = call.run();

            if (hasTransaction) {
                transaction.commit(status);
            }
        } catch (IDLException e) {
            if (hasTransaction) {
                transaction.rollback(status);
            }
            throw e;
        } catch (Throwable e) {
            if (hasTransaction) {
                transaction.rollback(status);
            }
            throw new IDLException("执行事务时错误","transaction",-1,e);//继续把异常抛出去
        }

        return rt;
    }

//    public static <T> T en(RemoteCache remoteCache, Object[] params, int cacheAge, Call<T> call) throws IDLException {
//        return en(remoteCache,params,cacheAge,false,call);
//    }
//
//    public static <T> T en(RemoteCache remoteCache, Object[] params, int cacheAge, boolean evict, Call<T> call) throws IDLException {
//        T rt = null;
//        try {
//            if (remoteCache != null && params != null) {
//
//            }
//            rt = call.run();
//        } catch (IDLException e) {
//            throw e;
//        } catch (Throwable e) {
//            throw new IDLException("执行缓存时错误","transaction",-1,e);//继续把异常抛出去
//        }
//        return rt;
//    }
//
//    public static <T> T en(RemoteCache remoteCache, Object[] params, int cacheAge, boolean evict, DataSourceTransactionManager transaction, int transactionDefinition, Call<T> call) throws IDLException {
//        if (remoteCache == null) {
//            return en(transaction,transactionDefinition,call);
//        } else {
//
//        }
//    }
}
