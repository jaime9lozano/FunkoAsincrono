package jaime.repositorios;

import jaime.Exception.FunkoNoEncontrado;
import jaime.modelos.Funko;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface CRUDRepositorio<T, ID> {
    CompletableFuture<Funko> save(T t) throws SQLException;

    // Actualizar
    CompletableFuture<Funko> update(T t) throws SQLException, FunkoNoEncontrado;

    // Buscar por ID
    CompletableFuture<Optional<Funko>> findById(ID id) throws SQLException;

    // Buscar todos
    CompletableFuture<List<Funko>> findAll() throws SQLException;

    // Borrar por ID
    CompletableFuture<Boolean> deleteById(ID id) throws SQLException;

    // Borrar todos
    CompletableFuture<Void> deleteAll() throws SQLException;
}
