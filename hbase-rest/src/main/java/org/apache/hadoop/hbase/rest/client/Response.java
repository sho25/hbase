begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|rest
operator|.
name|client
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
name|InputStream
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|Header
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|HttpResponse
import|;
end_import

begin_comment
comment|/**  * The HTTP result code, response headers, and body of a HTTP response.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
class|class
name|Response
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|Response
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|int
name|code
decl_stmt|;
specifier|private
name|Header
index|[]
name|headers
decl_stmt|;
specifier|private
name|byte
index|[]
name|body
decl_stmt|;
specifier|private
name|HttpResponse
name|resp
decl_stmt|;
specifier|private
name|InputStream
name|stream
decl_stmt|;
comment|/**    * Constructor    * @param code the HTTP response code    */
specifier|public
name|Response
parameter_list|(
name|int
name|code
parameter_list|)
block|{
name|this
argument_list|(
name|code
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructor    * @param code the HTTP response code    * @param headers the HTTP response headers    */
specifier|public
name|Response
parameter_list|(
name|int
name|code
parameter_list|,
name|Header
index|[]
name|headers
parameter_list|)
block|{
name|this
argument_list|(
name|code
argument_list|,
name|headers
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructor    * @param code the HTTP response code    * @param headers the HTTP response headers    * @param body the response body, can be null    */
specifier|public
name|Response
parameter_list|(
name|int
name|code
parameter_list|,
name|Header
index|[]
name|headers
parameter_list|,
name|byte
index|[]
name|body
parameter_list|)
block|{
name|this
operator|.
name|code
operator|=
name|code
expr_stmt|;
name|this
operator|.
name|headers
operator|=
name|headers
expr_stmt|;
name|this
operator|.
name|body
operator|=
name|body
expr_stmt|;
block|}
comment|/**    * Constructor    * @param code the HTTP response code    * @param headers headers the HTTP response headers    * @param resp the response    * @param in Inputstream if the response had one.    * Note: this is not thread-safe    */
specifier|public
name|Response
parameter_list|(
name|int
name|code
parameter_list|,
name|Header
index|[]
name|headers
parameter_list|,
name|HttpResponse
name|resp
parameter_list|,
name|InputStream
name|in
parameter_list|)
block|{
name|this
operator|.
name|code
operator|=
name|code
expr_stmt|;
name|this
operator|.
name|headers
operator|=
name|headers
expr_stmt|;
name|this
operator|.
name|body
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|resp
operator|=
name|resp
expr_stmt|;
name|this
operator|.
name|stream
operator|=
name|in
expr_stmt|;
block|}
comment|/**    * @return the HTTP response code    */
specifier|public
name|int
name|getCode
parameter_list|()
block|{
return|return
name|code
return|;
block|}
comment|/**    * Gets the input stream instance.    *    * @return an instance of InputStream class.    */
specifier|public
name|InputStream
name|getStream
parameter_list|()
block|{
return|return
name|this
operator|.
name|stream
return|;
block|}
comment|/**    * @return the HTTP response headers    */
specifier|public
name|Header
index|[]
name|getHeaders
parameter_list|()
block|{
return|return
name|headers
return|;
block|}
specifier|public
name|String
name|getHeader
parameter_list|(
name|String
name|key
parameter_list|)
block|{
for|for
control|(
name|Header
name|header
range|:
name|headers
control|)
block|{
if|if
condition|(
name|header
operator|.
name|getName
argument_list|()
operator|.
name|equalsIgnoreCase
argument_list|(
name|key
argument_list|)
condition|)
block|{
return|return
name|header
operator|.
name|getValue
argument_list|()
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
comment|/**    * @return the value of the Location header    */
specifier|public
name|String
name|getLocation
parameter_list|()
block|{
return|return
name|getHeader
argument_list|(
literal|"Location"
argument_list|)
return|;
block|}
comment|/**    * @return true if a response body was sent    */
specifier|public
name|boolean
name|hasBody
parameter_list|()
block|{
return|return
name|body
operator|!=
literal|null
return|;
block|}
comment|/**    * @return the HTTP response body    */
specifier|public
name|byte
index|[]
name|getBody
parameter_list|()
block|{
if|if
condition|(
name|body
operator|==
literal|null
condition|)
block|{
try|try
block|{
name|body
operator|=
name|Client
operator|.
name|getResponseBody
argument_list|(
name|resp
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"encountered ioe when obtaining body"
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|body
return|;
block|}
comment|/**    * @param code the HTTP response code    */
specifier|public
name|void
name|setCode
parameter_list|(
name|int
name|code
parameter_list|)
block|{
name|this
operator|.
name|code
operator|=
name|code
expr_stmt|;
block|}
comment|/**    * @param headers the HTTP response headers    */
specifier|public
name|void
name|setHeaders
parameter_list|(
name|Header
index|[]
name|headers
parameter_list|)
block|{
name|this
operator|.
name|headers
operator|=
name|headers
expr_stmt|;
block|}
comment|/**    * @param body the response body    */
specifier|public
name|void
name|setBody
parameter_list|(
name|byte
index|[]
name|body
parameter_list|)
block|{
name|this
operator|.
name|body
operator|=
name|body
expr_stmt|;
block|}
block|}
end_class

end_unit

