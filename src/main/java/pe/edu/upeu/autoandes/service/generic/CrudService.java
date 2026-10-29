package pe.edu.upeu.autoandes.service.generic;

/** Contrato común de los servicios CRUD, con la misma firma que el proyecto de referencia. */
public interface CrudService<REQ, RES, ID> {
    RES create(REQ request);
    RES update(ID id, REQ request);
    RES read(ID id);
    void delete(ID id);
    Iterable<RES> readAll();
}
