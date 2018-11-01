package de.bogenliga.application.business.competitionclass.impl.business;
import de.bogenliga.application.common.validation.Preconditions;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import de.bogenliga.application.business.competitionclass.api.CompetitionClassComponent;
import de.bogenliga.application.business.competitionclass.api.types.CompetitionClassDO;
import de.bogenliga.application.business.competitionclass.impl.dao.CompetitionClassDAO;
import de.bogenliga.application.business.competitionclass.impl.entity.CompetitionClassBE;
import de.bogenliga.application.business.competitionclass.impl.mapper.CompetitionClassMapper;

/**
 * Implementation of {@link CompetitionClassComponent}
 *
 * @author Giuseppe Ferrera, giuseppe.ferrera@student.reutlingen-university.de
 */

@Component
public class CompetitionClassComponentImpl implements CompetitionClassComponent {

    private static final String PRECONDITION_MSG_KLASSE = "CompetitionClass must not be null";
    private static final String PRECONDITION_MSG_KLASSE_ID = "CompetitionClass ID must not be negative";
    private static final String PRECONDITION_MSG_KLASSE_ALTER_MIN = "Minimum Age must not be negative";
    private static final String PRECONDITION_MSG_KLASSE_ALTER_MAX = "Max Age must be higher than Min Age and must not be negative";
    private static final String PRECONDITION_MSG_KLASSE_NR = "Something is wrong with the CompetitionClass Number";
    private static final String PRECONDITION_MSG_NAME = "The CompetitionClass must be given a name";
    private static final String PRECONDITION_MSG_KLASSE_CURRENT_ID = "The current Id cannot be negative";


    public final CompetitionClassDAO competitionClassDAO;


    /**
     * Constuctor
     * <p>
     * Dependency injection with {@link Autowired}
     *
     * @param competitionClassDAO
     */
    public CompetitionClassComponentImpl(
            CompetitionClassDAO competitionClassDAO) {
        this.competitionClassDAO = competitionClassDAO;
    }


    @Override
    public List<CompetitionClassDO> findAll() {
        final List<CompetitionClassBE> competitionClassBEList = competitionClassDAO.findAll();
        return competitionClassBEList.stream().map(CompetitionClassMapper.toCompetitionClassDO).collect(
                Collectors.toList());
    }


    @Override
    public CompetitionClassDO update(CompetitionClassDO competitionClassDO, long currentClassId) {
    checkCompetitionClassDO(competitionClassDO, currentClassId);
    Preconditions.checkArgument(competitionClassDO.getId() >= 0, PRECONDITION_MSG_KLASSE_ID);

    final CompetitionClassBE competitionClassBE = CompetitionClassMapper.toCompetitionClassBE.apply(competitionClassDO);
    final CompetitionClassBE persistedCompetitionClassBE = competitionClassDAO.update(competitionClassBE,currentClassId);

    return CompetitionClassMapper.toCompetitionClassDO.apply(persistedCompetitionClassBE);
    }


    private void checkCompetitionClassDO(final CompetitionClassDO competitionClassDO, final long currentCompetitionClassId){
        Preconditions.checkNotNull(competitionClassDO, PRECONDITION_MSG_KLASSE);
        Preconditions.checkNotNull(currentCompetitionClassId >= 0, PRECONDITION_MSG_KLASSE_CURRENT_ID);
        Preconditions.checkNotNull(competitionClassDO.getKlasseAlterMin(), PRECONDITION_MSG_KLASSE_ALTER_MIN);
        Preconditions.checkNotNull(competitionClassDO.getKlasseAlterMax(), PRECONDITION_MSG_KLASSE_ALTER_MAX);
        Preconditions.checkNotNull(competitionClassDO.getKlasseNr(), PRECONDITION_MSG_KLASSE_NR);
        Preconditions.checkNotNull(competitionClassDO.getKlasseName(), PRECONDITION_MSG_NAME);
        Preconditions.checkArgument(competitionClassDO.getId() < 0, PRECONDITION_MSG_KLASSE_ID);
        Preconditions.checkArgument(competitionClassDO.getKlasseAlterMin() < 0, PRECONDITION_MSG_KLASSE_ALTER_MIN);
        Preconditions.checkArgument(competitionClassDO.getKlasseAlterMin() > competitionClassDO.getKlasseAlterMax(),PRECONDITION_MSG_KLASSE_ALTER_MIN);

    }
}
