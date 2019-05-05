package de.bogenliga.application.business.wettkampftyp.impl.dao;


import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import de.bogenliga.application.business.wettkampftyp.impl.dao.WettkampftypDAO;
import de.bogenliga.application.business.wettkampftyp.impl.entity.WettkampftypBE;
import de.bogenliga.application.common.component.dao.BasicDAO;
import static de.bogenliga.application.business.wettkampftyp.impl.business.WettkampftypComponentImplTest.getWettkampftypBE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


public class WettkampftypBasicDAOTest {

    private static final long user_Id=13;
    private static final OffsetDateTime created_At_Utc = OffsetDateTime.now();
    private static final long version = 1234;

    private static final long wettkampftyp_Id = 1;
    private static final String wettkampftyp_Name = "Liga Satzsystem";

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();
    @Mock
    private BasicDAO basicDao;
    @InjectMocks
    private WettkampftypDAO underTest;

    @Test
    public void findAll() {
        // prepare test data
        final WettkampftypBE expectedBE = getWettkampftypBE();

        // configure mocks
        when(basicDao.selectEntityList(any(), any(), any())).thenReturn(Collections.singletonList(expectedBE));

        // call test method
        final List<WettkampftypBE> actual = underTest.findAll();

        // assert result
        assertThat(actual)
                .isNotNull()
                .isNotEmpty()
                .hasSize(1);

        assertThat(actual.get(0)).isNotNull();

        assertThat(actual.get(0).getId())
                .isEqualTo(expectedBE.getId());

        // verify invocations
        verify(basicDao).selectEntityList(any(), any(), any());


    }


    @Test
    public void findById() {
        // prepare test data
        final WettkampftypBE expectedBE = new WettkampftypBE();
        expectedBE.setId(wettkampftyp_Id);
        expectedBE.setName(wettkampftyp_Name);

        // configure mocks
        when(basicDao.selectSingleEntity(any(), any(), any())).thenReturn(expectedBE);

        // call test method
        final WettkampftypBE actual = underTest.findById(wettkampftyp_Id);

        // assert result
        assertThat(actual).isNotNull();

        assertThat(actual.getId())
                .isEqualTo(expectedBE.getId());
        assertThat(actual.getName())
                .isEqualTo(expectedBE.getName());

        // verify invocations
        verify(basicDao).selectSingleEntity(any(), any(), any());
    }


    @Test
    public void create() {
        // prepare test data
        final WettkampftypBE input = new WettkampftypBE();
        input.setId(wettkampftyp_Id);
        input.setName(wettkampftyp_Name);

        // configure mocks
        when(basicDao.insertEntity(any(), any())).thenReturn(input);

        // call test method
        final WettkampftypBE actual = underTest.create(input, user_Id);

        // assert result
        assertThat(actual).isNotNull();

        assertThat(actual.getId())
                .isEqualTo(input.getId());
        assertThat(actual.getName())
                .isEqualTo(input.getName());

        // verify invocations
        verify(basicDao).insertEntity(any(), eq(input));
    }


    @Test
    public void update() {
        // prepare test data
        final WettkampftypBE input = new WettkampftypBE();
        input.setId(wettkampftyp_Id);
        input.setName(wettkampftyp_Name);

        // configure mocks
        when(basicDao.updateEntity(any(), any(), any())).thenReturn(input);

        // call test method
        final WettkampftypBE actual = underTest.update(input, user_Id);

        // assert result
        assertThat(actual).isNotNull();

        assertThat(actual.getId())
                .isEqualTo(input.getId());
        assertThat(actual.getName())
                .isEqualTo(input.getName());

        // verify invocations
        verify(basicDao).updateEntity(any(), eq(input), any());
    }


    @Test
    public void delete() {
        // prepare test data
        final WettkampftypBE input = new WettkampftypBE();
        input.setId(wettkampftyp_Id);
        input.setName(wettkampftyp_Name);

        // configure mocks

        // call test method
        underTest.delete(input, user_Id);

        // assert result

        // verify invocations
        verify(basicDao).deleteEntity(any(), eq(input), any());
    }

}
