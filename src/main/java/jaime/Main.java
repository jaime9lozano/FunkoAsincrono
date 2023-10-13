package jaime;

import jaime.Servicios.CSVJSON;
import jaime.Servicios.DatabaseManager;
import jaime.Servicios.FunkoServicio;
import jaime.Servicios.FunkoServicioImp;
import jaime.modelos.Funko;
import jaime.repositorios.FunkoRepositorioImp;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
public class Main {
    public static void main(String[] args) throws SQLException, ExecutionException, InterruptedException {
        FunkoServicio funkoServicio = FunkoServicioImp.getInstance(FunkoRepositorioImp.getInstance(DatabaseManager.getInstance()));
        CSVJSON c= CSVJSON.getInstance();
        CompletableFuture<List<Funko>> funkos =c.mostrarCsv();
        funkos.thenAccept(funkoList -> {
            for (Funko funko : funkoList) {
                try {
                    funkoServicio.save(funko);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).join();
        System.out.println("Funko mas caro: ");
        System.out.println(funkoServicio.funkoCaro());
        System.out.println("Media de los funkos");
        System.out.println(funkoServicio.mediaFunko());
        System.out.println("Funkos lanzados en 2023");
        System.out.println(funkoServicio.funko2023());
        System.out.println("Funkos que se llaman Stitch");
        System.out.println(funkoServicio.findAllByNombre("Stitch"));
    }
}