package uk.gov.hmcts.cmc.ccd.mapper;

import org.junit.Test;
import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
import uk.gov.hmcts.cmc.ccd.domain.CCDContactDetails;
import uk.gov.hmcts.cmc.ccd.domain.CCDIndividual;
import uk.gov.hmcts.cmc.ccd.domain.CCDRepresentative;
import uk.gov.hmcts.cmc.domain.models.party.Individual;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

public class IndividualMapperTest {

    private AddressMapper addressMapper = new AddressMapper();
    private ContactDetailsMapper contactDetailsMapper = new ContactDetailsMapper();
    private RepresentativeMapper representativeMapper = new RepresentativeMapper(addressMapper, contactDetailsMapper);
    private IndividualMapper individualMapper = new IndividualMapper(addressMapper, representativeMapper);


    @Test
    public void shouldMapIndividualToCCD() {
        //given
        Individual individual = SampleParty.builder().individual();

        //when
        CCDIndividual ccdIndividual = individualMapper.to(individual);

        //then
        assertThat(individual).isEqualTo(ccdIndividual);
    }

    @Test
    public void sholdMapIndividualFromCCD() {
        //given
        final CCDAddress ccdAddress = CCDAddress.builder()
            .line1("line1")
            .line2("line1")
            .city("city")
            .postcode("postcode")
            .build();
        final CCDContactDetails ccdContactDetails = CCDContactDetails.builder()
            .phone("07987654321")
            .email(",my@email.com")
            .dxAddress("dx123")
            .build();
        CCDRepresentative ccdRepresentative = CCDRepresentative
            .builder()
            .organisationName("My Org")
            .organisationContactDetails(ccdContactDetails)
            .organisationAddress(ccdAddress)
            .build();
        CCDIndividual ccdIndividual = CCDIndividual.builder()
            .title("Mr.")
            .name("Individual")
            .mobilePhone("07987654321")
            .dateOfBirth("1950-01-01")
            .address(ccdAddress)
            .correspondenceAddress(ccdAddress)
            .representative(ccdRepresentative)
            .build();

        //when
        Individual individual = individualMapper.from(ccdIndividual);

        //then
        assertThat(individual).isEqualTo(ccdIndividual);
    }

}