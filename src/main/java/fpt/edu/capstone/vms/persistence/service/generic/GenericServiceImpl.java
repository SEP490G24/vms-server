package fpt.edu.capstone.vms.persistence.service.generic;

import fpt.edu.capstone.vms.persistence.entity.generic.ModelBaseInterface;
import fpt.edu.capstone.vms.persistence.repository.GenericRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;

@Service
public class GenericServiceImpl<T extends ModelBaseInterface<I>, I extends Serializable> implements IGenericService<T , I> {
    public GenericRepository<T, I> repository;

    public void init(GenericRepository genericRepository) {
        this.repository = genericRepository;
    }

    public List<T> findAll() {
        return repository.findAll();
    }

    public T findById(I id) {
        return repository.findById(id).orElseThrow(() -> new EmptyResultDataAccessException(1));
    }

    public T save(T entity) {
        return repository.save(entity);
    }

    public T update(T entity, I id) {

        entity.setId(id);

        return repository.save(entity);
    }

    public ResponseEntity<T> delete(I id) {
        repository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
