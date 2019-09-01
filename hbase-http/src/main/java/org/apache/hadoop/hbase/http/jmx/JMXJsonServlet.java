begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements.  See the NOTICE file distributed with  * this work for additional information regarding copyright ownership.  * The ASF licenses this file to You under the Apache License, Version 2.0  * (the "License"); you may not use this file except in compliance with  * the License.  You may obtain a copy of the License at  *  *      http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|http
operator|.
name|jmx
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|PrintWriter
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|management
operator|.
name|ManagementFactory
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|management
operator|.
name|MBeanServer
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|management
operator|.
name|MalformedObjectNameException
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|management
operator|.
name|ObjectName
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|management
operator|.
name|openmbean
operator|.
name|CompositeData
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|management
operator|.
name|openmbean
operator|.
name|TabularData
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|servlet
operator|.
name|ServletException
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|servlet
operator|.
name|http
operator|.
name|HttpServlet
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|servlet
operator|.
name|http
operator|.
name|HttpServletRequest
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|servlet
operator|.
name|http
operator|.
name|HttpServletResponse
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|http
operator|.
name|HttpServer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|JSONBean
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_comment
comment|/*  * This servlet is based off of the JMXProxyServlet from Tomcat 7.0.14. It has  * been rewritten to be read only and to output in a JSON format so it is not  * really that close to the original.  */
end_comment

