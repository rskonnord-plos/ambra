/* $HeadURL::                                                                            $
 * $Id$
 *
 */
package org.plos.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Hibernate;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.plos.registration.User;
import org.plos.service.UserDAO;
import org.plos.service.DuplicateLoginNameException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import java.sql.SQLException;
import java.util.List;

/**
 * Hibernate based implementation of the UserDAO.
 */
public class HibernateUserDAO extends HibernateDaoSupport implements UserDAO {
  private static final Log log = LogFactory.getLog(HibernateUserDAO.class);

  /**
   * @see UserDAO#saveOrUpdate(org.plos.registration.User)
   */
  public void saveOrUpdate(final User user) {
    getHibernateTemplate().saveOrUpdate(user);
  }

  /**
   * @see UserDAO#delete(org.plos.registration.User)
   */
  public void delete(final User user) {
     getHibernateTemplate().delete(user);
  }


  /**
   * If more than one user is found it throws an Exception.
   * @see UserDAO#findUserWithLoginName(String)
   */
  public User findUserWithLoginName(final String loginName) {
    return (User) getHibernateTemplate().execute(
      new HibernateCallback(){
        public Object doInHibernate(final Session session) throws HibernateException, SQLException {
          final DetachedCriteria detachedCriteria = DetachedCriteria.forClass(User.class);
//          detachedCriteria.add(Restrictions.eq("loginName", loginName));
          detachedCriteria.add(Restrictions.sqlRestriction("lower(loginName) = lower(?)", loginName, Hibernate.STRING));
          final List list = getHibernateTemplate().findByCriteria(detachedCriteria);

          if (list.size() > 1) {
            final DuplicateLoginNameException duplicateLoginNameException = new DuplicateLoginNameException(loginName);

            log.error("DuplicateLoginName:"+loginName, duplicateLoginNameException);
            throw duplicateLoginNameException;
          }

          if (list.isEmpty()) {
            return null;
          }
          return (User) list.get(0);
        }
    });
  }

}
