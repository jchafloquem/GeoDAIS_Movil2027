package pe.gob.devida.geodaismovil.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pe.gob.devida.geodaismovil.model.Participante;

@Repository
public interface ParticipanteRepository extends JpaRepository<Participante, Integer> {

    List<Participante> findByDniOrderByIdParticipanteAsc(String dni);
}