begin_comment
comment|/**  * Provides Read only web access to JMX.  *<p>  * This servlet generally will be placed under the /jmx URL for each  * HttpServer.  It provides read only  * access to JMX metrics.  The optional<code>qry</code> parameter  * may be used to query only a subset of the JMX Beans.  This query  * functionality is provided through the  * {@link MBeanServer#queryNames(ObjectName, javax.management.QueryExp)}  * method.  *</p>  *<p>  * For example<code>http://.../jmx?qry=Hadoop:*</code> will return  * all hadoop metrics exposed through JMX.  *</p>  *<p>  * The optional<code>get</code> parameter is used to query an specific  * attribute of a JMX bean.  The format of the URL is  *<code>http://.../jmx?get=MXBeanName::AttributeName</code>  *</p>  *<p>  * For example  *<code>  * http://../jmx?get=Hadoop:service=NameNode,name=NameNodeInfo::ClusterId  *</code> will return the cluster id of the namenode mxbean.  *</p>  *<p>  * If the<code>qry</code> or the<code>get</code> parameter is not formatted  * correctly then a 400 BAD REQUEST http response code will be returned.  *</p>  *<p>  * If a resouce such as a mbean or attribute can not be found,  * a 404 SC_NOT_FOUND http response code will be returned.  *</p>  *<p>  * The return format is JSON and in the form  *</p>  *<pre><code>  *  {  *    "beans" : [  *      {  *        "name":"bean-name"  *        ...  *      }  *    ]  *  }  *</code></pre>  *<p>  *  The servlet attempts to convert the the JMXBeans into JSON. Each  *  bean's attributes will be converted to a JSON object member.  *  *  If the attribute is a boolean, a number, a string, or an array  *  it will be converted to the JSON equivalent.  *  *  If the value is a {@link CompositeData} then it will be converted  *  to a JSON object with the keys as the name of the JSON member and  *  the value is converted following these same rules.  *  *  If the value is a {@link TabularData} then it will be converted  *  to an array of the {@link CompositeData} elements that it contains.  *  *  All other objects will be converted to a string and output as such.  *  *  The bean's name and modelerType will be returned for all beans.  *  *  Optional paramater "callback" should be used to deliver JSONP response.  *</p>  *  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|JMXJsonServlet
extends|extends
name|HttpServlet
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|JMXJsonServlet
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|CALLBACK_PARAM
init|=
literal|"callback"
decl_stmt|;
comment|/**    * If query string includes 'description', then we will emit bean and attribute descriptions to    * output IFF they are not null and IFF the description is not the same as the attribute name:    * i.e. specify a URL like so: /jmx?description=true    */
specifier|private
specifier|static
specifier|final
name|String
name|INCLUDE_DESCRIPTION
init|=
literal|"description"
decl_stmt|;
comment|/**    * MBean server.    */
specifier|protected
specifier|transient
name|MBeanServer
name|mBeanServer
decl_stmt|;
specifier|protected
specifier|transient
name|JSONBean
name|jsonBeanWriter
decl_stmt|;
comment|/**    * Initialize this servlet.    */
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|()
throws|throws
name|ServletException
block|{
comment|// Retrieve the MBean server
name|mBeanServer
operator|=
name|ManagementFactory
operator|.
name|getPlatformMBeanServer
argument_list|()
expr_stmt|;
name|this
operator|.
name|jsonBeanWriter
operator|=
operator|new
name|JSONBean
argument_list|()
expr_stmt|;
block|}
comment|/**    * Process a GET request for the specified resource.    *    * @param request    *          The servlet request we are processing    * @param response    *          The servlet response we are creating    */
annotation|@
name|Override
specifier|public
name|void
name|doGet
parameter_list|(
name|HttpServletRequest
name|request
parameter_list|,
name|HttpServletResponse
name|response
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
if|if
condition|(
operator|!
name|HttpServer
operator|.
name|isInstrumentationAccessAllowed
argument_list|(
name|getServletContext
argument_list|()
argument_list|,
name|request
argument_list|,
name|response
argument_list|)
condition|)
block|{
return|return;
block|}
name|String
name|jsonpcb
init|=
literal|null
decl_stmt|;
name|PrintWriter
name|writer
init|=
literal|null
decl_stmt|;
name|JSONBean
operator|.
name|Writer
name|beanWriter
init|=
literal|null
decl_stmt|;
try|try
block|{
name|jsonpcb
operator|=
name|checkCallbackName
argument_list|(
name|request
operator|.
name|getParameter
argument_list|(
name|CALLBACK_PARAM
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|=
name|response
operator|.
name|getWriter
argument_list|()
expr_stmt|;
comment|// "callback" parameter implies JSONP outpout
if|if
condition|(
name|jsonpcb
operator|!=
literal|null
condition|)
block|{
name|response
operator|.
name|setContentType
argument_list|(
literal|"application/javascript; charset=utf8"
argument_list|)
expr_stmt|;
name|writer
operator|.
name|write
argument_list|(
name|jsonpcb
operator|+
literal|"("
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|response
operator|.
name|setContentType
argument_list|(
literal|"application/json; charset=utf8"
argument_list|)
expr_stmt|;
block|}
name|beanWriter
operator|=
name|this
operator|.
name|jsonBeanWriter
operator|.
name|open
argument_list|(
name|writer
argument_list|)
expr_stmt|;
comment|// Should we output description on each attribute and bean?
name|String
name|tmpStr
init|=
name|request
operator|.
name|getParameter
argument_list|(
name|INCLUDE_DESCRIPTION
argument_list|)
decl_stmt|;
name|boolean
name|description
init|=
name|tmpStr
operator|!=
literal|null
operator|&&
name|tmpStr
operator|.
name|length
argument_list|()
operator|>
literal|0
decl_stmt|;
comment|// query per mbean attribute
name|String
name|getmethod
init|=
name|request
operator|.
name|getParameter
argument_list|(
literal|"get"
argument_list|)
decl_stmt|;
if|if
condition|(
name|getmethod
operator|!=
literal|null
condition|)
block|{
name|String
index|[]
name|splitStrings
init|=
name|getmethod
operator|.
name|split
argument_list|(
literal|"\\:\\:"
argument_list|)
decl_stmt|;
if|if
condition|(
name|splitStrings
operator|.
name|length
operator|!=
literal|2
condition|)
block|{
name|beanWriter
operator|.
name|write
argument_list|(
literal|"result"
argument_list|,
literal|"ERROR"
argument_list|)
expr_stmt|;
name|beanWriter
operator|.
name|write
argument_list|(
literal|"message"
argument_list|,
literal|"query format is not as expected."
argument_list|)
expr_stmt|;
name|beanWriter
operator|.
name|flush
argument_list|()
expr_stmt|;
name|response
operator|.
name|setStatus
argument_list|(
name|HttpServletResponse
operator|.
name|SC_BAD_REQUEST
argument_list|)
expr_stmt|;
return|return;
block|}
if|if
condition|(
name|beanWriter
operator|.
name|write
argument_list|(
name|this
operator|.
name|mBeanServer
argument_list|,
operator|new
name|ObjectName
argument_list|(
name|splitStrings
index|[
literal|0
index|]
argument_list|)
argument_list|,
name|splitStrings
index|[
literal|1
index|]
argument_list|,
name|description
argument_list|)
operator|!=
literal|0
condition|)
block|{
name|beanWriter
operator|.
name|flush
argument_list|()
expr_stmt|;
name|response
operator|.
name|setStatus
argument_list|(
name|HttpServletResponse
operator|.
name|SC_BAD_REQUEST
argument_list|)
expr_stmt|;
block|}
return|return;
block|}
comment|// query per mbean
name|String
name|qry
init|=
name|request
operator|.
name|getParameter
argument_list|(
literal|"qry"
argument_list|)
decl_stmt|;
if|if
condition|(
name|qry
operator|==
literal|null
condition|)
block|{
name|qry
operator|=
literal|"*:*"
expr_stmt|;
block|}
if|if
condition|(
name|beanWriter
operator|.
name|write
argument_list|(
name|this
operator|.
name|mBeanServer
argument_list|,
operator|new
name|ObjectName
argument_list|(
name|qry
argument_list|)
argument_list|,
literal|null
argument_list|,
name|description
argument_list|)
operator|!=
literal|0
condition|)
block|{
name|beanWriter
operator|.
name|flush
argument_list|()
expr_stmt|;
name|response
operator|.
name|setStatus
argument_list|(
name|HttpServletResponse
operator|.
name|SC_BAD_REQUEST
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
if|if
condition|(
name|beanWriter
operator|!=
literal|null
condition|)
block|{
name|beanWriter
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|jsonpcb
operator|!=
literal|null
condition|)
block|{
name|writer
operator|.
name|write
argument_list|(
literal|");"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|writer
operator|!=
literal|null
condition|)
block|{
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Caught an exception while processing JMX request"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|response
operator|.
name|sendError
argument_list|(
name|HttpServletResponse
operator|.
name|SC_INTERNAL_SERVER_ERROR
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|MalformedObjectNameException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Caught an exception while processing JMX request"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|response
operator|.
name|sendError
argument_list|(
name|HttpServletResponse
operator|.
name|SC_BAD_REQUEST
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Verifies that the callback property, if provided, is purely alphanumeric.    * This prevents a malicious callback name (that is javascript code) from being    * returned by the UI to an unsuspecting user.    *    * @param callbackName The callback name, can be null.    * @return The callback name    * @throws IOException If the name is disallowed.    */
specifier|private
name|String
name|checkCallbackName
parameter_list|(
name|String
name|callbackName
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
literal|null
operator|==
name|callbackName
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|callbackName
operator|.
name|matches
argument_list|(
literal|"[A-Za-z0-9_]+"
argument_list|)
condition|)
block|{
return|return
name|callbackName
return|;
block|}
throw|throw
operator|new
name|IOException
argument_list|(
literal|"'callback' must be alphanumeric"
argument_list|)
throw|;
block|}
block|}
end_class

end_unit

