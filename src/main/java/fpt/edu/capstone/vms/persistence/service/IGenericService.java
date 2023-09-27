package fpt.edu.capstone.vms.persistence.service;

import fpt.edu.capstone.vms.persistence.entity.ModelBaseInterface;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;

public interface IGenericService<T extends ModelBaseInterface<I>, I extends Serializable> {

    List<T> findAll();

    T findById(I id);

    T save(T entity);

    T update(T entity, I id);

    ResponseEntity<T> delete(I id);
}
