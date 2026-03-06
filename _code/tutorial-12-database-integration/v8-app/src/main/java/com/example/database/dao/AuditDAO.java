package com.example.database.dao;

import com.example.database.entity.AuditRecord;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import javax.annotation.Resource;
import java.util.List;

/**
 * v8 Data Access Object for Audit Records
 * Uses EJB @Stateless and manual transaction management
 */
@Stateless
public class AuditDAO {
    
    @PersistenceContext(unitName = "com.example.database")
    private EntityManager entityManager;
    
    @Resource
    private UserTransaction userTransaction;
    
    public void saveAuditRecord(AuditRecord record) {
        try {
            userTransaction.begin();
            entityManager.persist(record);
            userTransaction.commit();
        } catch (Exception e) {
            try {
                userTransaction.rollback();
            } catch (SystemException se) {
                throw new RuntimeException("Failed to rollback transaction", se);
            }
            throw new RuntimeException("Failed to save audit record", e);
        }
    }
    
    public AuditRecord updateAuditRecord(AuditRecord record) {
        try {
            userTransaction.begin();
            AuditRecord updated = entityManager.merge(record);
            userTransaction.commit();
            return updated;
        } catch (Exception e) {
            try {
                userTransaction.rollback();
            } catch (SystemException se) {
                throw new RuntimeException("Failed to rollback transaction", se);
            }
            throw new RuntimeException("Failed to update audit record", e);
        }
    }
    
    public List<AuditRecord> findByProcessId(String processId) {
        return entityManager.createQuery(
            "SELECT a FROM AuditRecord a WHERE a.processInstanceId = :pid ORDER BY a.eventTimestamp",
            AuditRecord.class)
            .setParameter("pid", processId)
            .getResultList();
    }
    
    public AuditRecord findById(Long id) {
        return entityManager.find(AuditRecord.class, id);
    }
    
    public List<AuditRecord> findAll() {
        return entityManager.createQuery(
            "SELECT a FROM AuditRecord a ORDER BY a.eventTimestamp DESC",
            AuditRecord.class)
            .getResultList();
    }
    
    public void deleteById(Long id) {
        try {
            userTransaction.begin();
            AuditRecord record = findById(id);
            if (record != null) {
                entityManager.remove(record);
            }
            userTransaction.commit();
        } catch (Exception e) {
            try {
                userTransaction.rollback();
            } catch (SystemException se) {
                throw new RuntimeException("Failed to rollback transaction", se);
            }
            throw new RuntimeException("Failed to delete audit record", e);
        }
    }
}


