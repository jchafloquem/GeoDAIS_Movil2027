package pe.gob.devida.geodaismovil.repository;
import java.time.OffsetDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.devida.geodaismovil.model.RegistroParcela;

@Repository
public interface RegistroParcelaRepository extends JpaRepository<RegistroParcela, String> {
    Page<RegistroParcela> findAllByEliminadoFalse(Pageable pageable);
    Optional<RegistroParcela> findByInternalKeyAndEliminadoFalse(String internalKey);
    @Modifying
    @Transactional
    @Query("""
            update RegistroParcela r
            set r.eliminado = true, r.fechaActualizacion = :actualizada
            where r.internalKey = :internalKey
            """)
    int marcarEliminado(@Param("internalKey") String internalKey,
                        @Param("actualizada") OffsetDateTime actualizada);
}
