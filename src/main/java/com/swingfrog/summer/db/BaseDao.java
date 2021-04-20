package com.swingfrog.summer.db;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseDao<T> {

	private static final Logger log = LoggerFactory.getLogger(BaseDao.class);
	private final QueryRunner queryRunner;
	private Class<T> clazz;
	private final BeanHandler<T> beanHandler;
	private final BeanListHandler<T> beanListHandler;

	
	@SuppressWarnings("unchecked")
	protected BaseDao() {
		queryRunner = new QueryRunner();
		Type superClass = getClass().getGenericSuperclass();
		if (superClass instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType) superClass;
			Type[] typeArgs = parameterizedType.getActualTypeArguments();
			if (typeArgs != null && typeArgs.length > 0) {
				if (typeArgs[0] instanceof Class) {
					clazz = (Class<T>) typeArgs[0];
				}
			}
		}
		beanHandler = new BeanHandler<>(clazz);
		beanListHandler = new BeanListHandler<>(clazz);
	}

	protected Class<T> getEntityClass() {
		return clazz;
	}

	protected String topic() {
		return null;
	}

	private Connection getConnection() throws SQLException {
		if (topic() == null) {
			return DataBaseMgr.get().getConnection();
		}
		return DataBaseMgr.get().getConnection(topic());
	}
	
	protected int update(String sql, Object... args) {
		if (log.isDebugEnabled())
			log.debug("{}  {}", sql, args);
		int i;
		try {
			i = queryRunner.update(getConnection(), sql, args);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new DaoRuntimeException("dao update failure, %s", getEntityClass().getName());
		} finally {
			 try {
				DataBaseMgr.get().discardConnectionFromDao();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}	
		return i;
	}
	
	protected int[] batch(String sql, Object[][] args) {
		if (log.isDebugEnabled()) {
			for (Object[] array : args) {				
				log.debug("{}  {}", sql, array);
			}
		}
		try {
			return queryRunner.batch(getConnection(), sql, args);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new DaoRuntimeException("dao batch update failure, %s", getEntityClass().getName());
		} finally {
			try {
				DataBaseMgr.get().discardConnectionFromDao();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}
	
	protected Object insertAndGetGeneratedKeys(String sql, Object... args) {
		if (log.isDebugEnabled())
			log.debug("{}  {}", sql, args);
		try {
			return queryRunner.insert(getConnection(), sql, new ScalarHandler<>(), args);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new DaoRuntimeException("dao insert failure, %s", getEntityClass().getName());
		} finally {
			 try {
				DataBaseMgr.get().discardConnectionFromDao();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	protected T getBean(String sql, Object... args) {
		return getBean(sql, beanHandler, args);
	}

	protected T getBean(String sql, ResultSetHandler<T> rsh, Object... args) {
		if (log.isDebugEnabled())
			log.debug("{}  {}", sql, args);
		try {
			return queryRunner.query(getConnection(), sql, rsh, args);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new DaoRuntimeException("dao get bean failure, %s", getEntityClass().getName());
		} finally {
			try {
				DataBaseMgr.get().discardConnectionFromDao();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	protected List<T> listBean(String sql, Object... args) {
		return listBean(sql, beanListHandler, args);
	}

	protected List<T> listBean(String sql, BeanListHandler<T> rsh, Object... args) {
		if (log.isDebugEnabled())
			log.debug("{}  {}", sql, args);
		try {
			return queryRunner.query(getConnection(), sql, rsh, args);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new DaoRuntimeException("dao list bean failure, %s", getEntityClass().getName());
		} finally {
			try {
				DataBaseMgr.get().discardConnectionFromDao();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	protected <E> E getValue(String sql, Object... args) {
		if (log.isDebugEnabled())
			log.debug("{}  {}", sql, args);
		try {
			return queryRunner.query(getConnection(), sql, new ScalarHandler<>(), args);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new DaoRuntimeException("dao get value failure, %s", getEntityClass().getName());
		} finally {
			try {
				DataBaseMgr.get().discardConnectionFromDao();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	protected <E> List<E> listValue(String sql, Object... args) {
		if (log.isDebugEnabled())
			log.debug("{}  {}", sql, args);
		try {
			return queryRunner.query(getConnection(), sql, new ColumnListHandler<>(), args);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new DaoRuntimeException("dao list value failure, %s", getEntityClass().getName());
		} finally {
			try {
				DataBaseMgr.get().discardConnectionFromDao();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}
	
	protected Map<String, Object> getMap(String sql, Object... args) {
		if (log.isDebugEnabled())
			log.debug("{}  {}", sql, args);
		try {
			return queryRunner.query(getConnection(), sql, new MapHandler(), args);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new DaoRuntimeException("dao get map failure, %s", getEntityClass().getName());
		} finally {
			try {
				DataBaseMgr.get().discardConnectionFromDao();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}
	
	protected List<Map<String, Object>> listMap(String sql, Object... args) {
		if (log.isDebugEnabled())
			log.debug("{}  {}", sql, args);
		try {
			return queryRunner.query(getConnection(), sql, new MapListHandler(), args);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new DaoRuntimeException("dao list map failure, %s", getEntityClass().getName());
		} finally {
			try {
				DataBaseMgr.get().discardConnectionFromDao();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}
	
	protected <E> E getBeanByClass(String sql, Class<E> clazz, Object... args) {
		if (log.isDebugEnabled())
			log.debug("{}  {}", sql, args);
		try {
			return queryRunner.query(getConnection(), sql, new BeanHandler<>(clazz), args);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new DaoRuntimeException("dao get bean by class failure, %s", getEntityClass().getName());
		} finally {
			try {
				DataBaseMgr.get().discardConnectionFromDao();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}
	
	protected <E> List<E> listBeanByClass(String sql, Class<E> clazz, Object... args) {
		if (log.isDebugEnabled())
			log.debug("{}  {}", sql, args);
		try {
			return queryRunner.query(getConnection(), sql, new BeanListHandler<>(clazz), args);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new DaoRuntimeException("dao list bean by class failure, %s", getEntityClass().getName());
		} finally {
			try {
				DataBaseMgr.get().discardConnectionFromDao();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}
}
