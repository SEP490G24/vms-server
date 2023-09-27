package fpt.edu.capstone.vms.persistence.entity.generic;

public interface ModelBaseInterface<I> {
    void setId(I id);

    I getId();
}
