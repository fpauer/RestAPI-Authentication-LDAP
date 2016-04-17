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


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.sun.jersey.core.util.Base64;

/**
 * @author Fernando Pauer
 *
 */
public class AccessToken implements Serializable {

    private final static long serialVersionUID = 1; 
    
    private String username;
    private String password;
    private Long lastAccess;

    public AccessToken(String username, String password, Long lastAccess) 
    {
    	this.username = username;
    	this.password = password;
    	this.lastAccess = lastAccess;
    }

    public AccessToken(String accessToken) throws Exception 
    {
        byte [] data = Base64.decode( accessToken );
        ObjectInputStream ois = new ObjectInputStream(  new ByteArrayInputStream( data ) );
        Object obj  = ois.readObject();
        ois.close();
        if( obj != null && AccessToken.class.equals(obj.getClass()))
        {
            this.username = ((AccessToken)obj).username;
            this.password = ((AccessToken)obj).password;
            this.lastAccess = ((AccessToken)obj).lastAccess;
        }
    }
    
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public Long getLastAccess() {
		return lastAccess;
	}
	public void setLastAccess(Long lastAccess) {
		this.lastAccess = lastAccess;
	}

    public String toString() {
		try {
	        ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        ObjectOutputStream oos = new ObjectOutputStream( baos );
	        oos.writeObject( this );
	        oos.close();
	        return new String(Base64.encode(baos.toByteArray())); 
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
    }
    
}
