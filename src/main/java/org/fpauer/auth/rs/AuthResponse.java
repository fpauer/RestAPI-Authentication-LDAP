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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Fernando Pauer
 *
 */
public class AuthResponse {
    private String status;
    private String accessToken;
    
    private Map<String, List<String>> lookup = new HashMap<String, List<String>>();

    public AuthResponse() {
        status = "SUCCESS";
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public Map<String, List<String>> getLookup() {
        return lookup;
    }

    public void setLookup(Map<String, List<String>> lookup) {
        this.lookup = lookup;
    }
}
