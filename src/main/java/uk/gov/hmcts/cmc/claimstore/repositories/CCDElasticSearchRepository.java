package uk.gov.hmcts.cmc.claimstore.repositories;

import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.repositories.elastic.Query;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.claimstore.utils.DateUtils;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.cmc.claimstore.repositories.CCDCaseApi.CASE_TYPE_ID;

@Repository("searchRepository")
public class CCDElasticSearchRepository implements CaseSearchApi {

    private final CoreCaseDataApi coreCaseDataApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final UserService userService;
    private final CaseDetailsConverter ccdCaseDetailsConverter;

    @Autowired
    public CCDElasticSearchRepository(CoreCaseDataApi coreCaseDataApi,
                                      AuthTokenGenerator authTokenGenerator,
                                      UserService userService,
                                      CaseDetailsConverter ccdCaseDataToClaim) {
        this.coreCaseDataApi = coreCaseDataApi;
        this.authTokenGenerator = authTokenGenerator;
        this.userService = userService;
        this.ccdCaseDetailsConverter = ccdCaseDataToClaim;
    }

    public List<Claim> getMediationClaims(String authorisation, LocalDate mediationAgreedDate) {
        User user = userService.getUser(authorisation);

        Query mediationQuery = new Query(
            QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery(
                    "data.respondents.value.responseFreeMediationOption", CCDYesNoOption.YES.name()))
                .must(QueryBuilders.termQuery(
                    "data.respondents.value.claimantResponse.freeMediationOption", CCDYesNoOption.YES.name()))
                .must(QueryBuilders.rangeQuery("data.respondents.value.claimantResponse.submittedOn")
                    .from(DateUtils.startOfDay(mediationAgreedDate), true)
                    .to(DateUtils.endOfDay(mediationAgreedDate), true)), 1000
        );

        return searchClaimsWith(user, mediationQuery);

    }

    private List<Claim> searchClaimsWith(User user,
                                         Query query) {

        String serviceAuthToken = this.authTokenGenerator.generate();

        return coreCaseDataApi.searchCases(
            user.getAuthorisation(),
            serviceAuthToken,
            CASE_TYPE_ID,
            query.toString()
        ).getCases()
            .stream()
            .map(ccdCaseDetailsConverter::extractClaim)
            .collect(Collectors.toList());
    }
}
