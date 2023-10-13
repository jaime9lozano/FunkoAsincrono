package jaime.repositorios;

import jaime.modelos.Funko;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface FunkoRepositorio extends CRUDRepositorio<Funko,Long>{
    CompletableFuture<List<Funko>> findByNombre(String nombre) throws SQLException;
}
