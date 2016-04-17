/**
 * Copyright 2012 Fernando Pauer
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fpauer.auth.rs;

import javax.naming.NamingSecurityException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Logger;

/**
 * @author Fernando Pauer
 *
 */
@Provider
public class AuthExceptionMapper implements ExceptionMapper<AuthException> {
    private static Logger logger = Logger.getLogger(AuthExceptionMapper.class.getName());

    @Override
    public Response toResponse(AuthException e) {
        logger.fine("Error authenticating user: " + e.getMessage());
        return Response
                .status(getStatus(e.getCause()))
                .entity("{\"status\":\"ERROR\", \"errorMessage\":\"" + e.getMessage().replaceAll("\\p{Cntrl}", "")
                        + "\"}").build();
    }

    private Status getStatus(Throwable e) {
        if (e == null) {
            return Status.BAD_REQUEST;
        } else if (e instanceof NamingSecurityException) {
            return Status.UNAUTHORIZED;
        }

        return Status.INTERNAL_SERVER_ERROR;
    }
}
