package net.cubosoft.tafelab.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class CustomNativeRepositoryImpl implements CustomNativeRepository {

  @PersistenceContext
  private EntityManager entityManager;
/*
  @Override
  public Query runNativeQuery(String query) {
     return entityManager.createNativeQuery(query);
  }
  */
  @Override
  @Transactional
  public Object getObjectFromQuerry(String query,String parametro) {
  //   EntityTransaction transaction = entityManager.getTransaction();
  //   transaction.begin();
     Object data=null;

     try {
         Query q= entityManager.createNativeQuery(query);
         q.setParameter(1, parametro);
         data=q.getSingleResult();
    //     transaction.commit();
     } catch (Exception e) {
      //   transaction.rollback();
         e.printStackTrace();
     } 
     //finally {
     //    entityManager.close();
     //}
     return data;
  }

  @Override
  @Transactional
  public List<String> getListStringFromQuerry(String query) {
     List<String> data=null;

     try {
         Query q= entityManager.createNativeQuery(query);
         data=q.getResultList();
        // transaction.commit();
     } catch (Exception e) {
        // transaction.rollback();
         e.printStackTrace();
     } 
     //finally {
     //    entityManager.close();
     //}
     return data;
  }

  
}