package fpt.edu.capstone.vms.controller.impl;

import fpt.edu.capstone.vms.controller.IGenericController;
import fpt.edu.capstone.vms.persistence.entity.ModelBaseInterface;
import fpt.edu.capstone.vms.persistence.service.IGenericService;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.Serializable;
import java.util.List;

public abstract class GenericController<T extends ModelBaseInterface<I>, I extends Serializable> implements IGenericController<T, I> {

    public IGenericService<T, I> service;

    @Override
    public ResponseEntity<T> findById(@PathVariable I id) {
        try {
            return ResponseEntity.ok(service.findById(id));
        } catch (EmptyResultDataAccessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

    }

    @Override
    public ResponseEntity<T> update(@RequestBody T entity, @PathVariable I id) {
        return ResponseEntity.ok(service.update(entity, id));
    }

    @Override
    public ResponseEntity<T> delete(@PathVariable I id) {
        service.delete(id);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<List<T>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @Override
    public ResponseEntity<T> save(@RequestBody T entity) {
        return ResponseEntity.ok(service.save(entity));
    }
}
