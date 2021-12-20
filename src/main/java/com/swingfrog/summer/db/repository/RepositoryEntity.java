package com.swingfrog.summer.db.repository;

public interface RepositoryEntity {

    default <T extends RepositoryEntity> T add() {
        Repository<T, ?> repository = (Repository<T, ?>) RepositoryMgr.get().findRepository(this.getClass());
        return repository.add((T) this);
    }

    default <T extends RepositoryEntity> boolean remove() {
        Repository<T, ?> repository = (Repository<T, ?>) RepositoryMgr.get().findRepository(this.getClass());
        return repository.remove((T) this);
    }

    default <T extends RepositoryEntity> boolean save() {
        Repository<T, ?> repository = (Repository<T, ?>) RepositoryMgr.get().findRepository(this.getClass());
        return repository.save((T) this);
    }

    default <T extends RepositoryEntity> void forceSave() {
        Repository<T, ?> repository = (Repository<T, ?>) RepositoryMgr.get().findRepository(this.getClass());
        repository.forceSave((T) this);
    }

}
