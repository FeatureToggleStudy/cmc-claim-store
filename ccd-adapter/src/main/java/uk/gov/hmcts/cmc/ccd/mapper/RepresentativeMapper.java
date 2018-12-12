package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimant;
import uk.gov.hmcts.cmc.domain.models.legalrep.Representative;

@Component
public class RepresentativeMapper implements BuilderMapper<CCDClaimant, Representative, CCDClaimant.CCDClaimantBuilder> {

    private final AddressMapper addressMapper;
    private final ContactDetailsMapper contactDetailsMapper;

    @Autowired
    public RepresentativeMapper(AddressMapper addressMapper, ContactDetailsMapper contactDetailsMapper) {
        this.addressMapper = addressMapper;
        this.contactDetailsMapper = contactDetailsMapper;
    }

    @Override
    public void to(Representative representative, CCDClaimant.CCDClaimantBuilder builder) {

        representative.getOrganisationContactDetails()
            .ifPresent(organisationContactDetails ->
                contactDetailsMapper.to(organisationContactDetails, builder));

        builder
            .representativeOrganisationName(representative.getOrganisationName())
            .representativeOrganisationAddress(addressMapper.to(representative.getOrganisationAddress()));
    }

    @Override
    public Representative from(CCDClaimant ccdClaimant) {
        if (ccdClaimant == null) {
            return null;
        }

        return new Representative(
            ccdClaimant.getRepresentativeOrganisationName(),
            addressMapper.from(ccdClaimant.getRepresentativeOrganisationAddress()),
            contactDetailsMapper.from(ccdClaimant)
        );
    }
}
