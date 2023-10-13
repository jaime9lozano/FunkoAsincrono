package jaime.Servicios;

import jaime.Exception.FunkoNoEncontrado;
import jaime.modelos.Funko;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public interface FunkoServicio {
    List<Funko> findAll() throws SQLException, ExecutionException, InterruptedException;

    List<Funko> findAllByNombre(String nombre) throws SQLException, ExecutionException, InterruptedException;

    Optional<Funko> findById(long id) throws SQLException, ExecutionException, InterruptedException;

    Funko save(Funko funko) throws SQLException, ExecutionException, InterruptedException;

    Funko update(Funko funko) throws SQLException, FunkoNoEncontrado, ExecutionException, InterruptedException;

    boolean deleteById(long id) throws SQLException, ExecutionException, InterruptedException;

    void deleteAll() throws SQLException, ExecutionException, InterruptedException;
}
