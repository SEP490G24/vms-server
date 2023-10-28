package fpt.edu.capstone.vms.persistence.repository;

import fpt.edu.capstone.vms.persistence.entity.File;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FileRepository extends GenericRepository<File, UUID> {

    File findByName(String name);
    List<File> findAllByType(String type);
    Boolean existsByName(String name);
}
