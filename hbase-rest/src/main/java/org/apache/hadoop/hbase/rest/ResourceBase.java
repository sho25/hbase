begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|javax
operator|.
name|ws
operator|.
name|rs
operator|.
name|WebApplicationException
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|ws
operator|.
name|rs
operator|.
name|core
operator|.
name|Response
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
name|TableNotFoundException
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
name|client
operator|.
name|RetriesExhaustedException
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
name|regionserver
operator|.
name|NoSuchColumnFamilyException
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
name|util
operator|.
name|StringUtils
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

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ResourceBase
implements|implements
name|Constants
block|{
name|RESTServlet
name|servlet
decl_stmt|;
name|Class
argument_list|<
name|?
argument_list|>
name|accessDeniedClazz
decl_stmt|;
specifier|public
name|ResourceBase
parameter_list|()
throws|throws
name|IOException
block|{
name|servlet
operator|=
name|RESTServlet
operator|.
name|getInstance
argument_list|()
expr_stmt|;
try|try
block|{
name|accessDeniedClazz
operator|=
name|Class
operator|.
name|forName
argument_list|(
literal|"org.apache.hadoop.hbase.security.AccessDeniedException"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{     }
block|}
specifier|protected
name|Response
name|processException
parameter_list|(
name|Throwable
name|exp
parameter_list|)
block|{
name|Throwable
name|curr
init|=
name|exp
decl_stmt|;
if|if
condition|(
name|accessDeniedClazz
operator|!=
literal|null
condition|)
block|{
comment|//some access denied exceptions are buried
while|while
condition|(
name|curr
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|accessDeniedClazz
operator|.
name|isAssignableFrom
argument_list|(
name|curr
operator|.
name|getClass
argument_list|()
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|WebApplicationException
argument_list|(
name|Response
operator|.
name|status
argument_list|(
name|Response
operator|.
name|Status
operator|.
name|FORBIDDEN
argument_list|)
operator|.
name|type
argument_list|(
name|MIMETYPE_TEXT
argument_list|)
operator|.
name|entity
argument_list|(
literal|"Forbidden"
operator|+
name|CRLF
operator|+
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|exp
argument_list|)
operator|+
name|CRLF
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
throw|;
block|}
name|curr
operator|=
name|curr
operator|.
name|getCause
argument_list|()
expr_stmt|;
block|}
block|}
comment|//TableNotFound may also be buried one level deep
if|if
condition|(
name|exp
operator|instanceof
name|TableNotFoundException
operator|||
name|exp
operator|.
name|getCause
argument_list|()
operator|instanceof
name|TableNotFoundException
condition|)
block|{
throw|throw
operator|new
name|WebApplicationException
argument_list|(
name|Response
operator|.
name|status
argument_list|(
name|Response
operator|.
name|Status
operator|.
name|NOT_FOUND
argument_list|)
operator|.
name|type
argument_list|(
name|MIMETYPE_TEXT
argument_list|)
operator|.
name|entity
argument_list|(
literal|"Not found"
operator|+
name|CRLF
operator|+
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|exp
argument_list|)
operator|+
name|CRLF
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
throw|;
block|}
if|if
condition|(
name|exp
operator|instanceof
name|NoSuchColumnFamilyException
condition|)
block|{
throw|throw
operator|new
name|WebApplicationException
argument_list|(
name|Response
operator|.
name|status
argument_list|(
name|Response
operator|.
name|Status
operator|.
name|NOT_FOUND
argument_list|)
operator|.
name|type
argument_list|(
name|MIMETYPE_TEXT
argument_list|)
operator|.
name|entity
argument_list|(
literal|"Not found"
operator|+
name|CRLF
operator|+
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|exp
argument_list|)
operator|+
name|CRLF
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
throw|;
block|}
if|if
condition|(
name|exp
operator|instanceof
name|RuntimeException
condition|)
block|{
throw|throw
operator|new
name|WebApplicationException
argument_list|(
name|Response
operator|.
name|status
argument_list|(
name|Response
operator|.
name|Status
operator|.
name|BAD_REQUEST
argument_list|)
operator|.
name|type
argument_list|(
name|MIMETYPE_TEXT
argument_list|)
operator|.
name|entity
argument_list|(
literal|"Bad request"
operator|+
name|CRLF
operator|+
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|exp
argument_list|)
operator|+
name|CRLF
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
throw|;
block|}
if|if
condition|(
name|exp
operator|instanceof
name|RetriesExhaustedException
condition|)
block|{
name|RetriesExhaustedException
name|retryException
init|=
operator|(
name|RetriesExhaustedException
operator|)
name|exp
decl_stmt|;
name|processException
argument_list|(
name|retryException
operator|.
name|getCause
argument_list|()
argument_list|)
expr_stmt|;
block|}
throw|throw
operator|new
name|WebApplicationException
argument_list|(
name|Response
operator|.
name|status
argument_list|(
name|Response
operator|.
name|Status
operator|.
name|SERVICE_UNAVAILABLE
argument_list|)
operator|.
name|type
argument_list|(
name|MIMETYPE_TEXT
argument_list|)
operator|.
name|entity
argument_list|(
literal|"Unavailable"
operator|+
name|CRLF
operator|+
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|exp
argument_list|)
operator|+
name|CRLF
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
throw|;
block|}
block|}
end_class

end_unit

