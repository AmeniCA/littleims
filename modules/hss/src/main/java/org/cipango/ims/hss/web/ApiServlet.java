// ========================================================================
// Copyright 2008-2009 NEXCOM Systems
// ------------------------------------------------------------------------
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at 
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ========================================================================

package org.cipango.ims.hss.web;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cipango.ims.hss.db.PrivateIdentityDao;
import org.cipango.ims.hss.db.SubscriptionDao;
import org.cipango.ims.hss.model.PrivateIdentity;
import org.cipango.ims.hss.model.Scscf;
import org.cipango.ims.hss.model.Subscription;
import org.mortbay.util.ajax.JSON;
import org.mortbay.util.ajax.JSON.Output;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class ApiServlet extends HttpServlet
{
	private SubscriptionDao _subscriptionDao;
	private PrivateIdentityDao _impiDao;
	
	static
	{
		JSON.getDefault().addConvertor(Subscription.class, new JSON.Convertor()
		{
			public Object fromJSON(Map object) { return null; }

			public void toJSON(Object obj, Output out) 
			{
				Subscription s = (Subscription) obj;
				out.add("id", s.getId());
				out.add("scscf", s.getScscf());
			}
			
		});
		JSON.getDefault().addConvertor(Scscf.class, new JSON.Convertor()
		{
			public Object fromJSON(Map object) { return null; }

			public void toJSON(Object obj, Output out) 
			{
				Scscf s = (Scscf) obj;
				out.add("name", s.getName());
				out.add("uri", s.getUri());
			}			
		});
		JSON.getDefault().addConvertor(PrivateIdentity.class, new JSON.Convertor()
		{
			public Object fromJSON(Map object) { return null; }

			public void toJSON(Object obj, Output out) 
			{
				PrivateIdentity impi = (PrivateIdentity) obj;
				out.add("id", impi.getIdentity());
				out.add("sub", impi.getSubscription());
			}			
		});
		
	}
	public void init()
	{
		ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
		_subscriptionDao = (SubscriptionDao) context.getBean("subscriptionDao");
		_impiDao = (PrivateIdentityDao) context.getBean("privateIdentityDao");
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		String pathInfo = request.getPathInfo();
		
		if (pathInfo != null)
		{
			if (pathInfo.startsWith("/"))
				pathInfo = pathInfo.substring(1);
		
			if (pathInfo.length() == 0)
				pathInfo = null;
		}
		
		if (pathInfo == null)
		{
			String path = request.getServletPath();
			response.getOutputStream().println("<html><body><h1>HSS</h1>");
			response.getOutputStream().println("<p><a href=\"." + path + "/subs\">Subscriptions</a>");
			response.getOutputStream().println("<p><a href=\"." + path + "/impis\">Private Identities</a>");
		}
		else
		{
			String[] path = pathInfo.split("/");

			if (path[0].equals("subs"))
			{
				response.getOutputStream().println(JSON.getDefault().toJSON(_subscriptionDao.findAll()));
			}
			else if (path[0].equals("impis"))
			{
				response.getOutputStream().println(JSON.getDefault().toJSON(_impiDao.findAll()));
			}
		}
	}
}
