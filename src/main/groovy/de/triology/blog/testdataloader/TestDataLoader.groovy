package de.triology.blog.testdataloader

import javax.persistence.EntityManager

/**
 * Loads test data from entity definition files, saves them to a database via a specified {@link EntityManager} and
 * makes the entities available by their names as defined in the entity definition files.
 */
class TestDataLoader {

    private EntityManager entityManager
    private EntityBuilder entityBuilder

    TestDataLoader(EntityManager entityManager) {
        this.entityManager = entityManager
        entityBuilder = EntityBuilder.instance()
    }

    /**
     * Loads the entities defined in the passed {@code entityDefinitionFiles} into the database.
     *
     * @param entityDefinitionFiles {@link Collection} of Strings - the names of files containing the entity
     * definitions; the files must be in the classpath
     */
    void loadTestData(Collection<String> entityDefinitionFiles) {
        EntityPersister persister = new EntityPersister(entityManager)
        entityBuilder.addEntityCreatedListener(persister)
        withTransaction {
            entityDefinitionFiles.each {
                entityBuilder.buildEntities(it)
            }
        }
        entityBuilder.removeEntityCreatedListener(persister)
    }

    /**
     * Gets the entity with the specified name from the set of entities created from entity definition files passed to
     * this {@code TestDataLoader}'s  {@code loadTestData} method.
     *
     * If no entity with the specified name has been loaded, an {@link NoSuchElementException} is thrown. If an entity
     * is found but has a different class than the passed {@code entityClass}, an {@link IllegalArgumentException} is
     * thrown.
     *
     * @param name {@link String} - the requested entity's name
     * @param entityClass the requested entity's {@link Class}
     * @return the requested entity
     */
    public <T> T getEntityByName(String name, Class<T> entityClass) {
        return entityBuilder.getEntityByName(name, entityClass)
    }

    /**
     * Clears all previously built entities so that they are no longer available through the {@code getEntityByName}
     * method and deletes all data from the database.
     */
    void clear() {
        withTransaction {
            // TODO clear database ...
        }
        entityBuilder.clear();
    }

    private void withTransaction(Closure doWithinTransaction) {
        if (!transactionIsActive()) {
            withNewTransaction(doWithinTransaction)
        } else {
            // Someone else is taking care of transaction handling
            doWithinTransaction();
        }
    }

    private boolean transactionIsActive() {
        return entityManager.getTransaction().isActive()
    }

    private void withNewTransaction(Closure doWithinTransaction) {
        try {
            entityManager.getTransaction().begin()
            doWithinTransaction()
            entityManager.getTransaction().commit()
        } catch (Exception e) {
            e.printStackTrace()
            entityManager.getTransaction().rollback();
        }
    }

}