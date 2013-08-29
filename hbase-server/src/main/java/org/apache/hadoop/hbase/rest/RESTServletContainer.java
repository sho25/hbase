begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *   http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_import
import|import
name|com
operator|.
name|sun
operator|.
name|jersey
operator|.
name|spi
operator|.
name|container
operator|.
name|servlet
operator|.
name|ServletContainer
import|;
end_import

begin_comment
comment|/**  * REST servlet container. It is used to get the remote request user  * without going through @HttpContext, so that we can minimize code changes.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|RESTServletContainer
extends|extends
name|ServletContainer
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
operator|-
literal|2474255003443394314L
decl_stmt|;
comment|/**    * This container is used only if authentication and    * impersonation is enabled. The remote request user is used    * as a proxy user for impersonation in invoking any REST service.    */
annotation|@
name|Override
specifier|public
name|void
name|service
parameter_list|(
specifier|final
name|HttpServletRequest
name|request
parameter_list|,
specifier|final
name|HttpServletResponse
name|response
parameter_list|)
throws|throws
name|ServletException
throws|,
name|IOException
block|{
name|RESTServlet
operator|.
name|getInstance
argument_list|()
operator|.
name|setEffectiveUser
argument_list|(
name|request
operator|.
name|getRemoteUser
argument_list|()
argument_list|)
expr_stmt|;
name|super
operator|.
name|service
argument_list|(
name|request
argument_list|,
name|response
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

