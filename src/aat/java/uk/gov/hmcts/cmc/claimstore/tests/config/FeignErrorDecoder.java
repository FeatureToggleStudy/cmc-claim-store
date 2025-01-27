package uk.gov.hmcts.cmc.claimstore.tests.config;

import feign.Response;
import feign.codec.ErrorDecoder;
import uk.gov.hmcts.cmc.claimstore.tests.exception.ForbiddenException;

public class FeignErrorDecoder implements ErrorDecoder {

    private ErrorDecoder delegate = new ErrorDecoder.Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        switch (response.status()) {
            case 403:
                //we want to handle and ignore this exception
                //IDAM returns when creating with users that already exist
                //this could be the case with retry logic - so ignore and just authenticate
                return new ForbiddenException("Already Exists");
            default:
                return delegate.decode(methodKey, response);
        }
    }
}
