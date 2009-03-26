package org.cipango.ims.hss.auth;

import org.cipango.diameter.AVPList;

public interface AuthenticationVector 
{
	AVPList asAuthItem();
}
