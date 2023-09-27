package fpt.edu.capstone.vms.persistence.entity;

public interface ModelBaseInterface<I> {
    void setId(I id);

    I getId();
}
