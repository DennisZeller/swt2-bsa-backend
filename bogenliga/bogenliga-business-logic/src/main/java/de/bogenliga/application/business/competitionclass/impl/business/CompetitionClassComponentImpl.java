package de.bogenliga.application.business.competitionclass.impl.business;

import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import de.bogenliga.application.business.competitionclass.api.CompetitionClassComponent;
import de.bogenliga.application.business.competitionclass.api.types.CompetitionClassDO;
import de.bogenliga.application.business.competitionclass.impl.dao.CompetitionClassDAO;
import de.bogenliga.application.business.competitionclass.impl.entity.CompetitionClassBE;
import de.bogenliga.application.business.competitionclass.impl.mapper.CompetitionClassMapper;
import de.bogenliga.application.common.validation.Preconditions;

/**
 * Implementation of {@link CompetitionClassComponent}
 *
 * @author Giuseppe Ferrera, giuseppe.ferrera@student.reutlingen-university.de
 */

@Component
public class CompetitionClassComponentImpl implements CompetitionClassComponent {

    private static final String PRECONDITION_MSG_KLASSE = "CompetitionClass must not be null";
    private static final String PRECONDITION_MSG_KLASSE_ID = "CompetitionClass ID must not be negative";
    private static final String PRECONDITION_MSG_KLASSE_JAHRGANG_MIN = "Minimum Age must not be negative";
    private static final String PRECONDITION_MSG_KLASSE_JAHRGANG_MAX = "Max Age must be higher than Min Age and must not be negative";
    private static final String PRECONDITION_MSG_KLASSE_NR = "Something is wrong with the CompetitionClass Number";
    private static final String PRECONDITION_MSG_NAME = "The CompetitionClass must be given a name";
    private static final String PRECONDITION_MSG_CURRENT_DSB_ID = "The currentDsbId cannot be negative";


    private final CompetitionClassDAO competitionClassDAO;


    /**
     * Constuctor
     * <p>
     * Dependency injection with {@link Autowired}
     *
     * @param competitionClassDAO
     */
    @Autowired
    public CompetitionClassComponentImpl(
            final CompetitionClassDAO competitionClassDAO) {
        this.competitionClassDAO = competitionClassDAO;
    }


    @Override
    public List<CompetitionClassDO> findAll() {
        final List<CompetitionClassBE> competitionClassBEList = competitionClassDAO.findAll();


        for(int i = 0; i < competitionClassBEList.size(); i++){

            Long alterMin = competitionClassBEList.get(i).getKlasseAlterMin();
            Long alterMax = competitionClassBEList.get(i).getKlasseAlterMax();

            int year = Calendar.getInstance().get(Calendar.YEAR);

            Long jahrgangMin = year - alterMin;
            Long jahrgangMax = year - alterMax;

            competitionClassBEList.get(i).setKlasseAlterMin(jahrgangMin);
            competitionClassBEList.get(i).setKlasseAlterMax(jahrgangMax);

        }

        return competitionClassBEList.stream().map(CompetitionClassMapper.toCompetitionClassDO).collect(
                Collectors.toList());
    }


    @Override
    public CompetitionClassDO findById(final long id) {
        Preconditions.checkArgument(id >= 0, PRECONDITION_MSG_KLASSE_ID);

        final CompetitionClassBE competitionClassBE = competitionClassDAO.findById(id);
        int year = Calendar.getInstance().get(Calendar.YEAR);
        Long jahrgangMin = year - competitionClassBE.getKlasseAlterMin();
        Long jahrgangMax = year - competitionClassBE.getKlasseAlterMax();

        competitionClassBE.setKlasseAlterMin(jahrgangMin);
        competitionClassBE.setKlasseAlterMax(jahrgangMax);
        return CompetitionClassMapper.toCompetitionClassDO.apply(competitionClassBE);
    }


    @Override
    public CompetitionClassDO create(final CompetitionClassDO competitionClassDO, final long currentDsbMitglied) {

        checkCompetitionClassDO(competitionClassDO, currentDsbMitglied);

        final CompetitionClassBE competitionClassBE = CompetitionClassMapper.toCompetitionClassBE.apply(competitionClassDO);

        int year = Calendar.getInstance().get(Calendar.YEAR);
        Long alterMin = year - competitionClassDO.getKlasseJahrgangMin();
        Long alterMax = year - competitionClassDO.getKlasseJahrgangMax();

        competitionClassBE.setKlasseAlterMin(alterMin);
        competitionClassBE.setKlasseAlterMax(alterMax);

        final CompetitionClassBE persistedCompetitionClassBE = competitionClassDAO.create(competitionClassBE,
                currentDsbMitglied);

        return CompetitionClassMapper.toCompetitionClassDO.apply(persistedCompetitionClassBE);
    }

    @Override
    public CompetitionClassDO update(final CompetitionClassDO competitionClassDO, final long currentDsbMitblied) {
        checkCompetitionClassDO(competitionClassDO, currentDsbMitblied);
        Preconditions.checkArgument(competitionClassDO.getId() >= 0, PRECONDITION_MSG_KLASSE_ID);

        final CompetitionClassBE competitionClassBE = CompetitionClassMapper.toCompetitionClassBE.apply(competitionClassDO);

        int year = Calendar.getInstance().get(Calendar.YEAR);
        Long alterMin = year - competitionClassDO.getKlasseJahrgangMin();
        Long alterMax = year - competitionClassDO.getKlasseJahrgangMax();

        competitionClassBE.setKlasseAlterMin(alterMin);
        competitionClassBE.setKlasseAlterMax(alterMax);

        final CompetitionClassBE persistedCompetitionClassBE = competitionClassDAO.update(competitionClassBE,currentDsbMitblied);

        return CompetitionClassMapper.toCompetitionClassDO.apply(persistedCompetitionClassBE);
    }


    private void checkCompetitionClassDO(final CompetitionClassDO competitionClassDO, final long currentDsbMitglied){
        Preconditions.checkNotNull(competitionClassDO, PRECONDITION_MSG_KLASSE);
        Preconditions.checkNotNull(currentDsbMitglied >= 0, PRECONDITION_MSG_CURRENT_DSB_ID);
        Preconditions.checkNotNull(competitionClassDO.getKlasseJahrgangMin(), PRECONDITION_MSG_KLASSE_JAHRGANG_MIN);
        Preconditions.checkNotNull(competitionClassDO.getKlasseJahrgangMax(), PRECONDITION_MSG_KLASSE_JAHRGANG_MAX);
        Preconditions.checkNotNull(competitionClassDO.getKlasseNr(), PRECONDITION_MSG_KLASSE_NR);
        Preconditions.checkNotNull(competitionClassDO.getKlasseName(), PRECONDITION_MSG_NAME);
        Preconditions.checkArgument(competitionClassDO.getKlasseJahrgangMin() > competitionClassDO.getKlasseJahrgangMax(),
                PRECONDITION_MSG_KLASSE_JAHRGANG_MIN);
    }
}