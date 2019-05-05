package de.bogenliga.application.business.wettkampftyp.impl.entity;

import java.time.OffsetDateTime;
import org.junit.Test;
import de.bogenliga.application.business.wettkampftyp.impl.entity.WettkampftypBE;
import static de.bogenliga.application.business.wettkampftyp.impl.business.WettkampftypComponentImplTest.getWettkampftypBE;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @Author Daniel Schott daniel.schott@student.reutlingen-university.de
 */
public class WettkampftypBETest {

    private static final long user_Id=13;
    private static final OffsetDateTime created_At_Utc = OffsetDateTime.now();
    private static final long version = 1234;

    private static final long wettkampftyp_Id = 1;
    private static final String wettkampftyp_Name = "Liga Satzsystem";



    @Test
    public void assertToString() {
        final WettkampftypBE underTest = getWettkampftypBE();
        underTest.setId(wettkampftyp_Id);
        underTest.setName(wettkampftyp_Name);

        final String actual = underTest.toString();

        assertThat(actual)
                .isNotEmpty()
                .contains(Long.toString(wettkampftyp_Id))
                .contains(wettkampftyp_Name);
    }

}
